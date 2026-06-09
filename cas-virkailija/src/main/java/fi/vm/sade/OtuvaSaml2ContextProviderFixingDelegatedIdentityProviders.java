package fi.vm.sade;

import org.apereo.cas.authentication.principal.ClientCustomPropertyConstants;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviderFactory;
import org.apereo.cas.pac4j.client.DelegatedIdentityProviders;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.context.WebContext;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.context.SAML2ContextProvider;
import org.pac4j.saml.credentials.extractor.SAML2CredentialsExtractor;

import java.util.List;

/**
 * Workaround for CAS 8 + identity-provider-metadata-aggregate=true: per-IdP
 * {@link SAML2Client} objects synthesised by {@code DelegatedClientSaml2Builder}
 * end up with their {@link SAML2ContextProvider} pointing at the wrong IdP's
 * {@code SAML2MetadataResolver}, so {@code peerEntityContext.entityId} resolves
 * to a different IdP than the one the client represents and signature trust
 * lookup fails.
 *
 * On first {@link #findAllClients} access, for each SAML2Client we rebuild a
 * fresh {@link SAML2ContextProvider} from the client's own (correctly wired)
 * configuration. A custom-property marker prevents repeated rebuilds.
 */
@Slf4j
public class OtuvaSaml2ContextProviderFixingDelegatedIdentityProviders implements DelegatedIdentityProviders {

    private static final String CONTEXT_PROVIDER_FIXED = "fi.vm.sade.contextProviderFixed";

    private final DelegatedIdentityProviderFactory factory;

    public OtuvaSaml2ContextProviderFixingDelegatedIdentityProviders(final DelegatedIdentityProviderFactory factory) {
        this.factory = factory;
        LOGGER.info("Installed Saml2ContextProviderFixingDelegatedIdentityProviders");
    }

    @Override
    public List<? extends Client> findAllClients(final Service service, final WebContext webContext) {
        return factory.build().stream().map(this::fixContentProviderIfNeeded).toList();
    }

    private Client fixContentProviderIfNeeded(final Client client) {
        if (!(client instanceof SAML2Client saml2Client)
                || !wasBuiltFromAggregate(saml2Client)
                || contentProviderIsAlreadyFixed(saml2Client)) {
            // Only the per-IdP clients synthesised from an aggregate metadata file need
            // this fix. Non-aggregate SAML2 clients (e.g. Suomi.fi) have a single IdP and
            // are wired correctly by pac4j on their own. Additionally, with
            // cas.authn.pac4j.core.lazy-init=true non-aggregate clients may not be init()-ed
            // at the time findAllClients runs — getIdentityProviderMetadataResolver() would
            // return null and we would overwrite their state with broken nulls.
            return client;
        } else {
            // Use the SAML2Client's own instance field (populated during init() from the
            // SAML2Configuration we set up in DelegatedClientSaml2Builder.createSaml2Configuration).
            // The config-side field is unreliable here: by the time findAllClients runs, it has
            // been nulled and getIdentityProviderMetadataResolver() falls back to the aggregate
            // default supplier, which returns the FIRST entity in the merged metadata.
            val clientResolver = saml2Client.getIdentityProviderMetadataResolver();
            val priorContextProvider = saml2Client.getContextProvider();

            // 1) Install a contextProvider built from this client's per-IdP resolver.
            val fixedContextProvider = new SAML2ContextProvider(
                    clientResolver,
                    saml2Client.getServiceProviderMetadataResolver(),
                    saml2Client.getConfiguration().getSamlMessageStoreFactory());
            saml2Client.setContextProvider(fixedContextProvider);

            // 2) Replace the credentialsExtractor too. DelegatedClientSaml2Builder.createSaml2Client
            // sets each per-IdP client's credentialsExtractor to the *parent* client's instance,
            // which captured the parent's (broken, aggregate-default) contextProvider in its own
            // field at construction time. We construct a fresh per-IdP extractor that captures
            // the contextProvider we just installed above.
            val fixedExtractor = new SAML2CredentialsExtractor(
                    saml2Client,
                    clientResolver,
                    saml2Client.getServiceProviderMetadataResolver(),
                    saml2Client.getSoapPipelineProvider());
            saml2Client.setCredentialsExtractor(fixedExtractor);

            saml2Client.getCustomProperties().put(CONTEXT_PROVIDER_FIXED, Boolean.TRUE);
            LOGGER.info("Rebuilt SAML2ContextProvider + credentialsExtractor for client [{}]:"
                            + " client-field resolver entityId=[{}], previous contextProvider=[{}],"
                            + " new contextProvider=[{}]",
                    saml2Client.getName(),
                    clientResolver != null ? clientResolver.getEntityId() : "null",
                    describe(priorContextProvider),
                    describe(fixedContextProvider));

            return saml2Client;
        }
    }

    private static String describe(final Object obj) {
        return obj == null ? "null" : obj.getClass().getSimpleName() + "@" + System.identityHashCode(obj);
    }

    private static boolean contentProviderIsAlreadyFixed(final SAML2Client client) {
        return Boolean.TRUE.equals(client.getCustomProperties().get(CONTEXT_PROVIDER_FIXED));
    }

    private static boolean wasBuiltFromAggregate(final SAML2Client client) {
        return Boolean.TRUE.equals(client.getCustomProperties()
                .get(ClientCustomPropertyConstants.CLIENT_CUSTOM_PROPERTY_IDENTITY_PROVIDER_METADATA_AGGREGATE));
    }
}
