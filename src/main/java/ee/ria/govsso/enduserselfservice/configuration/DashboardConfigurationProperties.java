package ee.ria.govsso.enduserselfservice.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("govsso-enduserselfservice.ui")
public record DashboardConfigurationProperties(
        boolean showCountry
) {
}
