---
summary: Create commands with factories, change them with decorators, and combine them into groups. The worked examples are the drive characterization routines in our code.
objectives:
  - Create commands with `Commands` and subsystem factory methods instead of new classes
  - Use decorators such as `withTimeout`, `until`, `beforeStarting`, and `finallyDo` correctly
  - Predict when `sequence`, `parallel`, `race`, and `deadline` groups finish and what they require
  - Avoid common composition bugs, including reused command objects, missing requirements, stale values, and commands that never end
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Three ways to make a command

You can create a command in three ways:

1. **Write a class** that extends `Command` and overrides the lifecycle methods.
2. **Call a factory** in `Commands`, such as `Commands.run(action, subsystem)`, which builds that class for you.
3. **Call a factory method on a subsystem**, such as `drive.run(action)`, which is shorthand for `Commands.run(action, drive)`.

Here is a class-based command. It is an example only, and its `Shooter` subsystem is not in our code:

```java title="Example: a command class"
/** Spins the flywheel up and finishes once it reaches speed. */
public class SpinUpFlywheel extends Command {
  private final Shooter shooter;
  private final double rpm;

  public SpinUpFlywheel(Shooter shooter, double rpm) {
    this.shooter = shooter;
    this.rpm = rpm;
    addRequirements(shooter);
  }

  @Override
  public void initialize() {
    shooter.setFlywheelRpm(rpm);
  }

  @Override
  public boolean isFinished() {
    return shooter.atSpeed();
  }

  @Override
  public void end(boolean interrupted) {
    if (interrupted) {
      shooter.stop();
    }
  }
}
```

The same behavior, built from factories and decorators:

```java title="Example: the same command from factories"
Command spinUp =
    shooter.runOnce(() -> shooter.setFlywheelRpm(2500))
        .andThen(Commands.waitUntil(shooter::atSpeed))
        .handleInterrupt(shooter::stop);
```

:::team Factories everywhere
Our robot code has no command classes. `DriveCommands` is a class of `static` factory methods with a private constructor, so it is never instantiated. The subsystems also provide factory methods, such as `Superstructure.setWantedStateCommand` and `Turret.changeTurretOffset`. Factories keep a command's code next to the subsystem it controls, and each call returns a fresh command object, which matters later in this lesson.
:::

## The factory toolbox

| Factory | What it does | When it finishes |
|---|---|---|
| `Commands.runOnce(action, reqs...)` or `subsystem.runOnce(action)` | Runs the action once, in `initialize()` | Immediately |
| `Commands.run(action, reqs...)` or `subsystem.run(action)` | Runs the action every loop | Never |
| `Commands.startEnd(start, end, reqs...)` | Runs `start` once, and `end` when the command stops | Never |
| `Commands.runEnd(run, end, reqs...)` | Runs `run` every loop, and `end` when the command stops | Never |
| `Commands.waitSeconds(seconds)` | Waits | After the time passes |
| `Commands.waitUntil(condition)` | Waits | When the condition is true |
| `Commands.none()` | Nothing | Immediately |
| `Commands.print(message)` | Prints a message once | Immediately |

`new InstantCommand(action)`, which the Superstructure uses, does the same thing as `Commands.runOnce(action)`.

The `reqs...` arguments are the command's requirements. **If you leave them out, the command requires nothing.** The scheduler will then let it run at the same time as another command that controls the same motors.

:::warning "Never finishes" is normal
`run`, `startEnd`, and `runEnd` commands keep going until something stops them. That can be another command that needs the same subsystem, a released `whileTrue` button, a decorator like `withTimeout`, or the robot being disabled. The joystick drive command is `Commands.run(...)` on purpose, because it should drive until something else takes over.
:::

## Decorators change a command

A decorator wraps a command and returns a new command with changed behavior:

| Decorator | Effect |
|---|---|
| `withTimeout(seconds)` | Interrupts the command after a time limit |
| `until(condition)` | Interrupts the command when the condition becomes true |
| `onlyWhile(condition)` | Interrupts the command when the condition becomes false |
| `beforeStarting(action)` | Runs an action just before the command starts |
| `andThen(next...)` | Runs more commands after this one finishes |
| `alongWith(others...)` | Runs alongside others and finishes when all of them finish |
| `raceWith(others...)` | Runs alongside others and finishes when any of them finishes |
| `deadlineFor(others...)` | Runs alongside others and finishes when this one finishes |
| `finallyDo(action)` | Runs an action however the command ends |
| `handleInterrupt(action)` | Runs an action only if the command is interrupted |
| `unless(condition)` / `onlyIf(condition)` | When scheduled, skips the command if the condition is true / false |
| `repeatedly()` | Restarts the command every time it finishes |
| `ignoringDisable(true)` | Lets the command keep running while the robot is disabled |
| `withName(name)` | Names the command for dashboards and logs |

A decorator applies only to the expression it is called on, so `a.andThen(b).withTimeout(2)` limits both commands together, while `a.andThen(b.withTimeout(2))` limits only `b`.

`joystickDriveFacingTarget` uses `beforeStarting` to reset its angle controller every time the command starts:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="// Construct command" lines=22 highlight="22"

A `ProfiledPIDController` remembers its last setpoint. Without the reset, the command would start from wherever the controller was when it last ran, and the robot could jerk toward an old heading.

## Groups run several commands

::diagram name="command-groups" caption="A and B are commands. Each group ends at the dashed line."

| Group | Factory | Decorator form | Finishes when |
|---|---|---|---|
| Sequential | `Commands.sequence(a, b, c)` | `a.andThen(b, c)` | The last command finishes |
| Parallel | `Commands.parallel(a, b)` | `a.alongWith(b)` | Every command finishes |
| Race | `Commands.race(a, b)` | `a.raceWith(b)` | Any command finishes; the others are interrupted |
| Deadline | `Commands.deadline(a, b)` | `a.deadlineFor(b)` | The first command (the deadline) finishes; the others are interrupted |

A few details matter when you time things precisely:

- A sequence moves to its next command in the same loop the current one finishes. It calls the next command's `initialize()` right away, but that command's first `execute()` waits for the next loop.
- `withTimeout(s)` is a race against `waitSeconds(s)`. `until(condition)` is a race against `waitUntil(condition)`. Both **interrupt** the command, so its `end(true)` runs.

## Worked example: feedforward characterization

This routine drives the robot in a straight line while slowly raising the voltage, then fits kS and kV (you will meet those in [Unit 9](course:09-controls/feedforward)). Its structure is a lesson in composition:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="return Commands.sequence(" lines=51 highlight="1,3,10,15,18,21,31"

Step by step:

1. **`runOnce`** clears both sample lists, so a second run doesn't mix in old data. It finishes immediately.
2. **`run(...).withTimeout(FF_START_DELAY)`** commands zero output for 2 seconds so the modules turn to face straight ahead. Here `withTimeout` is called on the `Commands.run(...)` expression, so it limits only this step.
3. **`runOnce(timer::restart)`** starts the clock at the moment the ramp begins.
4. **`run(...)`** raises the voltage at `FF_RAMP_RATE` (0.1 volts per second) and records a velocity and voltage sample every loop. It never finishes on its own.
5. **`finallyDo(...)`** fits kS and kV to the samples and prints the result. It runs however step 4 ends.

How does step 4 end? You select this routine in the auto chooser and run Autonomous mode from the Driver Station. When you press Disable, the scheduler cancels every command that doesn't run while disabled. The sequence's `end(true)` runs the `finallyDo` action, and the results appear in the Driver Station console.

Also notice what the steps require. Steps 2 and 4 pass `drive`, so the sequence requires the drive for its entire run. The joystick default command can't drive at the same time.

## Worked example: wheel radius characterization

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="return Commands.parallel(" lines=40 highlight="1,3,19,21,24,32,40"

This is a parallel group of two sequences:

- **The control sequence** resets a slew rate limiter, then spins the robot in place, ramping up to `WHEEL_RADIUS_MAX_VELOCITY`. Its `run` requires `drive`.
- **The measurement sequence** waits 1 second for the robot to settle into turning, records the starting wheel positions and heading, then adds up the heading change every loop. Its `run` requires nothing, because it only reads data.

Neither branch finishes on its own, so the group runs until you disable the robot. Then the `finallyDo` compares the distance the wheels rolled with the angle the gyro turned and calculates the wheel radius. The parallel group requires `drive` because one of its members does.

## Worked example: SysId commands

`Drive` builds its SysId commands with its own factory method `run`:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public Command sysIdQuasistatic" lines=10

`run(() -> runCharacterization(0.0))` is `Commands.run(..., this)`, so it requires the drive. It holds zero output for 1 second so the modules can orient, then `andThen` starts the routine that WPILib's `SysIdRoutine` provides. With the default settings used here, that routine stops on its own after 10 seconds unless something interrupts it first.

## Rules that prevent composition bugs

1. **A group requires every subsystem its members require, for as long as the group runs.** While `Commands.sequence(intakeStep, driveStep)` runs its intake step, the drive is already reserved, so the joystick default command can't run.
2. **Members of a parallel, race, or deadline group can't share a requirement.** WPILib throws an exception when the group is built, because two members would fight over the same motors.
3. **A command object can belong to only one composition.** After it is composed, it also can't be scheduled on its own. When you need the same behavior in two places, call the factory twice to get two objects. `setWantedStateCommand(...)` returns a new `InstantCommand` each time, so `RobotContainer` can use it for a button and a PathPlanner named command without conflict.
4. **Check when a value is read.** An expression passed to a factory is evaluated once, when the command is built. A lambda runs each time it is called. `joystickDrive` asks for the alliance *inside* its lambda, every loop, because the alliance is unknown when `RobotContainer` builds the command at boot. If you need to build a whole command at the moment it is scheduled, use `Commands.defer(() -> buildIt(), Set.of(subsystem))`.
5. **Plan how each `run` command ends.** It can end through a timeout, an `until`, a `whileTrue` binding, or by being a default command. The "Move Forward" auto option is `joystickDrive` with constant inputs, and it never finishes: it keeps driving until autonomous ends. [Joystick Driving](course:07-command-based/joystick-drive) looks at where it actually drives.
6. **Commands don't run while the robot is disabled** unless they are decorated with `ignoringDisable(true)`. Disabling the robot cancels them. Enabling it again does not restart them, except default commands, which the scheduler reschedules.

:::exercise id="u07-groups"
Build `sequence`, `parallel`, `race`, and `deadline` groups, plus `withTimeout` and `until`, for the `MiniCommand` interface from the scheduler exercise. So that the tests can check exact loop counts, timeouts here count loops instead of seconds.

Match WPILib's rules:

- A group requires every subsystem any of its members requires.
- `parallel`, `race`, and `deadline` reject members that share a subsystem.
- When a group is interrupted, it interrupts only the members that are still running.

The tests don't use a scheduler. They call each group's lifecycle methods directly, the same way the scheduler would.
---hint
Start with `sequence`. Keep an index to the current command. In `execute()`, run the current command. When it finishes, call its `end(false)`, move the index forward, and immediately call `initialize()` on the next command.
---hint
For `parallel` and `deadline`, track which members are still running with a `LinkedHashMap<MiniCommand, Boolean>`, which keeps the members in order. Only execute members whose value is `true`, and only call `end(true)` on those when the group is interrupted.
---hint
`withTimeout` and `until` need no new group logic. Race the command against a small command that finishes after the given number of `execute()` calls, or when the condition becomes true.
:::

:::quiz
? `Commands.run(() -> drive.stop(), drive)` is scheduled. When does it finish?
+ Never, until something interrupts it or a decorator ends it
- After one loop
- After 20 ms
- When the drive stops moving
> `run` commands repeat forever. Use `withTimeout` or `until`, or bind the command with `whileTrue`.

? In `feedforwardCharacterization`, what does `.withTimeout(DriveConstants.FF_START_DELAY.get())` limit?
+ Only the `Commands.run` that orients the modules, because that is the expression it is called on
- The whole sequence
- The final data-gathering `run`
- The `runOnce` that clears the samples
> A decorator wraps only the expression it is called on.

?order Put the feedforward characterization steps in order.
1. Clear the sample lists
2. Hold zero output while the modules orient
3. Restart the timer
4. Ramp the voltage and record samples
5. Compute kS and kV when the command ends
> The last step is a `finallyDo`, so it runs however the command ends. In practice, you disable the robot.

? In `Commands.parallel(a, b)`, `a` finishes after 1 s and `b` finishes after 3 s. When does the group finish?
+ After 3 s
- After 1 s
- After 4 s
- Never
> A parallel group waits for every member. A race would finish after 1 s.

? In `Commands.deadline(a, b)`, `a` finishes after 2 s and `b` never finishes. What happens at 2 s?
+ The group finishes and interrupts `b`
- The group keeps running until `b` finishes
- `a` starts again
- WPILib throws an exception
> The first argument is the deadline.

?tf While `Commands.sequence(intakeStep, driveStep)` is running its intake step, the drive's default command can still run.
= false
> A group reserves every member's subsystems for its entire run.

? Why is it useful that `setWantedStateCommand(...)` builds a new command each time it is called?
+ A command that belongs to one composition can't be used in another or scheduled on its own, so separate objects avoid conflicts
- New commands run faster
- The scheduler can only run instant commands once
- It makes the command require the Superstructure
> Factories give you a fresh object every time. That object requires nothing, because the factory passes no subsystems.

? A command is built at boot with `Commands.runOnce(() -> drive.setPose(startPose))`, and `startPose` was computed in the `RobotContainer` constructor from the alliance color. What goes wrong?
+ `startPose` was computed before the Driver Station reported the alliance, so it may be for the wrong alliance
- Lambdas can't call methods on subsystems
- `runOnce` never finishes
- `setPose` requires a second argument
> Compute alliance-dependent values when the command runs, or build the command at that moment with `Commands.defer`.
:::
