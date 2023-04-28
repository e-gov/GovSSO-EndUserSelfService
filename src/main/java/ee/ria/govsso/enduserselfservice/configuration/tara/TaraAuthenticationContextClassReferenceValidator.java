package ee.ria.govsso.enduserselfservice.configuration.tara;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.oauth2.jwt.Jwt;

import static com.nimbusds.openid.connect.sdk.assurance.IdentityAssuranceLevel.SUBSTANTIAL;

public class TaraAuthenticationContextClassReferenceValidator implements OAuth2TokenValidator<Jwt> {

    public static final EidasLevelOfAssurance MINIMUM_LEVEL_OF_ASSURANCE = EidasLevelOfAssurance.LOW;

    private static OAuth2TokenValidatorResult insufficientAcrValue() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_id_token",
                "The ID Token contains insufficient `acr` (eIDAS level of assurance) value, expected at least '%s'"
                        .formatted(SUBSTANTIAL.getValue()), null));
    }

    private static OAuth2TokenValidatorResult invalidAcrValue() {
        return OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_id_token",
                "The ID Token contains invalid `acr` (eIDAS level of assurance) value",
                "https://e-gov.github.io/GOVSSO/TechnicalSpecification#71-verification-of-the-id-token-and-logout-token"));
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        EidasLevelOfAssurance levelOfAssurance;
        try {
            levelOfAssurance = getLevelOfAssurance(token);
        } catch (IllegalArgumentException e) {
            return invalidAcrValue();
        }
        if (levelOfAssurance == null) {
            return insufficientAcrValue();
        }
        if (!levelOfAssurance.isAtLeast(MINIMUM_LEVEL_OF_ASSURANCE)) {
            return insufficientAcrValue();
        }
        return OAuth2TokenValidatorResult.success();
    }

    private EidasLevelOfAssurance getLevelOfAssurance(Jwt token) {
        String levelOfAssuranceString = token.getClaimAsString(IdTokenClaimNames.ACR);
        if (levelOfAssuranceString == null) {
            return null;
        }
        return EidasLevelOfAssurance.fromValue(levelOfAssuranceString);
    }

    enum EidasLevelOfAssurance implements Comparable<EidasLevelOfAssurance> {
        LOW, SUBSTANTIAL, HIGH;

        public static EidasLevelOfAssurance fromValue(String value) {
            return EidasLevelOfAssurance.valueOf(value.toUpperCase());
        }

        public boolean isAtLeast(EidasLevelOfAssurance minimumLevel) {
            return compareTo(minimumLevel) >= 0;
        }
    }

}
