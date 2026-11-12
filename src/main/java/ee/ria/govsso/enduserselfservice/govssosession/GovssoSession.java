package ee.ria.govsso.enduserselfservice.govssosession;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Jacksonized
@Builder
// TODO: Use `Instant` instead of `OffsetDateTime` for date-time fields as the offset is not really relevant.
public record GovssoSession(
         String sessionId,
         OffsetDateTime authenticatedAt,
         List<IpInfo> ipInfos,
         String userAgent,
         String os,
         String browser,
         List<Service> services
) {

    @Jacksonized
    @Builder
    public record Service(
             Map<String, String> clientNames,
             OffsetDateTime authenticatedAt,
             OffsetDateTime expiresAt,
             OffsetDateTime lastUpdatedAt
    ) {

        /**
         * GovSSO Session returns 1970-01-01T00:00:00Z as expires_at for SECURED_APP (long-living) sessions, as their
         * consents have no expiry. Such services are displayed as active (AUT-2776), until the UX is clarified (AUT-2812).
         */
        public boolean isActive() {
            return Instant.EPOCH.equals(expiresAt.toInstant()) || expiresAt.isAfter(lastUpdatedAt);
        }
    }

    @Jacksonized
    @Builder
    public record IpInfo(
            String ipAddress,
            String country
    ) {}

}
