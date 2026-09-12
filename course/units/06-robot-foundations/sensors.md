---
summary: The sensors our robot trusts (relative, absolute, and external encoders, the NavX gyro, and motor current), how zero offsets work, and how to check a sensor before believing it.
objectives:
  - Tell relative, absolute, and external encoders apart and know which mechanisms use each
  - Explain zero offsets and why the turret must be at its start position at power-on
  - Use current as a sensor, filter noisy signals, and validate a sensor's units and direction
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## Encoders measure rotation

An **encoder** tells code how far something has turned and how fast. There are three kinds on our robot, and mixing them up causes real bugs.

| Kind | Knows its position after power-on? | Used on our robot for |
|---|---|---|
| **Relative** (built into NEO and Vortex motors) | No, it starts at 0 every boot | Drive wheel distance, flywheel and roller speed |
| **Absolute** | Yes, it reports the actual angle | Swerve steering, intake arm, hood |
| **External quadrature** (REV Through Bore on the turret) | No, it counts from wherever it started | Turret angle, at 8192 counts per revolution |

A relative encoder is perfect for speed and for distance traveled since boot. For "which way is the wheel pointing right now?" you need an absolute encoder.

## Zero offsets

An absolute encoder reports an angle, but its zero is wherever the magnet happened to be mounted. A **zero offset** shifts the reading so 0 means something useful, like "wheel pointing straight forward."

Each swerve module has its own measured offset:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="// Zeroed rotation values for each module" lines=5

`ModuleIOSpark` subtracts the offset from every reading and adds it back to every setpoint. To find new offsets after rebuilding a module, point every wheel straight forward, with bevel gears facing the same way, read each raw absolute angle in AdvantageScope, and record those values.

The intake arm puts its offset in the SPARK configuration instead, so the controller applies it before any code sees the value:

::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from="arm = new SparkFlex(14, MotorType.kBrushless);" lines=18

The hood took a third approach, adding an offset constant in code:

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="public static final double hoodOffset" lines=3

The comment says why: the hood encoder "gets messed up." Adding `hoodOffset` by hand to every hood angle in the shot map works, but it is easy to forget once, which is exactly what happened in `redoPassingFunction` (finding **F4**). One offset, applied in one place, is safer.

## The turret: a relative encoder that must start right

The turret uses its external encoder as a **relative** sensor, and seeds its position at boot:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="turretConfig.externalEncoder.countsPerRevolution(8192);" lines=12

The last line tells the controller, "wherever the turret is right now, call it −1.6 radians." If the turret was rotated by hand before power-on, every aim and both soft limits are off by that amount, and soft limits that are wrong cannot protect the wiring.

:::danger Pit rule for this robot
Before turning the robot on, put the turret at its starting position. This is finding **F23** in the [Code Audit](course:reference/code-audit), and it belongs on the pit checklist in [The Pit Programmer Runbook](course:16-season/pit-programmer-runbook). A homing routine against a limit switch, or an absolute encoder, would remove the rule entirely.
:::

## The gyro

The **NavX** measures the robot's rotation. Two habits matter:

- **Keep the robot still while it powers on.** The gyro calibrates at startup, and motion during calibration adds drift.
- **Check the sign.** The NavX reports clockwise-positive degrees, and our code negates it for WPILib's counterclockwise-positive radians.

`GyroIOInputs` includes a `connected` flag. When it goes false, `Drive` switches to estimating rotation from wheel motion and raises an alert.

## Current is a sensor too

Every SPARK measures its motor current. A motor pushing against something it cannot move draws high current while barely turning. The intake uses exactly that to detect jams:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="if (inputs.intakeCurrent > IntakeConstants.jamCurrent" lines=6

High current alone happens briefly whenever the rollers start up, so the intake requires the condition for **50 loops in a row** (one second) before calling it a jam.

## Noisy signals

Real sensors flicker. Our code smooths them in a few ways:

- **Averaging in the controller:** `averageDepth(2)` and `uvwAverageDepth(2)` in the SPARK configs average readings before reporting them.
- **Debouncing:** a condition must hold for a while before code believes it. `ModuleIOSpark` only reports a motor as disconnected after 0.5 s of errors:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="// Connection debouncers" lines=3

- **Counting loops**, like the intake's jam counter.

Filtering trades speed for stability. A heavily filtered signal is calm but late. Choose the least filtering that stops false alarms.

## Trust, but verify

Before relying on any sensor in code, check it on the robot:

- [ ] **Direction:** move the mechanism by hand, with the robot disabled, and confirm the value moves the way positive is defined.
- [ ] **Units:** move a known amount, like one full wheel turn or 90° of turret, and confirm the change matches.
- [ ] **Zero:** put the mechanism at its reference position and confirm the reading.
- [ ] **Range:** sweep the full range of motion and watch for sudden jumps, which mean wrapping or a loose magnet.

:::exercise id="u06-encoders"
Convert raw encoder data into mechanism angles, and catch readings that should not be trusted.

- `applyZeroOffset` subtracts an offset from an absolute reading in rotations and wraps into [0, 1).
- `turretRadiansFromCounts` converts external encoder counts to turret radians with our 8192 CPR encoder and 180 : 44 gearing.
- `isJump` flags a reading that changed more than allowed, measured the short way around the 0/1 wrap.
---hint
For wrapping into [0, 1), use `value % 1.0`, then add 1.0 if the result is negative.
---hint
Counts divided by counts per revolution gives encoder rotations. Divide by the gear ratio, the encoder turns per turret turn, to get turret rotations, then multiply by 2π.
:::

:::quiz
? Which kind of encoder tells you a swerve wheel's steering angle immediately after the robot powers on?
+ An absolute encoder
- The motor's built-in relative encoder
- An external quadrature encoder counting from zero
- The NavX
> Only an absolute encoder knows its position without having watched the motion since boot.

? Why must the turret be at its starting position when the robot is turned on?
+ Its encoder position is seeded to −1.6 rad at boot, so any other starting angle offsets every aim and both soft limits
- The turret motor needs to cool down
- The NavX calibrates using the turret
- PathPlanner requires it
> A relative sensor seeded at boot is only correct if the mechanism really is where the code assumes.

? The intake current is above the jam threshold for a single loop while the rollers start spinning. What does our code do?
+ Nothing yet; it only declares a jam after 50 consecutive loops of high current and low speed
- Immediately reverses the intake
- Stops the robot
- Raises a disconnected-motor alert
> Requiring the condition over time filters out normal startup spikes.

?tf A heavily filtered sensor signal responds faster to real changes than an unfiltered one.
= false
> Filtering smooths noise at the cost of delay. Use the least filtering that prevents false alarms.

? You mounted a new absolute encoder on the intake arm. What should you check first on the robot?
+ That moving the arm by hand changes the reading in the expected direction, by the expected amount, from the expected zero
- That the code compiles
- That the Driver Station shows 12 V
- That the camera can see the arm
> Direction, units, and zero must all be confirmed before any code trusts a sensor.
:::
