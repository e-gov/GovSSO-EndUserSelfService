package ee.ria.govsso.enduserselfservice.configuration;

import ee.ria.govsso.enduserselfservice.configuration.tara.TaraAuthorizationRequestResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.web.client.RestOperations;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    public static final Duration HST_MAX_AGE = Duration.ofDays(186);

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            RestOperations taraRestTemplate,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        // @formatter:off
        http
                .requestCache()
                    .requestCache(httpSessionRequestCache())
                    .and()
                .authorizeHttpRequests()
                    .anyRequest()
                        .permitAll()
                    .and()
                .headers()
                    .xssProtection()
                        .disable()
                    .frameOptions()
                        .deny()
                    .contentSecurityPolicy(CONTENT_SECURITY_POLICY)
                        .and()
                    .httpStrictTransportSecurity()
                        .maxAgeInSeconds(HST_MAX_AGE.toSeconds())
                        .and()
                    .and()
                .oauth2Login()
                    .authorizationEndpoint()
                        .authorizationRequestResolver(
                            new TaraAuthorizationRequestResolver(clientRegistrationRepository))
                        .and()
                    .tokenEndpoint()
                        .accessTokenResponseClient(createAccessTokenResponseClient(taraRestTemplate))
                        .and()
                    .defaultSuccessUrl("/")
                    .failureHandler(authenticationFailureHandler())
                    .and()
                .logout()
                    .logoutUrl("/logout")
                    // TODO Disable GET /logout page (LogoutPageGeneratingWebFilter?)
                    .logoutSuccessUrl("/");
        // TODO Configure Spring in such a way that a session wouldn't be created when it's not necessary (see AUT-1048)
        // @formatter:on
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
