package ee.ria.govsso.enduserselfservice.configuration.ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.ssl.SslContextFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/* This is used instead of `org.apache.ignite:ignite-spring-boot-autoconfigure-ext` because we want to configure
 * Ignite a little differently from what is supported by that. We want to apply Java configuration before
 * configuration properties.
 */
@Configuration
public class CommonIgniteConfiguration {

    @Bean
    public Ignite ignite(IgniteConfiguration cfg) {
        return Ignition.getOrStart(cfg);
    }

    @Bean
    @ConfigurationProperties(prefix = "ignite")
    public IgniteConfiguration igniteConfiguration(Consumer<IgniteConfiguration> configurationPropertiesConfigurer) {
        IgniteConfiguration config = new IgniteConfiguration();
        TcpDiscoverySpi tcpDiscoverySpi = new TcpDiscoverySpi();
        tcpDiscoverySpi.setIpFinder(new TcpDiscoveryVmIpFinder());
        config.setDiscoverySpi(tcpDiscoverySpi);
        config.setSslContextFactory(new SslContextFactory());
        config.setGridLogger(new Slf4jLogger());
        configurationPropertiesConfigurer.accept(config);
        return config;
    }

    @Bean
    public Consumer<IgniteConfiguration> nodeConfigurer() {
        return cfg -> { /* No-op. */ };
    }
}
