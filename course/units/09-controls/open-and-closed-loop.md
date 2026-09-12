---
summary: The difference between telling a motor how hard to push and telling a mechanism where to be, which of our mechanisms use each, and where each control loop actually runs.
objectives:
  - Explain open-loop and closed-loop control and when each is the right choice
  - Name the sensor, the units, and the controller location for every closed loop on our robot
  - Explain why a setpoint's units come from the encoder's conversion factor
  - Recognize the symptoms that point to a control problem rather than a logic problem
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## Two ways to command a motor

**Open loop** means you command effort and hope for the best. "Apply 6 volts." Nothing checks the result. If the robot drives up the BUMP, it slows down and the code never knows.

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setDriveOpenLoop(double output)" lines=8

**Closed loop** means you command a result and something keeps measuring and correcting until it happens. "Spin the wheel at 40 rad/s." A controller compares the measurement against the setpoint every few milliseconds and adjusts the voltage.

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setDriveVelocity(double velocityRadPerSec)" lines=10

::diagram name="control-loop" caption="The setpoint feeds a prediction (feedforward) and a correction (PID). Their sum is what the motor gets."

Open loop isn't primitive; it's the right answer when you don't care about the result or when measuring would get in the way:

| Open loop in our code | Why |
|---|---|
| `Module.stop()` | Zero volts is zero volts |
| `runCharacterization(voltage)` | Measuring how the mechanism responds to a known voltage is the whole point |
| `flywheel.set(0)` when the shot speed is 0 | Letting a heavy flywheel coast down is gentler than braking it |

## Every closed loop on our robot

| Mechanism | Controls | Sensor | Setpoint units | Loop runs on |
|---|---|---|---|---|
| Drive wheels (1, 3, 5, 7) | Velocity | Motor encoder | rad/s at the wheel | The SPARK MAX |
| Turn motors (2, 4, 6, 8) | Position | Absolute encoder | radians, wrapped at 2π | The SPARK MAX |
| Flywheel (9, with 10 following) | Velocity | Motor encoder | RPM | The SPARK Flex |
| Hood (11) | Position | Absolute encoder | rotations | The SPARK Flex |
| Turret (12) | Position | External encoder | radians | The SPARK Flex |
| Intake rollers (13, 18) | Velocity | Motor encoder | RPM | The SPARK Flex |
| Intake arm (14) | Position | Absolute encoder | rotations | The SPARK Flex |
| Feeder (15) | Velocity | Motor encoder | RPM | The SPARK Flex |
| Horizontal rollers (16, 17) | Velocity | Motor encoder | RPM | The SPARK MAX |
| Robot heading (in `joystickDriveFacingTarget`) | Position | Pose estimator | radians | The roboRIO |
| Path following (PathPlanner) | Pose | Pose estimator | meters and radians | The roboRIO |

Almost everything on this robot closes its loop **on the motor controller**. [Onboard Control](course:09-controls/onboard-control) covers what that buys you and what it costs.

## The units come from the conversion factor

A setpoint means nothing without units, and on a SPARK the units are whatever the encoder's conversion factor produces.

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double driveEncoderPositionFactor" lines=5

With a 5.9:1 reduction, one motor rotation becomes 2π/5.9 radians of wheel rotation. After that:

- `drivePositionRad` is **wheel radians**, and multiplying by the 0.0508 m wheel radius gives meters.
- `driveVelocityRadPerSec` is **wheel radians per second**.
- The PID gains, the feedforward gains, and every setpoint are all in those same units.

That is why `Module.runSetpoint` divides by the wheel radius before handing the speed to the IO layer:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="public void runSetpoint(SwerveModuleState state)" lines=9

A `SwerveModuleState` speaks meters per second, the module's closed loop speaks wheel radians per second, and this line is the translation. Change the conversion factor and every gain tuned against it is wrong. **Conversion factors and gains travel together.**

## Which layer is broken?

When a mechanism misbehaves, it helps to know which of three layers to suspect:

| Symptom | Likely layer | What to check |
|---|---|---|
| The mechanism does the wrong *thing* (intakes when it should eject) | Logic | The state machine and its inputs, in the logs |
| The right thing, but slowly, or it overshoots and oscillates | Control | Gains, feedforward, and the profile |
| The right command, but nothing moves | Hardware or configuration | CAN IDs, inversion, encoder readings, current limits, brownouts |

This unit is about the middle row. The tell is that the **setpoint in the log looks right** while the measurement doesn't follow it. Logging both is what makes that visible, which is why our IO inputs record both, for instance `turretSetAngle` next to `turretAngle`.

:::team Log the setpoint next to the measurement
Every IO inputs class in our code records both, and it costs nothing. In AdvantageScope you can drag `Turret/turretSetAngle` and `Turret/turretAngle` onto the same graph, and one glance tells you whether a problem belongs to the logic or the controller.
:::

:::quiz
? Which of these is open-loop control?
+ `driveSpark.setVoltage(6.0)`
- `driveController.setSetpoint(40.0, ControlType.kVelocity)`
- A PID controller holding the turret at −1.6 rad
- PathPlanner following a path
> Open loop commands effort; nothing measures the result.

? Why does the feedforward characterization routine use open-loop voltage commands?
+ It measures how the mechanism responds to a known voltage, which a closed loop would hide by correcting
- Closed-loop control doesn't work while disabled
- The SPARK can't run closed loop during autonomous
- Open loop is more accurate
> You can't measure a relationship while something else is actively canceling it out.

? The drive's velocity setpoint is in wheel radians per second. Where do those units come from?
+ The encoder's velocity conversion factor, which divides by the 5.9:1 reduction
- The `SwerveModuleState`, which stores radians
- WPILib requires radians for all setpoints
- The roboRIO converts units automatically
> The conversion factor sets the units for readings, setpoints, and gains alike.

? Someone changes `driveMotorReduction` from 5.9 to 6.75 after a gearbox swap. What else must change?
+ The drive PID and feedforward gains, because the units they were tuned in have changed
- Only the wheel radius
- Nothing; the conversion factor handles it
- The CAN IDs
> Gains and conversion factors are tuned together and must move together.

? In a log, `turretSetAngle` jumps to 0.8 rad and `turretAngle` slowly drifts to 0.5 rad and stops. Which layer should you suspect?
+ Control: the setpoint is right, so the loop or its gains aren't getting there
- Logic: the state machine chose the wrong state
- The Driver Station
- The Superstructure's wanted state
> Steady-state error with a correct setpoint is a controls symptom.

?? Which of these mechanisms close their loop on the motor controller rather than the roboRIO? Select all that apply.
+ The drive wheels' velocity
+ The turret's position
+ The flywheel's velocity
- The robot's heading in `joystickDriveFacingTarget`
> Only the heading and path-following controllers run on the roboRIO.
:::
