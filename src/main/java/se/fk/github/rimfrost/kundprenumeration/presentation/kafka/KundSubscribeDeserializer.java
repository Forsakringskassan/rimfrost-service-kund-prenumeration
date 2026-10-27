package se.fk.github.rimfrost.kundprenumeration.presentation.kafka;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;
import se.fk.rimfrost.kund.prenumeration.KundSubscribeRequest;

/** Deserializer for {@link KundSubscribeRequest} Kafka messages. */
@SuppressWarnings("unused")
public class KundSubscribeDeserializer extends ObjectMapperDeserializer<KundSubscribeRequest>
{

   public KundSubscribeDeserializer()
   {
      super(KundSubscribeRequest.class);
   }
}
