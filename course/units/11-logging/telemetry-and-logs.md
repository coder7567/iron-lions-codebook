---
summary: Why a robot that logs everything is debuggable and one that doesn't isn't, where our logs go, what lands in them, and the metadata bug that makes every log lie about its code version.
objectives:
  - Explain the three places robot data goes and what each is good for
  - Read our logging setup in `Robot` and name what each receiver does
  - Choose good log keys and know what logging costs
  - Explain finding F12 and how to fix it
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/BuildConstants.java
  - build.gradle
---

## You cannot debug a match

During a match nobody is attached to the robot. There is no breakpoint, no console, no second chance. What you get afterward is whatever the robot wrote down, and the difference between "we fixed it between matches" and "it happened again" is usually the difference between a robot that logged the right thing and one that didn't.

Our robot logs a lot, and the reason it can is that its logging was designed in from the start rather than added after a bad match.

## Three destinations

| Destination | What it is | Good for | Limits |
|---|---|---|---|
| **NetworkTables** | Live values published to the dashboard | Watching now, tuning, driver feedback | Nothing is kept; bandwidth is shared with the FMS |
| **WPILOG file** | A file on the roboRIO or a USB stick | After-match review, sharing, evidence | Only what you chose to write |
| **AdvantageKit log** | A WPILOG that also contains every **input** | Everything above, plus replay | Slightly larger files |

The third is the one that changes how you work. Because it stores inputs, not just outputs, [replay](course:11-logging/replay) can re-run the code on a laptop with exactly the data the robot saw.

## Our setup

::source file="src/main/java/frc/robot/Robot.java" from="// Set up data receivers & replay source" lines=20 highlight="4-7,10-11,14-18"

| Mode | Receivers |
|---|---|
| `REAL` | `WPILOGWriter()` writes a log file, and `NT4Publisher()` publishes live values to the dashboard |
| `SIM` | NetworkTables only |
| `REPLAY` | Reads a log as the **source** of inputs, and writes a second log next to it with `_sim` appended |

On the robot, the log file lands on a USB stick when one is plugged into the roboRIO. **A missing USB stick is a pit checklist item**: the robot still works, the match still happens, and afterward there is nothing to look at.

`Logger.registerURCL(URCL.startExternal())` adds every REV device's data to the log without any code per motor, which is what makes the SysId analyzer and motor-level debugging possible.

## What our code logs

| Kind | How | Examples in our code |
|---|---|---|
| **Inputs** | `Logger.processInputs(key, inputs)` in each subsystem's `periodic` | `Drive/Module0`, `Drive/Gyro`, `Turret`, `Intake`, `Vision/AprilTag/Camera0` |
| **Outputs** | `Logger.recordOutput(key, value)` | `Wanted State`, `CurrentState`, `Hub Active`, `TOF`, `SwerveStates/Setpoints` |
| **Annotated outputs** | `@AutoLogOutput` on a getter | `Odometry/Robot`, `SwerveStates/Measured`, `TotalCurrent` |
| **Tunables** | `LoggedNetworkNumber` records itself | `Tuning/Drive/...`, `turretP`, `flywheelTolerance` |
| **Choices** | `LoggedDashboardChooser` | `Auto Choices` |
| **Metadata** | `Logger.recordMetadata` before `start()` | Project name, Git SHA, build date, branch |

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="Logger.recordOutput(" occurrence=1 lines=3

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="@AutoLogOutput(key" occurrence=1 lines=2

:::tip Name keys like a file system
Our keys are inconsistent: `Drive/Module0` and `SwerveStates/Setpoints` are grouped by subsystem, while `Wanted State`, `CurrentState`, `Hub Active`, and `Turret State` sit loose at the top level with different capitalization and spacing. AdvantageScope groups by the `/` separator, so grouped keys are far easier to find in a hurry.

A convention worth adopting: `Subsystem/Thing`, no spaces, consistent capitalization. `Superstructure/WantedState`, `Superstructure/CurrentState`, `Superstructure/HubActive`, `Turret/State`. Renaming keys breaks comparisons with older logs, so do it early in a season, not at an event.
:::

## What logging costs

- **Bandwidth**, for anything published to NetworkTables during a match. The FMS limits what a robot may use.
- **CPU**, in your 20 ms loop. Logging numbers is cheap; building strings is not. The build sets `-XDstringConcat=inline` so string concatenation compiles to straightforward code rather than the JVM's dynamic version, which behaves poorly on the roboRIO.
- **Disk**, which is mostly a non-issue with a USB stick, and a real one without.

The rule that follows: log every input and the values you would want after a failure, keep strings out of the hot path, and don't publish a number to the dashboard just because you can.

## Finding F12: every log lies about its version

::source file="src/main/java/frc/robot/BuildConstants.java" from="public static final String MAVEN_NAME" lines=8

That file is supposed to be regenerated at every build by the `gversion` Gradle plugin. Our `build.gradle` doesn't apply it, and the file is committed to the repository, so it is frozen at a build from **November 2025**, on a project named `967_AdvantageKitTemplate_2025`, with `DIRTY = 1` meaning "uncommitted changes."

Every log the robot has ever written carries that metadata. When you open a log from Iowa and ask "which code was this?", the answer it gives is wrong.

The fix is to add the plugin, have it write `BuildConstants.java` into a generated source folder at build time, and stop committing the file. Then a log from a match tells you the exact commit that produced it, which is what makes "it worked yesterday" a question you can answer.

:::team The jar carries its own source
Our `build.gradle` copies `src`, `vendordeps`, and `build.gradle` into the deployed jar under `backup/`:

::source file="build.gradle" from="from('src')" lines=3

If the laptop with the code dies at an event, the robot still has a copy of what is running on it. It is not a substitute for Git, and it has saved teams.
:::

:::quiz
? What can an AdvantageKit log do that a plain WPILOG of outputs cannot?
+ Re-run the code later with the exact inputs the robot saw
- Publish values to the dashboard
- Record the match time
- Store more values
> Logging inputs is what makes replay possible.

? Where does our robot's log file go during a match?
+ To a USB stick plugged into the roboRIO
- To the driver station laptop
- To NetworkTables only
- To the deploy folder
> A missing stick means no log, so it belongs on the pit checklist.

? Which logging call would you use for a value computed by the code rather than read from hardware?
+ `Logger.recordOutput(...)` or an `@AutoLogOutput` getter
- `Logger.processInputs(...)`
- `Logger.recordMetadata(...)`
- `LoggedNetworkNumber`
> Inputs come from the IO layer; outputs are what the code decided.

? Why does the build set `-XDstringConcat=inline`?
+ String concatenation compiles to simpler code, avoiding the JVM's dynamic version, which behaves poorly on the roboRIO
- It makes logs smaller
- It is required by JUnit
- It enables `@AutoLog`
> Strings are the expensive part of logging in a 20 ms loop.

? What does finding F12 mean for a log from a competition match?
+ Its metadata reports a November 2025 build and the wrong Git SHA, so you cannot tell which code produced it
- The log is unreadable
- Inputs are missing
- Replay will crash
> `BuildConstants` is committed and never regenerated because the build has no `gversion` plugin.

?? Which of these are good habits when choosing log keys? Select all that apply.
+ Group by subsystem with a `/` separator
+ Keep capitalization and spacing consistent
+ Rename keys early in the season rather than at an event
- Include the current match time in every key name
> AdvantageScope groups by `/`, and renaming breaks comparisons with older logs.
:::
