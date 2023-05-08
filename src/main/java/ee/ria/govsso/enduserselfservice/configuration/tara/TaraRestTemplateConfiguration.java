package ee.ria.govsso.enduserselfservice.configuration.tara;

import lombok.SneakyThrows;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.http.converter.OAuth2ErrorHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class TaraRestTemplateConfiguration {

    @Bean
    RestTemplate taraRestTemplate(TaraConfigurationProperties properties) {
        SSLContext sslContext = createSslContext(properties);
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        @SuppressWarnings("resource")
        HttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(socketFactory)
                .build();
        RestTemplate restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .build();
        List<HttpMessageConverter<?>> additionalMessageConverters = List.of(
                new FormHttpMessageConverter(),
                new OAuth2ErrorHttpMessageConverter(),
                new OAuth2AccessTokenResponseHttpMessageConverter()
        );
        addMessageConverters(restTemplate, additionalMessageConverters);
        return restTemplate;
    }

    @SneakyThrows
    private static SSLContext createSslContext(TaraConfigurationProperties properties) {
        return new SSLContextBuilder()
                .loadTrustMaterial(
                        properties.trustStore().getURL(),
                        properties.trustStorePassword().toCharArray())
                .build();
    }

    private static void addMessageConverters(
            RestTemplate restTemplate, List<HttpMessageConverter<?>> additionalMessageConverters) {
        List<HttpMessageConverter<?>> httpMessageConverters = Stream.concat(
                additionalMessageConverters.stream(),
                restTemplate.getMessageConverters().stream()
        ).toList();
        restTemplate.setMessageConverters(httpMessageConverters);
    }

}
