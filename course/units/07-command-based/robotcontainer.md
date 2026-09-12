---
summary: How RobotContainer builds the robot. It picks real, simulated, or replay IO for the current mode, wires subsystems together in a working order, registers named commands before building the auto chooser, and hands commands to Robot at each mode change.
objectives:
  - Trace startup from `Robot()` through `RobotContainer()` and explain why the order matters
  - Explain how `Constants.currentMode` selects real, simulated, or replay IO implementations
  - Explain why named commands must be registered before `AutoBuilder.buildAutoChooser()`
  - Recognize the pitfalls in our mode-transition code
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/Constants.java
---

## One class declares the robot

Command-based code is **declarative**. `Robot` stays small: its periodic method only runs the scheduler. `RobotContainer` declares the robot's structure. Its constructor does five jobs, in this order:

1. Creates every subsystem with the right IO for the current mode.
2. Creates the `Superstructure` from those subsystems.
3. Registers PathPlanner named commands.
4. Builds the auto chooser and adds characterization options.
5. Configures the controller bindings.

## Startup, step by step

The robot program starts in `Robot()`:

::source file="src/main/java/frc/robot/Robot.java" from="// Initialize URCL" lines=16 highlight="5,9,12,15"

| Step | What happens | Why it's in this position |
|---|---|---|
| 1 | Record metadata: project name, Git SHA, build date | AdvantageKit accepts metadata only before the logger starts |
| 2 | Choose log destinations for the mode | On the robot: a log file on the USB stick plus NetworkTables. In sim: NetworkTables. In replay: read a log, write a new one |
| 3 | `Logger.start()` | Logging must be running before subsystems start processing inputs |
| 4 | `new RobotContainer()` | Builds everything in this lesson |
| 5 | Set the pathfinder | `Drive` already did this (finding F11); one of the two calls can go |
| 6 | Start a web server on port 5800 | Serves the deploy folder, so Elastic can download `elastic-layout.json` |

The metadata comes from `BuildConstants`, which is out of date in our repo (finding F12). Every log records a stale Git SHA and build date until the build is set up to regenerate that file.

## Choosing IO by mode

::source file="src/main/java/frc/robot/Constants.java" from="public static final Mode simMode" lines=2

On a roboRIO, `RobotBase.isReal()` is true, so the mode is always `REAL`. On a laptop, `simMode` decides. Leave it `SIM` for physics simulation, or change it to `REPLAY` to rerun a match log through the code, which [Unit 11](course:11-logging/replay) covers.

`RobotContainer` switches on the mode and builds each subsystem with matching IO:

::source file="src/main/java/frc/robot/RobotContainer.java" from="switch (Constants.currentMode)" lines=19 highlight="13,14,17,18"

| Subsystem | `REAL` | `SIM` | `REPLAY` |
|---|---|---|---|
| Drive gyro | `GyroIONavX` | `new GyroIO() {}`: no gyro, so odometry estimates heading from the wheels | `new GyroIO() {}` |
| Drive modules | `ModuleIOSpark(0)` … `(3)` | `ModuleIOSim` | `new ModuleIO() {}` |
| Vision | Two `AprilTagIOPhotonVision` | Two `AprilTagIOSim`, which read the simulated pose | `new AprilTagIO() {}` |
| Turret | `TurretIOSpark` | `TurretIOSim` | `new TurretIO() {}` |
| Intake | `IntakeIOSpark` | `IntakeIOSim` | `new IntakeIO() {}` |

`new GyroIO() {}` is an anonymous class that keeps every default method of the interface, and those defaults do nothing. In replay, that's what you want: AdvantageKit fills every inputs object from the log file, so the IO layer must not add anything.

:::team The compiler checks the wiring
The subsystem fields are `private final`, so the Java compiler checks two things. Every field is assigned exactly once on every path through the `switch`, including `default`. And no field is used before it's assigned. Try moving the `turret = ...` line above `drive = ...`: the build fails with "variable drive might not have been initialized."
:::

## Wiring subsystems together

The constructor arguments are method references:

- `drive::addVisionMeasurement` is the `VisionConsumer`, which vision calls to push pose measurements into the drive's estimator.
- `drive::getChassisSpeeds` lets vision reject measurements taken while the robot moves or spins too fast.
- `drive::getPose` gives the turret the robot's position for aiming.
- `turret::getResetting` and `turret::shooterSpedUp` tell the intake when feeding is safe.

These references need their target objects to exist, so `Drive` is created first. `Superstructure` comes last because its constructor takes the other four subsystems:

::source file="src/main/java/frc/robot/RobotContainer.java" from="superstructure = new Superstructure(" lines=1

## Named commands before the auto chooser

::source file="src/main/java/frc/robot/RobotContainer.java" from="//Add Named Comands here" lines=6

PathPlanner autos refer to commands by name. `AutoBuilder.buildAutoChooser()` loads every `.auto` file in `deploy/pathplanner/autos` and builds each command right away. If an auto uses a name that isn't registered yet, PathPlanner prints a warning and puts in a command that does nothing. **Register named commands first, every time.** Ours are registered first:

| Name | Command |
|---|---|
| `start` | Set the Superstructure to `SHOOTING` |
| `reverse` | Set the Superstructure to `EJECTING` |

The chooser holds the nine autos in the repo: 4646 Left, 4646 right, Depot, Just Preload, NZ Depot, NZ Score, Reverse NZ Depot, Right NZ, and Right NZ Race. PathPlanner also adds "None." `LoggedDashboardChooser` is AdvantageKit's wrapper. It shows the options on the dashboard under "Auto Choices," and it logs the selection, so a replay picks the same auto.

## Characterization options share the chooser

::source file="src/main/java/frc/robot/RobotContainer.java" from="// Set up SysId routines" lines=18

Characterization routines and the Move Forward test appear in the **same chooser** as the match autos. That's convenient in the pit and risky at a competition.

:::danger Pit checklist item
Before every match, read the selected auto aloud from the dashboard. A robot that runs "Drive SysId (Dynamic Forward)" in a match drives at full voltage toward whatever is in front of it.
:::

## Handing commands to Robot

::source file="src/main/java/frc/robot/Robot.java" from="public void autonomousInit()" lines=9

::source file="src/main/java/frc/robot/Robot.java" from="public void teleopInit()" lines=10

| Transition | What `Robot` does |
|---|---|
| `autonomousInit` | Gets the chooser's selection, raises the drive current limit to 80 A, and schedules the auto |
| `teleopInit` | Lowers the limit to 30 A and cancels the auto, so an unfinished routine can't fight the driver |
| `testInit` | Cancels every command |

These methods have three problems, all described in the [code audit](course:reference/code-audit):

1. **Deprecated scheduling (F10).** `autonomousCommand.schedule()` is deprecated in WPILib 2026. Write `CommandScheduler.getInstance().schedule(autonomousCommand)` instead.
2. **Persisted configuration at every transition (F8).** `setCurrentLimit` reconfigures all four drive SPARKs with `PersistMode.kPersistParameters` ([ModuleIOSpark line 226](repo:src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java#L226)). That happens at the start of both auto and teleop, exactly when the robot must respond. Each call waits on the CAN bus and writes the controller's flash memory, which wears out with repeated writes. Apply the new limit with `PersistMode.kNoPersistParameters`. Also, PathPlanner's `ppConfig` assumes a 30 A limit while autos run at 80 A.
3. **Duplicate setup (F11).** `Pathfinding.setPathfinder` runs in both `Robot()` and `Drive()`.

## Keep RobotContainer declarative

- Put no robot logic in `Robot`'s periodic methods. `robotPeriodic()` should only run the scheduler.
- Never add a periodic method to `RobotContainer`. Behavior belongs in subsystems and commands.
- Delete commented-out bindings once the change is committed. Git history keeps the old code.
- Group bindings by controller, and keep the named-command list next to the auto chooser so both are easy to audit.

:::quiz
?order Put these startup steps in order.
1. Record log metadata
2. Start the logger
3. Create `Drive`
4. Create vision, turret, and intake
5. Create `Superstructure`
6. Register named commands
7. Build the auto chooser
8. Configure button bindings
> The logger starts before any subsystem exists. The Superstructure needs the other subsystems, and autos need their named commands.

? You run the code in simulation on a laptop without changing anything. What is `Constants.currentMode`?
+ `SIM`
- `REAL`
- `REPLAY`
- It depends on the alliance color
> `RobotBase.isReal()` is false on a laptop, so the mode is `simMode`, which is `SIM`.

? Why does `REPLAY` mode build subsystems with `new TurretIO() {}` and the other empty IO classes?
+ AdvantageKit fills the inputs from the log file, so the IO layer must do nothing
- Replay has no turret
- Empty IO runs faster than simulation
- Anonymous classes are required by PathPlanner
> Replay feeds logged inputs back through the same code.

? A new named command is registered *after* `AutoBuilder.buildAutoChooser()`. What happens in autos that use it?
+ They run a do-nothing command in its place, and PathPlanner prints a warning
- They wait until the command is registered
- The robot code crashes at startup
- The auto chooser removes those autos
> Autos are built when the chooser is built, so later registrations are too late.

?tf `teleopInit` cancels the autonomous command.
= true
> This keeps an unfinished auto from fighting the driver.

? Why is calling `configure(..., PersistMode.kPersistParameters)` in `autonomousInit` and `teleopInit` a problem?
+ It waits on the CAN bus and writes flash at the moment the robot must respond, and repeated flash writes wear out the controller's memory
- Persisted settings are erased when teleop starts
- Current limits can't be changed after the robot boots
- It changes the SPARK's CAN ID
> Apply runtime changes without persisting them.

? What should the drive team check in the auto chooser before every match?
+ That a match auto is selected, not a characterization or test routine
- That the chooser shows exactly nine options
- That "None" is selected
- That the Superstructure is `IDLE`
> Characterization routines share the chooser with match autos.
:::
