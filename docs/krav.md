# Krav — rimfrost-service-kund-prenumeration

Denna service tillhandahåller en generell subscribe/unsubscribe-mekanism mot kundcentrerat data.
När en användare kompletterar en uppgift på Mina sidor uppdateras kundcentrerat data i
rimfrost-extern. Regler som behöver reagera på sådana förändringar — primärt kompletteringsregler
— kan via denna service prenumerera på ändringar för ett givet identifieringsvärde. När en
förändring sker levereras ett event över Kafka, så att regeln kan påverka sitt utfall, avsluta
OUL-uppgiften, uppdatera Mimer och skicka svar tillbaka till processen.

## 1. Funktionella krav

### KUNDPREN-FR-01 — Prenumerationshantering

- **KUNDPREN-FR-01.1** Servicen ska exponera ett gränssnitt för att lägga till en prenumeration
  på förändringar i kundcentrerat data för en given kombination av identifieringstyp (`idtyp`)
  och identifieringsvärde (`idvarde`).
- **KUNDPREN-FR-01.2** Servicen ska exponera ett gränssnitt för att ta bort en aktiv
  prenumeration för en given kombination av `idtyp` och `idvarde`.
- **KUNDPREN-FR-01.3** En prenumeration ska knytas till den regel (eller process) som skapade
  den, så att event kan levereras till rätt mottagare.
- **KUNDPREN-FR-01.4** Servicen ska vara regeltyps-agnostisk — den ska kunna användas av
  godtyckliga regler.
- **KUNDPREN-FR-01.5** Om en `KundSubscribeRequest` tas emot med ett `prenumerationId` som redan
  finns ska prenumerationen uppdateras (idempotent upsert) — ingen felrespons ska ges.
- **KUNDPREN-FR-01.6** Om en `KundUnsubscribeRequest` tas emot med ett `prenumerationId` som
  inte finns ska meddelandet ignoreras utan felrespons.

### KUNDPREN-FR-02 — Eventleverans vid förändring

- **KUNDPREN-FR-02.1** När kundcentrerat data förändras för en prenumererad `idtyp`/`idvarde`-kombination
  ska servicen publicera ett event på den prenumerantens konfigurerade Kafka-topic.
- **KUNDPREN-FR-02.2** Eventet ska innehålla tillräcklig information för att mottagaren ska
  kunna identifiera vilket kundcentrerat data som förändrats (`idtyp` och `idvarde`).

### KUNDPREN-FR-03 — Testendpoint för manuell eventtrigger

- **KUNDPREN-FR-03.1** Servicen ska exponera `POST /test/kundcentrerat-data-changed` som publicerar ett
  kundcentrerat-data-changed-event för en given `idtyp`/`idvarde`-kombination, i syfte att möjliggöra
  manuell testning och demos i en Kubernetes-miljö.
- **KUNDPREN-FR-03.2** Om `idtyp` eller `idvarde` saknas i anropet ska HTTP 400 returneras.


---

## 2. Icke-funktionella krav

### KUNDPREN-NFR-02 — Observerbarhet

- **KUNDPREN-NFR-02.1** Servicen ska logga subscribe- och unsubscribe-anrop med `idtyp`,
  `idvarde` och prenumerant-identitet.
- **KUNDPREN-NFR-02.2** Servicen ska logga när ett event levereras till en prenumerant.
