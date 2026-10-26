package se.fk.github.rimfrost.kundprenumeration.application;

/**
 * Internal representation of an active subscription.
 *
 * @param prenumerationId client-chosen ID that correlates subscribe/unsubscribe and KundChangedEvent
 * @param typId           identifier type (e.g. PERSONNUMMER)
 * @param varde           identifier value
 * @param replyTo         Kafka topic to publish KundChangedEvent to when data changes
 */
public record Prenumeration(String prenumerationId,String typId,String varde,String replyTo){}
