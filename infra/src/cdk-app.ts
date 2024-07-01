import * as cdk from "aws-cdk-lib";
import * as constructs from "constructs";

import { ALARM_TOPIC_ARN, prefix, QUALIFIER } from "./shared-account";
import { aws_sns } from "aws-cdk-lib";

class CdkApp extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);
    const stackProps = {
      env: {
        account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
        region: process.env.CDK_DEPLOY_TARGET_REGION,
      },
    };

    new AlarmStack(this, prefix("AlarmStack"), stackProps);
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
