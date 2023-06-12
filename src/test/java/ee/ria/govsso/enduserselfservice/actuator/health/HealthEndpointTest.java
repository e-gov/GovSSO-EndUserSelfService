package ee.ria.govsso.enduserselfservice.actuator.health;

import ee.ria.govsso.enduserselfservice.BaseTest;
import io.restassured.response.ValidatableResponse;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

abstract class HealthEndpointTest extends BaseTest {

    void mockGovssoSessionHealthLivenessUp() {
        SESSION_MOCK_SERVER.stubFor(get(urlEqualTo("/actuator/health/liveness"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json; charset=UTF-8")
                        .withBody("{\"status\":\"UP\"}")));
    }

    void assertCertificatesHealthUp(ValidatableResponse response, String prefix) {
        response.body(prefix + "status", equalTo("UP"))
                .body(prefix + "components.keySet()", equalTo(
                        Set.of("govssoSessionTrustStore", "igniteKeyStore", "igniteTrustStore", "taraTrustStore")))
                .body(prefix + "components.govssoSessionTrustStore.status", equalTo("UP"))
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[0]", notNullValue())
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[0].alias", equalTo("govsso-ca.localhost"))
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[0].subjectDN", equalTo("CN=govsso-ca.localhost,O=govsso-local,L=Tallinn,C=EE"))
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[0].serialNumber", notNullValue())
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[0].state", equalTo("ACTIVE"))
                .body(prefix + "components.govssoSessionTrustStore.details.certificates[1].", nullValue())
                .body(prefix + "components.igniteKeyStore.status", equalTo("UP"))
                .body(prefix + "components.igniteKeyStore.details.certificates[0]", notNullValue())
                .body(prefix + "components.igniteKeyStore.details.certificates[0].alias", equalTo("enduserselfservice.localhost"))
                .body(prefix + "components.igniteKeyStore.details.certificates[0].subjectDN", equalTo("CN=enduserselfservice.localhost"))
                .body(prefix + "components.igniteKeyStore.details.certificates[0].serialNumber", notNullValue())
                .body(prefix + "components.igniteKeyStore.details.certificates[0].state", equalTo("ACTIVE"))
                .body(prefix + "components.igniteKeyStore.details.certificates[1].", nullValue())
                .body(prefix + "components.igniteTrustStore.status", equalTo("UP"))
                .body(prefix + "components.igniteTrustStore.details.certificates[0]", notNullValue())
                .body(prefix + "components.igniteTrustStore.details.certificates[0].alias", equalTo("govsso-ca.localhost"))
                .body(prefix + "components.igniteTrustStore.details.certificates[0].subjectDN", equalTo("CN=govsso-ca.localhost,O=govsso-local,L=Tallinn,C=EE"))
                .body(prefix + "components.igniteTrustStore.details.certificates[0].serialNumber", notNullValue())
                .body(prefix + "components.igniteTrustStore.details.certificates[0].state", equalTo("ACTIVE"))
                .body(prefix + "components.igniteTrustStore.details.certificates[1].", nullValue())
                .body(prefix + "components.taraTrustStore.status", equalTo("UP"))
                .body(prefix + "components.taraTrustStore.details.certificates[0]", notNullValue())
                .body(prefix + "components.taraTrustStore.details.certificates[0].alias", equalTo("tara-ca.localhost"))
                .body(prefix + "components.taraTrustStore.details.certificates[0].subjectDN", equalTo("CN=tara-ca.localhost,O=tara-local,L=Tallinn,C=EE"))
                .body(prefix + "components.taraTrustStore.details.certificates[0].serialNumber", notNullValue())
                .body(prefix + "components.taraTrustStore.details.certificates[0].state", equalTo("ACTIVE"))
                .body(prefix + "components.taraTrustStore.details.certificates[1].", nullValue());
    }
}
