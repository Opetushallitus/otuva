const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

export type Config = {
  otuvaDomain: string;
  opintopolkuHost: string;
  minCapacity: number;
  maxCapacity: number;
  serviceProviderCapacity: number;
  lampiExport?: {
    enabled: boolean;
    bucketName: string;
  };
  oppijanumerorekisteriBaseUrl: string;
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
  minCapacity: 2,
  maxCapacity: 8,
  oppijanumerorekisteriBaseUrl: "https://hahtuva.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const dev: Config = {
  ...defaultConfig,
  otuvaDomain: "dev.otuva.opintopolku.fi",
  opintopolkuHost: "untuvaopintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-dev",
  },
  oppijanumerorekisteriBaseUrl: "https://dev.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const qa: Config = {
  ...defaultConfig,
  otuvaDomain: "qa.otuva.opintopolku.fi",
  opintopolkuHost: "testiopintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-qa",
  },
  oppijanumerorekisteriBaseUrl: "https://qa.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};

export const prod: Config = {
  ...defaultConfig,
  otuvaDomain: "prod.otuva.opintopolku.fi",
  opintopolkuHost: "opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-prod",
  },
  oppijanumerorekisteriBaseUrl: "https://prod.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
};
