package se.fk.github.rimfrost.kundprenumeration.presentation.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import se.fk.github.rimfrost.kundprenumeration.application.KundChangedService;

/** Tests for {@link TestController}. */
@QuarkusTest
class TestControllerTest
{

   @InjectMock
   KundChangedService kundChangedService;

   @Test
   @DisplayName("KUNDPREN-FR-03.1: POST /test/kundcentrerat-data-changed returns 204 on valid request")
   void trigger_returns_204_on_valid_request()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\",\"idvarde\":\"123\"}")
            .when()
            .post("/test/kundcentrerat-data-changed")
            .then()
            .statusCode(204);

      Mockito.verify(kundChangedService).triggerKundChanged("TYP", "123");
   }

   @Test
   @DisplayName("KUNDPREN-FR-03.2: POST /test/kundcentrerat-data-changed returns 400 when idtyp is missing")
   void trigger_returns_400_when_idtyp_missing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idvarde\":\"123\"}")
            .when()
            .post("/test/kundcentrerat-data-changed")
            .then()
            .statusCode(400);
   }

   @Test
   @DisplayName("KUNDPREN-FR-03.2: POST /test/kundcentrerat-data-changed returns 400 when idvarde is missing")
   void trigger_returns_400_when_idvarde_missing()
   {
      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\"}")
            .when()
            .post("/test/kundcentrerat-data-changed")
            .then()
            .statusCode(400);
   }
}
