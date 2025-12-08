import * as path from "node:path";

import * as cdk from "aws-cdk-lib";
import * as cloudwatch from "aws-cdk-lib/aws-cloudwatch";
import * as cloudwatch_actions from "aws-cdk-lib/aws-cloudwatch-actions";
import * as certificatemanager from "aws-cdk-lib/aws-certificatemanager";
import * as constructs from "constructs";
import * as ec2 from "aws-cdk-lib/aws-ec2";
import * as ecr_assets from "aws-cdk-lib/aws-ecr-assets";
import * as ecs from "aws-cdk-lib/aws-ecs";
import * as elasticloadbalancingv2 from "aws-cdk-lib/aws-elasticloadbalancingv2";
import * as iam from "aws-cdk-lib/aws-iam";
import * as lambda from "aws-cdk-lib/aws-lambda";
import * as logs from "aws-cdk-lib/aws-logs";
import * as rds from "aws-cdk-lib/aws-rds";
import * as route53 from "aws-cdk-lib/aws-route53";
import * as route53_targets from "aws-cdk-lib/aws-route53-targets";
import * as s3 from "aws-cdk-lib/aws-s3";
import * as sns from "aws-cdk-lib/aws-sns";
import * as sns_subscriptions from "aws-cdk-lib/aws-sns-subscriptions";
import * as ssm from "aws-cdk-lib/aws-ssm";
import * as wafv2 from "aws-cdk-lib/aws-wafv2";
import * as secretsmanager from "aws-cdk-lib/aws-secretsmanager";
import { AuditLogExport } from "./AuditLogExport";
import { DatabaseBackupToS3 } from "./DatabaseBackupToS3";

import {
  prefix,
  legacyPrefix,
  CDK_QUALIFIER,
  VPC_NAME,
} from "./shared-account";
import { getConfig, getEnvironment } from "./config";
import {createHealthCheckStacks} from "./health-check";
import { ResponseAlarms } from "./response-alarms";

class CdkApp extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);
    const stackProps = {
      env: {
        account: process.env.CDK_DEPLOY_TARGET_ACCOUNT,
        region: process.env.CDK_DEPLOY_TARGET_REGION,
      },
    };

    const config = getConfig();

    const { oauthHostedZone } = new DnsStack(this, legacyPrefix("DnsStack"), stackProps);
    const { alarmsToSlackLambda, alarmTopic } = new AlarmStack(this, prefix("AlarmStack"), stackProps);
    const ecsStack = new ECSStack(this, prefix("ECSStack"), stackProps);
    const appStack = new ApplicationStack(
      this,
      legacyPrefix("ApplicationStack"),
      {
        ...stackProps,
        alarmTopic,
        oauthHostedZone,
      }
    );
    new CasVirkailijaApplicationStack(
      this,
      prefix("CasVirkailijaApplicationStack"),
      {
        alarmTopic,
        ecsCluster: ecsStack.cluster,
        ...stackProps,
        bastion: appStack.bastion,
      }
    );
    new CasOppijaApplicationStack(this, prefix("CasOppijaApplicationStack"), {
      ...stackProps,
      alarmTopic,
      bastion: appStack.bastion,
      ecsCluster: ecsStack.cluster,
    });
    new ServiceProviderApplicationStack(
      this,
      prefix("ServiceProviderApplicationStack"),
      {
        ...stackProps,
        bastion: appStack.bastion,
        ecsCluster: ecsStack.cluster,
      }
    );

    createHealthCheckStacks(this, alarmsToSlackLambda, [
      { name: "Kayttooikeus", url: new URL(`https://virkailija.${config.opintopolkuHost}/kayttooikeus-service/actuator/health`) },
      { name: "CasVirkailija", url: new URL(`https://virkailija.${config.opintopolkuHost}/cas/actuator/health`) },
      { name: "CasOppija", url: new URL(`https://${config.opintopolkuHost}/cas-oppija/actuator/health`) },
      { name: "ServiceProvider", url: new URL(`https://virkailija.${config.opintopolkuHost}/service-provider-app/buildversion.txt`) },
    ])
  }
}

class AlarmStack extends cdk.Stack {
  readonly alarmTopic: sns.ITopic;
  readonly alarmsToSlackLambda: lambda.IFunction;
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.alarmsToSlackLambda = this.createAlarmsToSlackLambda();
    this.alarmTopic = this.createAlarmTopic();

    this.alarmTopic.addSubscription(
      new sns_subscriptions.LambdaSubscription(this.alarmsToSlackLambda),
    );

    const pagerDutyIntegrationUrlSecret =
      secretsmanager.Secret.fromSecretNameV2(
        this,
        "PagerDutyIntegrationUrlSecret",
        "/otuva/PagerDutyIntegrationUrl",
      );

    this.alarmTopic.addSubscription(
      new sns_subscriptions.UrlSubscription(
        pagerDutyIntegrationUrlSecret.secretValue.toString(),
        { protocol: sns.SubscriptionProtocol.HTTPS },
      ),
    );
    this.exportValue(this.alarmTopic.topicArn);
  }

  createAlarmTopic() {
    return new sns.Topic(this, "AlarmTopic", {});
  }

  createAlarmsToSlackLambda() {
    const alarmsToSlack = new lambda.Function(this, "AlarmsToSlack", {
      code: lambda.Code.fromAsset("../alarms-to-slack"),
      handler: "alarms-to-slack.handler",
      runtime: lambda.Runtime.NODEJS_20_X,
      architecture: lambda.Architecture.ARM_64,
      timeout: cdk.Duration.seconds(30),
    });

    // https://docs.aws.amazon.com/secretsmanager/latest/userguide/retrieving-secrets_lambda.html
    const parametersAndSecretsExtension =
      lambda.LayerVersion.fromLayerVersionArn(
        this,
        "ParametersAndSecretsLambdaExtension",
        "arn:aws:lambda:eu-west-1:015030872274:layer:AWS-Parameters-and-Secrets-Lambda-Extension-Arm64:11",
      );

    alarmsToSlack.addLayers(parametersAndSecretsExtension);
    secretsmanager.Secret.fromSecretNameV2(
      this,
      "slack-webhook",
      "slack-webhook",
    ).grantRead(alarmsToSlack);

    return alarmsToSlack;
  }
}

class DnsStack extends cdk.Stack {
  readonly oauthHostedZone: route53.IHostedZone;
  private readonly config = getConfig();
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.oauthHostedZone = new route53.HostedZone(this, "OtuvaHostedZone", {
      zoneName: this.config.otuvaDomain,
    });
  }
}

class ECSStack extends cdk.Stack {
  public cluster: ecs.Cluster;

  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);

    this.cluster = new ecs.Cluster(this, "Cluster", {
      vpc: ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME }),
      clusterName: prefix(""),
    });
  }
}

class ApplicationStack extends cdk.Stack {
  readonly bastion: ec2.BastionHostLinux;

  constructor(
    scope: constructs.Construct,
    id: string,
    props: cdk.StackProps & {
      alarmTopic: sns.ITopic;
      oauthHostedZone: route53.IHostedZone;
    }
  ) {
    super(scope, id, props);
    const config = getConfig();

    const vpc = ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME });

    const dbSecurityGroup = new ec2.SecurityGroup(
      this,
      "DatabaseSecurityGroup",
      { vpc }
    );

    const exportBucket = new s3.Bucket(this, "ExportBucket", {});

    const database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      securityGroups: [dbSecurityGroup],
      defaultDatabaseName: "kayttooikeus",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_12_17,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("kayttooikeus", {
        secretName: legacyPrefix("DatabaseSecret"),
      }),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        enablePerformanceInsights: true,
        instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T4G,
          ec2.InstanceSize.LARGE
        ),
      }),
      readers: [],
      s3ExportBuckets: [exportBucket],
    });

    const cluster = new ecs.Cluster(this, "Cluster", {
      vpc,
      clusterName: "kayttooikeus",
    });

    this.bastion = new ec2.BastionHostLinux(this, "BastionHost", {
      vpc,
      instanceName: prefix("Bastion"),
    });
    database.connections.allowDefaultPortFrom(this.bastion.connections);

    const backup = new DatabaseBackupToS3(this, "DatabaseBackupToS3", {
      ecsCluster: cluster,
      dbCluster: database,
      dbName: "kayttooikeus",
    });
    dbSecurityGroup.addIngressRule(
      backup.securityGroup,
      ec2.Port.tcp(database.clusterEndpoint.port)
    );

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: "kayttooikeus",
      retention: logs.RetentionDays.INFINITE,
    });
    if (config.lampiExport) {
      this.exportFailureAlarm(logGroup, props.alarmTopic);
    }

    new AuditLogExport(this, "AuditLogExport", { logGroup });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../kayttooikeus-service"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 2048,
        memoryLimitMiB: 4096,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      }
    );
    exportBucket.grantReadWrite(taskDefinition.taskRole);

    const lampiProperties: ecs.ContainerDefinitionProps["environment"] =
      config.lampiExport
        ? {
            "kayttooikeus.tasks.export.enabled":
              config.lampiExport.enabled.toString(),
            "kayttooikeus.tasks.export.bucket-name": exportBucket.bucketName,
            "kayttooikeus.tasks.export.lampi-bucket-name":
              config.lampiExport.bucketName,
            "kayttooikeus.tasks.export.copy-to-lampi": "true",
          }
        : {
          "kayttooikeus.tasks.export.enabled": "false",
          "kayttooikeus.tasks.export.bucket-name": exportBucket.bucketName,
          };

    const lampiSecrets: ecs.ContainerDefinitionProps["secrets"] = config.lampiExport
      ? {
          "kayttooikeus.tasks.export.lampi-role-arn":
            this.ssmString("LampiRoleArn2"),
          "kayttooikeus.tasks.export.lampi-external-id":
            this.ssmSecret("LampiExternalId"),
        } : {};

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        postgres_host: database.clusterEndpoint.hostname,
        postgres_port: database.clusterEndpoint.port.toString(),
        postgres_database: "kayttooikeus",
        export_bucket_name: exportBucket.bucketName,
        ...lampiProperties,
        "oppijanumerorekisteri.baseurl": config.oppijanumerorekisteriBaseUrl,
      },
      secrets: {
        postgres_username: ecs.Secret.fromSecretsManager(
          database.secret!,
          "username"
        ),
        postgres_password: ecs.Secret.fromSecretsManager(
          database.secret!,
          "password"
        ),
        ssm_cas_mfa_username: this.ssmSecret("CasMfaUsername"),
        ssm_cas_mfa_password: this.ssmSecret("CasMfaPassword"),
        ssm_cas_gauth_encryption_key: this.ssmSecret("CasGauthEncryptionKey"),
        ssm_cas_gauth_signing_key: this.ssmSecret("CasGauthSigningKey"),
        ssm_kayttooikeus_username: this.ssmSecret("PalvelukayttajaUsername"),
        ssm_kayttooikeus_password: this.ssmSecret("PalvelukayttajaPassword"),
        ssm_kayttooikeus_oauth2_client_id: this.ssmSecret("PalvelukayttajaClientId"),
        ssm_kayttooikeus_oauth2_client_secret: this.ssmSecret("PalvelukayttajaClientSecret"),
        ssm_kayttooikeus_crypto_password: this.ssmSecret("CryptoPassword"),
        ssm_kayttooikeus_kutsu_allowlist: this.ssmSecret("KutsuAllowlist"),
        ssm_kayttooikeus_oauth2_publickey: this.ssmSecret("Oauth2PublicKey"),
        ssm_kayttooikeus_oauth2_privatekey: this.ssmSecret("Oauth2PrivateKey"),
        ssm_auth_cryptoservice_static_salt: this.ssmSecret(
          "CryptoserviceStaticSalt"
        ),
        ...lampiSecrets,
      },
      portMappings: [
        {
          name: "kayttooikeus",
          containerPort: appPort,
          hostPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    if (config.lampiExport) {
      taskDefinition.addToTaskRolePolicy(
        new iam.PolicyStatement({
          actions: ["sts:AssumeRole"],
          resources: [
            ssm.StringParameter.valueFromLookup(
              this,
              "/kayttooikeus/LampiRoleArn2"
            ),
          ],
        })
      );
    }

    const appSecurityGroup = new ec2.SecurityGroup(this, "AppSecurityGroup", {
      vpc,
    });

    const service = new ecs.FargateService(this, "Service", {
      cluster,
      taskDefinition,
      desiredCount: config.minCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      securityGroups: [appSecurityGroup],
      healthCheckGracePeriod: cdk.Duration.minutes(5),
      circuitBreaker: { enable: true },
    });
    const scaling = service.autoScaleTaskCount({
      minCapacity: config.minCapacity,
      maxCapacity: config.maxCapacity,
    });

    scaling.scaleOnMetric("ServiceScaling", {
      metric: service.metricCpuUtilization(),
      scalingSteps: [
        { upper: 15, change: -1 },
        { lower: 50, change: +1 },
        { lower: 65, change: +2 },
        { lower: 80, change: +3 },
      ],
    });

    dbSecurityGroup.addIngressRule(
      appSecurityGroup,
      ec2.Port.tcp(database.clusterEndpoint.port)
    );

    const albSecurityGroup = new ec2.SecurityGroup(this, "AlbSecurityGroup", {
      vpc,
    });
    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
        securityGroup: albSecurityGroup,
      }
    );

    const sharedHostedZone = route53.HostedZone.fromLookup(
      this,
      "YleiskayttoisetHostedZone",
      {
        domainName: ssm.StringParameter.valueFromLookup(this, "zoneName"),
      }
    );
    const albHostname = `kayttooikeus.${sharedHostedZone.zoneName}`;
    const albRecord = new route53.ARecord(this, "ALBARecord", {
      zone: sharedHostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb)
      ),
    });

    new route53.ARecord(this, "OAuthARecord", {
      zone: props.oauthHostedZone,
      recordName: config.otuvaDomain,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb),
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        subjectAlternativeNames: [config.otuvaDomain],
        validation: certificatemanager.CertificateValidation.fromDnsMultiZone({
          [albHostname]: sharedHostedZone,
          [config.otuvaDomain]: props.oauthHostedZone,
        }),
      }
    );

    const listener = alb.addListener("Listener", {
      protocol: elasticloadbalancingv2.ApplicationProtocol.HTTPS,
      port: 443,
      open: true,
      certificates: [albCertificate],
    });

    const target = listener.addTargets("ServiceTarget", {
      port: appPort,
      targets: [service],
      healthCheck: {
        enabled: true,
        interval: cdk.Duration.seconds(10),
        path: "/kayttooikeus-service/actuator/health",
        port: appPort.toString(),
      },
    });

    new ResponseAlarms(this, "ResponseAlarms", {
      prefix: prefix("Kayttooikeus"),
      alarmTopic: props.alarmTopic,
      alb,
      albThreshold: 10,
      target,
      targetThreshold: 25,
    });

    this.ipRestrictions(alb);
  }

  exportFailureAlarm(logGroup: logs.LogGroup, alarmTopic: sns.ITopic) {
    const metricFilter = logGroup.addMetricFilter(
      "ExportTaskSuccessMetricFilter",
      {
        filterPattern: logs.FilterPattern.literal(
          '"Kayttooikeus export task completed"'
        ),
        metricName: prefix("KayttooikeusExportTaskSuccess"),
        metricNamespace: "Otuva",
        metricValue: "1",
      }
    );
    const alarm = new cloudwatch.Alarm(this, "ExportFailingAlarm", {
      alarmName: prefix("KayttooikeusExportFailing"),
      metric: metricFilter.metric({
        statistic: "Sum",
        period: cdk.Duration.hours(1),
      }),
      comparisonOperator:
        cloudwatch.ComparisonOperator.LESS_THAN_OR_EQUAL_TO_THRESHOLD,
      threshold: 0,
      evaluationPeriods: 8,
      treatMissingData: cloudwatch.TreatMissingData.BREACHING,
    });
    alarm.addOkAction(new cloudwatch_actions.SnsAction(alarmTopic));
    alarm.addAlarmAction(new cloudwatch_actions.SnsAction(alarmTopic));
  }

  getIpAddresses(name: string): string[] {
    const parameterValue = ssm.StringParameter.valueFromLookup(
      this,
      `/ip-addresses/${name}`
    );
    return parameterValue.split(",");
  }

  ipRestrictions(alb: elasticloadbalancingv2.ApplicationLoadBalancer) {
    const config = getConfig();

    const ipSet = new wafv2.CfnIPSet(this, "UserDetailsIPSet", {
      ipAddressVersion: "IPV4",
      scope: "REGIONAL",
      addresses: [
        ...this.getIpAddresses("YleiskayttoisetAWS"),
        ...this.getIpAddresses("OpintopolkuVPN"),
        ...this.getIpAddresses("OpintopolkuAWS"),
        ...this.getIpAddresses("Valtori"),
      ],
    });

    const pathPatternSet = new wafv2.CfnRegexPatternSet(
      this,
      "UserDetailsPathPatternSet",
      {
        scope: "REGIONAL",
        regularExpressionList: [
          "/kayttooikeus-service/userDetails",
          "/kayttooikeus-service/userDetails/.*",
        ],
      }
    );

    const denyAccessRule: wafv2.CfnWebACL.RuleProperty = {
      name: "UserDetailsAllowRule",
      priority: 1,
      action: {
        block: {},
      },
      statement: {
        andStatement: {
          statements: [
            {
              notStatement: {
                statement: {
                  ipSetReferenceStatement: {
                    arn: ipSet.attrArn,
                  },
                },
              },
            },
            {
              regexPatternSetReferenceStatement: {
                arn: pathPatternSet.attrArn,
                fieldToMatch: {
                  uriPath: {},
                },
                textTransformations: [{ priority: 1, type: "NONE" }],
              },
            },
          ],
        },
      },
      visibilityConfig: {
        cloudWatchMetricsEnabled: false,
        metricName: "UserDetailsAllowRule",
        sampledRequestsEnabled: true,
      },
    };

    const acl = new wafv2.CfnWebACL(this, "UserDetailsACL", {
      defaultAction: {
        allow: {},
      },
      scope: "REGIONAL",
      rules: [denyAccessRule],
      visibilityConfig: {
        cloudWatchMetricsEnabled: false,
        metricName: "UserDetailsWebACL",
        sampledRequestsEnabled: true,
      },
    });

    new wafv2.CfnWebACLAssociation(this, "UserDetailsACLAssociation", {
      resourceArn: alb.loadBalancerArn,
      webAclArn: acl.attrArn,
    });
  }

  ssmString(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromStringParameterName(
        this,
        `Param${name}`,
        `/kayttooikeus/${name}`
      )
    );
  }

  ssmSecret(name: string): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/kayttooikeus/${name}` }
      )
    );
  }
}

type CasOppijaApplicationStackProps = cdk.StackProps & {
  alarmTopic: sns.ITopic;
  bastion: ec2.BastionHostLinux;
  ecsCluster: ecs.Cluster;
};

class CasOppijaApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    props: CasOppijaApplicationStackProps
  ) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME });

    const database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      defaultDatabaseName: "casoppija",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_16_4,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("casoppija", {
        secretName: prefix("CasOppijaDatabaseSecret"),
      }),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        enablePerformanceInsights: true,
        instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T4G,
          ec2.InstanceSize.LARGE
        ),
      }),
      readers: [],
    });

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: prefix("/cas-oppija"),
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../cas-oppija"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 2048,
        memoryLimitMiB: 5120,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      }
    );

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        cas_oppija_postgres_host: database.clusterEndpoint.hostname,
        cas_oppija_postgres_port: database.clusterEndpoint.port.toString(),
        cas_oppija_postgres_db: "casoppija",
      },
      secrets: {
        cas_oppija_postgres_username: ecs.Secret.fromSecretsManager(
          database.secret!,
          "username"
        ),
        cas_oppija_postgres_password: ecs.Secret.fromSecretsManager(
          database.secret!,
          "password"
        ),
        cas_oppija_tgc_encryption_key: this.ssmSecret("TgcEncryptionKey"),
        cas_oppija_tgc_signing_key: this.ssmSecret("TgcSigningKey"),
        cas_oppija_webflow_encryption_key: this.ssmSecret(
          "WebflowEncryptionKey"
        ),
        cas_oppija_webflow_signing_key: this.ssmSecret("WebflowSigningKey"),
        cas_oppija_suomifi_keystore_password: this.ssmSecret(
          "SuomifiKeystorePassword"
        ),
        cas_oppija_suomifi_private_key_password: this.ssmSecret(
          "SuomifiPrivateKeyPassword"
        ),
        cas_oppija_suomifi_valtuudet_client_id: this.ssmSecret(
          "SuomifiValtuudetClientId"
        ),
        cas_oppija_suomifi_valtuudet_api_key: this.ssmSecret(
          "SuomifiValtuudetApiKey"
        ),
        cas_oppija_suomifi_valtuudet_oauth_password: this.ssmSecret(
          "SuomifiValtuudetOauthPassword"
        ),
        cas_oppija_service_user_username: this.ssmSecret("ServiceUserUsername"),
        cas_oppija_service_user_password: this.ssmSecret("ServiceUserPassword"),
        keystore_base64: this.ssmSecret("keystore"),
        registered_service_1: this.ssmSecret("RegisteredService1"),
        registered_service_2: this.ssmSecret("RegisteredService2"),
        sp_metadata: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "SpMetadata",
            "/cas-oppija/SpMetadata"
          )
        ),
        idp_metadata: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "IdpMetadata",
            "/cas-oppija/IdpMetadata"
          )
        ),
      },
      portMappings: [
        {
          name: "cas-oppija",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    const config = getConfig();
    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: config.minCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
      circuitBreaker: { enable: true },
    });
    const scaling = service.autoScaleTaskCount({
      minCapacity: config.minCapacity,
      maxCapacity: config.maxCapacity,
    });

    scaling.scaleOnMetric("ServiceScaling", {
      metric: service.metricCpuUtilization(),
      scalingSteps: [
        { upper: 15, change: -1 },
        { lower: 50, change: +1 },
        { lower: 65, change: +2 },
        { lower: 80, change: +3 },
      ],
    });

    database.connections.allowDefaultPortFrom(service);
    database.connections.allowDefaultPortFrom(props.bastion.connections);

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
      }
    );

    const sharedHostedZone = route53.HostedZone.fromLookup(
      this,
      "YleiskayttoisetHostedZone",
      {
        domainName: ssm.StringParameter.valueFromLookup(this, "zoneName"),
      }
    );
    const albHostname = `cas-oppija.${sharedHostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: sharedHostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb)
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(sharedHostedZone),
      }
    );

    const listener = alb.addListener("Listener", {
      protocol: elasticloadbalancingv2.ApplicationProtocol.HTTPS,
      port: 443,
      open: true,
      certificates: [albCertificate],
    });

    const target = listener.addTargets("ServiceTarget", {
      port: appPort,
      targets: [service],
      healthCheck: {
        enabled: true,
        interval: cdk.Duration.seconds(10),
        path: "/cas-oppija/actuator/health",
        port: appPort.toString(),
      },
    });

    new ResponseAlarms(this, "ResponseAlarms", {
      prefix: prefix("CasOppija"),
      alarmTopic: props.alarmTopic,
      alb,
      albThreshold: 10,
      target,
      targetThreshold: 10,
    });
  }
  ssmSecret(name: string, prefix: string = "cas-oppija"): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/${prefix}/${name}` }
      )
    );
  }
}

type CasVirkailijaApplicationStackProps = cdk.StackProps & {
  alarmTopic: sns.ITopic;
  ecsCluster: ecs.Cluster;
  bastion: ec2.BastionHostLinux;
};

class CasVirkailijaApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    props: CasVirkailijaApplicationStackProps
  ) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME });

    const heapDumpBucket = new s3.Bucket(this, "CasVirkailijaHeapDumpBucket", {})

    const database = new rds.DatabaseCluster(this, "Database", {
      vpc,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_ISOLATED },
      defaultDatabaseName: "cas",
      engine: rds.DatabaseClusterEngine.auroraPostgres({
        version: rds.AuroraPostgresEngineVersion.VER_12_17,
      }),
      credentials: rds.Credentials.fromGeneratedSecret("cas", {
        secretName: prefix("CasVirkailijaDatabaseSecret"),
      }),
      storageType: rds.DBClusterStorageType.AURORA,
      writer: rds.ClusterInstance.provisioned("writer", {
        enablePerformanceInsights: true,
        instanceType: ec2.InstanceType.of(
          ec2.InstanceClass.T4G,
          ec2.InstanceSize.LARGE
        ),
      }),
      readers: [],
    });

    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: prefix("/cas-virkailija"),
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../cas-virkailija"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 2048,
        memoryLimitMiB: 8192,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      }
    );
    heapDumpBucket.grantReadWrite(taskDefinition.taskRole);

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
        cas_postgres_host: database.clusterEndpoint.hostname,
        cas_postgres_port: database.clusterEndpoint.port.toString(),
        cas_postgres_database: "cas",
        HEAP_DUMP_BUCKET: heapDumpBucket.bucketName,
      },
      secrets: {
        cas_postgres_username: ecs.Secret.fromSecretsManager(
          database.secret!,
          "username"
        ),
        cas_postgres_password: ecs.Secret.fromSecretsManager(
          database.secret!,
          "password"
        ),
        cas_pac4j_encryption_key: this.ssmSecret("CasPac4jEncryptionKey"),
        cas_pac4j_signing_key: this.ssmSecret("CasPac4jSigningKey"),
        cas_mpassid_oidc_id: this.ssmSecret("CasMpassidOidcId"),
        cas_mpassid_oidc_secret: this.ssmSecret("CasMpassidOidcSecret"),
        cas_haka_keystore_password: this.ssmSecret("CasHakaKeystorePassword"),
        cas_suomifi_keystore_password: this.ssmSecret("SuomifiKeystorePassword"),
        cas_suomifi_private_key_password: this.ssmSecret("SuomifiPrivateKeyPassword"),
        cas_tgc_encryption_key: this.ssmSecret("TgcEncryptionKey"),
        cas_tgc_signing_key: this.ssmSecret("TgcSigningKey"),
        cas_webflow_encryption_key: this.ssmSecret("WebflowEncryptionKey"),
        cas_webflow_signing_key: this.ssmSecret("WebflowSigningKey"),
        cas_interrupt_cookie_encryption_key: this.ssmSecret("InterruptCookieEncryptionKey"),
        cas_interrupt_cookie_signing_key: this.ssmSecret("InterruptCookieSigningKey"),
        cas_mfa_username: this.ssmSecret("CasMfaUsername", "kayttooikeus"),
        cas_mfa_password: this.ssmSecret("CasMfaPassword", "kayttooikeus"),
        cas_gauth_encryption_key: this.ssmSecret(
          "CasGauthEncryptionKey",
          "kayttooikeus"
        ),
        cas_gauth_signing_key: this.ssmSecret(
          "CasGauthSigningKey",
          "kayttooikeus"
        ),
        serviceprovider_app_username_to_usermanagement: this.ssmSecret(
          "AppUsernameToUserManagement",
          "service-provider"
        ),
        serviceprovider_app_password_to_usermanagement: this.ssmSecret(
          "AppPasswordToUserManagement",
          "service-provider"
        ),
        haka_keystore_base64: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "HakaKeystore",
            "/cas/HakaKeystore"
          )
        ),
        haka_sp_metadata: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "HakaSpMetadata",
            "/cas/HakaSpMetadata"
          )
        ),
        cas_suomifi_sp_metadata: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "SuomifiSpMetadata",
            "/cas/SuomifiSpMetadata"
          )
        ),
        cas_suomifi_keystore_base64: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "SuomifiKeystore",
            "/cas/SuomifiKeystore"
          )
        ),
        cas_suomifi_idp_metadata: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "IdpMetadata",
            "/cas-oppija/IdpMetadata"
          )
        ),
      },
      portMappings: [
        {
          name: "cas-virkailija",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    const config = getConfig();
    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: config.minCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
      circuitBreaker: { enable: true },
    });
    const scaling = service.autoScaleTaskCount({
      minCapacity: config.minCapacity,
      maxCapacity: config.maxCapacity,
    });

    scaling.scaleOnMetric("ServiceScaling", {
      metric: service.metricCpuUtilization(),
      scalingSteps: [
        { upper: 15, change: -1 },
        { lower: 50, change: +1 },
        { lower: 65, change: +2 },
        { lower: 80, change: +3 },
      ],
    });

    database.connections.allowDefaultPortFrom(service);
    database.connections.allowDefaultPortFrom(props.bastion.connections);

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
      }
    );

    const sharedHostedZone = route53.HostedZone.fromLookup(
      this,
      "YleiskayttoisetHostedZone",
      {
        domainName: ssm.StringParameter.valueFromLookup(this, "zoneName"),
      }
    );
    const albHostname = `cas.${sharedHostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: sharedHostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb)
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(sharedHostedZone),
      }
    );

    const listener = alb.addListener("Listener", {
      protocol: elasticloadbalancingv2.ApplicationProtocol.HTTPS,
      port: 443,
      open: true,
      certificates: [albCertificate],
    });

    const target = listener.addTargets("ServiceTarget", {
      port: appPort,
      targets: [service],
      stickinessCookieDuration: cdk.Duration.hours(1),
      healthCheck: {
        enabled: true,
        path: "/cas/actuator/health",
        port: appPort.toString(),
        interval: cdk.Duration.seconds(120),
        unhealthyThresholdCount: 5,
      },
    });
    new ResponseAlarms(this, "ResponseAlarms", {
      prefix: prefix("CasVirkailija"),
      alarmTopic: props.alarmTopic,
      alb,
      albThreshold: 10,
      target,
      targetThreshold: 10,
    });
  }
  ssmSecret(name: string, prefix: string = "cas"): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/${prefix}/${name}` }
      )
    );
  }
}

type ServiceProviderApplicationStackProps = cdk.StackProps & {
  bastion: ec2.BastionHostLinux;
  ecsCluster: ecs.Cluster;
};

class ServiceProviderApplicationStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    props: ServiceProviderApplicationStackProps
  ) {
    super(scope, id, props);

    const vpc = ec2.Vpc.fromLookup(this, "Vpc", { vpcName: VPC_NAME });
    const logGroup = new logs.LogGroup(this, "AppLogGroup", {
      logGroupName: prefix("/service-provider"),
      retention: logs.RetentionDays.INFINITE,
    });

    const dockerImage = new ecr_assets.DockerImageAsset(this, "AppImage", {
      directory: path.join(__dirname, "../../service-provider"),
      file: "Dockerfile",
      platform: ecr_assets.Platform.LINUX_ARM64,
    });

    const taskDefinition = new ecs.FargateTaskDefinition(
      this,
      "TaskDefinition",
      {
        cpu: 2048,
        memoryLimitMiB: 5120,
        runtimePlatform: {
          operatingSystemFamily: ecs.OperatingSystemFamily.LINUX,
          cpuArchitecture: ecs.CpuArchitecture.ARM64,
        },
      }
    );

    const appPort = 8080;
    taskDefinition.addContainer("AppContainer", {
      image: ecs.ContainerImage.fromDockerImageAsset(dockerImage),
      logging: new ecs.AwsLogDriver({ logGroup, streamPrefix: "app" }),
      environment: {
        ENV: getEnvironment(),
      },
      secrets: {
        ssm_keystore_password: this.ssmSecret("KeystorePassword"),
        ssm_app_username_to_usermanagement: this.ssmSecret("AppUsernameToUserManagement"),
        ssm_app_password_to_usermanagement: this.ssmSecret("AppPasswordToUserManagement"),
        ssm_sp_keyalias: this.ssmSecret("SpKeyAlias"),
        ssm_sp_keyalias_secondary: this.ssmSecret("SpKeyAliasSecondary"),
        ssm_sp_keypassword: this.ssmSecret("SpKeyPassword"),
        ssm_mpassid_keyalias: this.ssmSecret("MpassidKeyAlias"),
        keystore: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "Keystore",
            "/service-provider/Keystore"
          )
        ),
        hakasp: ecs.Secret.fromSecretsManager(
          secretsmanager.Secret.fromSecretNameV2(
            this,
            "HakaSpMetadata",
            "/service-provider/HakaSpMetadata"
          )
        ),
      },
      portMappings: [
        {
          name: "service-provider",
          containerPort: appPort,
          appProtocol: ecs.AppProtocol.http,
        },
      ],
    });

    const config = getConfig();
    const service = new ecs.FargateService(this, "Service", {
      cluster: props.ecsCluster,
      taskDefinition,
      desiredCount: config.serviceProviderCapacity,
      minHealthyPercent: 100,
      maxHealthyPercent: 200,
      vpcSubnets: { subnetType: ec2.SubnetType.PRIVATE_WITH_EGRESS },
      healthCheckGracePeriod: cdk.Duration.minutes(5),
      circuitBreaker: { enable: true },
    });

    const alb = new elasticloadbalancingv2.ApplicationLoadBalancer(
      this,
      "LoadBalancer",
      {
        vpc,
        internetFacing: true,
      }
    );

    const sharedHostedZone = route53.HostedZone.fromLookup(
      this,
      "YleiskayttoisetHostedZone",
      {
        domainName: ssm.StringParameter.valueFromLookup(this, "zoneName"),
      }
    );
    const albHostname = `service-provider.${sharedHostedZone.zoneName}`;

    new route53.ARecord(this, "ALBARecord", {
      zone: sharedHostedZone,
      recordName: albHostname,
      target: route53.RecordTarget.fromAlias(
        new route53_targets.LoadBalancerTarget(alb)
      ),
    });

    const albCertificate = new certificatemanager.Certificate(
      this,
      "AlbCertificate",
      {
        domainName: albHostname,
        validation:
          certificatemanager.CertificateValidation.fromDns(sharedHostedZone),
      }
    );

    const listener = alb.addListener("Listener", {
      protocol: elasticloadbalancingv2.ApplicationProtocol.HTTPS,
      port: 443,
      open: true,
      certificates: [albCertificate],
    });

    listener.addTargets("ServiceTarget", {
      port: appPort,
      targets: [service],
      healthCheck: {
        enabled: true,
        interval: cdk.Duration.seconds(10),
        path: "/service-provider-app/buildversion.txt",
        port: appPort.toString(),
      },
    });
  }
  ssmSecret(name: string, prefix: string = "service-provider"): ecs.Secret {
    return ecs.Secret.fromSsmParameter(
      ssm.StringParameter.fromSecureStringParameterAttributes(
        this,
        `Param${name}`,
        { parameterName: `/${prefix}/${name}` }
      )
    );
  }
}

const app = new CdkApp({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: CDK_QUALIFIER,
  }),
});
app.synth();
