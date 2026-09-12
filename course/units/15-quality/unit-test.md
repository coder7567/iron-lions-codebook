---
summary: Show that you can decide what to test, design code with seams, review a change against a checklist, protect the loop, and refactor without breaking the robot.
---

This test covers all of Unit 15.

:::exam Unit 15 test: code quality
? What is already true about testing in the team repository?
+ JUnit 5 and the test task are configured; there are simply no tests yet
- Tests exist but are excluded from the build
- WPILib does not support unit tests
- Tests need a robot connected
> The setup cost has already been paid.

? Which of these is most worth a unit test?
+ The HUB shift schedule, with its six branches and two alliances
- That `withTimeout` interrupts a command
- The flywheel's kP value
- Whether shots go in from 4 meters
> Rules with edge cases and pure logic pay for themselves fastest.

? Why assert against a named constant rather than the literal value it holds?
+ The test keeps describing behavior after someone retunes the constant
- Literals are forbidden in tests
- It runs faster
- JUnit requires it
> A test that hard-codes a tuning value becomes a lie the first time it changes.

? A test asserts the value of a private counter field. What is wrong with it?
+ It tests the implementation, so a refactor breaks it even when behavior is unchanged
- Private fields cannot be read from tests
- It will be flaky
- Nothing; it is precise
> Tests that break during improvements train a team to delete tests.

? What is a seam?
+ A place where a test can substitute something simple for something real
- A comment marking untested code
- The boundary between two subsystems
- A gap in coverage
> Testability is a property of the code, not of the tests.

? Which is usually the smallest change that makes logic testable?
+ Take the value as a parameter instead of reading it from the environment
- Extract an interface
- Add a mocking framework
- Make the class final
> The caller reads the environment; the logic does arithmetic.

? Why does taking time as a parameter matter?
+ A test can replay minutes of timeline instantly and deterministically
- Clocks are inaccurate on the roboRIO
- It reduces CPU use
- WPILib requires it
> Real time in a test means a slow test or a flaky one.

? Which seams already exist in our robot code?
+ The IO interfaces, the suppliers passed into `Turret` and `Intake`, and `Intake`'s one-argument constructor
- The `CommandScheduler` singleton
- `Superstructure`'s current-averaging block
- The static calls to `DriverStation`
> The last two are the lesson's counterexamples.

? Which review checklist question would have caught finding F1?
+ "Can every state and branch actually happen?"
- "Are the units right?"
- "Does anything block in the loop?"
- "Was commented-out code left behind?"
> One sentence, ten seconds.

? What are the three parts of a useful review comment?
+ The code path, the effect on the robot, and a question
- Severity, line number, and a suggested fix
- Praise, criticism, praise
- The rule, the standard, and a link
> The question leaves room for the author to know something you don't.

? At an event, someone proposes a one-line fix they cannot explain. What happens?
+ It does not get deployed
- Deploy it; one line is low risk
- Deploy it and watch the next match
- Let the drive team decide
> A change nobody can explain has not been understood.

? What happens when a robot loop takes longer than 20 ms?
+ The next loop starts late, and the watchdog prints how long each part took
- The robot disables itself
- WPILib skips a loop
- The scheduler drops the slowest command
> Those per-subsystem epochs are the fastest way to find the cause.

? Which is the most expensive thing our code does inside a loop?
+ Reconfiguring motor controllers and persisting settings to flash
- Swerve kinematics
- Shot map interpolation
- Angle wrapping
> Most loop-time problems are CAN and flash, not arithmetic.

? Overruns appear only at the start of autonomous and teleop. What does that point to?
+ Work done at mode transitions, such as reconfiguring the drive SPARKs
- Garbage collection
- A slow vision pipeline
- A disconnected module
> That correlation identifies finding F8 without reading code.

? What makes a change a refactor rather than a bug fix?
+ The robot behaves identically afterward
- The diff is small
- No tests changed
- A reviewer suggested it
> Which is also how you verify it.

? Why write a characterization test before refactoring?
+ It records what the code does today, bugs included, so you can prove the refactor changed nothing
- It documents intended behavior
- It is required before merging
- It speeds up the refactor
> The pure-logic exercises in this course are exactly that kind of test.

? Which finding gives the most value for the least risk?
+ F14, clearing vision inputs when a camera disconnects
- F13, the fusion weighting
- F5, the simulated interlock
- F20, deleting dead code
> An `else` branch and an initializer, and it stops a stale pose from being repeated.

? Why is a typo in a log key worse than a typo in a field name?
+ It silently breaks dashboard bindings and log comparisons, with no error anywhere
- The compiler rejects it
- It makes logs larger
- NetworkTables refuses to publish it
> A field typo is cosmetic; a key typo breaks a contract with another system.

?? Which comments are worth keeping in our repository? Select all that apply.
+ The note explaining that the alliance-aware getters exist because data taken on 2/7 was wrong
+ The four-step procedure for measuring the reality constant
+ The two earlier shot map tables, kept as dated calibration history
- `/** Add your docs here. */`
> Comments should answer why; the code already shows what.
:::
