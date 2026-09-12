---
summary: The intake's state machine, its counter-based jam detection and recovery, the feeder interlock, and the two places its IO layer blurs the line between reading and acting.
objectives:
  - Trace the intake through a jam and recovery, loop by loop
  - Explain why jam detection counts loops instead of reacting to one reading
  - Explain the feeder interlock and the arm-out check
  - Describe findings F16 and F6 and what they mean for testing
files:
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/intake/IntakeConstants.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java
---

## Six motors, four states

The intake is the most mechanically complicated subsystem on the robot: two roller motors (CAN 13 with 18 following it), an arm on an absolute encoder (14), a feeder (15), and two horizontal rollers (16 and 17). All of that is driven by four states.

::diagram name="intake-states" caption="IDLE and PAUSED come from the wanted state. The INTAKING to REVERSING cycle is the intake deciding for itself."

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="private void applyState()" lines=19

| State | Arm | Rollers | Feeder | Horizontals |
|---|---|---|---|---|
| `IDLE` | To `armRestingPosition` (0.15) | Stopped | 0 | 0 |
| `PAUSED` | **Not commanded**, so it holds its last setpoint | Stopped | 0 | 0 |
| `INTAKING` | To `intakePosition` (0.77) | 5000 RPM in | 4500 RPM, if allowed | 9000 RPM |
| `REVERSING` | To `intakePosition` (0.77) | 5000 RPM out | −4500 RPM | −9000 RPM |

`IDLE` and `PAUSED` differ in exactly one way: `IDLE` sends the arm home, and `PAUSED` doesn't. That's what lets the Superstructure park the intake safely while the turret is in the way.

## Jam detection counts loops

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="private CurrentState updateState()" lines=37 highlight="12-20"

The transition into `REVERSING` is not "the current is high." It is "the current has been high **and** the rollers have been slow for 50 loops in a row."

| Constant | Value | In real units |
|---|---|---|
| `jamCurrent` | 35 A | Above a normal intaking draw |
| `jamSpeed` | 1000 RPM | Well below the 5000 RPM setpoint |
| `jamMinCount` | 50 loops | **1.0 second** of continuous evidence |
| `unjamMinCount` | 10 loops | **0.2 seconds** of reversing |

Every loop that doesn't look like a jam sets `jamCount` back to 0, so the evidence has to be *continuous*. That is the same idea as a debouncer: trade a little reaction time for a lot fewer false alarms. A FUEL bouncing off the roller for two loops draws current too, and you do not want the intake spitting it back out.

Two details are easy to miss:

- **`!jammed` in the condition.** Once jammed, counting stops. While the intake reverses, the current is high and the speed is low again, and without that check it would immediately "re-detect" the jam it is already fixing.
- **Recovery clears everything.** After 10 reversing loops, `jammed`, `jamCount`, and `unjamCount` all reset. If the FUEL is still stuck, the next second of evidence starts a fresh cycle. The intake keeps trying, and the driver keeps feeling the rumble.

## Waiting for the arm

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public boolean armOut ()" lines=3

`armOut()` compares the arm's measured angle against its setpoint, not against a fixed number, so it means "the arm has reached wherever we asked it to go." Reversing only starts once that is true.

The reason is mechanical: a jam detected while the arm is still swinging out probably isn't a jam at all, it's the arm's own current draw. So a jammed intake with a moving arm keeps the state `INTAKING` and waits.

## The feeder interlock

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="private void intake(boolean resetting, boolean flywheelSpedUp)" lines=7

The feeder is what pushes FUEL into the flywheel, so it is gated on two facts from the turret:

- `turret.getResetting()`: the turret is more than 0.1 rad from its setpoint, so it is still swinging to a new aim.
- `turret.shooterSpedUp()`: the flywheel is within `flywheelTolerance` (1000 RPM, tunable) of its setpoint.

`RobotContainer` passes these into the intake's constructor as `BooleanSupplier`s, so the intake never touches the turret. Everything else keeps running while the feeder waits: the arm stays out, the rollers keep collecting, and FUEL stacks up behind the feeder.

## Two IO-layer wrinkles

:::warning Finding F16: an output inside `updateInputs`
::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from="inputs.subsystemCurrent" lines=3 highlight="3"

The last line of `updateInputs` sends the arm's setpoint to its closed-loop controller. A method named "update inputs" is where every reader expects sensing, not acting, and AdvantageKit's replay mode calls the same layer with the outputs thrown away.

It also adds a loop of delay: `setIntakeArmAngle` only stores a number, so a state's arm decision reaches the motor on the **next** loop, when `updateInputs` runs again. And because `armSetAngle` is initialized to `IntakeConstants.intakePosition`, the very first setpoint the arm receives after boot is "deployed," before any state machine has run.

The fix is small: move that `setSetpoint` call into `setIntakeArmAngle`, where the decision already arrives.
:::

:::warning Finding F6: jams can't happen in simulation
::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java" from="public void updateInputs(IntakeIOInputs inputs)" lines=13

The simulated IO never fills `intakeCurrent`, so the jam condition can't be true. It also sets `armAngle` from the arm's setpoint but never sets `inputs.armSetAngle`, which stays 0, so `armOut()` compares 0.77 against 0 and returns false. Even if a jam were detected, the intake would never reverse in simulation.

Both are a few lines to fix, and they would make jam recovery testable without a robot:

```java title="What the sim IO would need"
inputs.armSetAngle = armSetAngle;
inputs.intakeCurrent = simulatedCurrentAmps; // whatever your sim model produces
```

Notice also that `IntakeIOSim extends IntakeIOSpark` and calls `super()`, so running the simulator constructs real `SparkFlex` and `SparkMax` objects for hardware that isn't there (finding F7). A sim IO that implements `IntakeIO` directly avoids that.
:::

## How this code came to exist

The jam logic arrived in commit `6dd0a6b`, "Unjam," on 2026-03-11: four days after the Minnesota Bluff Country Regional and two weeks before Iowa. That is the normal shape of competition code. A problem shows up in matches, the fix is a counter and a state, and it ships between events. Reading the history is a good way to understand why code looks the way it does.

:::exercise id="u08-intake"
Rebuild the intake's jam detection and recovery as a pure class, with the same constants and the same loop-by-loop behavior as `Intake.updateState`.

The tests check the parts that are easy to get subtly wrong: a 49-loop spike is not a jam, a detected jam waits for the arm, recovery takes exactly 10 reversing loops, and leaving `INTAKING` clears everything.
---hint
Do the reset first: if the wanted state isn't `INTAKING`, zero both counters and clear `jammed`. Then a switch expression can map the four simple states, and send `INTAKING` to a private helper.
---hint
In the helper, follow the order in the class comment exactly: count or reset `jamCount`, then check `jamCount >= JAM_MIN_COUNT`, then handle `jammed && armOut`. The `!jammed` term in the counting condition is what stops a reversing intake from re-detecting its own jam.
---hint
The loop that reaches `UNJAM_MIN_COUNT` still returns `REVERSING`. Clear the flags first, then return.
:::

:::quiz
?num How long must the intake see high current and low roller speed before it declares a jam?
= 1.0 ± 0.05 s
> 50 loops at 20 ms each.

? Why does the jam condition include `!jammed`?
+ So a reversing intake, which also draws high current at low speed, doesn't re-detect the jam it is clearing
- To make the code shorter
- Because `jamCount` is a double
- So testing mode can skip jam detection
> Without it, the counter would climb again during recovery.

? A jam is detected while the arm is still swinging out. What does the intake do?
+ Stays `INTAKING` until the arm reaches its setpoint, then reverses
- Reverses immediately
- Returns to `IDLE`
- Stops the rollers and waits for the driver
> `armOut()` compares the measured arm angle with its setpoint, and the current spike from a moving arm isn't a jam.

? Which two conditions must both be satisfied before the feeder runs?
+ The turret is not resetting, and the flywheel is within tolerance of its setpoint
- The arm is out, and the robot is in our alliance zone
- The HUB is active, and the driver holds the right trigger
- The intake is not jammed, and the hood is at its maximum angle
> Both come from the turret through `BooleanSupplier`s passed to the intake's constructor.

? What is the only difference between the `IDLE` and `PAUSED` outputs?
+ `IDLE` sends the arm to its resting position; `PAUSED` leaves the arm where it is
- `PAUSED` reverses the rollers
- `IDLE` stops the feeder and `PAUSED` doesn't
- They are identical
> That difference is what lets the Superstructure park the intake while the turret is unsafe.

? Why can't you test jam recovery in simulation today?
+ The simulated IO never sets `intakeCurrent`, and it leaves `armSetAngle` at 0 so `armOut()` is always false
- Simulation runs too fast for the counters
- The CommandScheduler skips the intake in simulation
- Jam detection is disabled outside of matches
> Finding F6. Filling in those two inputs would make the whole cycle testable on a laptop.
:::
