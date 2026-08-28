const environments = ["hahtuva", "dev", "qa", "prod"] as const;
type EnvironmentName = (typeof environments)[number];

export type Config = {
  kayttooikeusTaskCpu: number;
  kayttooikeusTaskMemoryMiB: number;
  serviceProviderTaskCpu: number;
  serviceProviderTaskMemoryMiB: number;
  otuvaDomain: string;
  opintopolkuHost: string;
  minCapacity: number;
  maxCapacity: number;
  serviceProviderCapacity: number;
  lampiExport?: {
    enabled: boolean;
    bucketName: string;
  };
  auditCleanup: {
    enabled: boolean;
  };
  oppijanumerorekisteriBaseUrl: string;
  hakaMetadataCertificateValidDaysAlarm: {
    thresholdDays: number;
    hakaMetadataUrl: string;
  };
};
const defaultConfig = {
  kayttooikeusTaskCpu: 1024,
  kayttooikeusTaskMemoryMiB: 4096,
  serviceProviderTaskCpu: 1024,
  serviceProviderTaskMemoryMiB: 2048,
  // service-provider should run only single instance because it contains in-memory state for SAML message identifiers
  serviceProviderCapacity: 1,
  auditCleanup: {
    enabled: true,
  },
  hakaMetadataCertificateValidDaysAlarm: {
    thresholdDays: 30,
    hakaMetadataUrl: "https://haka.funet.fi/metadata/haka-metadata-v10.xml",
  },
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
  oppijanumerorekisteriBaseUrl:
    "https://hahtuva.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
  hakaMetadataCertificateValidDaysAlarm: {
    hakaMetadataUrl:
      "https://haka.funet.fi/metadata/haka_test_metadata_signed.xml",
    thresholdDays: 30,
  },
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
  oppijanumerorekisteriBaseUrl:
    "https://dev.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
  hakaMetadataCertificateValidDaysAlarm: {
    hakaMetadataUrl: "https://haka.funet.fi/metadata/haka-metadata-v9.xml",
    thresholdDays: 30,
  },
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
  oppijanumerorekisteriBaseUrl:
    "https://qa.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
  hakaMetadataCertificateValidDaysAlarm: {
    hakaMetadataUrl: "https://haka.funet.fi/metadata/haka-metadata-v9.xml",
    thresholdDays: 30,
  },
};

export const prod: Config = {
  ...defaultConfig,
  kayttooikeusTaskCpu: 2048,
  kayttooikeusTaskMemoryMiB: 4096,
  serviceProviderTaskCpu: 2048,
  serviceProviderTaskMemoryMiB: 5120,
  otuvaDomain: "prod.otuva.opintopolku.fi",
  opintopolkuHost: "opintopolku.fi",
  minCapacity: 2,
  maxCapacity: 8,
  lampiExport: {
    enabled: true,
    bucketName: "oph-lampi-prod",
  },
  oppijanumerorekisteriBaseUrl:
    "https://prod.oppijanumerorekisteri.opintopolku.fi/oppijanumerorekisteri-service",
  hakaMetadataCertificateValidDaysAlarm: {
    hakaMetadataUrl: "https://haka.funet.fi/metadata/haka-metadata-v10.xml",
    thresholdDays: 30,
  },
};
