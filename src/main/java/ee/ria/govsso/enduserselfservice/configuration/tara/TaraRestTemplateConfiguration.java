package ee.ria.govsso.enduserselfservice.configuration.tara;

import lombok.SneakyThrows;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.ssl.SSLContextBuilder;
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
import java.io.InputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.stream.Stream;

@Configuration
public class TaraRestTemplateConfiguration {

    @Bean
    @SneakyThrows
    public RestTemplate taraRestTemplate(KeyStore taraTrustStore) {
        SSLContext sslContext = new SSLContextBuilder()
                .loadTrustMaterial(taraTrustStore, null)
                .build();
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(sslContext);
        @SuppressWarnings("resource")
        HttpClient httpClient = HttpClients.custom()
          .setConnectionManager(PoolingHttpClientConnectionManagerBuilder.create()
            .setSSLSocketFactory(socketFactory)
            .build())
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

    @Bean
    @SneakyThrows
    public KeyStore taraTrustStore(TaraConfigurationProperties properties) {
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        char[] password = properties.trustStorePassword().toCharArray();
        try (InputStream trustStoreFile = properties.trustStore().getInputStream()) {
            trustStore.load(trustStoreFile, password);
        }
        return trustStore;
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
