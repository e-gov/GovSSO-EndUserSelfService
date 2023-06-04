package ee.ria.govsso.enduserselfservice.govssosession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class GovssoSessionRestConfiguration {

    @Bean
    public WebClient govssoSessionWebClient(KeyStore govssoSessionTrustStore,
                                            GovssoSessionConfigurationProperties properties,
                                            ObjectMapper objectMapper) {
        SslContext sslContext = initSslContext(govssoSessionTrustStore);

        HttpClient httpClient = HttpClient.create()
                .secure(sslProviderBuilder -> sslProviderBuilder.sslContext(sslContext));
        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(customizeObjectMapper(objectMapper), APPLICATION_JSON));
                })
                .build();
    }

    private ObjectMapper customizeObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.copy()
                .setPropertyNamingStrategy(SNAKE_CASE)
                .registerModule(new JavaTimeModule());
    }

    @Bean
    @SneakyThrows
    public KeyStore govssoSessionTrustStore(GovssoSessionConfigurationProperties.Tls tlsProperties) {
        InputStream trustStoreFile = tlsProperties.trustStore().getInputStream();
        KeyStore trustStore = KeyStore.getInstance(tlsProperties.trustStoreType());
        trustStore.load(trustStoreFile, tlsProperties.trustStorePassword().toCharArray());
        return trustStore;
    }

    @SneakyThrows
    private SslContext initSslContext(KeyStore trustStore) {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        return SslContextBuilder.forClient().trustManager(trustManagerFactory).build();
    }
}
