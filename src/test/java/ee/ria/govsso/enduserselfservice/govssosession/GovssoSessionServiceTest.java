package ee.ria.govsso.enduserselfservice.govssosession;

import ee.ria.govsso.enduserselfservice.configuration.TimeConfigurationProperties;
import ee.ria.govsso.enduserselfservice.logging.ClientRequestLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersUriSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GovssoSessionServiceTest {

    private ClientRequestLogger requestLogger;

    private GovssoSessionService service;

    private ResponseSpec responseSpec;
    private WebClient webClient;

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void setUp() {
        webClient = mock(WebClient.class);
        RequestHeadersUriSpec requestHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersUriSpec deleteHeadersUriSpec = mock(RequestHeadersUriSpec.class);
        RequestHeadersSpec requestHeadersSpec = mock(RequestHeadersSpec.class);
        responseSpec = mock(ResponseSpec.class);

        requestLogger = mock(ClientRequestLogger.class);
        TimeConfigurationProperties timeConfigurationProperties = mock(TimeConfigurationProperties.class);

        when(timeConfigurationProperties.localZone())
                .thenReturn(ZoneId.of("Europe/Tallinn"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any(MediaType[].class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(webClient.delete()).thenReturn(deleteHeadersUriSpec);
        when(deleteHeadersUriSpec.uri(anyString(), any(Function.class)))
                .thenReturn(requestHeadersSpec);

        service = new GovssoSessionService(
                webClient,
                requestLogger,
                timeConfigurationProperties
        );
    }

    @Test
    void getSubjectSessions_WhenBackendReturnsSession_PreservesDeviceInfoAndConvertsTimeZone() {
        String subject = "subject-1";
        OffsetDateTime utcTime = OffsetDateTime.parse("2024-01-01T10:00:00Z");

        GovssoSession backendSession = GovssoSession.builder()
                .sessionId("session-1")
                .authenticatedAt(utcTime)
                .ipAddresses(List.of(
                        GovssoSessionIpInfo.builder()
                                .ipAddress("127.0.0.1")
                                .country("EE")
                                .build()
                ))
                .userAgent("ua")
                .os("Windows")
                .browser("Chrome")
                .services(List.of(
                        GovssoSession.Service.builder()
                                .clientNames(Map.of("en", "Test service"))
                                .authenticatedAt(utcTime)
                                .expiresAt(utcTime.plusMinutes(10))
                                .lastUpdatedAt(utcTime.plusMinutes(5))
                                .build()
                ))
                .build();

        when(responseSpec.bodyToFlux(GovssoSession.class)).thenReturn(Flux.just(backendSession));

        List<GovssoSession> result = service.getSubjectSessions(subject);

        assertThat(result, hasSize(1));

        GovssoSession session = result.get(0);
        assertThat(session.ipAddresses().get(0).country(), equalTo("EE"));
        assertThat(session.os(), equalTo("Windows"));
        assertThat(session.browser(), equalTo("Chrome"));
        assertThat(
                session.authenticatedAt().getOffset(),
                equalTo(ZoneId.of("Europe/Tallinn")
                        .getRules()
                        .getOffset(utcTime.toInstant()))
        );

        GovssoSession.Service sessionService = session.services().get(0);
        assertThat(
                sessionService.authenticatedAt().getOffset(),
                equalTo(ZoneId.of("Europe/Tallinn")
                        .getRules()
                        .getOffset(utcTime.toInstant()))
        );

        verify(requestLogger).logResponse(eq(HttpStatus.OK.value()), any());
    }

    @Test
    void endSession_WhenCalled_DeletesSessionAndLogsResponse() {
        String subject = "subject-1";
        String sessionId = "session-1";
        when(responseSpec.bodyToMono(Void.class)).thenReturn(reactor.core.publisher.Mono.empty());
        service.endSession(subject, sessionId);
        verify(webClient).delete();
        verify(requestLogger).logResponse(HttpStatus.OK.value());
    }

    @Test
    void endSubjectSessions_WhenCalled_DeletesSessionsAndLogsResponse() {
        String subject = "subject-1";
        when(responseSpec.bodyToMono(Void.class)).thenReturn(reactor.core.publisher.Mono.empty());
        service.endSubjectSessions(subject);
        verify(webClient).delete();
        verify(requestLogger).logResponse(HttpStatus.OK.value());
    }
}
