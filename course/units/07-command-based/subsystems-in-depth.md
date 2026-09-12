---
summary: What SubsystemBase does for you, what belongs inside a subsystem, how our five subsystems use periodic(), and why the order they run in matters.
objectives:
  - Explain when a subsystem is registered and when `periodic()` and `simulationPeriodic()` run
  - Decide what code belongs in a subsystem and what belongs somewhere else
  - Trace how our subsystems share data through suppliers passed to their constructors
  - Predict the loop delay that registration order adds between a button press and a mechanism moving
files:
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/RobotContainer.java
---

## A subsystem owns hardware

A subsystem is a class that owns a group of hardware that works together. The swerve modules and gyro belong to `Drive`. The flywheel, hood, and turret motors belong to `Turret`. **Nothing else sends those motors outputs.**

Ownership is what makes robot behavior traceable. If two pieces of code set the same motor in one loop, the last write wins. The motor twitches between the two values, and reading either piece of code alone won't tell you why. The scheduler's requirements only protect you when all hardware access goes through the subsystem that owns it.

## What SubsystemBase does for you

All five of our subsystems extend `SubsystemBase`:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public class Intake extends SubsystemBase" lines=1

The `SubsystemBase` constructor does two things:

1. It **registers** the subsystem with the `CommandScheduler`, so its `periodic()` runs every loop.
2. It gives the subsystem a **name**, its class name, for dashboards and loop-timing messages.

Because registration happens in the constructor, a subsystem runs as soon as you create it. That explains a puzzle in `RobotContainer`: the `aprilTagVision` field is marked `@SuppressWarnings("unused")` because no code ever reads it. It still works, because creating it registered it and the scheduler keeps calling its `periodic()`. It also means you should **create each subsystem exactly once**. A second `Intake` object would add a second `periodic()` commanding the same hardware.

| Method | When the scheduler calls it |
|---|---|
| `periodic()` | Once per loop, **in every mode, including disabled** |
| `simulationPeriodic()` | Right after `periodic()`, but only in simulation |

Because `periodic()` runs while disabled, `Drive` checks for that explicitly and stops its modules. Simulated motors don't know the robot is disabled, and clearing the setpoints means no old command is waiting when the robot enables.

Every subsystem's `periodic()` shares the 20 ms loop. When a loop runs long, the scheduler's watchdog prints how long each `periodic()` took, so a slow subsystem is easy to identify. Keep `periodic()` free of sleeps, waits, and blocking calls.

## What belongs in a subsystem

| Put it in the subsystem | Keep it somewhere else |
|---|---|
| Hardware, through the subsystem's IO object | Button bindings (`RobotContainer`) |
| Reading sensor inputs and logging them every loop | Autonomous routines (PathPlanner and command groups) |
| State that describes the mechanism: wanted state, current state, jam counters | Another subsystem's hardware |
| Safety rules that must always apply | Blocking calls like `Thread.sleep` |
| Methods other code needs, like `setWantedState` and `getTurretAngle` | Code that coordinates several mechanisms (that is the Superstructure's job) |
| Factory methods for commands that use this subsystem | |

## How our subsystems use periodic()

`Intake` shows the pattern our mechanism subsystems follow: read inputs, log them, decide the state, then act on it.

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public void periodic()" lines=6

`Turret` has the same shape. It also reads the robot pose before deciding its state:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public void periodic()" lines=11

`Drive` does more. A separate odometry thread samples the SPARKs at 100 Hz, so `periodic()` reads that data while holding a lock. Then it stops the modules if the robot is disabled, and later in the method it feeds every high-rate sample into the pose estimator:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void periodic()" lines=15 highlight="2,8,11-14"

`AprilTagVision` updates each camera's inputs, filters and combines the pose observations, then hands one combined measurement to the drive's pose estimator, all within its own `periodic()`:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="public void periodic()" lines=9

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="consumer.accept(" occurrence=2 lines=4

## Sharing data between subsystems

`Turret` needs the robot's pose and speed. `Intake` needs to know whether the turret is resetting and whether the flywheel is at speed. Neither class holds a reference to the other subsystem. Instead, `RobotContainer` passes **suppliers** to their constructors:

::source file="src/main/java/frc/robot/RobotContainer.java" from="turret = new Turret(new TurretIOSpark()" lines=2

`drive::getPose` is a `Supplier<Pose2d>`, so `Turret` calls `poseSupplier.get()` whenever it needs the current pose. This keeps the dependency narrow. `Turret` can only *read* the pose; it can't drive the robot. In a test, you can hand it `() -> new Pose2d(2.0, 4.0, new Rotation2d())` without constructing a drivetrain.

`Superstructure` is the exception. Its constructor receives the subsystem objects themselves, because its whole job is coordinating them.

:::warning Public IO fields
`Turret` and `Intake` declare their IO objects as public fields:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public TurretIO io;" lines=1

Commented-out bindings in `RobotContainer` call `turret.io.setHoodAngle(...)` directly. That skips the state machine. On the next loop, `Turret.periodic()` sends its own hood setpoint, so the hood twitches toward the button's angle for one loop and snaps back. For bench testing, the team's `TESTING` state is the better tool: it reads tunable values and still goes through `periodic()`. In new code, make IO fields `private final`.
:::

## Registration order is run order

The scheduler calls `periodic()` in the order the subsystems were registered, which is the order `RobotContainer` creates them:

1. `Drive`
2. `AprilTagVision`
3. `Turret`
4. `Intake`
5. `Superstructure`

Some of this order helps. Vision adds its measurement right after `Drive` updates odometry, so `Turret` aims with a pose that already includes this loop's camera data. `Intake` reads `turret::shooterSpedUp` after `Turret` refreshed its inputs, so the feeder decision uses fresh flywheel speed.

Some of it costs time. Trace what happens when the driver pulls the right trigger:

| Loop | What happens |
|---|---|
| N | All five `periodic()` methods run with the old states. Then the scheduler polls triggers, sees the press, and schedules the `InstantCommand`, whose `initialize()` sets the Superstructure's wanted state to `SHOOTING`. |
| N + 1 | `Turret` and `Intake` still act on their old wanted states. Then `Superstructure.periodic()` switches to `SHOOTING` and calls `turret.setWantedState(SHOOTING)` and `intake.setWantedState(INTAKING)`. |
| N + 2 | `Turret.periodic()` and `Intake.periodic()` finally act on the new states. |

The mechanisms respond **two loops (about 40 ms) after the press is detected**. That is too short for a driver to notice. It matters when you debug with logs, because a state change appears in the logs one or two loops before the motors react. `Superstructure` must come last because its constructor needs the other subsystems, so this delay is built into our design. If it ever mattered, you would measure it in the logs before changing the architecture.

:::exercise id="u07-scheduler"
Build `MiniScheduler`, a small version of WPILib's `CommandScheduler`, using the provided `MiniCommand` and `MiniSubsystem` interfaces. The rules in the class comment are WPILib's rules. Once these tests pass, you know what the real scheduler does every loop: the periodic order, requirements, interruption, and default commands.
---hint
In `schedule()`, return early if the command is already scheduled. For each of its requirements, look up the command that currently holds it and cancel that command. Then call `initialize()`, add the new command to `scheduled`, and record it as the holder of each requirement.
---hint
In `run()`, loop over a copy of the scheduled commands (`new ArrayList<>(scheduled)`), because finishing a command changes the set. Skip any command that is no longer scheduled when you reach it.
---hint
`requirements.remove(subsystem, command)` removes the entry only if that subsystem is still held by that command, which keeps you from freeing a subsystem that a newer command now holds.
:::

:::quiz
? When is a `SubsystemBase` subclass registered with the scheduler?
+ When the object is constructed
- When a command first requires it
- When `robotInit()` runs
- When it is assigned to a field in `RobotContainer`
> Registration happens in the `SubsystemBase` constructor. That's why the "unused" `aprilTagVision` field still runs every loop.

?tf A subsystem's `periodic()` keeps running while the robot is disabled.
= true
> That is why `Drive` checks `DriverStation.isDisabled()` and stops its modules.

? Which subsystem's `periodic()` runs first on our robot each loop?
+ `Drive`
- `Superstructure`
- `Turret`
- The one whose command was scheduled most recently
> The order follows construction order in `RobotContainer`, and `Drive` is created first.

?num The driver pulls the right trigger. How many loops after the press is detected does `Turret.periodic()` first act on the `SHOOTING` state?
= 2
> Loop N sets the Superstructure's wanted state. Loop N + 1 passes it to the turret after the turret already ran. Loop N + 2 acts on it.

? Why does `Turret` receive `drive::getPose` instead of the `Drive` object?
+ It only needs to read the pose, and a supplier is easy to replace with a fake pose in a test
- Java doesn't allow one subsystem to hold a reference to another
- Method references run faster than method calls
- `Drive` isn't created until after `Turret`
> A narrow dependency limits what the class can do and makes it easy to test.

? A button binding calls `turret.io.setHoodAngle(0.5)` while the Superstructure is `IDLE`. What happens?
+ The hood gets 0.5 for one loop, then `Turret.periodic()` sends the idle hood position again
- The hood stays at 0.5 until another button is pressed
- The code doesn't compile because `io` is private
- The Superstructure switches to `TESTING`
> Writing to IO directly bypasses the state machine, and the state machine writes again every loop.
:::
