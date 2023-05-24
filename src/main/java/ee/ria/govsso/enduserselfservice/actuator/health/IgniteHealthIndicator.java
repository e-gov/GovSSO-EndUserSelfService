package ee.ria.govsso.enduserselfservice.actuator.health;

import org.apache.ignite.Ignite;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

@Component
public class IgniteHealthIndicator extends AbstractHealthIndicator {

    private final Ignite ignite;

    public IgniteHealthIndicator(Ignite ignite) {
        super("Authentication service health check failed");
        this.ignite = ignite;
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {

        if (ignite.cluster().state().active()) {
            builder.up();
        } else {
            builder.down();
        }
    }
}
