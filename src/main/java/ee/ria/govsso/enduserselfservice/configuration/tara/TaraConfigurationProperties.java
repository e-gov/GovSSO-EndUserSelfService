package ee.ria.govsso.enduserselfservice.configuration.tara;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@ConstructorBinding
@ConfigurationProperties("tara")
public record TaraConfigurationProperties(
        @NotNull String clientId,
        @NotNull String clientSecret,
        @NotNull String redirectUri,
        @NotNull String issuerUri,
        @NotNull Resource trustStore,
        @NotNull String trustStorePassword
) {}
