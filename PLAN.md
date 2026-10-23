# PLAN.md

One section per milestone. Fill them in order, with the plan before you run
anything, the guardrail before you run a task, and the run after.

Keep it short and specific. Point at modules, gates, and file paths.

---

## Milestone 1: The plan

Turn `CHANGE-REQUEST.md` into three or four agent-sized tasks in the
Supervision Plan format. Every task carries a verification checkpoint, and the
section ends with an out-of-scope list.

**The tasks.** Three or four of them in dependency order. A task is
agent-sized when one agent run can finish it and one gate can tell you whether
it did. For each one, give:

- what behavior it delivers (not "edit module X"), which modules it touches,
  and what it depends on
- its checkpoint, meaning the gate command you run and the observable result
  that would convince you the task is done ("Run the tests" is not a
  checkpoint)
- one specific way the task could go wrong and still leave the gate green

**AGENTS.md rules 3 and 4.** One line: which of your checkpoints catches an
agent that breaks rule 3 (the conflict predicate) or rule 4 (waitlist writes).

**Out of scope, with reasons.** Which modules you are not touching and why. A
reason is a clause in the change request or a rule in AGENTS.md, not "it seemed
unrelated".

1. **Modify a held booking in the domain** — `core-domain` only. Add a request/result
   type, modification history, the `booking-modified` domain event, and a
   `BookingService` operation that reuses `BookingRules` and `ConflictChecker`,
   excludes the booking being changed from both conflict checks, and promotes the
   waitlist only after an interval change. This has no prerequisites.
   **Checkpoint:** `mvn -q -pl core-domain -am test` passes, including new tests
   for a self-move, rejected conflict preserving the original booking, no-op
   modification, and promotion after shortening. That demonstrates the domain
   owns all state transitions. **Green-but-wrong risk:** this gate cannot prove
   the event has the required JSON wire shape.

2. **Publish and consume modification facts** — `storage`, `notifications`, and
   `analytics`. Serialize `booking-modified`, send its owner old and new details,
   and fold the event into the analytics ledger so utilization uses the current
   interval. It depends on task 1's event contract. **Checkpoint:**
   `mvn -q -pl storage -am test`, `mvn -q -pl notifications -am test`, and
   `cd analytics && pytest` pass, with new log/template/ledger tests. **Green-but-wrong
   risk:** those gates do not prove the owner-only HTTP route is wired correctly.

3. **Expose modification and history over HTTP** — `http-api`. Add owner-only
   `PATCH /bookings/{bookingId}` and render the existing booking read surface's
   history for its authorized owner or an administrator. It depends on task 1.
   **Checkpoint:** `mvn -q -pl http-api -am test` passes, including owner,
   administrator-read, invalid-input, and conflict cases. **Green-but-wrong
   risk:** handler tests can pass even if the cross-module JSON event contract is
   inconsistent.

4. **Publish the documented platform contract and integrate** — `AGENTS.md`,
   `ARCHITECTURE.md`, `README.md`, and the completed integration tree. Update the
   event-type lists, the HTTP table, and the README tool/model attribution; do
   not alter shipped tests. It depends on tasks 1–3. **Checkpoint:** `mvn -B
   clean test` and `cd analytics && pytest` pass, while the documentation names
   `booking-modified` and PATCH exactly as implemented. **Green-but-wrong risk:**
   a full build does not itself prove the prose remains complete.

**AGENTS.md rules 3 and 4.** Task 1's core-domain checkpoint catches a second
overlap implementation by requiring self-exclusion through `ConflictChecker` and
the `TimeInterval.overlaps` tests; its waitlist-promotion tests catch a path that
writes or promotes waitlist rows outside `BookingService`.

**Out of scope, with reasons.** `accounts` is unchanged because the request
reuses existing tokens and ownership checks. `billing` is unchanged because the
request explicitly freezes the booked price and forbids re-pricing, invoices, or
voids. `cli` is unchanged because the request explicitly says no CLI work.

---

## Milestone 2: The guardrail

Pick one recipe from `guardrails/README.md`. Configure it, then prove it binds.

**The recipe.** Which one you picked, the exact paths or trees it covers, and
one line on what it cannot catch.

**The captured block.** Paste the excerpt of the agent's own turn showing what
you asked it to do, its tool call, and the block the guardrail returned. A bare
terminal paste with no agent turn around it does not show the boundary binding.

**The recipe.** Recipe C, filesystem lockdown, protects `accounts`, `billing`,
and `cli`: their `src/` trees and Maven poms are made read-only with
`guardrails/lockdown.sh` before the implementation run. It cannot catch an
out-of-scope behavioral change made inside an allowed tree (and a shell user can
undo it with `chmod`).

---

## Milestone 3: The run

Run exactly ONE task from your plan with the agent, under the guardrail, through
its gate. Any task whose dependencies are done qualifies, including the
smallest one.

**The takeover point.** Write this before you start the run. A concrete,
conditional statement of when you would stop the agent and write the code
yourself. "If something looks wrong" does not count. Name the trigger and name
what you would do.

**The evidence.** Name the task from Milestone 1. Paste the result line of its
gate command (run it without `-q` so the line is in the output) and the result
line of one `mvn -B clean test` run after the task was finished. The module
gate cannot see a cross-module compile break on a warm tree. A cold build can,
and so can CI.

**The diff.** The files the agent changed, and how each one maps to that task's
declared scope (plain `git diff` leaves out new files, but
`git add -N . && git diff --stat` includes them). Note anything it touched that
you did not expect, including any shipped test. Then quote the one line you
read hardest and say what you were checking for.

**The refuter.** What you gave it and what you asked it to do (paste the
brief). Then the one finding you settled, and how (the new failing test with
its red run, or the evidence that dismissed it). If nothing it reported held
up, say how you checked its strongest claim.

**The takeover point.** For task 1, if an implementation compares booking start
and end values outside `TimeInterval.overlaps`/`ConflictChecker`, or invokes
`WaitlistStore.save` or `.remove` outside `BookingService`, I stop the run and
repair that code directly before continuing.
