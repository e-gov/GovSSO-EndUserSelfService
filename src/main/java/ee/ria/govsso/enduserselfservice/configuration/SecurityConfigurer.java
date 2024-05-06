package ee.ria.govsso.enduserselfservice.configuration;

import ee.ria.govsso.enduserselfservice.configuration.tara.TaraAuthorizationRequestResolver;
import ee.ria.govsso.enduserselfservice.session.SessionConfigurationProperties;
import ee.ria.govsso.enduserselfservice.session.SessionMaxAgeFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.client.RestOperations;
import org.springframework.web.servlet.LocaleResolver;

import java.io.IOException;
import java.time.Duration;

@Configuration
@Slf4j
@EnableWebSecurity
public class SecurityConfigurer {

    private static final String CONTENT_SECURITY_POLICY = "connect-src 'self'; " +
            "default-src 'none'; " +
            "font-src 'self'; " +
            "img-src 'self'; " +
            "script-src 'self'; " +
            "style-src 'self'; " +
            "base-uri 'none'; " +
            "frame-ancestors 'none'; " +
            "block-all-mixed-content";
    private static final Duration HST_MAX_AGE = Duration.ofDays(186);

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            RestOperations taraRestTemplate,
            ClientRegistrationRepository clientRegistrationRepository,
            LocaleResolver localeResolver,
            SessionConfigurationProperties sessionConfigurationProperties) throws Exception {
        // @formatter:off
        http
          .requestCache(cacheConfig -> cacheConfig.requestCache(httpSessionRequestCache()))
          .authorizeHttpRequests(httpRequestsConfig -> httpRequestsConfig.anyRequest().permitAll())
          .headers(headersConfig -> headersConfig
            .xssProtection(HeadersConfigurer.XXssConfig::disable)
            .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
            .contentSecurityPolicy(cspConfig -> cspConfig.policyDirectives(CONTENT_SECURITY_POLICY))
            .httpStrictTransportSecurity(hstsConfig -> hstsConfig.maxAgeInSeconds(HST_MAX_AGE.toSeconds()))
          )
          .oauth2Login(oAuth2LoginConfig -> oAuth2LoginConfig
            .authorizationEndpoint(authorizationEndpointConfig ->
              authorizationEndpointConfig.authorizationRequestResolver(new TaraAuthorizationRequestResolver(clientRegistrationRepository, localeResolver))
            )
            .tokenEndpoint(tokenEndpointConfig ->
              tokenEndpointConfig.accessTokenResponseClient(createAccessTokenResponseClient(taraRestTemplate))
            )
            .defaultSuccessUrl("/")
            .failureHandler(authenticationFailureHandler())
          )
          .exceptionHandling(exceptionHandlingConfig ->
            /* This disables default login and logout views, see
             * org.springframework.security.config.annotation.web.configurers.DefaultLoginPageConfigurer.
             * org.springframework.security.web.authentication.Http403ForbiddenEntryPoint is also the default
             * implementation, so everything else keeps working the same way.
             */
            exceptionHandlingConfig.authenticationEntryPoint(new Http403ForbiddenEntryPoint())
          )
          .logout(loggingConfig -> loggingConfig.logoutUrl("/logout").logoutSuccessUrl("/"));
        // TODO Configure Spring in such a way that a session wouldn't be created when it's not necessary (see AUT-1048)
        // @formatter:on
        http.addFilterAfter(new SessionMaxAgeFilter(sessionConfigurationProperties), SecurityContextPersistenceFilter.class);
        return http.build();
    }

    private static DefaultAuthorizationCodeTokenResponseClient createAccessTokenResponseClient(
            RestOperations taraRestTemplate
    ) {
        DefaultAuthorizationCodeTokenResponseClient accessTokenResponseClient =
                new DefaultAuthorizationCodeTokenResponseClient();
        accessTokenResponseClient.setRestOperations(taraRestTemplate);
        return accessTokenResponseClient;
    }

    private HttpSessionRequestCache httpSessionRequestCache() {
        HttpSessionRequestCache httpSessionRequestCache = new HttpSessionRequestCache();
        // Disables session creation if session does not exist and any request returns 401 unauthorized error.
        httpSessionRequestCache.setCreateSessionAllowed(false);
        return httpSessionRequestCache;
    }

    public SimpleUrlAuthenticationFailureHandler authenticationFailureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {

            private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
                    throws IOException {
                log.error("Authentication failed", exception);
                if (exception instanceof OAuth2AuthenticationException ex) {
                    redirectStrategy.sendRedirect(request, response, "/?error=" + ex.getError().getErrorCode());
                } else {
                    redirectStrategy.sendRedirect(request, response, "/?error=authentication_failure");
                }
            }
        };
    }

}
