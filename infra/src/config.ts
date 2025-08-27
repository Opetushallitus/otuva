const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

export type Config = {
  otuvaDomain: string;
  opintopolkuHost: string;
  oauthDomainName: string;
  minCapacity: number;
  maxCapacity: number;
  serviceProviderCapacity: number;
  lampiExport?: {
    enabled: boolean;
    bucketName: string;
  };
};
const defaultConfig = {
  // service-provider should run only single instance because it contains in-memory state for SAML message identifiers
  serviceProviderCapacity: 1,
};

export function getEnvironment(): EnvironmentName {
  const env = process.env.ENV;
  if (!env) {
    throw new Error("ENV environment variable is not set");
  }
  if (!contains(environments, env)) {
    throw new Error(`Invalid environment name: ${env}`);
  }
  return env as EnvironmentName;
}

function contains(arr: readonly string[], value: string): boolean {
  return arr.includes(value);
}

export function getConfig(): Config {
  const env = getEnvironment();
  return { hahtuva, dev, qa, prod }[env];
}

export const hahtuva: Config = {
  ...defaultConfig,
  otuvaDomain: "hahtuva.otuva.opintopolku.fi",
  opintopolkuHost: "hahtuvaopintopolku.fi",
  oauthDomainName: "hahtuva.otuva.opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
};

export const dev: Config = {
  ...defaultConfig,
  otuvaDomain: "dev.otuva.opintopolku.fi",
  opintopolkuHost: "untuvaopintopolku.fi",
  oauthDomainName: "dev.otuva.opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-dev",
  },
};

export const qa: Config = {
  ...defaultConfig,
  otuvaDomain: "qa.otuva.opintopolku.fi",
  opintopolkuHost: "testiopintopolku.fi",
  oauthDomainName: "qa.otuva.opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-qa",
  },
};

export const prod: Config = {
  ...defaultConfig,
  otuvaDomain: "prod.otuva.opintopolku.fi",
  opintopolkuHost: "opintopolku.fi",
  oauthDomainName: "prod.otuva.opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-prod",
  },
};
