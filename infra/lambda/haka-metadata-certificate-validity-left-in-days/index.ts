import { X509Certificate } from "node:crypto";

const hakaMetadataUrl: string = process.env.HAKA_METADATA_URL!;

interface Result {
  subject: string;
  issuer: string;
  validFrom: string;
  validTo: string;
  daysRemaining: number;
}

export const handler = async () => {
  const xml = await fetchMetadataXml();
  const x509Str = extractSignatureX509Cert(xml);
  const inPemFormat = toPem(x509Str);
  const evaluatedCert = evaluateCertificate(inPemFormat);

  console.log(
    JSON.stringify({
      level: "INFO",
      message: `Haka metadata X509 certificate valid days remaining: ${evaluatedCert.daysRemaining}`,
      validDaysLeft: evaluatedCert.daysRemaining,
      validFrom: evaluatedCert.validFrom,
      validTo: evaluatedCert.validTo,
    }),
  );
};

async function fetchMetadataXml() {
  const response = await fetch(hakaMetadataUrl);
  if (!response.ok) {
    throw new Error(
      `Failed to fetch Haka metadata with url ${hakaMetadataUrl}: ${response.status} ${response.statusText}`,
    );
  }
  const xmlStr = await response.text();
  return xmlStr;
}

function extractSignatureX509Cert(xml: string): string {
  const elementsBeforeEntityDescriptors =
    getElementsBeforeEntityDescriptors(xml);

  if (!/<(?:[\w.-]+:)?Signature\b/.test(elementsBeforeEntityDescriptors)) {
    throw new Error(
      "Root <EntitiesDescriptor> has no direct <Signature> child",
    );
  }

  const signatureX509CertMatch = elementsBeforeEntityDescriptors.match(
    /<(?:[\w.-]+:)?X509Certificate\b[^>]*>([^<]+)</,
  );
  if (!signatureX509CertMatch) {
    throw new Error(
      "Could not find X509Certificate under root Signature/KeyInfo/X509Data",
    );
  }

  return signatureX509CertMatch[1].replace(/\s+/g, "");
}

function getElementsBeforeEntityDescriptors(xml: string) {
  const rootOpenTagMatch = xml.match(
    /<(?:[\w.-]+:)?EntitiesDescriptor\b[^>]*>/,
  );
  if (!rootOpenTagMatch || rootOpenTagMatch.index === undefined) {
    throw new Error(
      "Unexpected XML format, XML root level element was not <EntitiesDescriptor>",
    );
  }

  const afterRootOpenTagIndex =
    rootOpenTagMatch.index + rootOpenTagMatch[0].length;

  const firstEntityDescriptorMatch = xml
    .slice(afterRootOpenTagIndex)
    .match(/<(?:[\w.-]+:)?EntityDescriptor\b/);

  const lastIndexBeforeEntityDescriptors =
    firstEntityDescriptorMatch && firstEntityDescriptorMatch.index !== undefined
      ? afterRootOpenTagIndex + firstEntityDescriptorMatch.index
      : xml.length; // no EntityDescriptor children at all - scan to end of document

  return xml.slice(afterRootOpenTagIndex, lastIndexBeforeEntityDescriptors);
}

function toPem(base64Cert: string): string {
  const lines = base64Cert.match(/.{1,64}/g) ?? [base64Cert];
  return `-----BEGIN CERTIFICATE-----\n${lines.join("\n")}\n-----END CERTIFICATE-----\n`;
}

function evaluateCertificate(pem: string): Result {
  const cert = new X509Certificate(pem);
  const validTo = new Date(cert.validTo);
  const validFrom = new Date(cert.validFrom);
  const timeRemainingInMillis = validTo.getTime() - Date.now();
  const daysRemaining = Math.floor(
    timeRemainingInMillis / (1000 * 60 * 60 * 24),
  );

  return {
    subject: cert.subject,
    issuer: cert.issuer,
    validTo: validTo.toISOString(),
    validFrom: validFrom.toISOString(),
    daysRemaining,
  };
}
