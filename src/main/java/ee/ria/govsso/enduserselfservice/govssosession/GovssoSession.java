package ee.ria.govsso.enduserselfservice.govssosession;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Jacksonized
@Builder
public record GovssoSession(
         String sessionId,
         OffsetDateTime authenticatedAt,
         List<String> ipAddresses,
         String userAgent,
         List<Service> services
) {

    @Jacksonized
    @Builder
    public record Service(
             Map<String, String> clientNames,
             OffsetDateTime authenticatedAt,
             OffsetDateTime expiresAt,
             OffsetDateTime lastUpdatedAt
    ) {}

}
