package ee.ria.govsso.enduserselfservice.configuration.tara;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Configuration
@RequiredArgsConstructor
public class TaraOidcConfiguration {

    public static final String REGISTRATION_ID = "tara";
    public static final String OPENID_SCOPE = "openid";

    private static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";

    @Bean
    ClientRegistrationRepository clientRegistrationRepository(
            TaraConfigurationProperties taraConfigurationProperties,
            RestOperations taraRestTemplate) {
        ClientRegistration govSsoClientRegistration =
                createClientRegistration(REGISTRATION_ID, taraConfigurationProperties, taraRestTemplate);
        return new InMemoryClientRegistrationRepository(List.of(govSsoClientRegistration));
    }

    public ClientRegistration createClientRegistration(
            String registrationId, TaraConfigurationProperties properties, RestOperations restOperations) {
        String issuer = requireNonNull(properties.issuerUri());
        OIDCProviderMetadata metadata = getMetadata(issuer, restOperations);
        return ClientRegistration.withRegistrationId(registrationId)
                .userNameAttributeName(IdTokenClaimNames.SUB)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationUri(requireNonNull(metadata.getAuthorizationEndpointURI()).toASCIIString())
                .tokenUri(requireNonNull(metadata.getTokenEndpointURI()).toASCIIString())
                .jwkSetUri(requireNonNull(metadata.getJWKSetURI()).toASCIIString())
                .providerConfigurationMetadata(metadata.toJSONObject())
                .issuerUri(issuer)
                .clientId(properties.clientId())
                .clientSecret(properties.clientSecret())
                .scope(OPENID_SCOPE)
                .redirectUri(properties.redirectUri())
                .build();
    }

    private OIDCProviderMetadata getMetadata(String issuer, RestOperations govssoRestTemplate) {
        URI issuerUri = URI.create(issuer);
        URI metadataUri = UriComponentsBuilder.fromUri(issuerUri)
                .replacePath(issuerUri.getPath() + OIDC_METADATA_PATH)
                .build(Collections.emptyMap());
        RequestEntity<Void> request = RequestEntity.get(metadataUri).build();
        JSONObject configuration = requireNonNull(govssoRestTemplate.exchange(request, JSONObject.class).getBody());
        OIDCProviderMetadata metadata = parseMetadata(configuration);
        verifyIssuer(issuer, metadata);
        return metadata;
    }

    private static OIDCProviderMetadata parseMetadata(JSONObject configuration) {
        try {
            return OIDCProviderMetadata.parse(configuration);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static void verifyIssuer(String issuer, OIDCProviderMetadata metadata) {
        String metadataIssuer = metadata.getIssuer().getValue();
        if (!Objects.equals(issuer, metadataIssuer)) {
            throw new IllegalStateException("The Issuer \"" + metadataIssuer + "\" provided " +
                    "in the configuration metadata did not match the requested issuer \"" + issuer + "\"");
        }
    }

}
