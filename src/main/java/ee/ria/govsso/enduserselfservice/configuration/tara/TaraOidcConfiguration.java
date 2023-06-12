package ee.ria.govsso.enduserselfservice.configuration.tara;

import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import lombok.RequiredArgsConstructor;
import net.minidev.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
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
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Configuration
@RequiredArgsConstructor
public class TaraOidcConfiguration {

    public static final String REGISTRATION_ID = "tara";
    public static final String OPENID_SCOPE = "openid";
    public static final String OIDC_METADATA_PATH = "/.well-known/openid-configuration";

    @Bean
    public Supplier<ResponseEntity<JSONObject>> taraMetadataRequestPerformer(
            TaraConfigurationProperties taraConfigurationProperties,
            RestOperations taraRestTemplate) {
        URI issuerUri = URI.create(taraConfigurationProperties.issuerUri());
        URI metadataUri = UriComponentsBuilder.fromUri(issuerUri)
                .replacePath(issuerUri.getPath() + OIDC_METADATA_PATH)
                .build(Collections.emptyMap());
        RequestEntity<Void> request = RequestEntity.get(metadataUri).build();
        return () -> taraRestTemplate.exchange(request, JSONObject.class);
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(
            TaraConfigurationProperties taraConfigurationProperties,
            Supplier<ResponseEntity<JSONObject>> taraMetadataRequestPerformer) {
        ClientRegistration clientRegistration =
                createClientRegistration(REGISTRATION_ID, taraConfigurationProperties, taraMetadataRequestPerformer);
        return new InMemoryClientRegistrationRepository(List.of(clientRegistration));
    }

    private static ClientRegistration createClientRegistration(
            String registrationId,
            TaraConfigurationProperties properties,
            Supplier<ResponseEntity<JSONObject>> taraMetadataRequestPerformer) {
        String issuer = requireNonNull(properties.issuerUri());
        OIDCProviderMetadata metadata = getMetadata(issuer, taraMetadataRequestPerformer);
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

    private static OIDCProviderMetadata getMetadata(String issuer,
                                             Supplier<ResponseEntity<JSONObject>> taraMetadataRequestPerformer) {
        ResponseEntity<JSONObject> response = taraMetadataRequestPerformer.get();
        JSONObject configuration = requireNonNull(response.getBody());
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
