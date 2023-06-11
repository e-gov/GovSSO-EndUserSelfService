package ee.ria.govsso.enduserselfservice.govssosession;

import com.fasterxml.jackson.databind.ObjectMapper;
import ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger.Service.SESSION;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class GovssoSessionRestConfiguration {

    @Bean
    public ClientRequestLogger govssoSessionRequestLogger() {
        return new ClientRequestLogger(SESSION, GovssoSessionService.class);
    }

    @Bean
    public WebClient govssoSessionWebClient(KeyStore govssoSessionTrustStore,
                                            GovssoSessionConfigurationProperties properties,
                                            ObjectMapper objectMapper,
                                            ClientRequestLogger requestLogger) {
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
                .filter(responseFilter(requestLogger))
                .build();
    }

    private ObjectMapper customizeObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.copy()
                .setPropertyNamingStrategy(SNAKE_CASE);
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

    private ExchangeFilterFunction responseFilter(ClientRequestLogger requestLogger) {
        // TODO Catch connection errors.
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("")
                        .flatMap(responseBody -> {
                            try {
                                requestLogger.logResponse(clientResponse.statusCode().value(), responseBody);
                                return Mono.just(clientResponse);
                            } catch (Exception ex) {
                                return Mono.error(new IllegalStateException("Failed to log response", ex));
                            }
                        });
            } else {
                return Mono.just(clientResponse);
            }
        });
    }
}
