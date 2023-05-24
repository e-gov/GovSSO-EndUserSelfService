package ee.ria.govsso.enduserselfservice.session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
public class SessionMaxAgeFilter extends OncePerRequestFilter {

    private final SessionConfigurationProperties sessionConfigurationProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (isSessionMaxAgeExceeded()) {
            log.info("Authentication reached max session age, clearing security context");
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private boolean isSessionMaxAgeExceeded() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext == null) {
            return false;
        }
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return false;
        }
        OidcUser authenticationPrincipal = (OidcUser) authentication.getPrincipal();
        if (authenticationPrincipal == null) {
            return false;
        }
        Instant expirationTime = authenticationPrincipal.getIssuedAt().plus(sessionConfigurationProperties.maxAge());
        return expirationTime.isBefore(Instant.now());
    }

}
