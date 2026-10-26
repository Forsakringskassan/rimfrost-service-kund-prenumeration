package se.fk.github.rimfrost.kundprenumeration.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link PrenumerationStore}. */
class PrenumerationStoreTest
{

   private PrenumerationStore store;

   @BeforeEach
   void setUp()
   {
      store = new PrenumerationStore();
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.1: upsert stores a new prenumeration")
   void upsert_stores_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic"));

      assertThat(store.findByTypIdAndVarde("TYP", "123")).hasSize(1);
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.5: duplicate prenumerationId is overwritten (idempotent upsert)")
   void upsert_overwrites_existing_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic-old"));
      store.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic-new"));

      var result = store.findByTypIdAndVarde("TYP", "123");
      assertThat(result).hasSize(1);
      assertThat(result.get(0).replyTo()).isEqualTo("reply-topic-new");
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.2: deleteByPrenumerationId removes an existing prenumeration")
   void deleteByPrenumerationId_removes_prenumeration()
   {
      store.upsert(new Prenumeration("id-1", "TYP", "123", "reply-topic"));
      store.deleteByPrenumerationId("id-1");

      assertThat(store.findByTypIdAndVarde("TYP", "123")).isEmpty();
   }

   @Test
   @DisplayName("KUNDPREN-FR-01.6: deleteByPrenumerationId silently ignores unknown ID")
   void deleteByPrenumerationId_ignores_missing_id()
   {
      store.deleteByPrenumerationId("unknown-id");
      // no exception expected
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: findByTypIdAndVarde returns all matching prenumerationer")
   void findByTypIdAndVarde_returns_matching_prenumerationer()
   {
      store.upsert(new Prenumeration("id-1", "TYP", "123", "reply-a"));
      store.upsert(new Prenumeration("id-2", "TYP", "123", "reply-b"));
      store.upsert(new Prenumeration("id-3", "TYP", "999", "reply-c"));

      assertThat(store.findByTypIdAndVarde("TYP", "123")).hasSize(2);
   }

   @Test
   @DisplayName("KUNDPREN-FR-02.1: findByTypIdAndVarde returns empty list when no match")
   void findByTypIdAndVarde_returns_empty_when_no_match()
   {
      assertThat(store.findByTypIdAndVarde("TYP", "no-match")).isEmpty();
   }
}
