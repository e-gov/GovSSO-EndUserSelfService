package ee.ria.govsso.enduserselfservice.govssosession;

import lombok.Builder;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
public record GovssoSessionIpInfo(
        String ipAddress,
        String country
) {
}
