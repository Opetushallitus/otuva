import * as path from "node:path";

import * as cdk from "aws-cdk-lib";
import * as aws_sns from "aws-cdk-lib/aws-sns";
import * as constructs from "constructs";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as rds from "aws-cdk-lib/aws-rds";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecr_assets from "aws-cdk-lib/aws-ecr-assets";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as logs from "aws-cdk-lib/aws-logs";
import * as ecs_patterns from "aws-cdk-lib/aws-ecs-patterns";

import { ALARM_TOPIC_ARN, prefix, QUALIFIER, VPC_NAME } from "./shared-account";

class CdkApp extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);
    const stackProps = {
      env: {
        account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
        region: process.env.CDK_DEPLOY_TARGET_REGION,
      },
    };

    new DnsStack(this, prefix("DnsStack"), stackProps);
    new AlarmStack(this, prefix("AlarmStack"), stackProps);
    new ApplicationStack(this, prefix("ApplicationStack"), stackProps);
  }
}

class DnsStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    new route53.HostedZone(this, "OtuvaHostedZone", {
      zoneName: ssm.StringParameter.valueFromLookup(this, "/otuva/zoneName"),
    });

    route53.HostedZone.fromLookup(this, "YleiskayttoisetHostedZone", {
      domainName: ssm.StringParameter.valueFromLookup(this, "zoneName"),
    });
  }
}

class AlarmStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    aws_sns.Topic.fromTopicArn(this, "AlarmTopic", ALARM_TOPIC_ARN);
  }
}

class ApplicationStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    const vpc = ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME });

    const dbSecurityGroup = new ec2.SecurityGroup(
      this,
      "DatabaseSecurityGroup",
      { vpc },
    );

    const database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      securityGroups: [dbSecurityGroup],
      defaultDatabaseName: "kayttooikeus",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_12_17,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("kayttooikeus"),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T4G,
          ec2.InstanceSize.LARGE,
        ),
      }),
      readers: [],
    });

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "kayttooikeus",
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../"),
      file: "Dockerfile",
      exclude: ["infra/"],
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 1024,
        memoryLimitMiB: 2048,
      },
    );
    const appPort = 8080;
    const environmentName = process.env.ENV!;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: environmentName === "hahtuva" ? "hahtuva2" : environmentName,
        postgres_host: database.clusterEndpoint.hostname,
        postgres_port: database.clusterEndpoint.port.toString(),
        postgres_database: "kayttooikeus",
        ssm_lampi_role_arn: "",
        ssm_lampi_external_id: "",
        ssm_postgresql_kayttooikeus_app_password: "",
        ssm_cas_mfa_username: "",
        ssm_cas_mfa_password: "",
        ssm_cas_gauth_encryption_key: "",
        ssm_cas_gauth_signing_key: "",
        ssm_kayttooikeus_username: "",
        ssm_kayttooikeus_password: "",
        ssm_kayttooikeus_crypto_password: "",
        ssm_kayttooikeus_kutsu_allowlist: "",
        ssm_auth_cryptoservice_static_salt: "",
      },
      secrets: {
        postgres_username: ecs.Secret.fromSecretsManager(
          database.secret!,
          "username",
        ),
        postgres_password: ecs.Secret.fromSecretsManager(
          database.secret!,
          "password",
        ),
      },
      portMappings: [{ containerPort: appPort, hostPort: appPort }],
    });
  }
}

const app = new CdkApp({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: QUALIFIER,
  }),
});
app.synth();
