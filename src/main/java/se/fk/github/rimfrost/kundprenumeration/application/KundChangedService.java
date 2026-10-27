package se.fk.github.rimfrost.kundprenumeration.application;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.fk.github.rimfrost.kundprenumeration.integration.kafka.KundChangedEvent;
import se.fk.github.rimfrost.kundprenumeration.integration.kafka.KundChangedEventPublisher;
import se.fk.rimfrost.kund.prenumeration.Idtyp;

/**
 * Orchestrates event delivery when kundcentrerat data changes.
 * Looks up active prenumerationer and publishes a {@link KundChangedEvent} to each replyTo-topic.
 */
@ApplicationScoped
public class KundChangedService
{

   private static final Logger log = LoggerFactory.getLogger(KundChangedService.class);

   @Inject
   PrenumerationStore prenumerationStore;

   @Inject
   KundChangedEventPublisher publisher;

   /**
    * Finds all active prenumerationer for the given identifier and publishes a KundChangedEvent
    * to each subscriber's replyTo-topic.
    *
    * @param typId  identifier type
    * @param varde  identifier value
    */
   public void triggerKundChanged(String typId, String varde)
   {
      var prenumerationer = prenumerationStore.findByTypIdAndVarde(typId, varde);
      log.info("Triggering KundChangedEvent for typId={} varde={} — {} subscriber(s)", typId, varde, prenumerationer.size());
      for (Prenumeration p : prenumerationer)
      {
         Idtyp kund = new Idtyp();
         kund.setTypId(typId);
         kund.setVarde(varde);
         publisher.publish(new KundChangedEvent(p.prenumerationId(), kund), p.replyTo());
      }
   }
}
