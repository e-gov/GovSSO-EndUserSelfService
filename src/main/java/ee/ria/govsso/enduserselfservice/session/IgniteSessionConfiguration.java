package ee.ria.govsso.enduserselfservice.session;

import org.apache.ignite.Ignite;
import org.apache.ignite.binary.BinaryObject;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.Session;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;

import javax.cache.Cache;
import javax.cache.expiry.Duration;
import javax.cache.expiry.TouchedExpiryPolicy;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.ignite.cache.CacheAtomicityMode.ATOMIC;
import static org.apache.ignite.cache.CacheMode.PARTITIONED;

@Configuration
@EnableSpringHttpSession
public class IgniteSessionConfiguration {

    public static final String SESSION_CACHE_NAME = "spring:session:sessions";

    // Enable one backup for each cache partition in order to ensure availability
    //  in case the node containing the primary partition should leave the cluster.
    private static final int NUMBER_OF_BACKUP_PARTITIONS = 1;

    // The default write synchronization mode for partitioned clusters is PRIMARY_SYNC,
    //  which means that on cache updates the client waits for the cache-write to finish
    //  in the primary partition, backup partitions are updated asynchronously.
    // By default, reading from backup partitions is enabled, which may lead to reading
    //  stale data if the backup partition is not updated yet - disable reading from
    //  backup partitions to avoid this.
    private static final boolean READ_FROM_BACKUP_PARTITIONS = false;

    @Bean
    public Cache<String, BinaryObject> sessionCache(
            Ignite igniteInstance, SessionConfigurationProperties sessionConfigurationProperties) {
        Duration maxIdleTime = new Duration(SECONDS, sessionConfigurationProperties.maxIdleTime().toSeconds());
        return igniteInstance.getOrCreateCache(new CacheConfiguration<String, Session>()
                        .setName(SESSION_CACHE_NAME)
                        .setCacheMode(PARTITIONED)
                        .setAtomicityMode(ATOMIC)
                        .setBackups(NUMBER_OF_BACKUP_PARTITIONS)
                        .setExpiryPolicyFactory(TouchedExpiryPolicy.factoryOf(maxIdleTime))
                        .setReadFromBackup(READ_FROM_BACKUP_PARTITIONS))
                .withKeepBinary();
    }

}
