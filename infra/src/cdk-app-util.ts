import * as codestarconnections from "aws-cdk-lib/aws-codestarconnections";
import * as cdk from "aws-cdk-lib";
import * as codebuild from "aws-cdk-lib/aws-codebuild";
import * as codepipeline from "aws-cdk-lib/aws-codepipeline";
import { PipelineType } from "aws-cdk-lib/aws-codepipeline";
import * as codepipeline_actions from "aws-cdk-lib/aws-codepipeline-actions";
import * as constructs from "constructs";
import * as iam from "aws-cdk-lib/aws-iam";
import * as ssm from "aws-cdk-lib/aws-ssm";
import { prefix, QUALIFIER } from "./shared-account";

class CdkAppUtil extends cdk.App {
  constructor(props: cdk.AppProps) {
    super(props);

    const env = {
      account: process.env.CDK_DEFAULT_ACCOUNT,
      region: process.env.CDK_DEFAULT_REGION,
    };
    new ContinousDeploymentStack(this, prefix("ContinuousDeploymentStack"), {
      env,
    });
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
      },
    );

    (["hahtuva", "dev", "qa", "prod"] as const).forEach(
      (env) =>
        new ContinousDeploymentPipelineStack(
          this,
          prefix(`${capitalize(env)}ContinuousDeploymentPipeline`),
          connection,
          env,
          { owner: "Opetushallitus", name: "otuva", branch: "master" },
          props,
        ),
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
    props: cdk.StackProps,
  ) {
    super(scope, id, props);
    const capitalizedEnv = capitalize(env);

    const pipeline = new codepipeline.Pipeline(
      this,
      `Deploy${capitalizedEnv}Pipeline`,
      {
        pipelineName: prefix(`Deploy${capitalizedEnv}`),
        pipelineType: PipelineType.V1,
      },
    );
    const tag = {
      hahtuva: repository.branch,
      dev: "green-hahtuva",
      qa: "green-dev",
      prod: "green-qa",
    }[env];
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
        triggerOnPush: env == "hahtuva",
      });
    const sourceStage = pipeline.addStage({ stageName: "Source" });
    sourceStage.addAction(sourceAction);
    const testCommands =
      env === "hahtuva" ? ["scripts/ci/run-tests.sh"] : [];
    const deployProject = new codebuild.PipelineProject(
      this,
      `Deploy${capitalizedEnv}Project`,
      {
        projectName: prefix(`Deploy${capitalizedEnv}`),
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
                `git checkout ${tag}`,
              ],
            },
            build: {
              commands: [
                ...testCommands,
                `./deploy-${env}.sh`,
                `./scripts/tag-green-build-${env}.sh`,
              ],
            },
          },
        }),
      },
    );

    const deploymentTargetAccount = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/account_id`,
    );
    const deploymentTargetRegion = ssm.StringParameter.valueFromLookup(
      this,
      `/env/${env}/region`,
    );

    deployProject.role?.attachInlinePolicy(
      new iam.Policy(this, `Deploy${capitalizedEnv}Policy`, {
        statements: [
          new iam.PolicyStatement({
            effect: iam.Effect.ALLOW,
            actions: ["sts:AssumeRole"],
            resources: [
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${QUALIFIER}-lookup-role-${deploymentTargetAccount}-${deploymentTargetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${QUALIFIER}-file-publishing-role-${deploymentTargetAccount}-${deploymentTargetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${QUALIFIER}-image-publishing-role-${deploymentTargetAccount}-${deploymentTargetRegion}`,
              `arn:aws:iam::${deploymentTargetAccount}:role/cdk-${QUALIFIER}-deploy-role-${deploymentTargetAccount}-${deploymentTargetRegion}`,
            ],
          }),
        ],
      }),
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

function capitalize(s: string) {
  return s.charAt(0).toUpperCase() + s.slice(1);
}

const app = new CdkAppUtil({
  defaultStackSynthesizer: new cdk.DefaultStackSynthesizer({
    qualifier: QUALIFIER,
  }),
});
app.synth();
