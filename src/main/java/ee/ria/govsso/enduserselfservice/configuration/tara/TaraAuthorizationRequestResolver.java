package ee.ria.govsso.enduserselfservice.configuration.tara;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static ee.ria.govsso.enduserselfservice.configuration.tara.TaraAuthenticationContextClassReferenceValidator.MINIMUM_LEVEL_OF_ASSURANCE;
import static ee.ria.govsso.enduserselfservice.configuration.tara.TaraOidcConfiguration.REGISTRATION_ID;

public class TaraAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver requestResolver;

    public TaraAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.requestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest httpServletRequest) {
        OAuth2AuthorizationRequest authorizationRequest = requestResolver.resolve(httpServletRequest);
        if (authorizationRequest == null) {
            return null;
        }
        return customAuthorizationRequest(authorizationRequest, httpServletRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest httpServletRequest, String clientRegistrationId) {
        if (!Objects.equals(clientRegistrationId, REGISTRATION_ID)) {
            throw new IllegalArgumentException(
                    TaraAuthorizationRequestResolver.class.getName() + " only supports Tara");
        }
        OAuth2AuthorizationRequest authorizationRequest = requestResolver.resolve(httpServletRequest, clientRegistrationId);
        if (authorizationRequest == null) {
            return null;
        }
        return customAuthorizationRequest(authorizationRequest, httpServletRequest);
    }

    private OAuth2AuthorizationRequest customAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest httpServletRequest) {
        Map<String, Object> additionalParameters = new LinkedHashMap<>(authorizationRequest.getAdditionalParameters());

        /*
            Make sure a session exists, otherwise attempting to change session ID will cause an exception.
            OAuth2AuthorizationRequestRedirectFilter will create new session right after resolving
            authorization request, so it's ok to create it here also.
        */
        httpServletRequest.getSession();
        httpServletRequest.changeSessionId();

        //TODO (GSSO-617)
        //additionalParameters.put(TaraParameters.UI_LOCALES, LocaleContextHolder.getLocale());
        additionalParameters.put(TaraParameters.ACR_VALUES, MINIMUM_LEVEL_OF_ASSURANCE.name().toLowerCase());

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
