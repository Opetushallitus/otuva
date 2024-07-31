import * as cdk from "aws-cdk-lib";
import * as constructs from "constructs";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as logs from "aws-cdk-lib/aws-logs";
import * as rds from "aws-cdk-lib/aws-rds";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as events from "aws-cdk-lib/aws-events";
import * as events_targets from "aws-cdk-lib/aws-events-targets";
import * as ecr_assets from "aws-cdk-lib/aws-ecr-assets";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as sns from "aws-cdk-lib/aws-sns";
import * as path from "node:path";

type DatabaseBackupToS3Props = {
  cluster: ecs.Cluster;
  database: rds.DatabaseCluster;
};

export class DatabaseBackupToS3 extends constructs.Construct {
  readonly securityGroup: ec2.SecurityGroup;
  readonly dbName = "kayttooikeus";

  constructor(
    scope: constructs.Construct,
    id: string,
    props: DatabaseBackupToS3Props,
  ) {
    super(scope, id);
    const { cluster, database } = props;

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../backup"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });
    const backupBucket = new s3.Bucket(this, "BackupBucket", {
      lifecycleRules: [
        {
          prefix: "daily/",
          expiration: cdk.Duration.days(30),
        },
        {
          prefix: "monthly/",
          transitions: [
            {
              storageClass: s3.StorageClass.GLACIER,
              transitionAfter: cdk.Duration.days(30),
            },
          ],
        },
      ],
    });

    this.securityGroup = new ec2.SecurityGroup(this, "AppSecurityGroup", {
      vpc: cluster.vpc,
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
    backupBucket.grantReadWrite(taskDefinition.taskRole);

    const logGroup = new logs.LogGroup(this, "LogGroup", {});
    const metricFilter = logGroup.addMetricFilter("SuccessMetricFilter", {
      metricNamespace: "Database backup to S3",
      metricName: "Success",
      metricValue: "1",
      dimensions: {
        dbname: "$.dbname",
        frequency: "$.frequency",
      },
      filterPattern: logs.FilterPattern.stringValue("msg", "=", "success"),
    });

    const capitalizedDbname = `${this.dbName.charAt(0).toUpperCase()}${this.dbName.slice(1)}`;
    const dailyFailureAlarm = new cloudwatch.Alarm(this, "DailyFailureAlarm", {
      alarmName: `${capitalizedDbname}DailyBackupToS3Failure`,
      alarmDescription: `Päivittäinen ${this.dbName} tietokannan backup ei ole siirtynyt S3:seen yli 24 tuntiin`,
      metric: metricFilter.metric().with({
        statistic: "Minimum",
        period: cdk.Duration.hours(1),
        dimensionsMap: {
          frequency: "daily",
          dbname: this.dbName,
        },
      }),
      threshold: 1,
      comparisonOperator: cloudwatch.ComparisonOperator.LESS_THAN_THRESHOLD,
      evaluationPeriods: 24,
      treatMissingData: cloudwatch.TreatMissingData.BREACHING,
      actionsEnabled: true,
    });

    const monthlyFailureAlarm = new cloudwatch.Alarm(
      this,
      "MonthlyFailureAlarm",
      {
        alarmName: `${capitalizedDbname}MonthlyBackupToS3Failure`,
        alarmDescription: `Kuukausittainen ${this.dbName} tietokannan backup ei ole siirtynyt S3:seen yli 24 tuntiin`,
        metric: metricFilter.metric().with({
          statistic: "Minimum",
          period: cdk.Duration.days(1),
          dimensionsMap: {
            frequency: "monthly",
            dbname: this.dbName,
          },
        }),
        threshold: 1,
        comparisonOperator: cloudwatch.ComparisonOperator.LESS_THAN_THRESHOLD,
        evaluationPeriods: 31,
        treatMissingData: cloudwatch.TreatMissingData.BREACHING,
        actionsEnabled: true,
      },
    );

    const alarmTopicName = "alarm";
    const stack = cdk.Stack.of(this);
    const alarmTopic = sns.Topic.fromTopicArn(
      this,
      "AlarmTopic",
      `arn:aws:sns:${stack.region}:${stack.account}:${alarmTopicName}`,
    );

    [(dailyFailureAlarm, monthlyFailureAlarm)].forEach((alarm) => {
      alarm.addAlarmAction(new cloudwatch_actions.SnsAction(alarmTopic));
      alarm.addOkAction(new cloudwatch_actions.SnsAction(alarmTopic));
    });

    taskDefinition.addContainer("Container", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "backup" }),
      environment: {
        DB_HOSTNAME: database.clusterEndpoint.hostname,
        DB_PORT: database.clusterEndpoint.port.toString(),
        DB_NAME: this.dbName,
        S3_BUCKET: backupBucket.bucketName,
      },
      secrets: {
        DB_USERNAME: ecs.Secret.fromSecretsManager(
          database.secret!,
          "username",
        ),
        DB_PASSWORD: ecs.Secret.fromSecretsManager(
          database.secret!,
          "password",
        ),
      },
    });

    const rule = new events.Rule(this, "Rule", {
      schedule: events.Schedule.cron({ minute: "0", hour: "6" }),
    });
    rule.addTarget(
      new events_targets.EcsTask({
        cluster,
        taskDefinition,
        securityGroups: [this.securityGroup],
      }),
    );
  }
}
