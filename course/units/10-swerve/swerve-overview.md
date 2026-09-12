---
summary: What swerve drive is, what our four modules are made of, the vocabulary the code uses, and the conventions everything else in this unit depends on.
objectives:
  - Describe a swerve module's two motors, two sensors, and two control loops
  - Use the words state, position, and setpoint the way WPILib does
  - Recite our drivetrain's geometry, gearing, and speed limits
  - Explain what the module zero offsets are and why each module has its own
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## Four wheels that can point anywhere

A swerve module is a wheel that can be driven at any speed **and** pointed in any direction. Four of them let a robot translate in any direction while rotating to any heading, independently. A tank drive has to point where it is going; our robot can drive sideways toward the trench while keeping the turret's side facing the HUB.

That freedom costs hardware and code. Each module has:

| Part | Ours | Job |
|---|---|---|
| Drive motor | NEO on a SPARK MAX, 5.9:1 | Spins the wheel |
| Turn motor | NEO 550 on a SPARK MAX, 18.75:1 | Points the wheel |
| Drive encoder | Built into the NEO | Wheel position and speed |
| Turn encoder | Absolute encoder on the module | Which way the wheel points, even at boot |
| Two closed loops | Both on the SPARK | Velocity for drive, position for turn |

Eight motors, eight closed loops, and four sets of calibration constants. A swerve drivetrain is more than half of the moving parts on this robot.

## The geometry

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final Translation2d[] moduleTranslations" lines=7

| Quantity | Value | Where it matters |
|---|---|---|
| Track width and wheelbase | 20 in (0.508 m) each | Kinematics; a square drivetrain simplifies everything |
| Module position | ±0.254 m in x and y | The four `Translation2d`s above |
| Drive base radius | 0.359 m | Maximum rotation speed; wheel radius characterization |
| Wheel radius | 2 in (0.0508 m) | Every conversion from wheel radians to meters |
| Max linear speed | 4.2 m/s | Desaturation, joystick scaling, PathPlanner |
| Max angular speed | 4.2 / 0.359 ≈ 11.7 rad/s | Joystick rotation scaling |

The order of those four translations is the order of everything else: **front left, front right, back left, back right**. `Drive` creates its modules in that order, kinematics returns states in that order, and the logged arrays are in that order. Get it wrong and the robot drives like it is possessed, with no error anywhere.

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="modules[0] = new Module(flModuleIO, 0);" lines=4

## Three words WPILib is strict about

| Type | Holds | Used for |
|---|---|---|
| `SwerveModuleState` | Speed (m/s) and angle | What a module should be doing **now** |
| `SwerveModulePosition` | Distance (m) and angle | How far a module has **traveled**; the input to odometry |
| `ChassisSpeeds` | vx, vy, omega | The whole robot's motion, robot-relative unless converted |

State is a velocity; position is a distance. Odometry needs positions, and control needs states. Mixing them compiles fine and produces a robot that thinks it has driven a kilometer.

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="public SwerveModulePosition getPosition()" lines=9

## Module zeros

An absolute encoder reports where the module is pointing, but its zero is wherever the magnet happened to be when it was assembled. Each module needs a constant that says "this reading means straight ahead."

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="// Zeroed rotation values" lines=6

Four different numbers, one per module, and the front right one is written as `1.603 - Math.PI` because that module reads half a turn off from the others. `ModuleIOSpark` subtracts the offset from every reading:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="new Rotation2d(value).minus(zeroRotation)" lines=1

:::danger These constants are not optional
If a module is rebuilt, its encoder is replaced, or the wheel is remounted, its zero changes. Symptoms of a wrong zero: the robot drifts sideways when you ask it to drive straight, one corner fights the others, or it turns in place when you ask it to translate. [Characterizing Swerve](course:10-swerve/characterizing-swerve) has the procedure for measuring them.
:::

## What the rest of this unit covers

| Lesson | Question it answers |
|---|---|
| [Kinematics](course:10-swerve/kinematics) | How do chassis speeds become four module states, and back again? |
| [Module Control](course:10-swerve/module-control) | How does one module get to its setpoint quickly and without spinning the long way? |
| [Odometry](course:10-swerve/odometry) | How do wheel and gyro readings become a field position? |
| [Drive Architecture](course:10-swerve/drive-architecture) | How is all of this organized, and why? |
| [Characterizing Swerve](course:10-swerve/characterizing-swerve) | What do you measure, and how often? |
| [Driving Well](course:10-swerve/driving-well) | What makes a drivetrain feel good to a driver? |

:::quiz
? What does each swerve module control, and with what?
+ Wheel speed with a drive motor in a velocity loop, and wheel direction with a turn motor in a position loop
- Both with a single motor and a differential
- Speed with the turn motor and direction with the drive motor
- Direction only; speed comes from the chassis
> Two motors, two sensors, two closed loops, four times over.

? What is the difference between `SwerveModuleState` and `SwerveModulePosition`?
+ A state holds a speed; a position holds a distance traveled
- A state is field-relative and a position is robot-relative
- A position includes the gyro angle
- They are interchangeable
> Control consumes states; odometry consumes positions.

?num Our modules sit on a 20 inch square. How far is each module from the robot's center, in meters?
= 0.359 ± 0.002 m
> hypot(0.254, 0.254). That's the drive base radius, and it sets the maximum rotation speed.

? Why does each module have its own zero rotation constant?
+ An absolute encoder's zero depends on how the magnet ended up mounted, so each module reads differently for "straight ahead"
- Because the modules are different models
- To compensate for gear backlash
- Because the front modules are mounted backwards
> The code subtracts the measured offset from each module's reading.

? The robot drifts to one side when the driver asks for straight forward, and nothing in the logs shows an error. What is the most likely cause?
+ A module zero is wrong, so one wheel is pointed slightly off from the others
- The gyro is disconnected
- The battery is low
- The deadband is too small
> Bad calibration produces a robot that fights itself while every value looks plausible.

?tf The order of the four module translations only matters for logging.
= false
> Kinematics, the module array, and the logged arrays all use the same order. Mixing it up produces motion that makes no sense.
:::
