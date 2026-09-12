---
summary: The three ideas behind WPILib's command-based framework (subsystems, commands, and the scheduler), and how our robot's state-machine design uses them.
objectives:
  - Explain what subsystems, commands, and the command scheduler each do
  - Describe a command's lifecycle, requirements, interruption, and default commands
  - Compare classic command-heavy design with our state-machine subsystems and thin commands
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Why a framework

A robot has several mechanisms, each with several behaviors, triggered by buttons, timers, sensors, and autonomous routines, all running in a 20 ms loop. Without structure, robot code becomes a tangle of `if` statements in `teleopPeriodic()`.

WPILib's **command-based** framework organizes that work around three ideas:

| Idea | Is | In our code |
|---|---|---|
| **Subsystem** | Owns a piece of hardware and its state | `Drive`, `Turret`, `Intake`, `AprilTagVision`, `Superstructure` |
| **Command** | An action that uses one or more subsystems | The joystick drive command, "set wanted state to SHOOTING," each autonomous routine |
| **Scheduler** | Runs subsystems and commands every loop and enforces the rules | `CommandScheduler.getInstance().run()` in `robotPeriodic()` |

It is called **declarative**: `RobotContainer` declares what should happen ("when the right trigger is pressed, start shooting"), and the scheduler makes it happen at the right time.

## The scheduler runs everything

The whole framework runs from one line:

::source file="src/main/java/frc/robot/Robot.java" from="public void robotPeriodic()" lines=15

Each call runs every subsystem's `periodic()`, checks triggers, advances running commands, and starts default commands, in that order. Remove this line and nothing in the command framework ever runs.

## The command lifecycle

A command is an object with four methods that the scheduler calls:

::diagram name="command-lifecycle" caption="The scheduler calls these methods. You rarely call them yourself."

| Method | Called | Typical use |
|---|---|---|
| `initialize()` | Once, when the command starts | Reset timers, capture a starting position |
| `execute()` | Every loop while running | Send motor commands |
| `isFinished()` | Every loop, after `execute()` | Return true when the job is done |
| `end(boolean interrupted)` | Once, when finishing or interrupted | Stop motors, clean up |

## Requirements: one command per subsystem

A command lists the subsystems it **requires**. The scheduler guarantees **at most one running command per subsystem**. When a new command needs a subsystem that is busy, the old command is **interrupted**: its `end(true)` runs, and the new one starts.

This is what stops two commands from fighting over the same motors. A "drive to the HUB" command and the joystick command can never both control the drive at once.

## Default commands

A **default command** runs whenever no other command requires its subsystem. Our drive's default command reads the joysticks:

::source file="src/main/java/frc/robot/RobotContainer.java" from="drive.setDefaultCommand(" lines=6

When an autonomous path takes over the drive, the joystick command is interrupted. When the path ends, the scheduler restarts the default command automatically.

## Two ways to build a robot

**Classic command-based design** puts behavior in commands. An `IntakeCommand` would spin the rollers in `execute()` and stop them in `end()`, and holding a button would run it.

**Our 2026 robot** mostly puts behavior in **subsystem state machines**. `Turret`, `Intake`, and `Superstructure` decide what to do in their own `periodic()` methods, based on a *wanted state*. Commands are tiny: they just change the wanted state and finish immediately.

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="public Command setWantedStateCommand(WantedState wantedState)" lines=3

`InstantCommand` runs its action in `initialize()`, and `isFinished()` returns true right away. So pressing the right trigger starts a command that sets `SHOOTING` and ends in the same loop, and from then on the Superstructure's `periodic()` does the work.

| | Behavior in commands | Behavior in subsystem state machines (ours) |
|---|---|---|
| Where logic lives | `execute()` and `end()` of many commands | `updateState()` and `applyState()` in each subsystem |
| Coordinating mechanisms | Command groups and requirements | One `Superstructure` state decides turret and intake together |
| What the robot is doing | Whichever commands are scheduled | A logged `CurrentState` for every subsystem |
| Autonomous | Compose commands | Named commands set wanted states while paths drive |

Neither design is simply better. Ours makes robot state explicit and easy to see in logs, and it made shoot-while-driving autos straightforward. The trade-off: these instant commands declare no requirements, so the scheduler's interruption rules no longer protect the turret and intake. The state machine has to handle every combination itself, which is why [Unit 8](course:08-state-machines/state-machine-basics) spends a whole unit on it.

:::quiz
? What does the command scheduler guarantee about subsystems?
+ At most one running command requires each subsystem at a time
- Every subsystem always has exactly two commands
- Subsystems run only when a button is pressed
- Commands never interrupt each other
> Scheduling a command that needs a busy subsystem interrupts the one using it.

?order Put a command's lifecycle calls in order, for a command that finishes on its own.
1. `initialize()`
2. `execute()`
3. `isFinished()` returns true
4. `end(false)`
> `execute()` and `isFinished()` repeat every loop until the command finishes.

? When does the drive's default joystick command run?
+ Whenever no other command requires the drive
- Only during autonomous
- Only once at startup
- Only while a button is held
> Default commands fill in whenever their subsystem is free, and restart automatically after an interruption.

? What happens after the driver presses the right trigger on our robot?
+ An `InstantCommand` sets the Superstructure's wanted state to SHOOTING and ends immediately; the Superstructure's `periodic()` does the work from then on
- A long-running shooting command runs until the trigger is released
- The turret subsystem is deleted and recreated
- The scheduler pauses the drive
> Our design keeps behavior in subsystem state machines and uses commands to change wanted states.

?tf If `CommandScheduler.getInstance().run()` were removed from `robotPeriodic()`, subsystem `periodic()` methods would still run.
= false
> The scheduler is what calls every subsystem's `periodic()`. Without it, the command framework does nothing.
:::
