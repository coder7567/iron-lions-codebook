---
summary: Debugging with six minutes, a crowd, and no practice field. The triage order, the event-specific causes that never appear in the shop, and how to know when to stop.
objectives:
  - Triage a robot problem under a match clock
  - Check the causes that only exist at an event
  - Roll back safely and quickly
  - Decide when to stop debugging and play the match
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/RobotContainer.java
---

## The method still applies, with less time

[The debugging method](course:11-logging/debugging-method) does not change at an event. What changes is
the budget: six minutes between matches, a robot that has to be on the field, and a person asking how
long you need. So the order shifts from "understand it" to "decide whether it must be fixed now."

:::steps
1. **Does it have to be fixed before the next match?** A robot with a known annoyance beats a robot
   with a fresh, untested change.
2. **Check the free things first**, in this order: battery, connectors, CAN chain, dashboard alerts,
   auto selection. Most event problems stop here.
3. **Pull the log and look at ten seconds**, the ten around the symptom. Not the whole match.
4. **Name a single cause** you could test. If you cannot, go back to step one and answer "no."
5. **Prefer the reversible fix**: a dashboard value beats a deploy, and a deploy beats a rewrite.
6. **Re-verify the basics** after any deploy, and tell the drive team in one sentence.
:::

## Causes that only exist at an event

The most confusing words in FRC are "it worked in the shop." The shop is missing most of what an event
has:

| Event-only cause | Symptom | How to check |
|---|---|---|
| **You are the other alliance** | Field-relative driving is reversed; autos run mirrored; the passing map is rebuilt mid-match | The alliance on the Driver Station, and `Odometry/Robot` at the start of the match |
| **Real game data** | Hub-active logic behaves differently than it ever did in practice | `Hub Active` and `Period Time` in the log |
| **The FMS** | Practice-mode timings differ from a real match; the robot is disabled between periods | Compare the match log with a practice log |
| **A different field** | Tags are at surveyed but slightly different positions; carpet grip differs | Vision poses against odometry; auto end positions |
| **Radio and network** | Camera disconnects, dashboard lag, failed deploys | `isConnected` for both cameras, and the Driver Station's network tab |
| **Other robots** | Collisions during autos; pushed off a shot | The log's pose track, and the match video |
| **Tired batteries** | Slower autos, brownouts, mechanisms that were fine this morning | Bus voltage across the match |

Two of those deserve special attention on our robot, because the code has known behavior there. The
alliance flip depends on the pose's heading being right, which is worth confirming before the first
match on a new side of the field. And a disconnected camera keeps reporting its **last** observation
(finding F14), so "vision looked fine in the log" is not the same as "vision was working."

## Rolling back

Know this sequence without looking it up:

```bash
git stash
```

```bash
git checkout event-iowa-known-good
```

```bash
./gradlew deploy
```

Then re-verify: enable, drive a few feet, run a mechanism, re-select the auto. Rolling back is not an
admission of failure; it is the reason the tag exists. The failure mode to avoid is discovering at
match 9 that nobody knows which commit was good.

:::danger Never debug on the field
When the robot is on the field and the match is about to start, the software is whatever it is. No
deploys, no dashboard edits, no "one quick thing." The only acceptable field-side action is confirming
the auto selection.
:::

## Knowing when to stop

You are done debugging, for now, when any of these is true:

- The robot works well enough to score, and the remaining issue is cosmetic.
- You cannot name a cause you could test in the time available.
- The fix requires a change you would not deploy without practice time.
- The problem is mechanical or electrical and belongs to someone else.

Write down where you stopped, with the match number and what you saw. The gap between "we never
figured it out" and "we know it happens when the battery is under 11.5 V, and we will test it Tuesday"
is entirely in the notes.

:::tip The two-log habit
Keep the log from the match that went wrong **and** a log from a match that went right, from the same
day. Comparing them is faster than reading either one alone, and AdvantageScope can open both at once.
That comparison is what turns "the robot felt slow" into "the current limit was applied at a different
time," in about a minute.
:::

:::quiz
? At an event, what is the first question when something goes wrong?
+ Whether it has to be fixed before the next match at all
- Which subsystem is responsible
- Whether the log shows an error
- Who wrote the code
> A known annoyance beats an untested change.

? Which of these can only be checked at an event?
+ That the robot behaves correctly on the alliance you were actually assigned
- That the code compiles
- That the tests pass
- That the flywheel reaches speed
> "It worked in the shop" usually means the shop was missing something.

? The log shows vision poses throughout the match, and vision was clearly wrong. What does finding F14 suggest checking?
+ Whether a camera disconnected, since the inputs keep reporting the last observation
- Whether the tags were damaged
- Whether the ambiguity limit was too high
- Whether the robot was moving too fast
> Poses in the log are not proof that a camera was working.

?order Put the free checks in the order this lesson recommends.
1. Battery
2. Connectors and the CAN chain
3. Dashboard alerts
4. Auto selection
> Most event problems stop before you open a log.

? What is the only acceptable software action once the robot is on the field?
+ Confirming the auto selection
- Deploying a one-line fix
- Adjusting a tunable
- Restarting robot code
> Whatever is on the robot is what plays the match.

? Why keep a log from a match that went right?
+ Comparing a bad match against a good one from the same day is faster than reading either alone
- To prove the robot works
- To satisfy the FMS
- Logs are deleted otherwise
> AdvantageScope can open both at once.
:::
