package se.fk.github.rimfrost.kundprenumeration.integration.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.fk.rimfrost.kund.prenumeration.Idtyp;

/**
 * Event published to a subscriber's replyTo-topic when kundcentrerat data changes.
 * Mirrors the KundChangedEvent schema in rimfrost-service-kund-prenumeration-asyncapi.
 */
public class KundChangedEvent
{

   @JsonProperty("prenumerationId")
   private String prenumerationId;

   @JsonProperty("kund")
   private Idtyp kund;

   public KundChangedEvent()
   {
   }

   /**
    * Creates a new KundChangedEvent.
    *
    * @param prenumerationId the client-chosen ID from the original subscribe request
    * @param kund            identifies the customer whose data changed
    */
   public KundChangedEvent(String prenumerationId, Idtyp kund)
   {
      this.prenumerationId = prenumerationId;
      this.kund = kund;
   }

   /** @return the client-chosen subscription ID */
   public String getPrenumerationId()
   {
      return prenumerationId;
   }

   /** @return the customer identifier */
   public Idtyp getKund()
   {
      return kund;
   }
}
