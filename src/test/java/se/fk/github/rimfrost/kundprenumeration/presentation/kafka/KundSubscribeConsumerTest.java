package se.fk.github.rimfrost.kundprenumeration.presentation.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;
import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.kundprenumeration.application.PrenumerationStore;
import se.fk.rimfrost.kund.prenumeration.Idtyp;
import se.fk.rimfrost.kund.prenumeration.KundSubscribeRequest;

/** Integration tests for {@link KundSubscribeConsumer}. */
@QuarkusTest
class KundSubscribeConsumerTest
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
   @DisplayName("KUNDPREN-FR-01.1: subscribe message stores prenumeration in store")
   void subscribe_stores_prenumeration()
   {
      var request = subscribeRequest("id-1", "TYP", "123", "reply-topic");

      connector.source("kund-subscribe").send(request);

      Awaitility.await().atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThat(prenumerationStore.findByTypIdAndVarde("TYP", "123")).hasSize(1));
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.5: duplicate prenumerationId overwrites existing subscription")
   void subscribe_upserts_on_duplicate_prenumeration_id()
   {
      connector.source("kund-subscribe").send(subscribeRequest("id-1", "TYP", "123", "reply-old"));
      connector.source("kund-subscribe").send(subscribeRequest("id-1", "TYP", "123", "reply-new"));

      Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
         var result = prenumerationStore.findByTypIdAndVarde("TYP", "123");
         assertThat(result).hasSize(1);
         assertThat(result.get(0).replyTo()).isEqualTo("reply-new");
      });
   }

   private KundSubscribeRequest subscribeRequest(String prenumerationId, String typId, String varde, String replyTo)
   {
      Idtyp kund = new Idtyp();
      kund.setTypId(typId);
      kund.setVarde(varde);
      KundSubscribeRequest request = new KundSubscribeRequest();
      request.setPrenumerationId(prenumerationId);
      request.setKund(kund);
      request.setReplyTo(replyTo);
      return request;
   }
}
