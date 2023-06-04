package ee.ria.govsso.enduserselfservice.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ee.ria.govsso.enduserselfservice.BaseTestLoggingAssertion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger.Service.SESSION;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Slf4j
class ClientRequestLoggerTest extends BaseTestLoggingAssertion {

    private final ClientRequestLogger clientRequestLogger = new ClientRequestLogger(SESSION, ClientRequestLogger.class);

    @Test
    void logRequest_WhenNoRequestBody() {
        clientRequestLogger.logRequest("https://session.localhost:15442", HttpMethod.GET);
        List<ILoggingEvent> loggedEvents = assertInfoIsLogged(ClientRequestLogger.class, "SESSION request");
        assertThat(loggedEvents, hasSize(1));
        ILoggingEvent logEvent = loggedEvents.get(0);
        assertThat(logEvent.getMarker().toString(), equalTo(
                "http.request.method=GET, url.full=https://session.localhost:15442"));
    }

    @Test
    void logRequest_WhenRequestBodyPresent() {
        clientRequestLogger.logRequest("https://session.localhost:15442", HttpMethod.GET, "RequestBody");
        List<ILoggingEvent> loggedEvents = assertInfoIsLogged(ClientRequestLogger.class, "SESSION request");
        assertThat(loggedEvents, hasSize(1));
        ILoggingEvent logEvent = loggedEvents.get(0);
        assertThat(logEvent.getMarker().toString(), equalTo(
                "http.request.method=GET, url.full=https://session.localhost:15442, http.request.body.content=\"RequestBody\""));
    }

    @Test
    void logResponse_WhenNoResponseBody() {
        clientRequestLogger.logResponse(HttpStatus.OK.value());
        List<ILoggingEvent> loggedEvents = assertInfoIsLogged(ClientRequestLogger.class, "SESSION response");
        assertThat(loggedEvents, hasSize(1));
        assertThat(loggedEvents.get(0).getMarker().toString(), equalTo("http.response.status_code=200"));
    }

    @Test
    void logResponse_WhenResponseBodyPresent() {
        OffsetDateTime dateTime = OffsetDateTime.of(2023, 6, 11, 12, 45, 0, 0, ZoneOffset.UTC);
        clientRequestLogger.logResponse(HttpStatus.OK.value(), dateTime);
        List<ILoggingEvent> loggedEvents = assertInfoIsLogged(ClientRequestLogger.class, "SESSION response");
        assertThat(loggedEvents, hasSize(1));
        ILoggingEvent logEvent = loggedEvents.get(0);
        assertThat(logEvent.getMarker().toString(),
                equalTo("http.response.status_code=200, http.response.body.content=\"2023-06-11T12:45:00Z\""));
    }
}
