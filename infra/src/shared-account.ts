export const CDK_QUALIFIER = "kayttooike";

export function legacyPrefix(name: string): string {
  return `Kayttooikeus${name}`;
}

export function prefix(name: string): string {
  return `Otuva${name}`;
}

export const ALARM_TOPIC_ARN = "arn:aws:sns:eu-west-1:471112979851:alarm";

export const VPC_NAME = "vpc";
