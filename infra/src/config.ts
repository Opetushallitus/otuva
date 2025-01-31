const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

const defaultConfig = {
  opintopolkuHost: "",
  minCapacity: 0,
  maxCapacity: 0,
  // service-provider should run only single instance because it contains in-memory state for SAML message identifiers
  serviceProviderCapacity: 1,
};

export type Config = typeof defaultConfig;

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
  opintopolkuHost: "hahtuvaopintopolku.fi",
  minCapacity: 1,
  maxCapacity: 2,
};

export const dev: Config = {
  ...defaultConfig,
  opintopolkuHost: "untuvaopintopolku.fi",
  minCapacity: 1,
  maxCapacity: 2,
};

export const qa: Config = {
  ...defaultConfig,
  opintopolkuHost: "testiopintopolku.fi",
  minCapacity: 1,
  maxCapacity: 2,
};

export const prod: Config = {
  ...defaultConfig,
  opintopolkuHost: "opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
};
