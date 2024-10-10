package fi.vm.sade.kayttooikeus.config.security;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

import fi.vm.sade.kayttooikeus.repositories.KayttajatiedotRepository;
import fi.vm.sade.kayttooikeus.repositories.Oauth2ClientRepository;
import fi.vm.sade.kayttooikeus.service.KayttajarooliProvider;
import lombok.RequiredArgsConstructor;


@Configuration
@ConditionalOnProperty("kayttooikeus.oauth2.enabled")
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

            @Override
            public RegisteredClient findById(String id) {
                return null;
            }

            @Override
            public RegisteredClient findByClientId(String clientId) {
                return oauth2ClientRepository.findById(clientId)
                    .map(c -> RegisteredClient.withId(UUID.randomUUID().toString())
                        .clientId(c.getId())
                        .clientSecret(c.getSecret())
                        .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                        .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                        .scope("oauth2")
                        .build()
                    )
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
        KeyPair keyPair = new KeyPair(publicKey, privateKey);
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
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
                var roles = kayttajatiedotRepository.findOidByUsername(username)
                    .map(oid -> kayttajarooliProvider.getRolesByOrganisation(oid))
                    .orElse(Map.of());
                context.getClaims().claim("roles", roles);
            }
        };
    }
}
