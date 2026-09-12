---
summary: How the drivetrain code is organized: one subsystem, four plain module objects, IO interfaces per device, and a PathPlanner configuration that lives in the constructor.
objectives:
  - Explain each layer of the drive and why `Module` is not a subsystem
  - Explain how `@AutoLog` inputs make simulation and replay work
  - Find where to change a constant, a behavior, or a device
  - Explain finding F9 and which configuration the robot actually uses
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## The layers

| Layer | Type | Knows about |
|---|---|---|
| `Drive` | `SubsystemBase` | Kinematics, the pose estimator, PathPlanner, all four modules, the gyro |
| `Module` | A plain class, one per corner | One module's setpoints, inputs, and alerts |
| `ModuleIO` / `GyroIO` | Interfaces with an `@AutoLog` inputs class | Nothing; they define what a device can do |
| `ModuleIOSpark`, `ModuleIOSim`, `GyroIONavX` | Implementations | The actual hardware or simulation model |

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public class Drive extends SubsystemBase" lines=8

**`Module` is deliberately not a subsystem.** Only `Drive` ever commands a module, so there is nothing for the scheduler's requirement system to protect. Making each module a subsystem would add four more `periodic()` methods to the scheduler and invite code elsewhere to command one directly, which is the exact problem requirements exist to prevent. Instead, `Drive.periodic` calls each module's `periodic` itself, inside the odometry lock:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="public void periodic()" lines=17 highlight="2,3,14-16"

## The IO layer

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="/** Updates the set of loggable inputs. */" lines=16

Every method has a default that does nothing, which is what makes `new ModuleIO() {}` a valid replay implementation. Every reading a module needs is a public field in the inputs class, and `@AutoLog` generates `ModuleIOInputsAutoLogged`, which knows how to write those fields into a log and read them back out.

That single trick is what gives us three modes from one set of logic:

| Mode | `updateInputs` comes from | Where the numbers originate |
|---|---|---|
| Real | `ModuleIOSpark` | The SPARKs and their encoders |
| Simulation | `ModuleIOSim` | `DCMotorSim` physics models |
| Replay | `new ModuleIO() {}` | The log file, injected by `Logger.processInputs` |

Because the logic above the IO layer never changes between modes, a bug seen at an event can be re-run on a laptop with the exact inputs that produced it.

## What gets logged

| Key | What it is |
|---|---|
| `Drive/Module0` … `Module3` | Every input for each module |
| `Drive/Gyro` | Gyro inputs, including the high-rate yaw samples |
| `SwerveStates/Setpoints` | What kinematics asked for, before optimization |
| `SwerveStates/SetpointsOptimized` | What the modules were actually told |
| `SwerveStates/Measured` | What the modules report doing |
| `SwerveChassisSpeeds/Setpoints` and `/Measured` | Commanded and measured chassis motion |
| `Odometry/Robot` | The pose estimate |
| `Odometry/Trajectory` and `/TrajectorySetpoint` | PathPlanner's plan, via callbacks set in the constructor |

Setpoints, optimized setpoints, and measurements as three separate keys is what lets you find where a problem entered: bad kinematics, bad optimization, or a module not tracking.

## PathPlanner lives in the constructor

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="AutoBuilder.configure(" lines=10

Five method references and a config object wire the whole autonomous system to this subsystem: where the robot thinks it is, how to reset that, what it's doing, how to command it, how to follow a path, what the robot's physical properties are, whether to flip for the red alliance, and which subsystem to require.

The physical properties come from `DriveConstants`:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final RobotConfig ppConfig" lines=12

:::warning Finding F9: two sets of drivetrain numbers
The PathPlanner GUI stores its own copy of the robot's settings in `src/main/deploy/pathplanner/settings.json`, and ours has drifted from the code: a 5.143:1 gearing instead of 5.9, a 0.546 m track width instead of 0.508, module offsets of ±0.273 m instead of ±0.254, and an 80 A current limit.

**The robot uses the code's `ppConfig`**, because that is what `AutoBuilder.configure` receives. The GUI's numbers only affect what the GUI *previews*, so paths look feasible on screen while the robot follows them differently. Fix the GUI settings to match the code, and keep them in sync whenever the drivetrain changes.
:::

## Where to change what

| You want to | Change |
|---|---|
| Retune a drive gain | `DriveConstants` (and remember gains there are read at boot) |
| Add a driver behavior | A factory in `DriveCommands`, bound in `RobotContainer` |
| Change how a module is commanded | `Module.runSetpoint` |
| Swap a motor controller or sensor | A new `ModuleIO` or `GyroIO` implementation; nothing above changes |
| Change the robot's mass or wheel COF for autos | `DriveConstants.ppConfig`, **and** the PathPlanner GUI settings |
| Add a logged value | The inputs class, which logs and replays it automatically |

:::quiz
? Why isn't `Module` a subsystem?
+ Only `Drive` commands the modules, so there is nothing for the scheduler's requirements to protect
- Subsystems can't be created in a loop
- It would break odometry
- WPILib limits a robot to five subsystems
> Requirements exist to stop two commands from fighting over hardware. Nothing else touches a module.

? What makes `new ModuleIO() {}` a valid implementation for replay?
+ Every method in the interface has a default that does nothing, and the inputs come from the log
- It inherits from `ModuleIOSpark`
- AdvantageKit generates it
- It reads from a simulated SPARK
> The IO layer is the seam where logged inputs are injected.

? Why does the code log `SwerveStates/Setpoints` and `SwerveStates/SetpointsOptimized` separately?
+ To show what kinematics asked for and what the modules were actually told, so you can tell which stage a problem came from
- One is for simulation and the other for the real robot
- PathPlanner requires both
- One is field-relative
> Three keys (requested, optimized, measured) localize a drivetrain problem quickly.

? The PathPlanner GUI says the gearing is 5.143:1 while `DriveConstants` says 5.9:1. Which does the robot use?
+ The code's value, because `AutoBuilder.configure` receives `ppConfig` from `DriveConstants`
- The GUI's value, because it is in the deploy folder
- Whichever was updated most recently
- Both, averaged
> The GUI's numbers only change what the GUI previews. That mismatch is finding F9.

? You are switching the drive motors to a different controller brand. How much of the drivetrain code changes?
+ A new `ModuleIO` implementation; `Drive`, `Module`, kinematics, and odometry are untouched
- Every file in the drive package
- `Drive` and `Module`
- Only `DriveConstants`
> That separation is the main practical benefit of the IO pattern.

? Where does `Drive` call each module's `periodic()`?
+ Inside its own `periodic()`, while holding the odometry lock
- The CommandScheduler calls them
- In `runVelocity`
- On the odometry thread
> Modules are owned by `Drive`, so `Drive` drives their loop.
:::
