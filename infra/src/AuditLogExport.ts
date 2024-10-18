import * as cdk from "aws-cdk-lib";
import * as constructs from "constructs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as kinesisfirehose from "aws-cdk-lib/aws-kinesisfirehose";
import * as kinesisfirehose_alpha from "@aws-cdk/aws-kinesisfirehose-alpha";
import * as kinesisfirehose_destinations_alpha from "@aws-cdk/aws-kinesisfirehose-destinations-alpha";
import * as logs from "aws-cdk-lib/aws-logs";
import * as s3 from "aws-cdk-lib/aws-s3";
import { legacyPrefix } from "./shared-account";

export type AuditLogExportProps = {
  logGroup: logs.LogGroup;
};

export class AuditLogExport extends constructs.Construct {
  constructor(
    scope: constructs.Construct,
    id: string,
    props: AuditLogExportProps
  ) {
    super(scope, id);
    const auditBucket = new s3.Bucket(this, "AuditLogBucket", {});

    const accountId = cdk.Stack.of(this).account;
    const keyPrefix = `yleiskayttoiset/${accountId}/!{timestamp:yyyy/MM/dd}/!{timestamp:yyyyMMddHHmmssSSS}.audit.kayttooikeus.`;
    const destination = new kinesisfirehose_destinations_alpha.S3Bucket(
      auditBucket,
      {
        dataOutputPrefix: keyPrefix,
        errorOutputPrefix: keyPrefix + "!{firehose:error-output-type}.",
      }
    );
    const deliveryStream = new kinesisfirehose_alpha.DeliveryStream(
      this,
      "AuditDeliveryStream",
      {
        deliveryStreamName: legacyPrefix("Audit"),
        destination: destination,
      }
    );
    const cfnDeliveryStream = deliveryStream.node
      .defaultChild as kinesisfirehose.CfnDeliveryStream;
    cfnDeliveryStream.addPropertyOverride(
      "ExtendedS3DestinationConfiguration.FileExtension",
      ".log"
    );
    cfnDeliveryStream.addPropertyOverride(
      "ExtendedS3DestinationConfiguration.ProcessingConfiguration",
      {
        Enabled: "True",
        Processors: [
          this.decompressionProcessor(),
          this.cloudwatchLogProcessingProcessor(),
        ],
      }
    );

    new logs.SubscriptionFilter(this, "KinesisDeliveryFilter", {
      logGroup: props.logGroup,
      filterPattern: logs.FilterPattern.literal("{ $.logSeq = * }"),
      destination: new KinesisDeliveryStreamDestination(deliveryStream),
    });
  }

  decompressionProcessor() {
    return {
      Type: "Decompression",
      Parameters: [
        {
          ParameterName: "CompressionFormat",
          ParameterValue: "GZIP",
        },
      ],
    };
  }

  cloudwatchLogProcessingProcessor() {
    return {
      Type: "CloudWatchLogProcessing",
      Parameters: [
        {
          ParameterName: "DataMessageExtraction",
          ParameterValue: "True",
        },
      ],
    };
  }
}

// Based on https://github.com/aws/aws-cdk/blob/v2.148.1/packages/aws-cdk-lib/aws-logs-destinations/lib/kinesis.ts
class KinesisDeliveryStreamDestination
  implements logs.ILogSubscriptionDestination
{
  constructor(
    private readonly deliveryStream: kinesisfirehose_alpha.DeliveryStream
  ) {}

  bind(
    scope: constructs.Construct,
    sourceLogGroup: cdk.aws_logs.ILogGroup
  ): cdk.aws_logs.LogSubscriptionDestinationConfig {
    const role = new iam.Role(scope, "LogsDeliveryRole", {
      assumedBy: new iam.ServicePrincipal("logs.amazonaws.com"),
    });
    this.deliveryStream.grantPutRecords(role);
    role.grantPassRole(role);

    const policy = role.node.tryFindChild("DefaultPolicy") as iam.CfnPolicy;
    if (policy) {
      // Remove circular dependency
      const cfnRole = role.node.defaultChild as iam.CfnRole;
      cfnRole.addOverride("DependsOn", undefined);
      // Ensure policy is created before subscription filter
      scope.node.addDependency(policy);
    }

    return {
      arn: this.deliveryStream.deliveryStreamArn,
      role,
    };
  }
}
