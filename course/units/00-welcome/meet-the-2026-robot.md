---
summary: A software-eye tour of the 2026 REBUILT robot, covering every mechanism, motor, sensor, and control and where each one lives in code.
objectives:
  - Name the robot's mechanisms and the subsystem class that controls each
  - Read the CAN ID map and explain why every ID must be unique
  - Describe what each driver and operator control does
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/RobotContainer.java
---

## The robot in one breath

The 2026 robot is a **swerve drive** carrying a **turret shooter** and a **deploying intake**. Two **AprilTag cameras** and a **NavX gyro** tell it where it is on the field. The intake gathers FUEL, rollers move it to a feeder, and the turret spins to aim at the HUB while the flywheel launches the FUEL and the hood sets the shot angle.

From software's point of view, the robot is a set of **motors to command** and **sensors to read**. Every one of them talks to the roboRIO over the CAN bus, or through a port on the roboRIO itself.

::diagram name="robot-top-view" caption="Top view. Module positions and camera transforms come from `DriveConstants` and `VisionConstants`; mechanism placement is simplified."

## Swerve drive

Each corner has a **swerve module**: one motor spins the wheel (the *drive* motor) and another rotates the whole wheel assembly to point it (the *turn* motor). Because every wheel can point any direction, the robot can drive sideways while facing the HUB.

- **Drive motors:** NEO motors on SPARK MAX controllers, geared 5.9 : 1 to 4-inch wheels.
- **Turn motors:** NEO 550 motors on SPARK MAX controllers, with an absolute encoder that reports each wheel's angle.
- **Top speed in code:** 4.2 m/s, about 9.4 mph.

These numbers are constants you will use for real math later:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double maxSpeedMetersPerSec" lines=12

The four `Translation2d` values place each module 10 inches from the center in X and Y. [Unit 10](course:10-swerve/kinematics) turns them into wheel speeds.

## Turret shooter

The shooter lives in the `turret` package and has three mechanisms:

| Mechanism | Hardware | What the code sets |
|---|---|---|
| Flywheel | Two SPARK Flex controllers; CAN 10 follows CAN 9 | Speed in RPM, from a distance-based shot map |
| Hood | SPARK Flex, CAN 11, with an absolute encoder | Angle, which changes the shot's arc |
| Turret | SPARK Flex, CAN 12, with an 8192-count external encoder | Angle toward the target, within soft limits |

The turret can rotate from −4.261 rad to 1.6 rad, about 336° of travel. It cannot spin forever, because wires run through it. [Unit 14](course:14-shooting/turret-aiming) shows how the code handles that limit.

## Intake and indexing

The `intake` package runs five jobs with six motors:

- **Arm** (SPARK Flex, CAN 14) swings the intake out to 0.77 and back to a resting 0.15, measured by an absolute encoder.
- **Intake rollers** (SPARK Flex, CAN 13, with CAN 18 following) grab FUEL.
- **Two horizontal rollers** (SPARK MAX, CAN 16 and 17) move FUEL toward the shooter.
- **Feeder** (SPARK Flex, CAN 15) pushes FUEL into the flywheel, but only once the flywheel is up to speed.

If the roller motor draws a lot of current while barely turning, FUEL is probably jammed. The intake then briefly reverses and signals the driver with controller rumble. [Unit 8](course:08-state-machines/intake-state-machine) walks through that logic.

## Sensors

- **NavX gyro**, on the roboRIO's MXP port, measures the robot's heading (which way it faces).
- **Two PhotonVision cameras**, `April_Tag_1` and `April_Tag_2`, sit at the back corners, 9.3 inches up, tilted up 30° and angled 135° outward. They see AprilTags on the field and estimate the robot's position.
- **Encoders** in every motor and on the steering, hood, arm, and turret report positions and speeds.
- **Current sensing** in every motor controller reports how hard each motor is working.

:::note Why a gyro if the wheels have encoders?
Wheel encoders can estimate how the robot turned, but wheels slip, especially when another robot pushes you. The gyro measures heading directly. If the gyro disconnects, our code falls back to estimating from the wheels and shows an alert.
:::

## The CAN map

Every SPARK on the CAN bus needs a **unique ID**. If two SPARKs share an ID, the roboRIO cannot tell them apart and both misbehave. (Devices of different kinds, like the power distribution hub and a SPARK, may reuse a number.) IDs are set with the REV Hardware Client and must match the numbers in code.

| CAN ID | Device | Controller | Set in |
|---|---|---|---|
| 1, 2 | Front-left drive, turn | SPARK MAX | `DriveConstants` |
| 3, 4 | Back-left drive, turn | SPARK MAX | `DriveConstants` |
| 5, 6 | Back-right drive, turn | SPARK MAX | `DriveConstants` |
| 7, 8 | Front-right drive, turn | SPARK MAX | `DriveConstants` |
| 9 | Flywheel leader | SPARK Flex | `TurretIOSpark` |
| 10 | Flywheel follower | SPARK Flex | `TurretIOSpark` |
| 11 | Hood | SPARK Flex | `TurretIOSpark` |
| 12 | Turret | SPARK Flex | `TurretIOSpark` |
| 13 | Intake rollers | SPARK Flex | `IntakeIOSpark` |
| 14 | Intake arm | SPARK Flex | `IntakeIOSpark` |
| 15 | Feeder | SPARK Flex | `IntakeIOSpark` |
| 16, 17 | Horizontal rollers 1, 2 | SPARK MAX | `IntakeIOSpark` |
| 18 | Intake roller follower | SPARK Flex | `IntakeIOSpark` |

:::team In our code
Drive IDs live in named constants, but the mechanism IDs are typed directly into constructors, like `new SparkFlex(12, MotorType.kBrushless)`. Numbers without names are called *magic numbers*. In [Unit 3](course:03-java-objects/encapsulation-static-final) you will see why teams move them into constants.
:::

## How the drive team controls it

The driver uses an Xbox controller on Driver Station port 0. A second controller on port 1 lets the operator nudge the turret's aim.

::source file="src/main/java/frc/robot/RobotContainer.java" from="drive.setDefaultCommand(" to="adjController.rightBumper()"

| Control | What it does |
|---|---|
| Left stick | Drive, field-relative, so pushing forward always moves away from your alliance wall |
| Right stick left/right | Rotate the robot |
| Right trigger | Superstructure **SHOOTING**: aim at the HUB in the alliance zone, otherwise pass |
| Right bumper | **PAUSED**: stop feeding and rollers, keep aiming |
| Left trigger | **IDLE**: stop the shooter and stow |
| Left bumper | **EJECTING**: run the intake backward to clear FUEL |
| Start | **TESTING**: tuning mode with dashboard-set shooter values |
| Operator bumpers | Shift the turret aim by ±0.05 rad |
| Rumble | Shooting while our HUB is inactive, a jam, or a target the turret cannot reach |

The lines commented out with `//` are leftovers from bench testing. The compiler ignores them.

:::quiz
? Which CAN ID is the turret rotation motor?
- 9
- 11
+ 12
- 14
> `TurretIOSpark` creates the turret with `new SparkFlex(12, MotorType.kBrushless)`. CAN 9 and 10 are the flywheel, 11 is the hood, and 14 is the intake arm.

?tf The code sends separate speed commands to both flywheel motors.
= false
> CAN 10 is configured with `.follow(flywheel, true)`. It copies whatever CAN 9 does, inverted because it faces the other way. The code only commands CAN 9.

? What happens when the driver pulls the right trigger?
- The flywheel spins, but only while the trigger is held
+ The Superstructure's wanted state becomes SHOOTING and stays that way until another button changes it
- The robot drives to the HUB automatically
- The intake reverses to clear a jam
> `controller.rightTrigger().onTrue(...)` runs a command once when the trigger is pressed. The command sets the wanted state to SHOOTING, and the state stays until another button sets a different one.

?num The swerve modules sit on a 20-inch square. How many meters is 20 inches? (1 inch = 0.0254 m)
= 0.508 ± 0.001 m
> 20 × 0.0254 = 0.508 m. The code does this conversion with `Units.inchesToMeters(20)`.

? Why does the robot use a gyro when every wheel already has an encoder?
- Encoders cannot measure speed
+ Wheels slip, so heading estimated from wheels drifts, while the gyro measures heading directly
- The gyro controls the turn motors
- PathPlanner refuses to run without a NavX
> Heading from wheel motion goes wrong whenever wheels slip. The NavX measures rotation directly, and the code only falls back to wheel-based heading if the gyro disconnects.
:::
