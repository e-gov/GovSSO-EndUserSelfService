package ee.ria.govsso.enduserselfservice.dashboard;

import ee.ria.govsso.enduserselfservice.BaseTest;
import ee.ria.govsso.enduserselfservice.govssosession.GovssoSession;
import ee.ria.govsso.enduserselfservice.govssosession.GovssoSessionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
class DashboardCloudflareRenderingTest extends BaseTest {

    public static final String SUBJECT_SESSION = "test1234";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GovssoSessionService govssoSessionService;

    @Test
    void index_whenAuthenticatedAndCountryOsBrowserExist_rendersDashboardFields() throws Exception {
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithCountry("EE")));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("active-sessions__session-location")))
                .andExpect(content().string(containsString("active-sessions__session-flag")))
                .andExpect(content().string(containsString("data-country-code=\"EE\"")))
                .andExpect(content().string(containsString("data-os=\"Windows\"")))
                .andExpect(content().string(containsString(">Windows<")))
                .andExpect(content().string(containsString("data-browser=\"Chrome\"")))
                .andExpect(content().string(containsString("Chrome,")))
                .andExpect(content().string(containsString("192.168.1.5")))
                .andExpect(content().string(containsString("00000000-0000-0000-0000-111111111111")));
    }

    @Test
    void index_whenCountryIsEmpty_rendersLocationWithoutFlag() throws Exception {
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithCountry("")));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("active-sessions__session-location")))
                .andExpect(content().string(containsString("active-sessions__session-location-text")))
                .andExpect(content().string(not(containsString("data-country-code="))))
                .andExpect(content().string(not(containsString("active-sessions__session-flag"))));
    }

    @Test
    void index_whenCountryIsNull_doesNotRenderLocationBlock() throws Exception {
        when(govssoSessionService.getSubjectSessions(SUBJECT_SESSION))
                .thenReturn(List.of(sessionWithCountry(null)));

        mockMvc.perform(get("/").with(oidcUser()))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("active-sessions__session-location"))))
                .andExpect(content().string(not(containsString("active-sessions__session-flag"))))
                .andExpect(content().string(not(containsString("data-country-code="))));
    }

    private GovssoSession sessionWithCountry(String country) {
        return new GovssoSession(
                "00000000-0000-0000-0000-111111111111",
                OffsetDateTime.parse("2023-01-01T10:00:00Z"),
                List.of(new GovssoSession.IpInfo("192.168.1.5", country)),
                "Mozilla/5.0",
                "Windows",
                "Chrome",
                List.of()
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