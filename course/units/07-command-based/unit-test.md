---
summary: Show that you can read, predict, and change our command-based code, including scheduling, composition, bindings, joystick math, RobotContainer, and tests.
---

This test covers all of Unit 7. Several questions ask you to predict what the robot does. Work them out before choosing an answer.

:::exam Unit 7 test: command-based programming
? What does the scheduler guarantee about subsystem requirements?
+ At most one scheduled command requires each subsystem at a time
- Every subsystem always has a command running
- Commands that share a requirement run on separate threads
- Requirements only matter during autonomous
> Scheduling a command that needs a busy subsystem interrupts the command using it.

?order Put the work inside one `CommandScheduler.run()` in order.
1. Run every subsystem's `periodic()`
2. Poll triggers
3. Run scheduled commands and end the finished ones
4. Schedule default commands for subsystems nothing requires
> Subsystems read fresh inputs before any trigger or command uses them.

? The `aprilTagVision` field in `RobotContainer` is never read. Why does vision still run every loop?
+ Constructing a `SubsystemBase` registered it with the scheduler
- The Superstructure calls vision's `periodic()`
- PhotonVision runs it on the coprocessor
- `@SuppressWarnings("unused")` makes the scheduler call it
> Registration happens in the constructor.

?num The driver pulls the right trigger. How many loops after the press is detected does `Turret.periodic()` first act on `SHOOTING`?
= 2
> The Superstructure runs after the turret, so the new state reaches the turret one loop later than it reaches the Superstructure.

? `Commands.run(() -> io.setVolts(4.0))` is scheduled while another command is running the same motor. What happens?
+ Both run, because the new command declared no requirements, so the scheduler sees no conflict
- The first command is interrupted
- The new command waits until the first finishes
- WPILib throws an exception
> Requirements only protect you when commands declare them.

? In `Commands.race(a, b)`, `a` finishes after 1 s and `b` would run for 3 s. What happens?
+ The group finishes at 1 s, and `b` is interrupted
- The group finishes at 3 s
- `a` restarts until `b` finishes
- The group never finishes
> A race ends when any member finishes.

? What does `a.andThen(b).withTimeout(2.0)` limit to 2 seconds?
+ The whole sequence of `a` then `b`
- Only `b`
- Only `a`
- Nothing, because timeouts don't work on sequences
> A decorator wraps the entire expression it is called on.

?tf Two commands inside `Commands.parallel(...)` may require the same subsystem.
= false
> WPILib throws an exception when the group is built.

? The drive team wants one press to start the intake and a second press to stop it. Which binding fits?
+ `toggleOnTrue`
- `onTrue`
- `whileTrue`
- `onFalse`
> `toggleOnTrue` schedules on one press and cancels on the next.

? The driver presses the left trigger once. What is the Superstructure's wanted state ten seconds later, if no other button was pressed?
+ `IDLE`
- The state it had before the press
- `PAUSED`
- It is unknown until the trigger is pressed again
> The driver bindings use `onTrue`, so states latch.

? The rumble condition turns false while the robot is disabled. What prevents the controller from being left set to rumble?
+ Adding `.ignoringDisable(true)` to the rumble commands
- Using `whileTrue` instead of `onTrue`
- Making the Superstructure require the controller
- Calling `getRumble()` twice per loop
> Commands without `ignoringDisable(true)` are ignored while the robot is disabled.

?num The driver pushes the left stick to a magnitude of 0.75. How fast does the robot drive?
= 2.19 ± 0.02 m/s
> (0.75 − 0.1) ÷ 0.9 = 0.722. Squared, that's 0.522, and 0.522 × 4.2 = 2.19 m/s.

?num On the blue alliance, the robot's heading is 180°. `joystickDrive` asks for vx = 1 m/s field-relative and vy = 0. What robot-relative vx does the drive receive?
= -1 ± 0.01 m/s
> vx_robot = vx · cos 180° + vy · sin 180° = −1. The robot faces −X, so it drives backward to move toward +X.

? Why do the two `aprilTagVision = ...` branches pass `drive::addVisionMeasurement` instead of letting vision read the drive's pose estimator?
+ Vision pushes measurements into the drive through a narrow callback, so it never needs the whole `Drive` object
- Method references are required by PhotonVision
- `Drive` is created after vision
- The pose estimator is private and can't be read
> Narrow dependencies keep subsystems decoupled and testable.

? A named command is registered after `AutoBuilder.buildAutoChooser()`. What happens in autos that use it?
+ Each use becomes a do-nothing command, and PathPlanner warns about it
- The command runs normally
- The robot code refuses to deploy
- PathPlanner registers it automatically
> Autos are built when the chooser is built.

? What is `Constants.currentMode` when the code runs on the roboRIO?
+ `REAL`, regardless of `simMode`
- `SIM`
- Whatever `simMode` is set to
- `REPLAY`
> `RobotBase.isReal()` is true on the roboRIO.

? A JUnit test schedules a command and calls `CommandScheduler.getInstance().run()`, but the command never executes. What is the most likely cause?
+ The simulated robot is disabled
- JUnit can't load the scheduler
- The test forgot to call `robotPeriodic()`
- Commands need a real Driver Station
> Enable it with `DriverStationSim.setEnabled(true)` and `DriverStationSim.notifyNewData()`.

?? Which statements about default commands are true? Select all that apply.
+ It runs whenever no other command requires its subsystem
+ It must require its own subsystem
+ The scheduler starts it again after another command finishes
- It keeps running while the robot is disabled
> Default commands follow the same disabled rules as other commands.
:::
