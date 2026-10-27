package se.fk.github.rimfrost.kundprenumeration.presentation.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import se.fk.github.rimfrost.kundprenumeration.application.KundChangedService;
import se.fk.rimfrost.service.kund.prenumeration.test.jaxrsspec.controllers.generatedsource.TestControllerApi;
import se.fk.rimfrost.service.kund.prenumeration.test.jaxrsspec.controllers.generatedsource.model.TriggerRequest;

/**
 * Implements the test endpoint for manually triggering a kundcentrerat-data-changed event.
 * Only intended for use in test and demo environments (FR-03).
 */
@ApplicationScoped
@Path("/test/kundcentrerat-data-changed")
public class TestController implements TestControllerApi
{

   @Inject
   KundChangedService kundChangedService;

   /**
    * Triggers a KundChangedEvent for the given idtyp/idvarde combination.
    * Returns 204 on success, 400 if idtyp or idvarde is missing (FR-03.2).
    *
    * @param triggerRequest the trigger request containing idtyp and idvarde
    */
   @Override
   public void triggerKundcentreratDataChanged(TriggerRequest triggerRequest)
   {
      kundChangedService.triggerKundChanged(triggerRequest.getIdtyp(), triggerRequest.getIdvarde());
   }
}
