package ee.ria.govsso.enduserselfservice.dashboard;

import ee.ria.govsso.enduserselfservice.BaseTest;
import ee.ria.govsso.enduserselfservice.govssosession.GovssoSession;
import ee.ria.govsso.enduserselfservice.govssosession.GovssoSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;

import static java.time.Instant.now;
import static java.util.Map.of;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class DashboardServiceActivityRenderingTest extends BaseTest {

    public static final String SUBJECT_SESSION = "test1234";
    private static final OffsetDateTime AUTHENTICATED_AT = OffsetDateTime.now().minusHours(1);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GovssoSessionService govssoSessionService;

    @Test
    void index_whenServiceHasNoExpiry_rendersServiceAsActive() throws Exception {
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithService(null, AUTHENTICATED_AT)));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aktiivne viimati")))
                .andExpect(content().string(not(containsString("mitteaktiivne alates"))))
                .andExpect(content().string(not(containsString("1970"))));
    }

    @Test
    void index_whenServiceExpiresInFuture_rendersServiceAsActive() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(10);
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithService(expiresAt, expiresAt.minusMinutes(15))));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("aktiivne viimati")))
                .andExpect(content().string(not(containsString("mitteaktiivne alates"))));
    }

    @Test
    void index_whenServiceHasExpired_rendersServiceAsInactive() throws Exception {
        OffsetDateTime expiresAt = OffsetDateTime.now().minusMinutes(10);
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithService(expiresAt, expiresAt.minusMinutes(15))));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mitteaktiivne alates")));
    }

    private GovssoSession sessionWithService(OffsetDateTime expiresAt, OffsetDateTime lastUpdatedAt) {
        return new GovssoSession(
                "00000000-0000-0000-0000-111111111111",
                AUTHENTICATED_AT,
                List.of(new GovssoSession.IpInfo("192.168.1.5", null)),
                "Mozilla/5.0",
                "Windows",
                "Chrome",
                List.of(new GovssoSession.Service(
                        of("et", "Teenusenimi", "en", "Service name", "ru", "Название услуги"),
                        AUTHENTICATED_AT,
                        expiresAt,
                        lastUpdatedAt))
        );
    }

    private SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor oidcUser() {
        return oidcLogin()
                .idToken(token -> token
                        .claim("sub", SUBJECT_SESSION)
                        .issuedAt(now())
                        .expiresAt(now().plusSeconds(600)))
                .userInfoToken(userInfo -> userInfo
                        .claim("profile_attributes", of(
                                "given_name", "Test",
                                "surname", "User"
                        )));
    }
}
