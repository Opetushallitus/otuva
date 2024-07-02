import * as cdk from "aws-cdk-lib";
import { aws_sns } from "aws-cdk-lib";
import * as constructs from "constructs";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as ssm from "aws-cdk-lib/aws-ssm";

import { ALARM_TOPIC_ARN, prefix, QUALIFIER } from "./shared-account";

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
  }
}

class DnsStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    new route53.HostedZone(this, "OtuvaHostedZone", {
      zoneName: ssm.StringParameter.valueFromLookup(this, "/otuva/zoneName"),
    });
  }
}

class AlarmStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    aws_sns.Topic.fromTopicArn(this, "AlarmTopic", ALARM_TOPIC_ARN);
  }
}

const app = new CdkApp({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: QUALIFIER,
  }),
});
app.synth();
