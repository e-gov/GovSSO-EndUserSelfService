package ee.ria.govsso.enduserselfservice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import ee.ria.govsso.enduserselfservice.configuration.tara.TaraOidcConfiguration;
import io.restassured.RestAssured;
import io.restassured.filter.log.ResponseLoggingFilter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Slf4j
@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseTest extends BaseTestLoggingAssertion {

    protected static final WireMockServer SESSION_MOCK_SERVER = new WireMockServer(WireMockConfiguration.wireMockConfig()
            .httpDisabled(true)
            .httpsPort(15442)
            .keystorePath("src/test/resources/session.localhost.keystore.p12")
            .keystorePassword("changeit")
            .keyManagerPassword("changeit")
            .notifier(new ConsoleNotifier(true))
    );

    protected static final WireMockServer TARA_MOCK_SERVER = new WireMockServer(WireMockConfiguration.wireMockConfig()
            .httpDisabled(true)
            .httpsPort(16442)
            .keystorePath("src/test/resources/tara.localhost.keystore.p12")
            .keystorePassword("changeit")
            .keyManagerPassword("changeit")
            .notifier(new ConsoleNotifier(true))
    );

    @LocalServerPort
    protected int port;

    @BeforeAll
    static void setUpAll() {
        configureRestAssured();
        SESSION_MOCK_SERVER.start();
        TARA_MOCK_SERVER.start();
        mockTaraOidcConfiguration();
    }

    private static void configureRestAssured() {
        RestAssured.filters(new ResponseLoggingFilter());
        RestAssured.config = RestAssured.config().redirect(redirectConfig().followRedirects(false));
    }

    @BeforeEach
    public void beforeEachTest() {
        RestAssured.port = port;
        SESSION_MOCK_SERVER.resetAll();
        TARA_MOCK_SERVER.resetAll();
        mockTaraOidcConfiguration();
    }

    static void mockTaraOidcConfiguration() {
        TARA_MOCK_SERVER.stubFor(get(urlPathEqualTo(TaraOidcConfiguration.OIDC_METADATA_PATH))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBodyFile("mock_responses/mock_tara_openid_configuration.json")));

    }
}
