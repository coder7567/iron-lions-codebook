---
summary: Replay re-runs match code on a laptop with the exact inputs the robot saw. Here is how to do it with our project, what it can and can't answer, and a worked example on a real bug.
objectives:
  - Run a replay of a real log with our code
  - Explain what `setUseTiming(false)` and the `_sim` output log do
  - Use replay to investigate a bug by adding logging after the fact
  - Recognize when replay will not reproduce a problem
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/Constants.java
  - build.gradle
---

## The idea

A log holds every input the robot read. Replay feeds those inputs back into the same code, on a laptop, as fast as the laptop can run them. The robot's decisions happen again, in order, and you can watch them.

It is the closest thing FRC has to a time machine, and our project already has everything it needs.

## Running one

:::steps
1. **Get the log.** Pull the `.wpilog` off the USB stick, or download it with AdvantageScope's log downloader over the robot's network.
2. **Check out the matching code.** The log's metadata names the commit; check it out so the replay runs the code that produced the log. (Ours names the wrong commit, finding F12, so for now use the date and your memory of what was deployed.)
3. **Switch the mode.** In `Constants`, set `simMode` to `Mode.REPLAY`:

::source file="src/main/java/frc/robot/Constants.java" from="public static final Mode simMode" lines=2

4. **Run the simulator.** `./gradlew simulateJava`. `LogFileUtil.findReplayLog()` opens a file picker (or uses the last log you chose).
5. **Watch the output.** A new log appears next to the original with `_sim` on the end, containing everything the replay recorded.
6. **Compare.** Open both in AdvantageScope and put the original and replayed versions of a value on the same graph.
:::

::source file="src/main/java/frc/robot/Robot.java" from="case REPLAY:" lines=6 highlight="2,3,4,5"

`setUseTiming(false)` is what makes it quick: instead of pretending to be a robot at 50 Hz, the replay runs loops as fast as it can, so a 2.5 minute match takes a few seconds.

:::tip `./gradlew replayWatch`
::source file="build.gradle" from="task(replayWatch" lines=4

This task re-runs the replay automatically every time you save a file. Change a constant or add a log line, hit save, and the `_sim` log updates. It turns "what would this change have done in that match?" into a loop you can run every few seconds.
:::

## The trick that makes it worth it

Outputs are recomputed during replay, so **you can add logging to code that already ran**.

Suppose the drive team says the turret behaved strangely while paused near midfield at the Iowa regional. You can:

1. Replay that match's log.
2. Add `Logger.recordOutput("Turret/PausedBranch", ...)` inside `updateState` to record which branch of the `PAUSED` case was taken.
3. Save; `replayWatch` re-runs.
4. Open the `_sim` log and look at the new value across the whole match.

You would see `PAUSEDSHOOTING` every time and `PAUSEDPASSING` never, because both branches yield the same state. That is finding F1, discovered from a log of a match that happened weeks ago, without a robot.

## What replay can't do

| Question | Replay? | Why |
|---|---|---|
| "Why did the state machine choose that?" | **Yes** | Logic is recomputed from logged inputs |
| "What would a different gain have done?" | **Partly** | The controller's response replays, but the robot's physical reaction to a different output did not happen |
| "What if the driver had done something else?" | No | Joystick values are inputs; they are what they were |
| "Why did the module lose its connection?" | No | Replay shows when, not why; that is a hardware question |
| "Did our hub-shift logic work?" | **Not today** | Match time and game data are read straight from `DriverStation`, so they aren't in the log |

The last row is worth internalizing: **replay is only as good as the IO boundary.** Every input that bypasses the IO layer is a hole in the time machine. In our code those holes are the Driver Station calls in `Superstructure`, and closing them is a contained, high-value project.

## Habits that make replay work when you need it

- **Log the inputs you don't think you need.** Storage is cheap; a missing input is permanent.
- **Keep the IO boundary clean**, even when a direct call would be shorter.
- **Fix the build metadata** so a log identifies its code.
- **Keep logs.** Copy the USB stick's contents to a shared drive after every event, named by match. A log you deleted is a bug you will debug twice.

:::quiz
? What does replay feed into the code?
+ The exact inputs the robot recorded, in order
- The outputs the robot produced
- A physics simulation of the robot
- Live NetworkTables values
> Inputs in, decisions recomputed.

? What does `setUseTiming(false)` do during replay?
+ Runs the loops as fast as the computer can instead of in real time
- Disables timestamps in the log
- Turns off the watchdog
- Skips loops that overran on the robot
> A whole match replays in seconds.

? Why can you add a `recordOutput` call and see its values for a past match?
+ Outputs are recomputed during replay, so new ones appear in the replayed log
- AdvantageKit records every variable automatically
- The log file can be edited
- The dashboard caches old values
> This is the single most useful property of replay-based logging.

? Which question can replay answer?
+ "Which branch of the state machine ran at that moment?"
- "Why did the CAN bus drop out?"
- "What would the robot have done if the driver turned left instead?"
- "Is the battery healthy?"
> Replay reruns decisions; it can't change what happened or explain hardware.

? Our hub-shift logic doesn't replay faithfully. Why?
+ Match time, game data, and alliance are read directly from `DriverStation` instead of through a logged IO layer
- The logic uses randomness
- `Superstructure` has no `@AutoLog` inputs class
- Replay skips subsystems with no hardware
> Inputs that bypass the IO boundary are holes in replay.

? What should happen to the USB stick's logs after an event?
+ Copy them to shared storage, named by match, before the stick gets reused
- Delete them to save space
- Leave them on the stick for the season
- Upload them to the FMS
> A deleted log is a bug you get to debug twice.
:::
