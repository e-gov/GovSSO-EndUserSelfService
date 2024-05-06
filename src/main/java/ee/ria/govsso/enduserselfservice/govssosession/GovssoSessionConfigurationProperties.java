package ee.ria.govsso.enduserselfservice.govssosession;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import java.net.URL;

@Validated
@ConfigurationProperties("govsso-enduserselfservice.govsso-session")
public record GovssoSessionConfigurationProperties(
        @NotNull URL baseUrl,
        @NotNull Tls tls
) {

    @Validated
    @ConfigurationProperties(prefix = "govsso-enduserselfservice.govsso-session.tls")
    public record Tls(
        @NotNull Resource trustStore,
        @NotBlank String trustStorePassword,
        @DefaultValue("PKCS12") @NotNull String trustStoreType
    ) {
    }
}
