package ee.ria.govsso.enduserselfservice.session;

import lombok.Data;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.binary.BinaryObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.session.MapSession;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.cache.Cache;
import java.io.Serializable;
import java.time.Duration;

@Slf4j
@Component
public class IgniteSessionRepository implements SessionRepository<Session> {

    private final Cache<String, BinaryObject> sessionCache;
    private final Ignite ignite;
    private final Duration sessionTimeout;

    public IgniteSessionRepository(
            @Qualifier("sessionCache") Cache<String, BinaryObject> sessionCache,
            Ignite ignite,
            SessionConfigurationProperties sessionConfigurationProperties) {
        this.sessionCache = sessionCache;
        this.ignite = ignite;
        this.sessionTimeout = sessionConfigurationProperties.maxIdleTime();
    }

    @Override
    public IgniteSession createSession() {
        IgniteSession igniteSession = new IgniteSession();
        igniteSession.setMaxInactiveInterval(sessionTimeout);
        return igniteSession;
    }

    @Override
    public void save(Session session) {
        IgniteSession igniteSession = (IgniteSession) session;
        BinaryObject binaryObject = ignite.binary().toBinary(igniteSession);
        sessionCache.put(igniteSession.getId(), binaryObject);
    }

    @Override
    public IgniteSession findById(String id) {
        BinaryObject binaryObject = sessionCache.get(id);
        if (binaryObject != null) {
            IgniteSession session = binaryObject.deserialize();
            if (session.isExpired()) {
                log.debug("Found session {} which has expired, deleting session", session.getId());
                deleteById(id);
                return null;
            } else {
                return session;
            }
        } else {
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        sessionCache.remove(id);
        log.info("Session is removed from cache: {}", id);
    }

    @Data
    static final class IgniteSession implements Session, Serializable {
        private static final long serialVersionUID = 7160779239673823561L;

        @Delegate(excludes = DelegateExclusions.class)
        private final MapSession mapSession = new MapSession();

        public void setAttribute(String attributeName, Object attributeValue) {
            mapSession.setAttribute(attributeName, SerializationUtils.serialize(attributeValue));
        }

        public <T> T getAttribute(String attributeName) {
            byte[] result = mapSession.getAttribute(attributeName);
            if (result == null) {
                return null;
            }
            //noinspection unchecked
            return (T) SerializationUtils.deserialize(result);
        }

        interface DelegateExclusions {
            void setAttribute(String attributeName, Object attributeValue);
            <T> T getAttribute(String attributeName);
        }
    }
}
