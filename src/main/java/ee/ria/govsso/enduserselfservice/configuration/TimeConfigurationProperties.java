package ee.ria.govsso.enduserselfservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;

@Validated
@ConfigurationProperties("govsso-enduserselfservice.time")
public record TimeConfigurationProperties(
        ZoneId localZone
) {
    public TimeConfigurationProperties {
        if (localZone == null) {
            localZone = ZoneId.systemDefault();
        }
    }
}
