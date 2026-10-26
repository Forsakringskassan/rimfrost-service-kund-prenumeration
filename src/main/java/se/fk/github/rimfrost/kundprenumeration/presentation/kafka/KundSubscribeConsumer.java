package se.fk.github.rimfrost.kundprenumeration.presentation.kafka;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.kundprenumeration.application.Prenumeration;
import se.fk.github.rimfrost.kundprenumeration.application.PrenumerationStore;
import se.fk.rimfrost.kund.prenumeration.KundSubscribeRequest;

/**
 * Consumes {@code kund.subscribe} messages and registers the subscription in {@link PrenumerationStore}.
 * Idempotent: an existing prenumerationId is overwritten (FR-01.5).
 */
@ApplicationScoped
public class KundSubscribeConsumer
{

   private static final Logger log = LoggerFactory.getLogger(KundSubscribeConsumer.class);

   @Inject
   PrenumerationStore prenumerationStore;

   /**
    * Handles an incoming subscribe request.
    *
    * @param request the subscribe payload
    */
   @Incoming("kund-subscribe")
   @Blocking
   public void onSubscribe(KundSubscribeRequest request)
   {
      log.info("Subscribe: prenumerationId={} idtyp={} idvarde={}", request.getPrenumerationId(), request.getKund().getTypId(),
            request.getKund().getVarde());
      prenumerationStore.upsert(
            new Prenumeration(
                  request.getPrenumerationId(),
                  request.getKund().getTypId(),
                  request.getKund().getVarde(),
                  request.getReplyTo()));
   }
}
