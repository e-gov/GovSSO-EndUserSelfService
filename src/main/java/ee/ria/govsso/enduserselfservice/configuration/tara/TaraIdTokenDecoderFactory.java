package ee.ria.govsso.enduserselfservice.configuration.tara;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.client.RestOperations;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters;

@Component
@Qualifier("idTokenDecoderFactory")
@RequiredArgsConstructor
public class TaraIdTokenDecoderFactory implements JwtDecoderFactory<ClientRegistration> {

    private static final SignatureAlgorithm SIGNATURE_ALGORITHM = SignatureAlgorithm.RS256;

    private final RestOperations taraRestTemplate;
    private final Map<String, JwtDecoder> jwtDecoders = new ConcurrentHashMap<>();

    @Override
    public JwtDecoder createDecoder(ClientRegistration clientRegistration) {
        Assert.notNull(clientRegistration, "clientRegistration cannot be null");
        return jwtDecoders.computeIfAbsent(
                clientRegistration.getRegistrationId(),
                registrationId -> doCreateDecoder(clientRegistration));
    }

    private NimbusJwtDecoder doCreateDecoder(ClientRegistration clientRegistration) {
        String jwkSetUri = clientRegistration.getProviderDetails().getJwkSetUri();
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .jwsAlgorithm(SIGNATURE_ALGORITHM)
                .restOperations(taraRestTemplate)
                .build();
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new OidcIdTokenValidator(clientRegistration),
                new TaraAuthenticationContextClassReferenceValidator()
        ));
        jwtDecoder.setClaimSetConverter(new ClaimTypeConverter(createDefaultClaimTypeConverters()));
        return jwtDecoder;
    }

}
