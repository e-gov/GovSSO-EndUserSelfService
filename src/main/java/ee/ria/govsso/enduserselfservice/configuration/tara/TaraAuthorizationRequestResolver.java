package ee.ria.govsso.enduserselfservice.configuration.tara;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.web.servlet.LocaleResolver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static ee.ria.govsso.enduserselfservice.configuration.tara.TaraAuthenticationContextClassReferenceValidator.MINIMUM_LEVEL_OF_ASSURANCE;
import static ee.ria.govsso.enduserselfservice.configuration.tara.TaraOidcConfiguration.REGISTRATION_ID;

public class TaraAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver requestResolver;
    private final LocaleResolver localeResolver;

    public TaraAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository, LocaleResolver localeResolver) {
        this.requestResolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        this.localeResolver = localeResolver;
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

        /* Use `LocaleResolver` instead of `LocaleContextHolder` as the latter doesn't work for some reason.
         * I assume `LocaleContextHolder` would be initialized later in the filter chain and returns some kind of
         * default value here but who knows.
         */
        additionalParameters.put(TaraParameters.UI_LOCALES, localeResolver.resolveLocale(httpServletRequest));
        additionalParameters.put(TaraParameters.ACR_VALUES, MINIMUM_LEVEL_OF_ASSURANCE.name().toLowerCase());

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }
}
