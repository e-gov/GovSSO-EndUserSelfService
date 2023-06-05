package ee.ria.govsso.enduserselfservice.logging;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.LogstashMarker;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.authentication.event.LogoutSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import static net.logstash.logback.marker.Markers.append;

@Slf4j
@Component
public class AuthenticationEventLoggerListener implements ApplicationListener<AbstractAuthenticationEvent> {

    // TODO Set client.user.id and session.id in MDC for other log messages?
    // TODO Also log Ignite session expiration?

    @Override
    public void onApplicationEvent(AbstractAuthenticationEvent event) {
        String userId = event.getAuthentication().getName();
        String sessionId = ((WebAuthenticationDetails) event.getAuthentication().getDetails()).getSessionId();
        LogstashMarker marker = append("client.user.id", userId)
                .and(append("session.id", sessionId));
        if (event instanceof AuthenticationSuccessEvent) {
            log.info(marker, "Login successful");
        } else if (event instanceof LogoutSuccessEvent) {
            log.info(marker, "Logout successful");
        } else if (event instanceof AbstractAuthenticationFailureEvent failureEvent) {
            log.info(marker, "Authentication error", failureEvent.getException());
        }
    }
}
