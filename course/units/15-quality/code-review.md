---
summary: Every finding in this course is something a review could have caught. Here is the checklist to run, how to write a comment somebody can act on, and how to review under competition pressure.
objectives:
  - Review a change against a checklist rather than by impression
  - Write review comments that name the path, the effect, and the question
  - Run a review that fits the time available
  - Explain what review does for a team beyond finding bugs
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
---

## What review is actually for

Finding bugs is the obvious reason, and this course is a long argument for it: F1, F4, F13, F14, F16, and F19 are all things a second reader would plausibly have caught, and none of them announced itself on the field.

The less obvious reasons matter more over a season:

- **It spreads knowledge.** After a review, two people understand the turret instead of one. That is what lets the team survive a graduation or an illness before an event.
- **It teaches.** A new programmer learns more from having their pull request read carefully than from any tutorial, including this one.
- **It creates a record.** "Why is the hood offset here?" answered in a review thread is documentation nobody had to write separately.

## The checklist

Run it top to bottom. It takes about ten minutes on a normal change.

| # | Question |
|---|---|
| 1 | **Units and frames.** Is every value in the units its name implies, in the frame its name implies? |
| 2 | **Reachability.** Can every state and branch in this change actually happen? |
| 3 | **Completeness.** Does every state set every output it needs? Is every field a consumer reads updated on every path? |
| 4 | **Reads and writes.** Do sensor reads happen in the IO layer and outputs in the output methods? |
| 5 | **Timing.** Does anything here block, allocate in a loop, or write flash? |
| 6 | **Safety.** Are clamps, soft limits, and current limits still intact for anything this change commands? |
| 7 | **Logging.** Could you diagnose this code from a log alone? |
| 8 | **Simulation.** Does the simulated path behave like the real one? |
| 9 | **Tests.** Is any pure logic here worth a test, and did it get one? |
| 10 | **Deletion.** Did this change leave behind commented-out code or now-unused constants? |

Question 2 is the highest-yield one in this codebase. It is a single sentence, it takes ten seconds, and it catches F1.

## Writing the comment

A comment somebody can act on has three parts: **the path**, **the effect**, and **a question**.

> In `Turret.updateState`, both branches of `case PAUSED` yield `PAUSEDSHOOTING`, so `PAUSEDPASSING` is unreachable even though `applyState` handles it. Paused outside our alliance zone, the turret aims at the HUB instead of the passing target. Should the second branch yield `PAUSEDPASSING`?

Compare the versions people actually write:

| Comment | Problem |
|---|---|
| "This looks wrong." | No path, no effect, nothing to do |
| "Change line 182 to PAUSEDPASSING." | An instruction without a reason; if the author had a reason, you never hear it |
| "Why is this here?" | Reads as an accusation when it is meant as a question |
| The version above | Names the code, the consequence, and leaves room for the author to know something you don't |

Two more habits worth having. **Separate must-fix from nice-to-have** explicitly, because an author cannot tell them apart from tone. And **approve when it is better than what is there**, rather than holding out for perfect; a review that blocks for a week teaches people to stop asking for reviews.

## Reviewing under pressure

At a competition, ten minutes is not available. Triage instead:

| Change | Review depth |
|---|---|
| A constant or a tuning value | Read the value; confirm it is the one that was measured |
| A binding or an auto selection | Read it aloud with the drive team present |
| Logic in a state machine | Full checklist questions 2, 3, and 6, no exceptions |
| Anything touching safety limits | Full checklist, and test on the robot before the next match |
| A "quick fix" nobody can explain | Do not deploy it |

The last row is the one that saves matches. A change nobody can explain in one sentence is a change that has not been understood, and understanding it takes less time than a failed match does.

## What to look at besides the diff

A diff shows what changed, not what it broke. Three things live outside it:

- **The invariants.** `redoPassingFunction` looks fine on its own; it is only wrong when you compare it with the constructor it duplicates (F4).
- **The other implementation.** A change to a real IO class needs the simulated one checked too, which is how F5 and F6 survived.
- **The consumers.** Adding a state means finding every `switch` over that enum. The compiler helps for switch expressions and not for switch statements.

:::try Review something real
Open `AprilTagVision.periodic` and run the checklist against it, without rereading [Unit 13](course:13-vision/vision-pipeline). Write your findings as comments in the three-part form above, then compare with F13 and F14.

This is a genuinely useful exercise, because that file is well-written, readable, and contains four separate problems. Reviewing it teaches something that reviewing bad code cannot: **clear code is not the same as correct code.**
:::

:::quiz
? Which checklist question would have caught finding F1?
+ "Can every state and branch actually happen?"
- "Are the units right?"
- "Is anything blocking in the loop?"
- "Did this leave commented-out code behind?"
> One sentence, ten seconds, and it catches an unreachable state.

? What are the three parts of a useful review comment?
+ The code path, the effect on the robot, and a question
- The severity, the line number, and a fix
- Praise, criticism, and praise
- The rule violated, the standard, and a link
> The question leaves room for the author to know something you don't.

? Why review the simulated implementation when the real one changes?
+ They drift apart silently, which is how findings F5 and F6 survived
- Simulation runs the same code
- The compiler cannot see simulation classes
- Simulation is what the drive team uses
> A diff on one file says nothing about its twin.

? At an event, someone proposes a one-line fix that they cannot explain. What should happen?
+ It does not get deployed
- Deploy it; one line is low risk
- Deploy it and watch the next match
- Ask the drive team to decide
> A change nobody can explain has not been understood yet.

? Besides finding bugs, what is review's biggest long-term benefit to this team?
+ Two people end up understanding each subsystem instead of one
- It produces documentation automatically
- It enforces formatting
- It reduces the number of commits
> That is what lets a team survive graduation, illness, and a busy week.

?tf A reviewer should block a change until it is as good as they would have written it.
= false
> Approve when it improves on what is there. Reviews that block for a week teach people to stop asking.
:::
