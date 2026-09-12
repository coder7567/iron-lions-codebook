---
summary: The Superstructure is the one place that decides what the whole robot is doing. Read its state table, its rumble logic, its alliance watcher, and the broken current-averaging block.
objectives:
  - Explain why a coordinating subsystem exists and what belongs in it
  - Read the Superstructure's state table and predict the turret and intake requests for any state
  - Explain the intake-safe interlock and the alliance watcher
  - Describe finding F2 and how you would fix the current-averaging code
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/RobotContainer.java
---

## One robot, one state

The turret and the intake share the robot. The intake arm swings through space the turret can occupy, and the feeder must not push FUEL into a flywheel that isn't ready. Someone has to decide what the robot as a whole is doing.

That someone is `Superstructure`: a subsystem that owns no hardware. It holds references to the other four subsystems, keeps its own wanted and current state, and translates one robot-level state into a request for each mechanism.

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="public Superstructure(Drive drive" lines=6

Each driver button sets one Superstructure state, and the Superstructure decides what that means for each mechanism. When the intake grows a new behavior, the buttons don't change.

## The state table

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private void applyState()" lines=32 highlight="4-9"

| Superstructure state | Turret wanted | Intake wanted |
|---|---|---|
| `IDLE` | `IDLE` | `IDLE` when the turret reports intake-safe, else `PAUSED` |
| `PAUSED` | `PAUSED` | `PAUSED` |
| `SHOOTING` | `SHOOTING` | `INTAKING` |
| `EJECTING` | `SHOOTING` | `REVERSING` |
| `TESTING` | `TESTING` | `TESTING` |

Two rows carry real design decisions.

**`IDLE` has an interlock.** Going idle sends the intake arm back to its resting position, which sweeps it through the turret's space. The Superstructure only allows that once `turret.intakeSafe()` is true, which the turret reports when its angle is within 0.05 rad of its idle position of −1.6 rad. Until then the intake is `PAUSED`: rollers stopped, arm left where it is.

**`EJECTING` keeps the turret shooting.** Ejecting is for clearing a stuck FUEL, so the turret keeps aiming and spinning while every intake roller reverses. The operator can go straight back to `SHOOTING` without waiting for a spin-up.

## What `updateState` does today

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private CurrentState updateState(WantedState wantedState)" lines=14

Every wanted state maps to the current state with the same name. That looks pointless, and today it nearly is. What it buys you is a **place to put a rule** the day you need one, without touching any button binding. For example: refuse `SHOOTING` while the intake is jammed, or drop to `IDLE` if the turret is disconnected. Those rules go here, and every path into shooting is covered at once.

## Rumble: telling the driver something is wrong

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="public boolean getRumble()" lines=3

Three warnings share one signal, bound in `RobotContainer` with `onTrue`/`onFalse` ([Unit 7](course:07-command-based/triggers-and-bindings)):

1. **Shooting into an inactive HUB.** The turret is in `SHOOTING` (not `PASSING`) while `hubActive` is false, so those shots wouldn't score.
2. **The intake is jammed.** `intake.getRumble()` returns the jam flag.
3. **The turret can't reach its target.** `turret.getRumble()` returns `inDeadzone`.

Note the first term reads the **turret's** current state, not the Superstructure's. A robot passing from the neutral zone doesn't rumble, because passing doesn't depend on our HUB being active.

## Watching for the alliance

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="if (previousAlliance != DriverStation.getAlliance()" lines=4

The turret builds its passing map in its constructor, using field positions that are mirrored for the red alliance. At boot the Driver Station usually hasn't reported an alliance yet, so those positions come out blue-side. When the alliance finally arrives and differs from the last known one, this block calls `turret.redoPassingFunction()` to rebuild the map.

`getAlliance()` returns an `Optional`, and `orElse(previousAlliance)` turns "unknown" into "no change," so an unplugged Driver Station never triggers a rebuild. The pattern is worth copying: **remember the last known value, and act only on a real change.**

:::warning Finding F2: the current averaging never averages
::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="//current averaging" lines=23 highlight="8-12"

Two bugs sit in this block:

1. `timestamp` and `lastTimestamp` are readings from **consecutive loops**, so their difference is about 0.02 s. The test `timestamp - lastTimestamp > 1` is never true, `totalTimestamps` stays 0, and nothing is ever logged.
2. `toggleCurrentLogging()` returns a command, but no binding in `RobotContainer` ever schedules it, so `currentLogging` is always false anyway.

If you want an average draw over a window, accumulate every loop and divide by the loop count, or use the timer to mark a window boundary:

```java title="One way to fix it"
if (currentLogging) {
  totalCurrent += getTotalCurrent();
  totalTimestamps++;
  Logger.recordOutput("Average Current", totalCurrent / totalTimestamps);
}
```

The `@AutoLogOutput` on `getTotalCurrent()` already records the instantaneous total every loop, so the average is a convenience, not a necessity. Deleting the block is a legitimate fix too.
:::

## Two loops of latency

The Superstructure is constructed last, so its `periodic()` runs after the turret's and the intake's. A button press reaches the mechanisms two loops (about 40 ms) later. [Subsystems in Depth](course:07-command-based/subsystems-in-depth) traces it loop by loop. Remember it when you read logs: a state change appears in `Wanted State` one or two loops before the motors react.

:::exercise id="u08-superstructure"
Rebuild the Superstructure's decisions as pure functions: the state table (including the intake-safe interlock), the rumble condition, and the alliance watcher.

None of it needs WPILib, which is the point. This is the logic most worth testing, and it's the part that's hardest to test while it's tangled up with `DriverStation` calls.
---hint
`requestsFor` is a switch expression over `SuperState` that returns a `Requests` record. Only the `IDLE` case looks at `turretIntakeSafe`.
---hint
`rumble` is one boolean expression with three terms. Check the turret's state against `SHOOTING` exactly, not `PASSING`.
---hint
`AllianceWatcher.update` mirrors `getAlliance().orElse(previousAlliance)`: treat `null` as "same as before," compare with the previous value, store the new one, and return whether it changed.
:::

:::quiz
? Why does the Superstructure own no hardware?
+ Its job is coordinating the subsystems that do own hardware
- Subsystems can't own hardware in WPILib
- It runs on a different thread
- It would conflict with the CommandScheduler
> It translates one robot-level state into requests for each mechanism.

? The Superstructure is `IDLE` and the turret is at 0.9 rad, far from its idle position. What does the intake get?
+ `PAUSED`, so the arm stays where it is
- `IDLE`, so the arm returns to rest
- `REVERSING`
- Nothing; the intake keeps its last request
> The arm sweeps through the turret's space, so returning to rest waits for `intakeSafe()`.

? What does `EJECTING` ask of the turret?
+ `SHOOTING`, so it keeps aiming and spinning while the intake reverses
- `IDLE`
- `PAUSED`
- `TESTING`
> That way the driver can return to shooting without waiting for a spin-up.

? The turret is `PASSING` while our HUB is inactive, and nothing is jammed. Does the controller rumble?
+ No, because the first rumble term requires the turret's state to be `SHOOTING`
- Yes, because the HUB is inactive
- Yes, because passing always rumbles
- Only in autonomous
> Passing doesn't depend on our HUB being active, so it isn't a warning.

? Why does the alliance check use `getAlliance().orElse(previousAlliance)`?
+ So an unknown alliance counts as "no change" instead of a change
- Because `Optional` can't be compared directly
- To convert the alliance to a string
- To flip the field coordinates
> Before the Driver Station connects, the alliance is empty, which shouldn't trigger a rebuild.

? Why does the current-averaging block never log anything?
+ It compares two timestamps taken one loop apart and waits for a difference greater than 1 second
- `getTotalCurrent()` returns 0
- `Logger.recordOutput` can't log doubles
- The timer is never started
> Consecutive loops are 0.02 s apart, so the condition is never true. The toggle command is also never bound.
:::
