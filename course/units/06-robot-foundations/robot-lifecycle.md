---
summary: What happens from power-on to the final buzzer, including the constructor, the 20 ms loop, init and periodic methods for each mode, what the scheduler does each cycle, and why blocking code causes loop overruns.
objectives:
  - Describe the order of Robot's constructor, robotPeriodic, and each mode's init and periodic methods
  - Explain what CommandScheduler.run() does every cycle
  - Identify loop overruns and the kinds of code that cause them, including F8 in our robot
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
---

## From power-on to running

When the roboRIO boots, it starts the Java program in our jar:

1. **`Main.main`** calls `RobotBase.startRobot(Robot::new)`.
2. **The `Robot` constructor** runs **once**. Ours sets up logging, creates `RobotContainer` (every subsystem, IO object, controller binding, and the auto chooser), registers the pathfinder, and starts the dashboard layout web server.
3. **The loop begins.** Every 20 ms, WPILib calls `robotPeriodic()` and the method for the current mode, forever.

Here is the part of our constructor that chooses where logs go in each mode:

::source file="src/main/java/frc/robot/Robot.java" from="// Set up data receivers & replay source" lines=27

On the real robot, logs go to a USB stick **and** to NetworkTables for live viewing. In simulation, only NetworkTables. In replay, the code reads a log file instead of sensors and writes a new log, and `setUseTiming(false)` runs as fast as possible.

## Modes, init, and periodic

The robot is always in one mode: **disabled**, **autonomous**, **teleoperated**, or **test**. WPILib calls:

| Method | When |
|---|---|
| `robotPeriodic()` | Every loop, in **every** mode |
| `disabledInit()`, `autonomousInit()`, `teleopInit()`, `testInit()` | **Once**, when the mode starts |
| `disabledPeriodic()`, `autonomousPeriodic()`, `teleopPeriodic()`, `testPeriodic()` | Every loop while in that mode |
| `simulationPeriodic()` | Every loop, in simulation only |

A real match goes **disabled → autonomous → disabled (a few seconds) → teleoperated → disabled**. Every transition calls the new mode's `init` method.

Our `Robot` does its mode-specific work in the init methods:

::source file="src/main/java/frc/robot/Robot.java" from="public void autonomousInit()" lines=26

- `autonomousInit` gets the chosen auto from the chooser, raises the drive current limit to 80 A, and schedules the auto.
- `teleopInit` lowers the limit back to 30 A and cancels the auto if it is still running.

Everything else happens through the command scheduler in `robotPeriodic()`, which is why the other periodic methods are empty.

## One cycle, step by step

::diagram name="robot-loop" caption="Each cycle must finish inside 20 ms. The third cycle shows what a blocking call does."

`CommandScheduler.getInstance().run()` does four things, in order, every cycle:

:::steps
1. **Runs every subsystem's `periodic()`**, such as `Drive`, `Turret`, `Intake`, `AprilTagVision`, and `Superstructure`. This is where our subsystems read inputs and run their state machines.
2. **Polls triggers**, like controller buttons and the rumble trigger, and schedules or cancels commands they are bound to.
3. **Runs scheduled commands**, calling `execute()`, checking `isFinished()`, and calling `end()` on commands that finished or were interrupted.
4. **Starts default commands** for subsystems no other command is using, like the drive's joystick command.
:::

AdvantageKit's `LoggedRobot` also records inputs before your code and writes the log after it, every cycle.

## Disabled does not mean stopped

When the robot is disabled, the roboRIO blocks motor outputs, but **your code keeps running**. Subsystem `periodic()` methods still run, sensors are still read, and odometry is still updated. `Drive` also stops its modules explicitly so nothing restarts suddenly when the robot is enabled:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Stop moving when disabled" lines=6

That is useful: vision can correct the robot's pose while it sits disabled before a match.

## Loop overruns

If a cycle's work takes longer than 20 ms, the next cycle starts late. WPILib prints a warning like *"Loop time of 0.02s overrun"* and lists how long each part took. In AdvantageScope, look at `LoggedRobot/UserCodeMS` and `LoggedRobot/FullCycleMS`.

Occasional small overruns happen. Frequent or large ones make driving feel laggy and control loops unstable. Common causes:

- **Blocking calls**: waiting for hardware to answer, sleeping, or waiting in a loop.
- **Heavy work every loop**: large logging bursts, printing to the console, file access.
- **Expensive startup work** in an init method that runs just as a match starts.

:::team In our code: finding F8
`autonomousInit()` and `teleopInit()` call `setCurrentLimit`, which reaches this method for each of the four drive modules:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setCurrentLimit(int limit)" lines=6

`configure(...)` sends the whole configuration to the SPARK and waits for it to answer, and `PersistMode.kPersistParameters` also asks it to save the settings to flash memory. Doing that four times, right at the start of autonomous and again at the start of teleop, is a recipe for a loop overrun at the worst possible moment. Saving to flash every match also wears the controller's memory. Settings changed at runtime generally should not be persisted. Finding **F8** in the [Code Audit](course:reference/code-audit) discusses fixes.
:::

:::exercise id="u06-looptiming"
Analyze loop timing and mode changes the way WPILib does.

- `LoopStats` computes cycle lengths from loop start timestamps, counts overruns, and finds the worst cycle.
- `ModeTracker` watches the Driver Station state each loop and reports which init method WPILib would call when the mode changes.
---hint
With timestamps `t`, the length of cycle `i` is `t[i + 1] - t[i]`. There is one fewer cycle length than timestamps.
---hint
In `ModeTracker.update`, figure out the current mode name first, then compare it with the mode from the previous call. Only report an init method when they differ.
:::

:::quiz
?order Put these in the order they first run after the robot powers on.
1. `Main.main` calls `RobotBase.startRobot`
2. The `Robot` constructor creates `RobotContainer`
3. `robotPeriodic()` runs for the first time
4. `autonomousInit()` runs when the match enables autonomous
> The constructor runs once before the loop starts. Init methods run when their mode begins.

? What does `robotPeriodic()` do on our robot?
+ Calls `CommandScheduler.getInstance().run()`, which runs subsystems, triggers, and commands
- Only runs during teleop
- Deploys code to the roboRIO
- Resets odometry every loop
> `robotPeriodic()` runs in every mode, and our only line in it runs the scheduler.

?tf While the robot is disabled, subsystem `periodic()` methods stop running.
= false
> Code keeps running while disabled. Only motor outputs are blocked.

? Why is calling `configure(..., kPersistParameters)` on four motors inside `teleopInit()` risky?
+ It blocks while each motor controller answers and saves to flash, likely causing a loop overrun right as teleop begins
- It resets the robot's pose
- It changes the alliance color
- It prevents autonomous from running
> Slow, blocking work in an init method lands at exactly the moment the drivers need a responsive robot.

? Which happens first inside `CommandScheduler.run()` each cycle?
+ Every registered subsystem's `periodic()` method
- Default commands are started
- Commands' `end()` methods
- Controller triggers are polled
> Subsystems run first, then triggers are polled, then commands run, then default commands are scheduled.
:::
