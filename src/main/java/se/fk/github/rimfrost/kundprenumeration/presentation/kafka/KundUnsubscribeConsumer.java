package se.fk.github.rimfrost.kundprenumeration.presentation.kafka;

import io.smallrye.reactive.messaging.annotations.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.kundprenumeration.application.PrenumerationStore;
import se.fk.rimfrost.kund.prenumeration.KundUnsubscribeRequest;

/**
 * Consumes {@code kund.unsubscribe} messages and removes the subscription from {@link PrenumerationStore}.
 * Silent no-op if the prenumerationId is not found (FR-01.6).
 */
@ApplicationScoped
public class KundUnsubscribeConsumer
{

   private static final Logger log = LoggerFactory.getLogger(KundUnsubscribeConsumer.class);

   @Inject
   PrenumerationStore prenumerationStore;

   /**
    * Handles an incoming unsubscribe request.
    *
    * @param request the unsubscribe payload
    */
   @Incoming("kund-unsubscribe")
   @Blocking
   public void onUnsubscribe(KundUnsubscribeRequest request)
   {
      log.info("Unsubscribe: prenumerationId={}", request.getPrenumerationId());
      prenumerationStore.deleteByPrenumerationId(request.getPrenumerationId());
   }
}
