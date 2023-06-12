package ee.ria.govsso.enduserselfservice.actuator.health;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ApplicationHealthEndpointTest extends HealthEndpointTest {

    @Test
    void health_WhenAllComponentsUp_RespondsWith200() {
        mockGovssoSessionHealthLivenessUp();

        ValidatableResponse response = given()
                .when()
                .get("/actuator/health")
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("UP"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.livenessState.status", equalTo("UP"))
                .body("components.ping.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("UP"))
                .body("groups", equalTo(List.of("liveness", "readiness")));

        assertCertificatesHealthUp(response, "components.certificates.");
    }

    // "/actuator/health/readiness" endpoint outcome depends on other services (including GovSSO Session status),
    // but readiness in general health endpoint does not.
    @Test
    void health_WhenAllComponentsButGovssoSessionUp_RespondsWith503ButReadinessUp() {
        ValidatableResponse response = given()
                .when()
                .get("/actuator/health")
                .then()
                .assertThat()
                .statusCode(503)
                .body("status", equalTo("DOWN"))
                .body("components.keySet()", equalTo(
                        Set.of("certificates", "diskSpace", "govssoSession", "ignite", "livenessState", "ping",
                                "readinessState", "tara")))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("DOWN"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.livenessState.status", equalTo("UP"))
                .body("components.ping.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("UP"))
                .body("groups", equalTo(List.of("liveness", "readiness")));

        assertCertificatesHealthUp(response, "components.certificates.");
    }

    @Test
    void health_WhenAllComponentsButTaraUp_RespondsWith503ButReadinessUp() {
        mockGovssoSessionHealthLivenessUp();
        TARA_MOCK_SERVER.resetAll();

        ValidatableResponse response = given()
                .when()
                .get("/actuator/health")
                .then()
                .assertThat()
                .statusCode(503)
                .body("status", equalTo("DOWN"))
                .body("components.keySet()", equalTo(
                        Set.of("certificates", "diskSpace", "govssoSession", "ignite", "livenessState", "ping",
                                "readinessState", "tara")))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("UP"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.livenessState.status", equalTo("UP"))
                .body("components.ping.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("DOWN"))
                .body("groups", equalTo(List.of("liveness", "readiness")));

        assertCertificatesHealthUp(response, "components.certificates.");
    }
}
