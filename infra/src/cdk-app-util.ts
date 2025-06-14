import * as codestarconnections from "aws-cdk-lib/aws-codestarconnections";
import * as cdk from "aws-cdk-lib";
import * as codebuild from "aws-cdk-lib/aws-codebuild";
import * as codepipeline from "aws-cdk-lib/aws-codepipeline";
import { PipelineType } from "aws-cdk-lib/aws-codepipeline";
import * as codepipeline_actions from "aws-cdk-lib/aws-codepipeline-actions";
import * as constructs from "constructs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { legacyPrefix, CDK_QUALIFIER } from "./shared-account";
import { ROUTE53_HEALTH_CHECK_REGION } from "./health-check";

class CdkAppUtil extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);

    const env = {
      account: process.env.CDK_DEFAULT_ACCOUNT,
      region: process.env.CDK_DEFAULT_REGION,
    };
    new ContinousDeploymentStack(
      this,
      legacyPrefix("ContinuousDeploymentStack"),
      {
        env,
      }
    );
  }
}

class ContinousDeploymentStack extends cdk.Stack {
  constructor(scope: constructs.Construct, id: string, props: cdk.StackProps) {
    super(scope, id, props);
    const connection = new codestarconnections.CfnConnection(
      this,
      "GithubConnection",
      {
        connectionName: "GithubConnection",
        providerType: "GitHub",
      }
    );

    new ContinousDeploymentPipelineStack(
      this,
      legacyPrefix("HahtuvaContinuousDeploymentPipeline"),
      connection,
      "hahtuva",
      { owner: "Opetushallitus", name: "otuva", branch: "master" },
      props
    );
    new ContinousDeploymentPipelineStack(
      this,
      legacyPrefix("DevContinuousDeploymentPipeline"),
      connection,
      "dev",
      { owner: "Opetushallitus", name: "otuva", branch: "green-hahtuva" },
      props
    );
    new ContinousDeploymentPipelineStack(
      this,
      legacyPrefix("QaContinuousDeploymentPipeline"),
      connection,
      "qa",
      { owner: "Opetushallitus", name: "otuva", branch: "green-dev" },
      props
    );
    new ContinousDeploymentPipelineStack(
      this,
      legacyPrefix("ProdContinuousDeploymentPipeline"),
      connection,
      "prod",
      { owner: "Opetushallitus", name: "otuva", branch: "green-qa" },
      props
    );
  }
}

type EnvironmentName = "hahtuva" | "dev" | "qa" | "prod";

type Repository = {
  owner: string;
  name: string;
  branch: string;
};

class ContinousDeploymentPipelineStack extends cdk.Stack {
  constructor(
    scope: constructs.Construct,
    id: string,
    connection: codestarconnections.CfnConnection,
    env: EnvironmentName,
    repository: Repository,
    props: cdk.StackProps
  ) {
    super(scope, id, props);
    const capitalizedEnv = capitalize(env);

    const pipeline = new codepipeline.Pipeline(
      this,
      `Deploy${capitalizedEnv}Pipeline`,
      {
        pipelineName: legacyPrefix(`Deploy${capitalizedEnv}`),
        pipelineType: PipelineType.V1,
      }
    );
    cdk.Tags.of(pipeline).add("Repository", `${repository.owner}/${repository.name}`, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });
    cdk.Tags.of(pipeline).add("FromBranch", repository.branch, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });
    cdk.Tags.of(pipeline).add("ToBranch", `green-${env}`, { includeResourceTypes: ["AWS::CodePipeline::Pipeline"] });
    const sourceOutput = new codepipeline.Artifact();
    const sourceAction =
      new codepipeline_actions.CodeStarConnectionsSourceAction({
        actionName: "Source",
        connectionArn:
          "arn:aws:codestar-connections:eu-west-1:905418271050:connection/7ac52483-3a38-49db-8f38-5708a9dc8ccc",
        codeBuildCloneOutput: true,
        owner: repository.owner,
        repo: repository.name,
        branch: repository.branch,
        output: sourceOutput,
        triggerOnPush: ["hahtuva", "dev"].includes(env),
      });
    const sourceStage = pipeline.addStage({ stageName: "Source" });
    sourceStage.addAction(sourceAction);

    const runTests = env === "hahtuva";
    if (runTests) {
      const testStage = pipeline.addStage({ stageName: "Test" });
      testStage.addAction(
        new codepipeline_actions.CodeBuildAction({
          actionName: "TestKayttooikeus",
          input: sourceOutput,
          project: makeTestProject(
            this,
            env,
            "TestKayttooikeus",
            ["scripts/ci/run-tests-kayttooikeus.sh"],
            "corretto21"
          ),
        })
      );
      testStage.addAction(
        new codepipeline_actions.CodeBuildAction({
          actionName: "TestCasVirkailija",
          input: sourceOutput,
          project: makeTestProject(
            this,
            env,
            "TestCasVirkailija",
            ["scripts/ci/run-tests-cas-virkailija.sh"],
            "corretto21"
          ),
        })
      );
      testStage.addAction(
        new codepipeline_actions.CodeBuildAction({
          actionName: "TestCasOppija",
          input: sourceOutput,
          project: makeTestProject(
            this,
            env,
            "TestCasOppija",
            ["scripts/ci/run-tests-cas-oppija.sh"],
            "corretto11"
          ),
        })
      );
      testStage.addAction(
        new codepipeline_actions.CodeBuildAction({
          actionName: "TestServiceProvider",
          input: sourceOutput,
          project: makeTestProject(
            this,
            env,
            "TestServiceProvider",
            ["scripts/ci/run-tests-service-provider.sh"],
            "corretto21"
          ),
        })
      );
    }

    const deployProject = new codebuild.PipelineProject(
      this,
      `Deploy${capitalizedEnv}Project`,
      {
        projectName: legacyPrefix(`Deploy${capitalizedEnv}`),
        concurrentBuildLimit: 1,
        environment: {
          buildImage: codebuild.LinuxArmBuildImage.AMAZON_LINUX_2_STANDARD_3_0,
          computeType: codebuild.ComputeType.SMALL,
          privileged: true,
        },
        environmentVariables: {
          CDK_DEPLOY_TARGET_ACCOUNT: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/account_id`,
          },
          CDK_DEPLOY_TARGET_REGION: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/region`,
          },
          DOCKER_USERNAME: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: "/docker/username",
          },
          DOCKER_PASSWORD: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: "/docker/password",
          },
          SLACK_NOTIFICATIONS_CHANNEL_WEBHOOK_URL: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/env/${env}/slack-notifications-channel-webhook`,
          },
          MVN_SETTINGSXML: {
            type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
            value: `/mvn/settingsxml`,
          },
        },
        buildSpec: codebuild.BuildSpec.fromObject({
          version: "0.2",
          env: {
            "git-credential-helper": "yes",
          },
          phases: {
            install: {
              "runtime-versions": {
                java: "corretto21",
              },
            },
            pre_build: {
              commands: [
                "docker login --username $DOCKER_USERNAME --password $DOCKER_PASSWORD",
                "sudo yum install -y perl-Digest-SHA", // for shasum command
                "echo $MVN_SETTINGSXML > ./kayttooikeus-service/settings.xml",
              ],
            },
            build: {
              commands: [
                `./deploy-${env}.sh && ./scripts/tag-green-build-${env}.sh && ./scripts/ci/publish-release-notes-${env}.sh`,
              ],
            },
          },
        }),
      }
    );

    const deploymentTargetAccount = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/account_id`
    );
    const deploymentTargetRegion = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/region`
    );

    const targetRegions = [deploymentTargetRegion, ROUTE53_HEALTH_CHECK_REGION];
    deployProject.role?.attachInlinePolicy(
      new iam.Policy(this, `Deploy${capitalizedEnv}Policy`, {
        statements: [
          new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            actions: ["sts:AssumeRole"],
            resources: targetRegions.flatMap((targetRegion) => [
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${CDK_QUALIFIER}-lookup-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${CDK_QUALIFIER}-file-publishing-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${CDK_QUALIFIER}-image-publishing-role-${deploymentTargetAccount}-${targetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${CDK_QUALIFIER}-deploy-role-${deploymentTargetAccount}-${targetRegion}`,
            ]),
          }),
        ],
      })
    );
    const deployAction = new codepipeline_actions.CodeBuildAction({
      actionName: "Deploy",
      input: sourceOutput,
      project: deployProject,
    });
    const deployStage = pipeline.addStage({ stageName: "Deploy" });
    deployStage.addAction(deployAction);
  }
}

function makeTestProject(
  scope: constructs.Construct,
  env: string,
  name: string,
  testCommands: string[],
  javaVersion: "corretto11" | "corretto21"
): codebuild.PipelineProject {
  return new codebuild.PipelineProject(
    scope,
    `${name}${capitalize(env)}Project`,
    {
      projectName: legacyPrefix(`${name}${capitalize(env)}`),
      concurrentBuildLimit: 1,
      environment: {
        buildImage: codebuild.LinuxArmBuildImage.AMAZON_LINUX_2_STANDARD_3_0,
        computeType: codebuild.ComputeType.SMALL,
        privileged: true,
      },
      environmentVariables: {
        CDK_DEPLOY_TARGET_ACCOUNT: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: `/env/${env}/account_id`,
        },
        CDK_DEPLOY_TARGET_REGION: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: `/env/${env}/region`,
        },
        DOCKER_USERNAME: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/docker/username",
        },
        DOCKER_PASSWORD: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/docker/password",
        },
        MVN_SETTINGSXML: {
          type: codebuild.BuildEnvironmentVariableType.PARAMETER_STORE,
          value: "/mvn/settingsxml",
        },
      },
      buildSpec: codebuild.BuildSpec.fromObject({
        version: "0.2",
        env: {
          "git-credential-helper": "yes",
        },
        phases: {
          install: {
            "runtime-versions": {
              java: javaVersion,
            },
          },
          pre_build: {
            commands: [
              "docker login --username $DOCKER_USERNAME --password $DOCKER_PASSWORD",
              "sudo yum install -y perl-Digest-SHA", // for shasum command
              "echo $MVN_SETTINGSXML > ./kayttooikeus-service/settings.xml",
            ],
          },
          build: {
            commands: testCommands,
          },
        },
      }),
    }
  );
}

function capitalize(s: string) {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const app = new CdkAppUtil({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: CDK_QUALIFIER,
  }),
});
app.synth();
