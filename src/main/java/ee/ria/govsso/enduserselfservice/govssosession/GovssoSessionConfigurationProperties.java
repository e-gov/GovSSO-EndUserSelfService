package ee.ria.govsso.enduserselfservice.govssosession;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.core.io.Resource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.net.URL;

@Validated
@ConstructorBinding
@ConfigurationProperties("govsso-enduserselfservice.session")
public record GovssoSessionConfigurationProperties(
        @NotNull URL baseUrl,
        @NotNull Tls tls
) {

    public record Tls(
        @NotNull Resource trustStore,
        @NotBlank String trustStorePassword
    ) {}

}
