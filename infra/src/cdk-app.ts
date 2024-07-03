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

    const cluster = new ecs.Cluster(this, "Cluster", {
      vpc,
      clusterName: "kayttooikeus",
    });

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "kayttooikeus",
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../"),
      file: "Dockerfile",
      exclude: ["infra/"],
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 1024,
        memoryLimitMiB: 2048,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
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
        ssm_lampi_role_arn: this.ssmSecret("LampiRoleArn"),
        ssm_lampi_external_id: this.ssmSecret("LampiExternalId"),
        ssm_cas_mfa_username: this.ssmSecret("CasMfaUsername"),
        ssm_cas_mfa_password: this.ssmSecret("CasMfaPassword"),
        ssm_cas_gauth_encryption_key: this.ssmSecret("CasGauthEncryptionKey"),
        ssm_cas_gauth_signing_key: this.ssmSecret("CasGauthSigningKey"),
        ssm_kayttooikeus_username: this.ssmSecret("PalvelukayttajaUsername"),
        ssm_kayttooikeus_password: this.ssmSecret("PalvelukayttajaPassword"),
        ssm_kayttooikeus_crypto_password: this.ssmSecret("CryptoPassword"),
        ssm_kayttooikeus_kutsu_allowlist: this.ssmSecret("KutsuAllowlist"),
        ssm_auth_cryptoservice_static_salt: this.ssmSecret(
          "CryptoserviceStaticSalt",
        ),
      },
      portMappings: [{ containerPort: appPort, hostPort: appPort }],
    });

    const appSecurityGroup = new ec2.SecurityGroup(this, "AppSecurityGroup", {
      vpc,
    });

    const service = new ecs.FargateService(this, "Service", {
      cluster,
      taskDefinition,
      desiredCount: 0,
      maxHealthyPercent: 200,
      minHealthyPercent: 0,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      securityGroups: [appSecurityGroup],
    });

    dbSecurityGroup.addIngressRule(
      appSecurityGroup,
      ec2.Port.tcp(database.clusterEndpoint.port),
    );
  }

  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/kayttooikeus/${name}` },
      ),
    );
  }
}

const app = new CdkApp({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: QUALIFIER,
  }),
});
app.synth();
