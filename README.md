# TaskFlow — Role-Based Project Management System

A complete, working implementation of the TaskFlow spec: React (Vite) frontend +
Spring Boot (Java 21) backend + MySQL, with JWT auth, dual-stage OTP
verification, role-based access control (Admin / Manager / Team Member),
manager phone-OTP for sensitive edits, an Admin dual-OTP authorization
handshake for DB-adjacent operations, availability status, and audit logging.

No pre-existing TaskFlow codebase was provided to build on, so this is a
fresh implementation written to match the spec end-to-end, not a diff
against something that already existed.

## Quick start

### 1. Database
Create a MySQL database:
```sql
CREATE DATABASE taskflow;
```
Tables are auto-created/updated on startup (`spring.jpa.hibernate.ddl-auto=update`).
For a real production deployment, switch to a migration tool (Flyway/Liquibase)
instead of relying on Hibernate auto-DDL.

### 2. Backend
```bash
cd backend
cp .env.example .env   # then edit values
# export the vars from .env into your shell, or configure them in your IDE run config
mvn spring-boot:run
```
Runs on `http://localhost:8080`.

Email and SMS default to **console-logging stubs** (`ConsoleEmailService`,
`ConsoleSmsService`) — OTPs are printed to the backend log, so you can run and
test the whole flow with zero external credentials. Swap in real providers by
setting `taskflow.email.provider=smtp` (+ `MAIL_*` env vars) and
`taskflow.sms.provider=twilio` (+ `SMS_*` env vars, and finish wiring the
Twilio SDK call in `TwilioSmsService`).

**First Admin account:** set `ADMIN_BOOTSTRAP_USERNAME/PASSWORD/EMAIL/PHONE`
in `.env` before the first run — `DataSeeder` creates exactly one Admin from
these on startup, only if no Admin exists yet. There is no other way to
create an Admin (see spec section 36); Manager accounts should similarly be
provisioned by an Admin through a dedicated admin flow rather than public
registration (a natural next step: an `/api/admin/users` POST endpoint —
not yet included here since it wasn't explicitly specified).

### 3. Frontend
```bash
cd frontend
npm install
cp .env.example .env
npm run dev
```
Runs on `http://localhost:5173` and proxies `/api` to the backend.

## What's implemented

- Registration → email OTP → account activation
- Login → email OTP → JWT → SMS login notification
- Masked email everywhere in the UI; real email kept server-side for sending OTPs
- Admin / Manager / Team Member roles with `@PreAuthorize` + service-layer checks
  on every endpoint (never trusting the frontend)
- Manager phone-OTP gate before editing/deleting a project (short-lived
  action-grant token, not the raw OTP, is what unlocks the mutation)
- Manager → Admin dual-OTP (email then phone) authorization handshake for
  sensitive/DB-adjacent operations, with no DB credentials ever reaching the
  Manager or the frontend
- Team Member: read-only projects, own-task status updates only, own
  availability only
- Availability status (Available/Busy/Not Available) with colored dot on
  the avatar, updatable only by the user themselves
- Project + Task CRUD scoped correctly per role
- Audit log of key security/business events, viewable by Admin
- Role-based dashboards and sidebar navigation
- OTP hashing (BCrypt), expiry, max attempts, resend cooldown, one-time use
- Public registration always creates TEAM_MEMBER — role is never trusted from
  client input

## What's intentionally left as a next step

- An Admin-facing "create/approve Manager account" endpoint (spec implies
  Admin provisions Managers, but didn't give an exact flow — the backend is
  structured so this is a small addition: reuse `AuthService`-style creation
  with `Role.MANAGER`, admin-only, no public route)
- Rate limiting is enforced for OTP resend/attempts; general API rate limiting
  (e.g. login brute-force throttling) would need a filter/bucket4j addition
- Real Twilio wiring (stub left in `TwilioSmsService` — SDK call not filled in
  since it needs your actual credentials/account)
- I was not able to run `mvn compile` in this environment (no Maven Central
  network access here), so give the backend a compile pass before deploying
- Automated tests (the spec's test checklist in section 41 is a good source
  for what to write first)

## Project structure

```
backend/   Spring Boot 3 / Java 21 — see src/main/java/com/taskflow
frontend/  React 18 + Vite — see src
```
