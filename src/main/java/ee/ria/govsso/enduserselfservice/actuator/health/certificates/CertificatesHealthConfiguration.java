package ee.ria.govsso.enduserselfservice.actuator.health.certificates;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.boot.actuate.health.CompositeHealthContributor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class CertificatesHealthConfiguration {

    @Bean
    @SneakyThrows
    public KeyStore igniteKeyStore(IgniteConfiguration igniteConfiguration) {
        SslContextFactory sslContextFactory = (SslContextFactory) igniteConfiguration.getSslContextFactory();
        KeyStore trustStore = KeyStore.getInstance(sslContextFactory.getKeyStoreType());
        try (InputStream trustStoreFile = new FileInputStream(sslContextFactory.getKeyStoreFilePath())) {
            trustStore.load(trustStoreFile, sslContextFactory.getKeyStorePassword());
        }
        return trustStore;
    }

    @Bean
    @SneakyThrows
    public KeyStore igniteTrustStore(IgniteConfiguration igniteConfiguration) {
        SslContextFactory sslContextFactory = (SslContextFactory) igniteConfiguration.getSslContextFactory();
        KeyStore trustStore = KeyStore.getInstance(sslContextFactory.getTrustStoreType());
        try (InputStream trustStoreFile = new FileInputStream(sslContextFactory.getTrustStoreFilePath())) {
            trustStore.load(trustStoreFile, sslContextFactory.getTrustStorePassword());
        }
        return trustStore;
    }

    @Bean
    public CompositeHealthContributor certificatesHealthContributor(KeyStore govssoSessionTrustStore,
                                                                   KeyStore igniteKeyStore,
                                                                   KeyStore igniteTrustStore,
                                                                   KeyStore taraTrustStore) {
        return CompositeHealthContributor.fromMap(Map.<String, HealthIndicator>of(
                "govssoSessionTrustStore", certificatesHealthIndicator(govssoSessionTrustStore),
                "igniteKeyStore", certificatesHealthIndicator(igniteKeyStore),
                "igniteTrustStore", certificatesHealthIndicator(igniteTrustStore),
                "taraTrustStore", certificatesHealthIndicator(taraTrustStore)
        ));
    }

    private static CertificatesHealthIndicator certificatesHealthIndicator(KeyStore trustStore) {
        return new CertificatesHealthIndicator(
                new CertificateInfoCache(CertificateInfoLoader.loadCertificateInfos(trustStore))
        );
    }
}
