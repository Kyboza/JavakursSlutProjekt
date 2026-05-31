# Testguide – Hotellbokningssystem

Manuella tester för API:et med `curl`.

> **Skal:** Kommandona nedan är skrivna för **bash** (t.ex. Git Bash, macOS eller Linux).
> Det innebär `\` för radbrytning och enkla citattecken `'...'` runt JSON-bodyn, så att
> de inre dubbla citattecknen inte behöver escapas. Kör du i Windows **cmd** i stället:
> byt `\` mot `^` och kapsla in bodyn i `"..."` med `\"` på varje inre citattecken.

## Förberedelser

Starta appen:

```bash
./mvnw spring-boot:run
```

Basadress: `http://localhost:8080`

Inloggningar:

| Användare | Lösenord | Roll  |
|-----------|----------|-------|
| Johan     | 123456   | ADMIN |
| Anna      | 123456   | USER  |

Rumstyper och lager: 10 Enkelrum, 7 Dubbelrum, 3 Sviter.
Priser: Enkelrum 500 kr, Dubbelrum 1000 kr, Svit 2000 kr.

---

## 1. Visa rum (publikt)

```bash
curl http://localhost:8080/api/rooms
```

Förväntat: `{"Enkelrum":10,"Dubbelrum":7,"Svit":3}`

---

## 2. Boka utan token – ska nekas

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -d '{"guestName":"Anna","roomType":"Dubbelrum","numberOfGuests":2}'
```

Förväntat: `401 Unauthorized` / `403 Forbidden` (endpointen är skyddad).

---

## 3. Logga in som USER

```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"Anna","password":"123456"}'
```

Förväntat: `{"token":"<JWT>"}` – kopiera token-värdet till nästa steg.

---

## 4. Skapa en bokning (med token)

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DIN_TOKEN" \
  -d '{"guestName":"Anna","roomType":"Dubbelrum","numberOfGuests":2}'
```

Förväntat: `201 Created` med bokningen, t.ex.:

```json
{ "id": 1, "guestName": "Anna", "roomType": "Dubbelrum", "numberOfGuests": 2, "totalPrice": 1000 }
```

---

## 5. Felhantering (VG)

Alla felsvar ska innehålla `timestamp`, `status` och `message`.

### 5a. För många gäster (kapacitet)

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DIN_TOKEN" \
  -d '{"guestName":"Anna","roomType":"Enkelrum","numberOfGuests":2}'
```

Förväntat: `400` med meddelande "För många gäster för rumstypen: Enkelrum".

### 5b. Ogiltig rumstyp (validering)

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DIN_TOKEN" \
  -d '{"guestName":"Anna","roomType":"Foo","numberOfGuests":1}'
```

Förväntat: `400` med meddelande "Rumstyp måste vara Enkelrum, Dubbelrum eller Svit".

### 5c. Tomt namn (validering)

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DIN_TOKEN" \
  -d '{"guestName":"","roomType":"Svit","numberOfGuests":1}'
```

Förväntat: `400` med ett valideringsmeddelande om namnet, t.ex. "Namn får inte vara tomt" (ett tomt namn bryter mot både `@NotBlank` och `@Pattern`, så endera meddelandet kan visas).

### 5d. Slutbokad rumstyp

Boka `Svit` med 1 gäst fyra gånger (det finns bara 3 sviter):

```bash
curl -i -X POST http://localhost:8080/api/bookings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer DIN_TOKEN" \
  -d '{"guestName":"Anna","roomType":"Svit","numberOfGuests":1}'
```

Det fjärde anropet ger: `409 Conflict` med meddelande "Svit är slutbokat".

---

## 6. Admin-skydd

Med USER-token (Anna) mot admin-listan:

```bash
curl -i http://localhost:8080/api/bookings \
  -H "Authorization: Bearer ANNAS_TOKEN"
```

Förväntat: `403 Forbidden`.

Logga in som **Johan** (ADMIN), använd den token:en och kör samma anrop:

```bash
curl -i http://localhost:8080/api/bookings \
  -H "Authorization: Bearer JOHANS_TOKEN"
```

Förväntat: `200 OK` med en lista över alla bokningar.

---

## 7. Radera en bokning (admin)

```bash
curl -i -X DELETE http://localhost:8080/api/bookings/1 \
  -H "Authorization: Bearer JOHANS_TOKEN"
```

Förväntat: `204 No Content`. Kör sedan `GET /api/rooms` igen – rummet ska ha kommit tillbaka i lagret.

Radera ett id som inte finns:

```bash
curl -i -X DELETE http://localhost:8080/api/bookings/999 \
  -H "Authorization: Bearer JOHANS_TOKEN"
```

Förväntat: `404 Not Found` med meddelande "Bokning med id 999 finns inte".

---

## 8. Ogiltig eller utgången token

```bash
curl -i http://localhost:8080/api/bookings \
  -H "Authorization: Bearer ogiltig.token.har"
```

Förväntat: `401`/`403`. En trasig eller utgången token avvisas i `JwtRequestFilter`
(ingen autentisering sätts) i stället för att ge ett `500 Internal Server Error`.

---

## Flödesförklaringar

Beskriver de två centrala flödena i applikationen: inloggning med JWT och skapande
av en bokning. Båda visar hur ett anrop vandrar genom lagren och vilken fil som
ansvarar för vad.

### Login-flödet (JWT)

Login består av flera samverkande filer:

| Fil | Ansvar |
|-----|--------|
| `AuthController` | Tar emot `POST /login` och returnerar en token. |
| `LoginRequest` (DTO) | Bär `username` och `password` från anropet, med validering. |
| `SecurityConfig` | Definierar användare (in-memory), `AuthenticationManager`, `PasswordEncoder` och vilka endpoints som är skyddade. |
| `JwtService` | Skapar och validerar själva JWT-token. |
| `JwtRequestFilter` | Körs vid varje anrop och kopplar en giltig token till en inloggad användare. |

#### Steg 1 – Inloggning sker en gång

1. Klienten skickar `POST /login` med JSON `{ "username": "...", "password": "..." }`.
2. Spring läser kroppen till ett `LoginRequest` och kör Bean Validation (`@NotBlank`, `@Size`, `@Pattern`).
3. `AuthController.login()` skickar användarnamn + lösenord till `AuthenticationManager.authenticate(...)`.
4. `AuthenticationManager` slår upp användaren via `UserDetailsService` (de in-memory-användare som definieras i `SecurityConfig`) och jämför lösenordet med hjälp av `PasswordEncoder` (BCrypt).
5. Stämmer uppgifterna: `JwtService.generateToken(username)` skapar en signerad token med användarnamnet som `subject`, en utfärdandetid och en utgångstid (2 timmar). Token signeras med en hemlig nyckel (HMAC-SHA256).
6. Token returneras som `{ "token": "<JWT>" }`.
7. Stämmer uppgifterna inte: `authenticate(...)` kastar ett fel som fångas i `catch`-blocket och ger `401 Unauthorized`.

Token är alltså en signerad "stämpel" som bevisar vem du är. Servern sparar ingen
session – all information om vem du är ligger inuti token, skyddad av signaturen.

#### Steg 2 – Token används vid varje efterföljande anrop

1. Klienten skickar med token i headern: `Authorization: Bearer <JWT>` på alla skyddade anrop.
2. `JwtRequestFilter` (som ärver `OncePerRequestFilter` och körs en gång per anrop) läser headern.
3. Om headern börjar med `Bearer ` plockas själva token ut, och `JwtService.validateTokenAndGetUsername(token)` verifierar signaturen och läser ut användarnamnet.
4. Är token giltig: filtret laddar användaren via `UserDetailsService` och lägger en autentisering i `SecurityContextHolder`. Därmed är användaren "inloggad" för just det anropet, med sina roller (USER/ADMIN).
5. `SecurityConfig` avgör sedan om användaren får nå endpointen:
   - `GET /api/rooms` och `POST /login` – öppna för alla.
   - `POST /api/bookings` – kräver roll USER eller ADMIN.
   - `GET /api/bookings` och `DELETE /api/bookings/**` – kräver roll ADMIN.
6. Saknas token, eller är den ogiltig, sätts ingen autentisering, och Spring Security svarar `401`/`403` på skyddade endpoints.

Filtret placeras **före** `UsernamePasswordAuthenticationFilter` i kedjan, så att
token hinner kontrolleras innan Spring fattar sitt behörighetsbeslut.

### Booking-flödet

Att skapa en bokning involverar dessa filer:

| Fil | Ansvar |
|-----|--------|
| `BookingController` | Tar emot HTTP-anropet och returnerar svaret. |
| `BookingRequest` (DTO) | Bär inkommande data (`guestName`, `roomType`, `numberOfGuests`) med validering. |
| `BookingService` | All affärslogik: kapacitet, lager, pris. |
| `BookingRepository` | Lagrar bokningar i minnet och håller ordning på rumslagret. |
| `BookingModel` | Den färdiga, sparade bokningen (alla fem fält inkl. `id` och `totalPrice`). |
| `GlobalExceptionHandler` | Fångar fel och formaterar dem som strukturerad JSON. |

#### Steg för steg – `POST /api/bookings`

1. **Säkerhet.** `JwtRequestFilter` validerar token och `SecurityConfig` kontrollerar att användaren har roll USER eller ADMIN. Annars `401`/`403` innan controllern ens körs.
2. **Controller.** `BookingController.createBooking()` tar emot kroppen som `@Valid @RequestBody BookingRequest`. `@Valid` triggar Bean Validation:
   - `guestName` får inte vara tomt och måste matcha tillåtna tecken.
   - `roomType` måste vara `Enkelrum`, `Dubbelrum` eller `Svit`.
   - `numberOfGuests` måste vara mellan 1 och 3.
   Misslyckas valideringen kastas `MethodArgumentNotValidException` → `400` (se felhantering nedan). Controllern innehåller ingen logik – den skickar bara DTO:n vidare till servicen.
3. **Service – kapacitet.** `BookingService.createBooking()` översätter rumstypen till en kapacitet (Enkelrum=1, Dubbelrum=2, Svit=3) via en `switch`. Är typen okänd kastas `ResourceNotFoundException` (404).
4. **Service – lager.** `repository.getAvailable(roomType)` läser hur många rum av typen som finns kvar. Är det 0 kastas `RoomFullyBookedException` (409).
5. **Service – gäster.** Är `numberOfGuests` större än rummets kapacitet kastas `GuestCapacityException` (400).
6. **Service – pris.** `totalPrice` räknas ut via en `switch` (Enkelrum=500, Dubbelrum=1000, Svit=2000).
7. **Service – bygg och spara.** Ett `BookingModel` skapas med de fyra fälten (utan `id`). `repository.save(booking)` lägger bokningen i listan och tilldelar ett löpande `id`. `repository.decreaseAvailable(roomType)` minskar lagret med 1.
8. **Svar.** Den sparade bokningen returneras, och controllern svarar `201 Created` med hela bokningen (nu med `id` och `totalPrice`) som JSON.

#### Radering – `DELETE /api/bookings/{id}`

1. Endast ADMIN släpps in (kontrolleras i `SecurityConfig`).
2. `BookingService.deleteBooking(id)` letar upp bokningen via `repository.findById(id)`. Finns den inte kastas `ResourceNotFoundException` (404).
3. Bokningen tas bort med `repository.delete(...)`, och `repository.increaseAvailable(roomType)` lägger tillbaka rummet i lagret.
4. Controllern svarar `204 No Content`.

#### Felhantering (genomgående)

Ingen av kontrollerna fångar fel lokalt. När servicen kastar ett undantag bubblar
det upp förbi controllern till `GlobalExceptionHandler` (`@RestControllerAdvice`),
som har en `@ExceptionHandler` per feltyp:

| Undantag | HTTP-status |
|----------|-------------|
| `ResourceNotFoundException` | 404 Not Found |
| `RoomFullyBookedException` | 409 Conflict |
| `GuestCapacityException` | 400 Bad Request |
| `MethodArgumentNotValidException` (valideringsfel) | 400 Bad Request |
| Övriga (`Exception`) | 500 Internal Server Error |

Varje handler returnerar ett `ErrorResponse` med `timestamp`, `status` och `message`,
så att klienten alltid får ett enhetligt, strukturerat felsvar.

#### Lagring (in-memory)

`BookingRepository` är en Spring-singleton (`@Repository`) – det finns exakt en
instans under hela körningen. Bokningarna ligger i en `List`, lagret i en `Map`,
och `id`-räknaren i ett fält. Datan lever så länge appen kör och nollställs vid
omstart, helt enligt kravet att data lagras i minnet.
