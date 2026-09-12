---
summary: The command-based framework on one page: the loop, subsystems, command factories, decorators, groups, triggers, and the mistakes that cost the most time.
---

For the same material taught in order, see [Unit 7](course:07-command-based/subsystems-commands-scheduler).

## One scheduler run

```text
CommandScheduler.getInstance().run()   // called from robotPeriodic, every 20 ms
  1. subsystem.periodic() for every registered subsystem, in registration order
  2. poll triggers, scheduling and cancelling commands
  3. execute() every scheduled command, ending the finished ones
  4. schedule the default command of any subsystem nothing requires
```

Our registration order is `Drive`, `AprilTagVision`, `Turret`, `Intake`, `Superstructure`, because that
is the order `RobotContainer` constructs them.

## Subsystems

```java
public class Turret extends SubsystemBase {
  @Override
  public void periodic() { }              // every loop, in every mode, including disabled

  @Override
  public void simulationPeriodic() { }    // right after periodic, in simulation only
}
```

| Fact | Consequence |
|---|---|
| `SubsystemBase`'s constructor registers the subsystem | Creating one is enough to make it run |
| `periodic()` runs while disabled | Stop motors explicitly when that matters |
| Only one command may require a subsystem at a time | This is the framework's core guarantee |
| A default command runs when nothing else requires it | Ours drives from the joysticks |

## Command lifecycle

| Method | When |
|---|---|
| `initialize()` | Once, when scheduled |
| `execute()` | Every loop while scheduled |
| `isFinished()` | Every loop, after `execute()` |
| `end(boolean interrupted)` | Once, when finished or interrupted |

## Factories

| Factory | Runs | Finishes |
|---|---|---|
| `Commands.runOnce(action, reqs...)` | The action once, in `initialize` | Immediately |
| `Commands.run(action, reqs...)` | The action every loop | Never |
| `Commands.startEnd(start, end, reqs...)` | `start` once, `end` when it stops | Never |
| `Commands.runEnd(run, end, reqs...)` | `run` every loop, `end` when it stops | Never |
| `Commands.waitSeconds(s)` | Nothing | After the time |
| `Commands.waitUntil(condition)` | Nothing | When true |
| `Commands.none()` | Nothing | Immediately |
| `subsystem.run(action)` | Same as `Commands.run(action, subsystem)` | Never |
| `subsystem.runOnce(action)` | Same, requiring the subsystem | Immediately |

**Requirements come from the arguments.** `Commands.run(action)` with no subsystem requires nothing and
can run alongside anything, which is occasionally what you want and usually a bug.

## Decorators

| Decorator | Effect |
|---|---|
| `withTimeout(seconds)` | Interrupts after a time |
| `until(condition)` | Interrupts when the condition becomes true |
| `onlyWhile(condition)` | Interrupts when it becomes false |
| `beforeStarting(action)` | Runs an action first; used to reset stateful controllers |
| `andThen(next...)` | Runs more commands afterward |
| `alongWith(others...)` | Parallel; ends when all end |
| `raceWith(others...)` | Parallel; ends when any ends |
| `deadlineFor(others...)` | Parallel; ends when this one ends |
| `finallyDo(action)` | Runs however the command ends |
| `handleInterrupt(action)` | Runs only if interrupted |
| `unless(condition)` / `onlyIf(condition)` | Skips the command when scheduled |
| `repeatedly()` | Restarts on finish |
| `ignoringDisable(true)` | Runs while the robot is disabled |
| `withName(name)` | Names it for dashboards and logs |

A decorator applies to the expression it is called on: `a.andThen(b).withTimeout(2)` limits both;
`a.andThen(b.withTimeout(2))` limits only `b`.

## Groups

| Group | Ends when | Note |
|---|---|---|
| `Commands.sequence(a, b)` | The last ends | Requires every member's subsystems the whole time |
| `Commands.parallel(a, b)` | All end | Members may not share a requirement |
| `Commands.race(a, b)` | Any ends | The rest are interrupted |
| `Commands.deadline(a, b)` | `a` ends | The rest are interrupted |

A composed command cannot also be scheduled on its own, so build a fresh one from a factory each time
you need it.

## Triggers

```java
controller.rightTrigger().onTrue(superstructure.setWantedStateCommand(WantedState.SHOOTING));
new Trigger(superstructure::getRumble)
    .onTrue(rumbleOn)
    .onFalse(rumbleOff);
```

| Binding | Rising edge | Falling edge |
|---|---|---|
| `onTrue(cmd)` | Schedule | nothing |
| `onFalse(cmd)` | nothing | Schedule |
| `whileTrue(cmd)` | Schedule | Cancel |
| `whileFalse(cmd)` | Cancel | Schedule |
| `toggleOnTrue(cmd)` | Schedule, or cancel if running | nothing |

Combine with `and`, `or`, `negate`, and `debounce(seconds)`. Triggers are polled once per loop, so a
change shorter than 20 ms can be missed, and a button already held when a binding is created is not a
press.

## Controller inputs

```java
CommandXboxController controller = new CommandXboxController(0);
controller.a(); controller.leftBumper(); controller.start(); controller.povUp();
controller.rightTrigger();        // pressed above 0.5 by default
controller.rightTrigger(0.2);     // or a threshold you choose
controller.getLeftY();            // an axis, negative when pushed forward
controller.getHID();              // the underlying XboxController, for rumble
```

## Patterns from our robot

```java
// A state change as an instant command, which is how every driver button works.
public Command setWantedStateCommand(WantedState state) {
  return new InstantCommand(() -> setWantedState(state));
}

// A default command that reads suppliers every loop.
drive.setDefaultCommand(DriveCommands.joystickDrive(
    drive, () -> -controller.getLeftY(), () -> -controller.getLeftX(), () -> -controller.getRightX()));

// A sequence that ends with results printed however it stops.
Commands.sequence(
    Commands.runOnce(this::clearSamples),
    Commands.run(this::rampVoltage, drive).finallyDo(this::printResults));
```

## Mistakes that cost the most time

| Mistake | Symptom |
|---|---|
| A command with no requirements controlling a motor | Two commands fight; the last write wins |
| Creating bindings inside `periodic` | Thousands of bindings; one press schedules them all |
| Reusing one command object in two groups | An exception about composed commands |
| Forgetting a command runs only while enabled | Nothing happens, and no error appears |
| Expecting `whileTrue` to restart a finished command | It does not; add `repeatedly()` |
| A stateful controller that is never reset | A jump the second time a command runs |
| Assuming a state change takes effect this loop | Our Superstructure chain takes two loops |
