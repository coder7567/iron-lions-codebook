---
summary: How triggers turn buttons and robot conditions into command scheduling, the five binding types, and every binding on our driver and operator controllers.
objectives:
  - Explain how a `Trigger` is polled every loop and reacts to edges
  - Choose between `onTrue`, `onFalse`, `whileTrue`, `whileFalse`, and `toggleOnTrue`
  - Build triggers from robot state and combine them with `and`, `or`, `negate`, and `debounce`
  - Read our controller bindings and change them safely
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## A trigger is a condition checked every loop

A `Trigger` wraps a `BooleanSupplier`, which is anything that answers true or false. It might read a button, a sensor, or a robot state. Each loop, `CommandScheduler.run()` polls every trigger right after the subsystems' `periodic()` methods.

Each binding compares the condition with its value on the previous poll, so it can react to **edges**:

- the **rising edge**, the first loop the condition is true, such as the loop a button goes down;
- the **falling edge**, the first loop it is false again, such as the loop the button comes back up.

This has two consequences:

- **A change shorter than one loop can be missed.** If a condition turns true and back to false between two polls, no binding ever sees it. A human tap almost always lasts longer than 20 ms, but a flickering sensor can slip through.
- **A button that is already held when the binding is created is not a press.** Bindings read the condition when they are made. A trigger held while the code boots does nothing until it is released and pressed again.

## Controller triggers

`RobotContainer` creates one `CommandXboxController` per Driver Station USB slot:

::source file="src/main/java/frc/robot/RobotContainer.java" from="private final CommandXboxController controller" lines=2

A `CommandXboxController` gives you a trigger for every input:

| Method | Input |
|---|---|
| `a()`, `b()`, `x()`, `y()` | Face buttons |
| `leftBumper()`, `rightBumper()` | Bumpers |
| `back()`, `start()` | Center buttons |
| `leftStick()`, `rightStick()` | Pressing a stick down |
| `povUp()`, `povRight()`, `povDown()`, `povLeft()` | D-pad directions |
| `leftTrigger()`, `rightTrigger()` | Analog triggers; they count as pressed above 0.5, or above the value you pass, as in `rightTrigger(0.2)` |

It also reads stick axes with `getLeftX()` and similar methods. `getHID()` returns the underlying `XboxController` for things triggers can't do, such as rumble.

## The five binding types

| Binding | On the rising edge | On the falling edge | Use it for |
|---|---|---|---|
| `onTrue(cmd)` | Schedule | nothing | Latching actions: set a state, or start a routine that ends itself |
| `onFalse(cmd)` | nothing | Schedule | Cleanup when something stops, like turning rumble off |
| `whileTrue(cmd)` | Schedule | Cancel | Hold to run, release to stop |
| `whileFalse(cmd)` | Cancel | Schedule | The reverse of `whileTrue` |
| `toggleOnTrue(cmd)` | Schedule, or cancel if already running | nothing | Press once to start, press again to stop |

Two details matter often:

- If a `whileTrue` command finishes on its own while the button is still held, it does **not** restart. Use `cmd.repeatedly()` if it should.
- Binding methods return the trigger, so you can chain several bindings on the same trigger: `trigger.onTrue(a).onFalse(b)`.

## Our driver controller

::source file="src/main/java/frc/robot/RobotContainer.java" from="controller.rightTrigger().onTrue" lines=5

Every driver button uses `onTrue` with a command that sets the Superstructure's wanted state. **The states latch.** The driver presses once, and the robot keeps doing that until another button is pressed. Nothing needs to be held while driving.

| Driver input | Wanted state | What the robot does |
|---|---|---|
| Right trigger | `SHOOTING` | Inside our alliance zone, the turret aims at the HUB and spins up; elsewhere it passes. The intake deploys and runs, and the feeder waits until the flywheel is at speed. |
| Right bumper | `PAUSED` | The flywheel and intake rollers stop. Inside our alliance zone, the turret keeps aiming at the HUB. Outside it, the turret idles ([finding F1](course:08-state-machines/turret-state-machine)). |
| Left trigger | `IDLE` | The flywheel stops, and the hood and turret move to their idle positions. The intake returns to rest once the turret reports it is in the intake-safe position. |
| Left bumper | `EJECTING` | The turret keeps its shooting behavior while every intake roller reverses. |
| Start | `TESTING` | Flywheel speed and hood angle come from tunable dashboard values. The intake runs without jam detection. |

The trade-off of latching is that nothing returns the robot to `IDLE` on its own. That is one reason the robot rumbles the controller.

## A trigger made from robot state: rumble

Triggers don't have to be buttons. Any `BooleanSupplier` works:

::source file="src/main/java/frc/robot/RobotContainer.java" from="new Trigger(superstructure::getRumble)" lines=5

The condition combines three warnings:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="public boolean getRumble()" lines=3

1. The turret is `SHOOTING`, but our HUB is inactive in the current shift.
2. The intake detected a jam.
3. The turret was asked to aim into its **deadzone**, the arc it can't reach:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="public void setTurretAngle(double angle)" lines=10 highlight="3,4,6-10"

The requested angle, plus the operator's trim, is wrapped into −π to π. Anything between the 1.6 rad soft limit and 2.022 rad is flagged as deadzone. Angles past 2.022 rad are shifted by −2π so the turret reaches them from the other side.

`onTrue` turns the rumble on at the rising edge, and `onFalse` turns it off at the falling edge, so the controller rumbles exactly while the condition is true. The commands set rumble on the controller object, not on a subsystem, so they require nothing.

:::warning The disabled edge case
Unless a command is decorated with `ignoringDisable(true)`, the scheduler ignores it while the robot is disabled. The rumble commands are plain `InstantCommand`s. If the condition turns false while the robot is disabled, for example when an intake jam clears after the match ends, the scheduler skips the rumble-off command, and the controller is left set to rumble. Adding `.ignoringDisable(true)` to both rumble commands fixes this.
:::

## Our operator controller: turret trim

::source file="src/main/java/frc/robot/RobotContainer.java" from="adjController.leftBumper()" lines=2

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public Command changeTurretOffset" lines=3

Each bumper press adds ±0.05 rad (about 2.9°) to an offset that `TurretIOSpark` adds to every turret setpoint. The operator uses it to correct aim during a match. The offset appears in the logs as `turretOffset` in the turret inputs, and it resets to its starting value whenever the robot code restarts.

## Combining triggers

Triggers have methods that build new triggers:

| Method | New trigger is true when |
|---|---|
| `a.and(b)` | Both are true |
| `a.or(b)` | Either is true |
| `a.negate()` | `a` is false |
| `a.debounce(seconds)` | `a` has stayed true for `seconds` (it turns false immediately) |

These are examples, not code from our robot:

```java title="Examples: combined triggers"
// Feed only while the driver holds the trigger AND the flywheel is at speed.
controller.rightTrigger().and(turret::shooterSpedUp).whileTrue(feedCommand);

// Ignore current spikes shorter than a quarter second before warning about a jam.
new Trigger(intake::getRumble).debounce(0.25).onTrue(flashLedsCommand);
```

## Rules for bindings

1. **Create bindings once**, in `configureButtonBindings()`. A binding created inside a `periodic()` method adds another binding every loop. After one minute there are 3,000 of them, and one press schedules 3,000 commands.
2. **Give every binding its own command object.** Factories like `setWantedStateCommand` make this automatic.
3. **Match the binding to the command.** Hold-to-run needs `whileTrue` with a command that doesn't finish on its own. A latching state change needs `onTrue` with an instant command.
4. **Check controller order before every match.** `RobotContainer` expects the driver in USB slot 0 and the operator in slot 1.

:::exercise id="u07-bindings"
Implement `ButtonBindings`, which models how WPILib's `Trigger` binds commands. Each binding remembers the condition's value from the previous poll and acts only on edges. As in WPILib, a binding reads the condition when it is created, so a button already held at that moment doesn't count as a press.
---hint
Store each binding as a small object with its own `previous` field, initialized with `condition.getAsBoolean()` when the binding is added. `poll()` asks each binding to read the condition, compare, act, and save the new value.
---hint
A rising edge is `!previous && current`. A falling edge is `previous && !current`.
---hint
`toggleOnTrue` checks `target.isScheduled()` on each rising edge. It cancels a running target and schedules one that isn't running, which also restarts a command that already finished on its own.
:::

:::quiz
? The drive team wants the intake to run only while the operator holds A. Which binding fits?
+ `whileTrue` with a command that runs until it's canceled
- `onTrue` with an `InstantCommand`
- `onFalse` with a command that runs until it's canceled
- `toggleOnTrue` with an `InstantCommand`
> `whileTrue` schedules on press and cancels on release.

? The driver presses and releases the right bumper during teleop. What is the Superstructure's wanted state afterward?
+ `PAUSED`, until another button is pressed
- Back to whatever it was before the press
- `IDLE`, because the button was released
- `SHOOTING`
> `onTrue` bindings that set a wanted state latch.

?tf Holding the right trigger while the robot code boots makes the robot start `SHOOTING` on the first loop.
= false
> Bindings read the condition when they are created, so a held button isn't a rising edge.

? A beam-break sensor blinks true for 5 ms, between two scheduler loops. What does a trigger bound with `onTrue` do?
+ Nothing, because no poll ever saw the condition as true
- Schedules the command on the next loop
- Schedules the command twice
- Throws an exception
> Triggers sample once per loop, so events shorter than 20 ms can be missed.

?num The operator presses the left bumper 3 times. How much trim, in radians, was added to the turret?
= 0.15 ± 0.001 rad
> Each press adds `turretOffsetChange`, which is 0.05 rad.

?? Which situations make the driver's controller rumble? Select all that apply.
+ The turret is shooting while our HUB is inactive
+ The intake is jammed
+ The turret was asked to aim into its deadzone
- The robot is in the alliance zone
- The flywheel is at speed
> These are the three terms of `Superstructure.getRumble()`.

? Why don't the rumble commands need a subsystem requirement?
+ They set rumble on the controller object, which no subsystem owns, so they can't conflict with any subsystem's commands
- Commands with requirements can't be bound to triggers
- `InstantCommand` can't have requirements
- The Superstructure requires them automatically
> Requirements protect hardware that subsystems own. Controller rumble isn't subsystem hardware.
:::
