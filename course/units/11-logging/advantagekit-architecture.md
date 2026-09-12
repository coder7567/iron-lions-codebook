---
summary: How AdvantageKit is wired into our project: the `@AutoLog` inputs classes, the one line that makes replay work, and the rules your code has to follow to stay replayable.
objectives:
  - Explain what `@AutoLog` generates and how `processInputs` uses it
  - Explain the difference between an input and an output, and why it matters for replay
  - List the rules that keep code deterministic under replay
  - Find the AdvantageKit wiring in `build.gradle`
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/Robot.java
  - build.gradle
---

## Inputs, outputs, and the line between them

AdvantageKit divides every value into two kinds, and the division is the whole design:

| | Input | Output |
|---|---|---|
| Comes from | The world: sensors, the Driver Station, cameras | Your code's decisions |
| Recorded by | `Logger.processInputs` | `Logger.recordOutput` or `@AutoLogOutput` |
| On replay | **Fed back in** from the log | **Recomputed** by the code |
| Example | `driveVelocityRadPerSec`, `turretAngle` | `SwerveStates/Setpoints`, `CurrentState` |

If every input is logged, and the code is a pure function of its inputs, then re-running the code on those inputs must produce the same outputs. That is the promise replay depends on.

## What `@AutoLog` generates

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="public static class ModuleIOInputs" lines=18

The annotation processor reads that class and generates `ModuleIOInputsAutoLogged`, a subclass that knows how to write every field into a log table and read every field back. You never see the generated file unless you look in `build/generated`, and you never edit it.

The processor is wired up in `build.gradle`, with its version read out of the vendordep file so the two can't drift apart:

::source file="build.gradle" from="def akitJson" lines=2

## The one line that matters

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="io.updateInputs(inputs);" lines=2

Those two lines run in every subsystem, and the second is the switch:

- **Real or simulation:** `processInputs` takes the values the IO layer just wrote into `inputs` and records them.
- **Replay:** it **overwrites** `inputs` with the values from the log, discarding whatever the IO layer produced.

That is why replay uses `new ModuleIO() {}`, an IO that does nothing: its output would be thrown away anyway. Everything above the IO layer, including the state machines and the controllers, runs unchanged and unaware.

## The rules that keep replay honest

Replay works only if the code's behavior depends on nothing except its logged inputs. Four rules follow:

:::steps
1. **All sensor data comes through an IO layer.** A `DriverStation.getAlliance()` call in a subsystem is a hidden input. Ours has several, which is a known limitation of our code rather than a rule we follow perfectly.
2. **No wall-clock time except through logged timestamps.** `Timer.getFPGATimestamp()` is replayed by AdvantageKit; `System.currentTimeMillis()` is not.
3. **No randomness.** `Math.random()` produces a different robot every run.
4. **Replay the code that produced the log.** A log carries metadata naming its commit, which is exactly why finding F12 hurts: ours names the wrong one.
:::

Our robot follows rules 2 and 3 and breaks rule 1 in a few places. `Superstructure.periodic` reads `DriverStation.getMatchTime()`, `getGameSpecificMessage()`, and `getAlliance()` directly. Those are inputs that never pass through an IO layer, so a replay of a match re-reads them from the replaying laptop (where they are empty) rather than from the log. The hub logic therefore does not replay faithfully. Routing them through a small `DriverStationIO` would fix it, and it is a good first contribution.

## Outputs are recomputed, not restored

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="@AutoLogOutput(key" occurrence=1 lines=2

Because outputs are recomputed during replay, you can **add new ones and see them for matches that already happened**. Add a `recordOutput` inside a state machine, replay last weekend's log, and the new value appears in the new log as though you had always been recording it. No other debugging tool in FRC does that.

## URCL: inputs you didn't have to write

`Logger.registerURCL(URCL.startExternal())` in `Robot` registers the unofficial REV logging library, which records every SPARK's position, velocity, applied output, current, and faults directly from the CAN bus. It costs one line and gives you motor-level data for every device, including ones your IO layer doesn't read.

:::exercise id="u11-inputs"
Build the record-and-replay contract yourself: an inputs object that writes its fields into a table and reads them back, and a tiny logger with a REAL mode that records and a REPLAY mode that overwrites the inputs from recorded frames.

The important test runs the same logic twice: once with real readings, then again with an IO layer that reports nonsense. If your logger is right, the replay produces identical outputs anyway.
---hint
`toLog` puts each field under `prefix + "/" + fieldName`. `fromLog` reads the same keys and leaves a field alone when its entry is missing, which is what makes an unlogged input silently keep whatever it had.
---hint
In `beginLoop`, advance the frame index first, then check the replay source for the end of the log. Start a fresh recorded frame in both modes, so the replay's outputs can be compared with the original run's.
---hint
`processInputs` in REPLAY mode calls `fromLog` from the source frame **before** writing the values into the new recorded frame.
:::

:::quiz
? What does `@AutoLog` generate?
+ A subclass of the inputs class that can write every field to a log and read it back
- A dashboard widget
- A replay script
- A NetworkTables publisher
> It's an annotation processor that runs at build time.

? In replay mode, what does `Logger.processInputs` do?
+ Overwrites the inputs object with the values from the log, discarding whatever the IO produced
- Writes the inputs to a new log only
- Skips the subsystem
- Reads from NetworkTables
> That single behavior is what makes replay possible.

? Why can you add a new `recordOutput` call and see its values for a match that already happened?
+ Outputs are recomputed during replay from the logged inputs
- AdvantageKit stores every possible value
- The log is editable
- NetworkTables keeps a history
> The inputs are the record; everything downstream is recomputed.

? `Superstructure.periodic` calls `DriverStation.getMatchTime()` directly. What does that mean for replay?
+ Match time is an input that never reaches the log, so replaying the hub logic doesn't reproduce the match
- Nothing; the Driver Station is replayed automatically
- The replay crashes
- The value is recorded by `@AutoLogOutput`
> Every input has to pass through a logged IO layer to be replayable.

? Why does replay mode construct subsystems with `new ModuleIO() {}`?
+ Its readings would be discarded anyway, so an IO that does nothing is exactly right
- It's faster than the simulation IO
- The log contains the IO implementation
- WPILib requires it
> The log supplies the inputs; the IO layer is bypassed.

?? Which habits keep code replayable? Select all that apply.
+ Read every sensor through an IO layer
+ Use `Timer.getFPGATimestamp()` rather than `System.currentTimeMillis()`
+ Avoid randomness in robot logic
- Log fewer inputs to keep files small
> Replay reproduces a run only when the run depended on nothing but logged inputs.
:::
