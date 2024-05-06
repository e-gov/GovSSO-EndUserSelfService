package ee.ria.govsso.enduserselfservice.session;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties("govsso-enduserselfservice.session")
public record SessionConfigurationProperties(
        @NotNull Duration maxIdleTime,
        @NotNull Duration maxAge
) {}
