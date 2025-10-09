package fi.vm.sade.kayttooikeus.config.security;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import fi.vm.sade.kayttooikeus.model.Oauth2Client;
import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.Oauth2ClientRepository;
import fi.vm.sade.kayttooikeus.service.KayttajarooliProvider;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class AuthorizationServerSecurityConfig {
    private final KayttajatiedotRepository kayttajatiedotRepository;
    private final KayttajarooliProvider kayttajarooliProvider;
    private final Oauth2ClientRepository oauth2ClientRepository;

    @Value("${kayttooikeus.oauth2.publickey}")
    RSAPublicKey publicKey;

    @Value("${kayttooikeus.oauth2.privatekey}")
    RSAPrivateKey privateKey;

    @Bean
    @Order(3)
    SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
        return http.build();
    }

    @Bean
    RegisteredClientRepository registeredClientRepository() {
        return new RegisteredClientRepository() {
            @Override
            public void save(RegisteredClient registeredClient) {
                return;
            }

            private RegisteredClient toRegisteredClient(Oauth2Client client) {
                return RegisteredClient.withId(client.getUuid().toString())
                    .clientId(client.getId())
                    .clientSecret(client.getSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                    .scope("oauth2")
                    .build();
            }

            @Override
            public RegisteredClient findById(String id) {
                return oauth2ClientRepository.findByUuid(UUID.fromString(id))
                    .map(this::toRegisteredClient)
                    .orElse(null);
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                return oauth2ClientRepository.findById(clientId)
                    .map(this::toRegisteredClient)
                    .orElse(null);
            }
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    JWKSource<SecurityContext> jwkSource() {
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID("022a9cf4-556d-45e7-90b4-8330d4f33f8c")
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    @Bean
    JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer() {
        return context -> {
            if (context.getTokenType() == OAuth2TokenType.ACCESS_TOKEN) {
                String username = context.getPrincipal().getName();
                incrementLoginCounter(username);
                var oid = kayttajatiedotRepository.findOidByUsername(username)
                        .orElseThrow(() -> new RuntimeException("Henkilö oid not found for username: " + username));
                var roles = kayttajarooliProvider.getRolesByOrganisation(oid);
                context.getClaims().subject(oid);
                context.getClaims().claim("roles", roles);
            }
        };
    }

    private void incrementLoginCounter(String username) {
        var kayttajatiedot = kayttajatiedotRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kayttajatiedot not found for username: " + username));
        kayttajatiedot.incrementLoginCount();
        kayttajatiedotRepository.save(kayttajatiedot);
    }
}
