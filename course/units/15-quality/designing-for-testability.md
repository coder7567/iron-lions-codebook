---
summary: Testable code is code with seams: places where a test can substitute something simple for something real. Our robot has three good seams and a few places that need one.
objectives:
  - Identify the seams in our code and what each one enables
  - Recognize the patterns that make code untestable
  - Inject time, hardware, and Driver Station data instead of reaching for them
  - Rewrite the current-averaging block as testable code
files:
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## A seam is a place you can substitute

Testability is not a property of a test. It is a property of the code: can you run this logic while giving it inputs you choose? Every place where you can is a **seam**.

Our robot has three good ones.

**IO interfaces.** Every subsystem talks to hardware through an interface whose methods do nothing by default, so a test can implement it in five lines and report whatever it likes. This is the strongest seam in the codebase, and it exists because AdvantageKit's replay needs it. Testability came along for free.

**Constructor suppliers.** `Turret` takes a `Supplier<Pose2d>` rather than a `Drive`:

```java title="From RobotContainer"
turret = new Turret(new TurretIOSpark(), drive::getPose, drive::getChassisSpeeds);
```

A test can hand it `() -> new Pose2d(2.0, 4.0, new Rotation2d())` and check what the turret decides from that position, with no drivetrain in the picture.

**A convenience constructor.**

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public Intake(IntakeIO io) {" lines=3

Whoever wrote that was thinking about the caller. It fills in "the turret is not resetting" and "the flywheel is at speed" so an intake test does not need a turret at all.

## The patterns that block a test

| Pattern | Why it blocks | Where it appears |
|---|---|---|
| Static calls to the environment | You cannot make `DriverStation.getAlliance()` return red in a test | `Superstructure.periodic`, `Turret.isPastLine`, `chooseTargetBasedOnY` |
| Reading a clock inside the logic | The test would have to wait, or the result depends on the machine | `Superstructure`'s current averaging |
| Deciding and acting in one method | Calling it moves motors | Anything that mixed `updateState` with `applyState` |
| Values computed in a constructor from the environment | The object is built before the environment is known | The turret's passing map at boot |
| Hidden global state | One test changes what the next test sees | The `CommandScheduler` singleton |

None of these makes code wrong. They make code untestable, which is a different problem with the same long-term cost: the only way to find out whether it works is to try it on a robot.

## Four moves that create seams

:::steps
1. **Take the value as a parameter.** The most common fix, and the smallest. A method that reads `DriverStation.getAlliance()` becomes a method that takes `boolean isRed`. The caller reads the environment; the logic does arithmetic.
2. **Take time as a parameter.** `addSample(timestampSeconds, amps)` instead of calling a clock. A test then owns the timeline: one second of samples takes no time at all to run.
3. **Take an interface.** When the substitution is more than a value, define the smallest interface that covers what the logic needs. `IntakeIO` is that pattern, and it needs no framework.
4. **Return a value instead of setting a field.** A method that returns a record can be tested by calling it. A method that mutates three fields and returns a fourth can only be tested by inspecting the object afterward, and it invites the stale-field bugs of finding F19.
:::

## The worked example

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="//current averaging" lines=23

Every problem in this lesson is in those twenty lines. It reads a clock it created, mutates five fields, compares two timestamps taken one loop apart, and logs from inside the mutation. It cannot be tested, and it has never worked (finding F2).

The rewrite is a class with no clock and no fields anybody else can see:

```java title="The same job, with a seam"
CurrentAverager averager = new CurrentAverager(1.0);   // one second window

// in periodic:
averager.addSample(Timer.getFPGATimestamp(), getTotalCurrent());
Logger.recordOutput("Current/Average", averager.average());
Logger.recordOutput("Current/Peak", averager.peak());
```

The subsystem still reads the clock, because that is a subsystem's job. The **logic** does not, so a test can drive it through a two minute match in a millisecond, including the case that broke the original: samples arriving every 20 ms.

:::exercise id="u15-averager"
Write that averager: a rolling window of current samples where time arrives as a parameter, with an average, a peak, and a reset.

The last test runs a realistic 20 ms loop for four seconds and checks that a one second window holds the right number of samples, which is exactly the case the original code got wrong.
---hint
An `ArrayDeque` of samples gives you cheap adds at the end and cheap removes at the front, which is all a sliding window needs.
---hint
Evict after adding, comparing each old sample against the **new** sample's timestamp. A sample exactly one window old is still inside the window.
---hint
Both `average()` and `peak()` have to answer for an empty window without dividing by zero or returning a stale value.
:::

:::quiz
? What is a seam?
+ A place where a test can substitute something simple for something real
- A comment marking code that needs tests
- The boundary between two subsystems
- A gap in test coverage
> Testability is a property of the code, not of the tests.

? Why is `Turret` taking a `Supplier<Pose2d>` better for testing than taking a `Drive`?
+ A test can supply any pose without building a drivetrain
- Suppliers are faster
- It avoids a circular dependency at compile time
- `Drive` is final
> A narrow dependency is easier to satisfy in a test and easier to reason about everywhere.

? Which is the smallest change that usually makes logic testable?
+ Take the value as a parameter instead of reading it from the environment
- Extract an interface
- Add a mocking framework
- Make the class final
> The caller reads the environment; the logic does arithmetic.

? Why does taking time as a parameter matter so much?
+ A test can replay minutes of timeline instantly and deterministically
- Clocks are inaccurate on the roboRIO
- It reduces CPU use
- WPILib requires it
> Real time in a test means either a slow test or a flaky one.

? What is wrong with a method that mutates several fields and returns one value?
+ It can only be tested by inspecting the object afterward, and it invites paths that update some fields and not others
- It is slower than returning a record
- Java forbids it
- It cannot be logged
> That is the exact shape of finding F19.

?? Which of these are seams already present in our robot code? Select all that apply.
+ The IO interfaces every subsystem uses
+ The suppliers passed into `Turret` and `Intake`
+ `Intake`'s one-argument constructor
- `Superstructure`'s current-averaging block
> The averaging block is the lesson's counterexample.
:::
