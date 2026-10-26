package se.fk.github.rimfrost.kundprenumeration.integration.kafka;

import io.smallrye.reactive.messaging.kafka.api.OutgoingKafkaRecordMetadata;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Publishes {@link KundChangedEvent} to the replyTo-topic of each prenumerant.
 */
@ApplicationScoped
public class KundChangedEventPublisher
{

   private static final Logger log = LoggerFactory.getLogger(KundChangedEventPublisher.class);

   @Inject
   @Channel("kund-changed-out")
   @OnOverflow(value = OnOverflow.Strategy.FAIL)
   Emitter<KundChangedEvent> emitter;

   /**
    * Publishes an event to the given dynamic replyTo-topic.
    *
    * @param event   the event to publish
    * @param replyTo the Kafka topic to publish to
    */
   public void publish(KundChangedEvent event, String replyTo)
   {
      log.info("Publishing KundChangedEvent: prenumerationId={} replyTo={}", event.getPrenumerationId(), replyTo);
      var metadata = OutgoingKafkaRecordMetadata.builder().withTopic(replyTo).build();
      emitter.send(Message.of(event).addMetadata(metadata));
   }
}
