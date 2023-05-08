package ee.ria.govsso.enduserselfservice.govssosession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManager;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.HttpComponentsClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.SSLContext;

import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Configuration
public class GovssoSessionRestConfiguration {

    @Bean
    public WebClient sessionRestClient(GovssoSessionConfigurationProperties properties, ObjectMapper objectMapper) {
        SSLContext sslContext = createSslContext(properties.tls());
        TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                .setSslContext(sslContext)
                .build();
        PoolingAsyncClientConnectionManager connectionManager = PoolingAsyncClientConnectionManagerBuilder.create()
                .setTlsStrategy(tlsStrategy)
                .build();
        CloseableHttpAsyncClient httpClient = HttpAsyncClients.custom()
                .setConnectionManager(connectionManager)
                .build();

        return WebClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .clientConnector(new HttpComponentsClientHttpConnector(httpClient))
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

    @SneakyThrows
    private SSLContext createSslContext(GovssoSessionConfigurationProperties.Tls tlsProperties) {
        return new SSLContextBuilder()
                .loadTrustMaterial(
                        tlsProperties.trustStore().getURL(),
                        tlsProperties.trustStorePassword().toCharArray())
                .build();
    }

}
