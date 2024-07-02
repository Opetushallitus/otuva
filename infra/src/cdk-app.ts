import * as cdk from "aws-cdk-lib";
import { aws_sns } from "aws-cdk-lib";
import * as constructs from "constructs";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as rds from "aws-cdk-lib/aws-rds";
import * as ec2 from "aws-cdk-lib/aws-ec2";

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

    new rds.DatabaseCluster(this, "Database", {
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
  }
}

const app = new CdkApp({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: QUALIFIER,
  }),
});
app.synth();
