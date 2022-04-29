package fi.vm.sade.cas.oppija.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Pac4jConfiguration {
/*

    private CookieRetrievingCookieGenerator pac4jSessionStoreCookieGenerator;
    private Module pac4jJacksonModule;

    public Pac4jConfiguration(CookieRetrievingCookieGenerator pac4jSessionStoreCookieGenerator,
                              Module pac4jJacksonModule) {
        this.pac4jSessionStoreCookieGenerator = pac4jSessionStoreCookieGenerator;
        this.pac4jJacksonModule = pac4jJacksonModule;
    }

    @Bean("pac4jDelegatedSessionCookieManager")
    public DelegatedSessionCookieManager pac4jDelegatedSessionCookieManager() {
        return new DelegatedSessionCookieManager(pac4jSessionStoreCookieGenerator, pac4jDelegatedSessionStoreCookieSerializer());
    }

    public StringSerializer<Map<String, Object>> pac4jDelegatedSessionStoreCookieSerializer() {
        SessionStoreCookieSerializer serializer = new SessionStoreCookieSerializer();
        serializer.getObjectMapper().registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        serializer.getObjectMapper().registerModule(getConversationContainerModule());
        serializer.getObjectMapper().registerModule(getContainedConversationModule());
        serializer.getObjectMapper().registerModule(pac4jJacksonModule);
        return serializer;
    }

    private SimpleModule getConversationContainerModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ConversationContainer.class, new ConversationContainerDeserializer());
        return module;
    }

    private SimpleModule getContainedConversationModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(ContainedConversation.class, new ContainedConversationDeserializer());
        return module;
    }

    public class ConversationContainerDeserializer extends StdDeserializer<ConversationContainer> {

        public ConversationContainerDeserializer() {
            this(null);
        }

        public ConversationContainerDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ConversationContainer deserialize(JsonParser jp, DeserializationContext ctxt) {
            return new ConversationContainer(1, null);
        }
    }

    public class ContainedConversationDeserializer extends StdDeserializer<ContainedConversation> {

        public ContainedConversationDeserializer() {
            this(null);
        }

        public ContainedConversationDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ContainedConversation deserialize(JsonParser jp, DeserializationContext ctxt) {
            return new ContainedConversation(null, null, null);
        }
    }
*/

}
