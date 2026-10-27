package se.fk.github.rimfrost.kundprenumeration.presentation.kafka;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.kundprenumeration.application.Prenumeration;
import se.fk.github.rimfrost.kundprenumeration.application.PrenumerationStore;
import se.fk.github.rimfrost.kundprenumeration.integration.kafka.KundChangedEvent;
import se.fk.rimfrost.kund.prenumeration.KundUnsubscribeRequest;

/** Integration tests for {@link KundUnsubscribeConsumer}. */
@QuarkusTest
class KundUnsubscribeConsumerTest
{

   @Inject
   @Any
   InMemoryConnector connector;

   @Inject
   PrenumerationStore prenumerationStore;

   @AfterEach
   void tearDown()
   {
      InMemoryConnector.clear();
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.2: unsubscribe message removes prenumeration from store")
   void unsubscribe_removes_prenumeration()
   {
      prenumerationStore.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic"));

      connector.source("kund-unsubscribe").send(unsubscribeRequest("id-1"));

      Awaitility.await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(prenumerationStore.findByTypIdAndVarde("TYP", "123")).isEmpty());
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.6: unsubscribe for unknown ID is silently ignored")
   void unsubscribe_ignores_unknown_id()
   {
      connector.source("kund-unsubscribe").send(unsubscribeRequest("unknown-id"));
      // no exception, store remains consistent
      Awaitility.await().atMost(Duration.ofSeconds(2))
            .untilAsserted(() -> assertThat(prenumerationStore.findByTypIdAndVarde("TYP", "123")).isEmpty());
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.2 + FR-02.1: no event is published after prenumeration is removed")
   void unsubscribe_prevents_event_delivery()
   {
      prenumerationStore.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic"));
      InMemorySink<KundChangedEvent> sink = connector.sink("kund-changed-out");

      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\",\"idvarde\":\"123\"}")
            .when()
            .post("/test/kundcentrerat-data-changed")
            .then()
            .statusCode(204);

      Awaitility.await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(sink.received()).hasSize(1));

      connector.source("kund-unsubscribe").send(unsubscribeRequest("id-1"));

      Awaitility.await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(prenumerationStore.findByTypIdAndVarde("TYP", "123")).isEmpty());

      given()
            .contentType(ContentType.JSON)
            .body("{\"idtyp\":\"TYP\",\"idvarde\":\"123\"}")
            .when()
            .post("/test/kundcentrerat-data-changed")
            .then()
            .statusCode(204);

      assertThat(sink.received()).hasSize(1);
   }

   private KundUnsubscribeRequest unsubscribeRequest(String prenumerationId)
   {
      KundUnsubscribeRequest request = new KundUnsubscribeRequest();
      request.setPrenumerationId(prenumerationId);
      return request;
   }
}
