package ee.ria.govsso.enduserselfservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("govsso-enduserselfservice.ui")
public record UiConfigurationProperties(
        boolean showCountry
) {
}
