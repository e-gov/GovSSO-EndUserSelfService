package ee.ria.govsso.enduserselfservice.session;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.time.Duration;

@Validated
@ConstructorBinding
@ConfigurationProperties("govsso-enduserselfservice.session")
public record SessionConfigurationProperties(
        @NotNull Duration maxIdleTime,
        @NotNull Duration maxAge
) {}
