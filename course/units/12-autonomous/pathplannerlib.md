---
summary: What PathPlannerLib does on the robot: the one configure call that wires it to our drivetrain, the controller that follows a trajectory, and what happens each loop while a path runs.
objectives:
  - Explain every argument of `AutoBuilder.configure`
  - Describe what the holonomic controller does each loop
  - Explain how paths are flipped for the red alliance
  - Find a path-following problem in the logs
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/RobotContainer.java
---

## One call wires everything

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="AutoBuilder.configure(" lines=10 highlight="2,3,4,5,6,7,8,9"

| Argument | Ours | What PathPlanner does with it |
|---|---|---|
| Pose supplier | `this::getPose` | Asks where the robot is, every loop |
| Pose reset | `this::setPose` | Sets the pose at the start of an auto, when `resetOdom` is true |
| Speeds supplier | `this::getChassisSpeeds` | Reads the robot's measured, robot-relative motion |
| Output | `this::runVelocity` | Sends the speeds the controller computed |
| Controller | `PPHolonomicDriveController` | Turns "where I am" and "where I should be" into chassis speeds |
| Robot config | `ppConfig` | Mass, moment of inertia, module geometry, and motor limits |
| Should flip | Alliance is red | Mirrors the path for the red alliance |
| Subsystem | `this` | The requirement every generated auto command holds |

Everything PathPlanner knows about our robot passes through those eight arguments. There is no hidden coupling, which is what makes it possible to swap in a different drivetrain by changing one file.

## The controller

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="new PPHolonomicDriveController(" lines=2

Two `PIDConstants`, both 5.0 with no I or D: one for translation and one for rotation. Every loop while a path runs, the controller:

1. Looks up the trajectory's **state at the current time**: the pose the robot should be at, and the velocity it should have.
2. Takes that velocity as a feedforward.
3. Adds a PID correction based on the difference between the target pose and the actual pose.
4. Hands the total to `runVelocity`, which is the same method the joystick command uses.

That structure is the same feedforward-plus-feedback idea from [Unit 9](course:09-controls/feedforward), one level up: the trajectory predicts, and the controller corrects.

A gain of 5.0 means one meter of position error asks for 5 m/s of correction, which saturates immediately. That is intentional here: path errors are normally centimeters, and a gain that aggressive keeps the robot pinned to its trajectory.

## Flipping for the alliance

Paths are drawn once, in the blue-origin frame, and `shouldFlip` mirrors them for red. The supplier is checked while the auto runs, not when the code boots:

```java title="From Drive's constructor"
() -> DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red
```

The `orElse(Alliance.Blue)` is the same defensive pattern as everywhere else in our code: an unknown alliance behaves like blue rather than throwing. It also means that if the Driver Station has not reported the alliance by the time autonomous starts, a red robot runs a blue path. In practice the alliance arrives well before the match, which is why this works, and it is worth knowing as a failure mode when a robot drives somewhere absurd in a practice match.

## What the logs record

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="PathPlannerLogging.setLogActivePathCallback(" lines=5

Two callbacks push PathPlanner's view of the world into our log:

| Key | What it is |
|---|---|
| `Odometry/Trajectory` | The whole path the robot is currently following, as poses |
| `Odometry/TrajectorySetpoint` | Where the robot *should* be right now |

With `Odometry/Robot` on the same Field 2D view, path problems become obvious:

| What you see | What it means |
|---|---|
| Robot tracks the setpoint closely, but the path is in the wrong place | The pose was wrong at the start: odometry reset, or the robot was placed wrong |
| Robot lags the setpoint consistently | The robot can't accelerate as hard as the path asks; check the constraints and `ppConfig` |
| Robot oscillates around the path | Translation gains too high for the loop, or module control is unhealthy |
| Trajectory appears mirrored | Alliance flip disagreed with reality |

## Where the auto command comes from

`AutoBuilder.buildAutoChooser()` reads every `.auto` file, builds a command for each, and returns a chooser. `RobotContainer` wraps it in AdvantageKit's `LoggedDashboardChooser` so the selection is logged, and `Robot.autonomousInit` schedules whatever is selected.

Because the commands are built at startup, **everything an auto references must exist by then**: paths on the robot, and named commands registered. That is the subject of the [next lesson](course:12-autonomous/named-commands-and-events).

:::info The unused constraints
`DriveConstants` also defines a `pathConstraints` object at half the robot's top speed:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final PathConstraints pathConstraints" lines=4

Nothing uses it (part of finding F20). It exists for on-the-fly pathfinding, which the [next-next lesson](course:12-autonomous/pathfinding) covers. Either wire it up or delete it.
:::

:::quiz
? What does `AutoBuilder.configure` need from the drivetrain?
+ How to read the pose and speeds, how to reset the pose, how to command speeds, a controller, the robot's physical config, an alliance flip rule, and the subsystem to require
- Only the pose supplier and the output consumer
- The list of paths
- The auto chooser
> Eight arguments, and they are the entire interface between PathPlanner and our robot.

? What does the holonomic drive controller do each loop?
+ Looks up the trajectory state for the current time, uses its velocity as feedforward, and adds a PID correction for the pose error
- Recomputes the whole path
- Drives to the next waypoint at full speed
- Uses only PID on the final pose
> Feedforward from the plan, feedback for the error.

? Our translation gain is 5.0 with no I or D. What does that mean for a 5 cm error?
+ A correction of 0.25 m/s, added to the trajectory's feedforward velocity
- A correction of 5 m/s
- Nothing, until the error exceeds a deadband
- The path restarts
> 0.05 m × 5.0. Aggressive gains keep the robot pinned to a path whose errors are normally small.

? The robot follows its setpoint closely, but the whole path is offset from the field by half a meter. What is wrong?
+ The pose was wrong when the path started
- The translation gains are too low
- The path constraints are too aggressive
- The alliance flip is inverted
> Tracking the setpoint well means the follower is fine; the frame it is following in is not.

? When is `shouldFlip` evaluated?
+ While the auto runs, by calling the supplier
- Once, when `RobotContainer` is constructed
- When the path file is saved
- At the end of autonomous
> Which is why an unreported alliance at match start is a real failure mode.

? Which keys would you put on a Field 2D view to diagnose path following?
+ `Odometry/Robot`, `Odometry/Trajectory`, and `Odometry/TrajectorySetpoint`
- `SwerveStates/Measured` and `TotalCurrent`
- `Wanted State` and `CurrentState`
- `Hub Active` and `Match Time`
> Where it is, where it should be, and the whole plan.
:::
