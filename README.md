# kund-prenumeration

> **Mock-tjänst** — Avsedd endast för test- och demomiljöer, inte produktion.
> Prenumerationer lagras enbart i minnet och försvinner vid omstart.


Quarkus-tjänst som tillhandahåller en generell subscribe/unsubscribe-mekanism mot kundcentrerat
data. När kundcentrerat data förändras (t.ex. efter att en användare kompletterat en uppgift på
Mina sidor) levererar servicen ett Kafka-event till alla aktiva prenumeranter för det berörda
`idtyp`/`idvarde`-paret.

Servicen är regeltyps-agnostisk — den är inte kopplad till en specifik regeltyp — men det
primära användningsfallet är kompletteringsregler som behöver reagera på förändringar i
kundcentrerat data för att kunna påverka sitt utfall.

## Aktörer

| Aktör | Roll |
|---|---|
| Kompletteringsregler | Prenumererar på förändringar i kundcentrerat data för ett givet `idtyp`/`idvarde` |
| `rimfrost-framework-regel-komplettering` | Hanterar subscribe vid start av kompletteringsuppgift och unsubscribe vid avslut |

## Ansvarsområden

- **Prenumerationshantering** — tar emot och lagrar subscribe/unsubscribe-anrop från regler,
  knutna till prenumerantens Kafka-topic.
- **Eventleverans** — publicerar ett event till prenumerantens topic när kundcentrerat data
  förändras för en prenumererad `idtyp`/`idvarde`-kombination.
- **Testendpoint** — exponerar `POST /test/kundcentrerat-data-changed` för manuell
  eventtrigger i test- och demomiljöer.

## REST API

| Metod | Sökväg | Beskrivning |
|---|---|---|
| `POST` | `/test/kundcentrerat-data-changed` | Publicera ett testevent manuellt |
| `GET` | `/q/health` | Hälsostatus |

## Kafka

| Riktning | Topic | Beskrivning |
|---|---|---|
| In | `kund.subscribe` | Prenumerera på förändringar för ett `idtyp`/`idvarde` |
| In | `kund.unsubscribe` | Avprenumerera för ett `idtyp`/`idvarde` |
| Ut | Prenumerantens topic | Kundcentrerat-data-event vid förändring |

## Konfiguration

| Property | Beskrivning | Default |
|---|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka-anslutning (prod, via miljövariabel) | — |

I `dev`- och `test`-profil startas Kafka automatiskt som Dev Services-container.

## Bygga och köra

```bash
# Kör i utvecklingsläge (med live reload)
./mvnw quarkus:dev

# Kör tester
./mvnw test

# Bygg körbar JAR
./mvnw package
java -jar target/quarkus-app/quarkus-run.jar
```

Standardport: **8080**
