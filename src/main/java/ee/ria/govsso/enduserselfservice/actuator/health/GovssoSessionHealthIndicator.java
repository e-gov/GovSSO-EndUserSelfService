package ee.ria.govsso.enduserselfservice.actuator.health;

import ee.ria.govsso.enduserselfservice.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class GovssoSessionHealthIndicator implements HealthIndicator {

    // Load balancer itself performs readiness checks for GovSSO Session service. We perform requests to load balancer,
    // therefore this check should be to see if GovSSO Session load balancer has determined at least one node to be
    // ready. It should be a request to GovSSO Session endpoint that performs the least work and returns the smallest
    // response (e.g. a static resource or version information or liveness check).
    private static final String HEALTH_CHECK_PATH = "/actuator/health/liveness";

    private final WebClient webclient;

    @Override
    public Health health() {
        HttpStatusCode httpStatus = checkStatus();
        return httpStatus != null && httpStatus.is2xxSuccessful()
                ? Health.up().build()
                : Health.down().build();
    }

    @SneakyThrows
    private HttpStatusCode checkStatus() {
        try {
            return webclient
                    .get()
                    .uri(HEALTH_CHECK_PATH)
                    .exchangeToMono(response -> Mono.just(response.statusCode()))
                    .block();
        } catch (WebClientResponseException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to check Session service status: {}", ExceptionUtil.getCauseMessages(e), e);
            }
            return e.getStatusCode();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to check Session service status: {}", ExceptionUtil.getCauseMessages(e), e);
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
