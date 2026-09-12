---
summary: What happens to one module's setpoint between kinematics and the motors: optimization, cosine scaling, unit conversion, and two closed loops on the SPARKs.
objectives:
  - Explain optimization and predict when a module flips its direction
  - Explain cosine scaling and what it fixes
  - Trace a setpoint from `runSetpoint` to the motor controller
  - Read the module's inputs and know which ones to graph when something is wrong
files:
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
---

## One method, four jobs

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="public void runSetpoint(SwerveModuleState state)" lines=9 highlight="3,4,7,8"

Four things happen in those four lines, and each of them is a real decision.

## 1. Optimize: never turn more than 90 degrees

A module asked to go from 0° to 170° has a faster option: turn −10° instead and run the wheel **backwards**. The wheel ends up moving the same direction through space, and the module turned 10° instead of 170°.

`state.optimize(getAngle())` does exactly that whenever the required turn exceeds 90°: it adds half a turn to the angle and negates the speed. The rule is worth memorizing, because it explains something confusing in the logs: **a module's commanded speed can be negative even though the robot is driving forward.**

Without optimization, a swerve robot reversing direction would stop, rotate its modules half a turn, and then accelerate. With it, the modules barely move and the wheels change direction instead.

## 2. Cosine scale: don't drive sideways while you're still turning

`state.cosineScale(inputs.turnPosition)` multiplies the commanded speed by the cosine of the angle the module still has to turn.

| Remaining turn | Cosine | Commanded speed |
|---|---|---|
| 0° | 1.00 | Full |
| 30° | 0.87 | 87% |
| 60° | 0.50 | Half |
| 90° | 0.00 | Zero |

A module that is 60° away from its target angle is, for the moment, pushing the robot mostly sideways. Scaling its speed down means the robot accelerates in the direction it is actually being asked to go, and the wheels scrub less. The effect is most visible at the start of a fast direction change, which on our robot happens constantly.

## 3. Convert units

`io.setDriveVelocity(state.speedMetersPerSecond / wheelRadiusMeters)` turns meters per second into wheel radians per second, because that is the unit the drive encoder's conversion factor produces. This is the seam between WPILib's world (meters) and the controller's world (radians at the wheel), and it is one line.

## 4. Command both loops

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setTurnPosition(Rotation2d rotation)" lines=6

The turn setpoint gets the module's zero offset added back (the readings had it subtracted), then wrapped into the range the SPARK's position wrapping expects, 0 to 2π. Because wrapping is enabled on the controller, the SPARK itself takes the short way around; the code doesn't have to.

The drive setpoint goes out with its feedforward attached, as [Unit 9](course:09-controls/feedforward) covered.

## What a module reports

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="public static class ModuleIOInputs" lines=18

Ten values per module, four modules, every 20 ms. When something is wrong with the drivetrain, these are the graphs to open:

| Symptom | Graph | What you are looking for |
|---|---|---|
| Robot pulls to one side | `turnPosition` for all four | One module sitting a few degrees off the others |
| Sluggish acceleration | `driveCurrentAmps` and `driveAppliedVolts` | Hitting the current limit, or saturating at 12 V |
| A module whines but doesn't move | `turnAppliedVolts` with `turnPosition` flat | A mechanical bind or a dead encoder |
| The robot drives unevenly | `driveVelocityRadPerSec` against the setpoint | One module not tracking its command |
| Random jerks | `driveConnected` and `turnConnected` | A CAN dropout, which also raises an alert |

The connection flags come from `SparkUtil`'s sticky-fault checks with a 0.5 second debouncer, and `Module.periodic` turns them into dashboard alerts:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="// Update alerts" lines=3

A disconnected module announces itself in the pit, instead of during a match.

:::exercise id="u10-optimize"
Implement the two adjustments this lesson describes: `optimize`, which flips a module rather than turning more than 90°, and `cosineScale`, which trims speed while a module is still turning.

The tests include the details WPILib pins down: exactly 90° is not flipped, the flip wraps correctly across ±π, and the two operations compose the way `runSetpoint` uses them.
---hint
Work with the wrapped turn error: `wrapRadians(desired - current)`. Every decision in this exercise is about that one number.
---hint
Flipping means `speed → -speed` and `angle → wrapRadians(angle + PI)`. Use a strict `>` against π/2 so exactly 90° stays put.
---hint
`cosineScale` multiplies the speed by `Math.cos(error)` and leaves the angle alone. Apply it **after** optimizing, so the error is the small one.
:::

:::quiz
? A module is at 0° and is asked for 170° at 2 m/s. What does `optimize` command?
+ −10° at −2 m/s
- 170° at 2 m/s
- −10° at 2 m/s
- 190° at −2 m/s
> Turning 10° and running the wheel backwards gets the same motion with far less turning.

? Why can a module's commanded speed be negative while the robot drives forward?
+ Optimization flipped its angle by half a turn, so a negative wheel speed produces forward motion
- The encoder is inverted
- The module is on the back of the robot
- The gyro reads negative
> It is normal and expected in the logs.

?num A module still has 60° to turn. By what factor does cosine scaling reduce its commanded speed?
= 0.5 ± 0.01
> cos(60°) = 0.5. At 90° the speed goes to zero.

? What problem does cosine scaling solve?
+ A module that hasn't finished turning would push the robot in the wrong direction at full speed
- The modules would draw too much current
- The gyro would drift
- Kinematics would return the wrong angles
> It trades a little speed during transitions for motion in the right direction.

? Why does `setTurnPosition` add the zero rotation back before commanding the SPARK?
+ The readings had the offset subtracted, so the setpoint has to be converted back into raw encoder space
- To convert radians to rotations
- Because the module is inverted
- To enable position wrapping
> The offset converts between "straight ahead is 0" and what the encoder actually reports.

? The robot drives unevenly and you suspect one module. Which two signals do you graph first?
+ That module's `driveVelocityRadPerSec` against its setpoint, and its `turnPosition` against the others
- `odometryTimestamps` and `driveCurrentLimit`
- The gyro yaw and the match time
- `driveAppliedVolts` only
> Compare command against measurement, and compare the module against its peers.
:::
