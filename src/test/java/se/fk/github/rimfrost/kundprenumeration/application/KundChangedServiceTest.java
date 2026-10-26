package se.fk.github.rimfrost.kundprenumeration.application;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.fk.github.rimfrost.kundprenumeration.integration.kafka.KundChangedEvent;
import se.fk.github.rimfrost.kundprenumeration.integration.kafka.KundChangedEventPublisher;

/** Unit tests for {@link KundChangedService}. */
class KundChangedServiceTest
{

   private PrenumerationStore prenumerationStore;
   private KundChangedEventPublisher publisher;
   private KundChangedService service;

   @BeforeEach
   void setUp()
   {
      prenumerationStore = mock(PrenumerationStore.class);
      publisher = mock(KundChangedEventPublisher.class);
      service = new KundChangedService();
      service.prenumerationStore = prenumerationStore;
      service.publisher = publisher;
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: publishes KundChangedEvent to each matching subscriber's replyTo")
   void triggerKundChanged_publishes_event_to_each_subscriber()
   {
      when(prenumerationStore.findByTypIdAndVarde("TYP", "123")).thenReturn(List.of(
            new Prenumeration("id-1", "TYP", "123", "reply-a"),
            new Prenumeration("id-2", "TYP", "123", "reply-b")));

      service.triggerKundChanged("TYP", "123");

      verify(publisher, times(1)).publish(
            argThat(e -> "id-1".equals(e.getPrenumerationId())), eq("reply-a"));
      verify(publisher, times(1)).publish(
            argThat(e -> "id-2".equals(e.getPrenumerationId())), eq("reply-b"));
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.2: published event contains idtyp and idvarde")
   void triggerKundChanged_event_contains_kund_identifier()
   {
      when(prenumerationStore.findByTypIdAndVarde("TYP", "123")).thenReturn(List.of(
            new Prenumeration("id-1", "TYP", "123", "reply-a")));

      service.triggerKundChanged("TYP", "123");

      verify(publisher).publish(
            argThat((KundChangedEvent e) -> "TYP".equals(e.getKund().getTypId()) && "123".equals(e.getKund().getVarde())),
            eq("reply-a"));
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: no event published when no subscribers match")
   void triggerKundChanged_does_nothing_when_no_subscribers()
   {
      when(prenumerationStore.findByTypIdAndVarde("TYP", "999")).thenReturn(List.of());

      service.triggerKundChanged("TYP", "999");

      verify(publisher, never()).publish(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
   }
}
