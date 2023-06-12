package ee.ria.govsso.enduserselfservice.actuator.health;

import io.restassured.response.ValidatableResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

class ReadinessHealthEndpointTest extends HealthEndpointTest {

    @Test
    void healthReadiness_WhenAllIncludedComponentsUp_RespondsWith200() {
        mockGovssoSessionHealthLivenessUp();

        ValidatableResponse response = given()
                .when()
                .get("/actuator/health/readiness")
                .then()
                .assertThat()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("components.keySet()", equalTo(
                        Set.of("certificates", "diskSpace", "govssoSession", "ignite", "readinessState", "tara")))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("UP"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("UP"));

        assertCertificatesHealthUp(response, "components.certificates.");
    }

    @Test
    void healthReadiness_WhenAllIncludedComponentsButGovssoSessionUp_RespondsWith503AndGovssoSessionDown() {
        ValidatableResponse response = given()
                .when()
                .get("/actuator/health/readiness")
                .then()
                .assertThat()
                .statusCode(503)
                .body("status", equalTo("DOWN"))
                .body("components.keySet()", equalTo(
                        Set.of("certificates", "diskSpace", "govssoSession", "ignite", "readinessState", "tara")))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("DOWN"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("UP"));

        assertCertificatesHealthUp(response, "components.certificates.");
    }

    @Test
    void healthReadiness_WhenAllIncludedComponentsButTaraUp_RespondsWith503AndTaraDown() {
        mockGovssoSessionHealthLivenessUp();
        TARA_MOCK_SERVER.resetAll();

        ValidatableResponse response = given()
                .when()
                .get("/actuator/health/readiness")
                .then()
                .assertThat()
                .statusCode(503)
                .body("status", equalTo("DOWN"))
                .body("components.keySet()", equalTo(
                        Set.of("certificates", "diskSpace", "govssoSession", "ignite", "readinessState", "tara")))
                .body("components.diskSpace.status", equalTo("UP"))
                .body("components.govssoSession.status", equalTo("UP"))
                .body("components.ignite.status", equalTo("UP"))
                .body("components.readinessState.status", equalTo("UP"))
                .body("components.tara.status", equalTo("DOWN"));

        assertCertificatesHealthUp(response, "components.certificates.");
    }
}
