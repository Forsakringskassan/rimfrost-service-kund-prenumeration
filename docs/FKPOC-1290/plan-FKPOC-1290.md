# Plan — FKPOC-1290: rimfrost-service-kund-prenumeration initial implementation

Base package: `se.fk.github.rimfrost.kundprenumeration`

## Steps

- [x] **1. Add dependencies**
- [x] **2. Domain model**
- [x] **3. Persistence**
- [x] **4. Kafka consumers (subscribe / unsubscribe)**
- [x] **5. Event publisher**
- [x] **6. Application service**
- [x] **7. Test endpoint**
- [x] **8. Tests**

---

## Design

### Step 1 — Add dependencies

Add to `pom.xml`:

| Dependency | Scope | Purpose |
|---|---|---|
| `quarkus-messaging-kafka` | compile | Kafka consumer + producer |
| `quarkus-rest-jackson` | compile | REST (test endpoint) |
| `se.fk.rimfrost.kund.prenumeration:rimfrost-service-kund-prenumeration-asyncapi:0.1.0` | compile | Generated payload POJOs (KundSubscribeRequest, KundUnsubscribeRequest, KundChangedEvent, Idtyp) |
| `se.fk.rimfrost.service.kund.prenumeration.test:rimfrost-service-kund-prenumeration-test-openapi-jaxrs-spec:0.0.1` | compile | Generated JAX-RS interface for TestController |
| `smallrye-reactive-messaging-in-memory` | test | In-memory Kafka for tests |

### Step 2 — Domain model

Generated from asyncapi artifact (no hand-written POJOs needed):
`KundSubscribeRequest`, `KundUnsubscribeRequest`, `Idtyp` — note: `prenumerationId` is `String`, not UUID.

`KundChangedEvent` is **not** generated — hand-written in `integration/kafka/`.

Two classes created:

```
application.Prenumeration(String prenumerationId, String typId, String varde, String replyTo)
integration.kafka.KundChangedEvent(String prenumerationId, Idtyp kund)
```

### Step 3 — In-memory subscription store

This is a mock/test service — no persistent storage required.

Layer: **application**
Package: `se.fk.github.rimfrost.kundprenumeration.application`

```
@ApplicationScoped
PrenumerationStore
  ConcurrentHashMap<UUID, Prenumeration> store

  void   upsert(Prenumeration)                         // FR-01.5
  void   deleteByPrenumerationId(UUID id)              // silent if missing — FR-01.6
  List<Prenumeration> findByTypIdAndVarde(String, String)
```

### Step 4 — Kafka consumers

Layer: **presentation / kafka**
Package: `se.fk.github.rimfrost.kundprenumeration.presentation.kafka`

```
KundSubscribeConsumer
  @Incoming("kund-subscribe")
  @Blocking
  void onSubscribe(KundSubscribeRequest payload)
    → maps to Prenumeration, calls PrenumerationRepository.upsert

KundUnsubscribeConsumer
  @Incoming("kund-unsubscribe")
  @Blocking
  void onUnsubscribe(KundUnsubscribeRequest payload)
    → calls PrenumerationRepository.deleteByPrenumerationId
```

`application.properties` channels:
```
mp.messaging.incoming.kund-subscribe.connector=smallrye-kafka
mp.messaging.incoming.kund-subscribe.topic=kund.subscribe
mp.messaging.incoming.kund-unsubscribe.connector=smallrye-kafka
mp.messaging.incoming.kund-unsubscribe.topic=kund.unsubscribe
```

### Step 5 — Event publisher

Layer: **integration / kafka**
Package: `se.fk.github.rimfrost.kundprenumeration.integration.kafka`

```
KundChangedEventPublisher
  @Channel("kund-changed-out")
  Emitter<KundChangedEvent> emitter

  void publish(KundChangedEvent event, String replyTo)
    → OutgoingKafkaRecordMetadata.builder().withTopic(replyTo).build()
    → emitter.send(Message.of(event).addMetadata(metadata))
```

`application.properties`:
```
mp.messaging.outgoing.kund-changed-out.connector=smallrye-kafka
mp.messaging.outgoing.kund-changed-out.value.serializer=io.quarkus.kafka.client.serialization.ObjectMapperSerializer
```

### Step 6 — Application service

Layer: **application**
Package: `se.fk.github.rimfrost.kundprenumeration.application`

```
KundChangedService
  void triggerKundChanged(Idtyp kund)
    → prenumerationRepository.findByTypIdAndVarde(kund.typId(), kund.varde())
    → for each: publisher.publish(new KundChangedEvent(p.prenumerationId, kund), p.replyTo)
```

### Step 7 — Test endpoint

Layer: **presentation / rest**
Package: `se.fk.github.rimfrost.kundprenumeration.presentation.rest`

`TestController` implements the generated JAX-RS interface from `rimfrost-service-kund-prenumeration-test-openapi-jaxrs-spec`.
`TriggerRequest` is the generated request class from the same artifact.

```
@ApplicationScoped
TestController implements TestControllerApi  // generated interface (returns void, 400 via @NotNull)
  triggerKundcentreratDataChanged(TriggerRequest triggerRequest)
    → KundChangedService.triggerKundChanged(triggerRequest.getIdtyp(), triggerRequest.getIdvarde())
```

### Step 8 — Tests

- Unit tests for `PrenumerationRepository` (upsert idempotency, silent delete)
- Unit tests for `KundChangedService` (dispatches to all matching prenumerationer)
- `@QuarkusTest` for `TestController` (POST → 204, verifies event emitted via in-memory connector)
- `@QuarkusTest` for consumers (subscribe → stored, unsubscribe → removed, duplicate subscribe → upserted)
