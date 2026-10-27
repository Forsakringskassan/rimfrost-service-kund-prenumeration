package se.fk.github.rimfrost.kundprenumeration.application;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for active prenumerationer.
 * This is a mock/test service — subscriptions are not persisted across restarts.
 */
@ApplicationScoped
public class PrenumerationStore
{

   private final ConcurrentHashMap<String, Prenumeration> store = new ConcurrentHashMap<>();

   /**
    * Adds or replaces the prenumeration identified by its prenumerationId (FR-01.5).
    *
    * @param prenumeration the prenumeration to store
    */
   public void upsert(Prenumeration prenumeration)
   {
      store.put(prenumeration.prenumerationId(), prenumeration);
   }

   /**
    * Removes the prenumeration with the given ID. Silent no-op if not found (FR-01.6).
    *
    * @param prenumerationId the client-chosen ID to remove
    */
   public void deleteByPrenumerationId(String prenumerationId)
   {
      store.remove(prenumerationId);
   }

   /**
    * Returns all prenumerationer matching the given identifier type and value.
    *
    * @param typId identifier type
    * @param varde identifier value
    * @return matching prenumerationer, possibly empty
    */
   public List<Prenumeration> findByTypIdAndVarde(String typId, String varde)
   {
      List<Prenumeration> result = new ArrayList<>();
      for (Prenumeration p : store.values())
      {
         if (typId.equals(p.typId()) && varde.equals(p.varde()))
         {
            result.add(p);
         }
      }
      return result;
   }
}
