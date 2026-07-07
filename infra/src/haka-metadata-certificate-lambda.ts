import * as path from "node:path";
import constructs from "constructs";
import * as cdk from "aws-cdk-lib";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as iam from "aws-cdk-lib/aws-iam";
import * as lambda from "aws-cdk-lib/aws-lambda";
import * as lambda_nodejs from "aws-cdk-lib/aws-lambda-nodejs";
import * as logs from "aws-cdk-lib/aws-logs";
import * as scheduler from "aws-cdk-lib/aws-scheduler";
import * as scheduler_targets from "aws-cdk-lib/aws-scheduler-targets";
import * as sns from "aws-cdk-lib/aws-sns";
import { VPC_NAME } from "./shared-account";
import { getConfig } from "./config";

type HakaMetadataCertificateValidityLeftInDaysLambdaStackProps =
  cdk.StackProps & {
    alarmTopic: sns.ITopic;
  };

export class HakaMetadataCertificateValidityLeftInDaysLambdaStack
  extends cdk.Stack
{
  constructor(
    scope: constructs.Construct,
    id: string,
    props: HakaMetadataCertificateValidityLeftInDaysLambdaStackProps,
  ) {
    super(scope, id, props);

    const {
      hakaMetadataCertificateValidDaysAlarm: { hakaMetadataUrl, thresholdDays },
    } = getConfig();

    const lambdaFn = new lambda_nodejs.NodejsFunction(
      this,
      "HakaMetadataCertificateValidityLeftInDays",
      {
        functionName: "haka-metadata-validity-left-in-days",
        entry: path.join(
          __dirname,
          "../lambda/haka-metadata-certificate-validity-left-in-days/index.ts",
        ),
        bundling: { sourceMap: true },
        runtime: lambda.Runtime.NODEJS_24_X,
        architecture: lambda.Architecture.ARM_64,
        timeout: cdk.Duration.seconds(30),
        vpc: ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME }),
        vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
        environment: {
          HAKA_METADATA_URL: hakaMetadataUrl,
        },
      },
    );

    lambdaFn.addToRolePolicy(
      new iam.PolicyStatement({
        actions: ["cloudwatch:PutMetricData"],
        resources: ["*"],
      }),
    );

    new scheduler.Schedule(
      this,
      "LogHakaMetadataX509CertificateValidityOncePerDaySchedule",
      {
        schedule: scheduler.ScheduleExpression.cron({
          minute: "0",
          hour: "9",
          timeZone: cdk.TimeZone.EUROPE_HELSINKI,
        }),
        target: new scheduler_targets.LambdaInvoke(lambdaFn, {
          retryAttempts: 3,
          maxEventAge: cdk.Duration.hours(1),
        }),
        description:
          "Haka metadata certificate validity is logged every day at 9 AM Helsinki time",
      },
    );

    const metricFilter = lambdaFn.logGroup.addMetricFilter(
      "HakaMetadataCertificateValidDaysLeft",
      {
        metricName: "haka-metadata-certificate-valid-days-left",
        metricNamespace: "Otuva",
        filterPattern: logs.FilterPattern.exists("$.validDaysLeft"),
        metricValue: "$.validDaysLeft",
        unit: cloudwatch.Unit.COUNT,
      },
    );

    const alarm = new cloudwatch.Alarm(
      this,
      "HakaMetadataCertificateValidDaysLeftAlarm",
      {
        alarmName: "HakaMetadataCertificateValidDaysLeftAlarm",
        alarmDescription: `Hakan metadatan (osoitteesta ${hakaMetadataUrl}) X509 sertifikaatti vanhenee alle ${thresholdDays} päivän päästä. Päivitä cas.authn.pac4j.saml[1].metadata.identity-provider-metadata-path arvo cas-virkailijassa. Tarkista https://wiki.eduuni.fi/spaces/CSCHAKA/pages/27297775/Metadata onko uutta metadataa tarjolla.`,
        metric: metricFilter.metric().with({
          statistic: "Minimum",
          period: cdk.Duration.hours(24),
        }),
        comparisonOperator:
          cloudwatch.ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
        threshold: thresholdDays,
        evaluationPeriods: 1,
        treatMissingData: cloudwatch.TreatMissingData.BREACHING,
      },
    );

    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(props.alarmTopic));
    alarm.addOkAction(new cloudwatch_actions.SnsAction(props.alarmTopic));
  }
}
