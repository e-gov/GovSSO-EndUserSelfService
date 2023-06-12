package ee.ria.govsso.enduserselfservice.actuator.health;

import ee.ria.govsso.enduserselfservice.util.ExceptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.minidev.json.JSONObject;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaraHealthIndicator implements HealthIndicator {

    // Load balancer itself performs readiness checks for TARA service. We perform requests to load balancer, therefore
    // this check should be to see if TARA load balancer has determined at least one node to be ready. It should be a
    // request to TARA endpoint that performs the least work and returns the smallest response (e.g. a static resource
    // or version information or liveness check).
    private final Supplier<ResponseEntity<JSONObject>> taraMetadataRequestPerformer;

    @Override
    public Health health() {
        HttpStatus httpStatus = checkStatus();
        return httpStatus != null && httpStatus.is2xxSuccessful()
                ? Health.up().build()
                : Health.down().build();
    }

    @SneakyThrows
    private HttpStatus checkStatus() {
        try {
            return taraMetadataRequestPerformer.get().getStatusCode();
        } catch (HttpStatusCodeException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to check TARA status: {}", ExceptionUtil.getCauseMessages(e), e);
            }
            return e.getStatusCode();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to check TARA status: {}", ExceptionUtil.getCauseMessages(e), e);
            }
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
