---
summary: How to test command-based code. Pull logic into pure functions, test subsystems through fake IO, and run real commands and triggers under WPILib's scheduler in JUnit.
objectives:
  - Choose the cheapest kind of test for a piece of robot code
  - Test a subsystem's decisions by giving it a fake IO implementation
  - Run commands and triggers under the real CommandScheduler with simulated time
  - Reset shared state so tests don't affect each other
files:
  - build.gradle
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Why test robot code

Time on the real robot is scarce. Mechanical needs it for repairs, and the drive team needs it for practice. Every bug caught on a laptop is one less discovered in a practice match or on the field.

Our `build.gradle` is already set up for tests:

::source file="build.gradle" from="testImplementation" lines=2

It also calls `wpi.java.configureTestTasks(test)`, which lets tests load WPILib's native libraries. The repo has no tests yet, so the first ones you write will be the start of the team's test suite.

Run tests with:

```shell
./gradlew test
```

## Three levels of tests

| Level | What you test | What you need | Setup cost |
|---|---|---|---|
| 1. Pure logic | Math and decisions pulled out into static methods or small classes | Plain JUnit | None |
| 2. Subsystem with fake IO | A subsystem's state machine, fed made-up sensor inputs | JUnit and HAL | Small |
| 3. Commands and triggers | Scheduling, requirements, timeouts, and bindings | JUnit, HAL, the scheduler, and simulated time | Moderate |

Use the lowest level that can catch the bug.

## Level 1: pull the logic out

`Superstructure.updateHubStatusAndPeriod` mixes REBUILT's shift rules with Driver Station calls, so a test can't reach the rules without faking the Driver Station. Moving the rules into a pure function makes them easy to test. This is an example refactor, not code in the repo:

```java title="Example: the shift rules with no WPILib calls"
/** Returns true if our HUB is active at this match time. */
public static boolean hubActive(double matchTime, boolean redInactiveFirst, boolean weAreRed) {
  boolean shift1Active = weAreRed ? !redInactiveFirst : redInactiveFirst;
  if (matchTime > 130) return true; // transition
  if (matchTime > 105) return shift1Active; // shift 1
  if (matchTime > 80) return !shift1Active; // shift 2
  if (matchTime > 55) return shift1Active; // shift 3
  if (matchTime > 30) return !shift1Active; // shift 4
  return true; // end game
}
```

```java title="Example: a plain JUnit test"
@Test
void blueHubAlternatesWhenBlueIsInactiveFirst() {
  // Game data 'B': blue's HUB is inactive in shift 1.
  assertFalse(HubRules.hubActive(120.0, false, false)); // shift 1
  assertTrue(HubRules.hubActive(90.0, false, false)); // shift 2
}
```

The test needs no robot, no simulator, and no setup, and it runs in milliseconds. [Unit 8](course:08-state-machines/hub-shift-logic) develops the full rules this way.

## Level 2: a subsystem with fake IO

AdvantageKit's IO layer makes subsystems easy to test. Each IO interface method has a default that does nothing, and the inputs are plain fields. A fake IO sets the inputs to whatever the test needs:

```java title="Example: a fake intake IO"
class FakeIntakeIO implements IntakeIO {
  double current = 0.0;
  double speed = 0.0;

  @Override
  public void updateInputs(IntakeIOInputs inputs) {
    inputs.intakeCurrent = current;
    inputs.intakeSpeed = speed;
  }
}
```

`Intake` already has a constructor that helps. `new Intake(io)` fills in "turret not resetting" and "flywheel at speed," so the test doesn't need a turret:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public Intake(IntakeIO io)" lines=3

```java title="Example: testing jam detection"
@Test
void sustainedHighCurrentAtLowSpeedReverses() {
  assertTrue(HAL.initialize(500, 0));
  FakeIntakeIO io = new FakeIntakeIO();
  Intake intake = new Intake(io);
  intake.setWantedState(Intake.WantedState.INTAKING);

  io.current = IntakeConstants.jamCurrent + 5.0;
  io.speed = IntakeConstants.jamSpeed - 1.0;
  for (int loop = 0; loop < IntakeConstants.jamMinCount; loop++) {
    intake.periodic();
  }

  assertEquals(Intake.CurrentState.REVERSING, intake.getCurrentState());
}
```

The test calls `periodic()` itself, once per simulated loop, and refers to the constants by name. If someone retunes the jam threshold, the test still checks the *behavior*.

## Level 3: commands and triggers under the real scheduler

Some bugs live in the command layer: a missing requirement, a command that doesn't stop its motor when interrupted, or a trigger bound to the wrong thing. To catch those, run the real `CommandScheduler` in a test:

```java title="Test setup for commands"
@BeforeEach
void setUp() {
  assertTrue(HAL.initialize(500, 0)); // start the simulated hardware layer
  SimHooks.pauseTiming(); // time only moves when the test steps it
  DriverStationSim.setEnabled(true); // commands don't run while disabled
  DriverStationSim.notifyNewData();
  CommandScheduler.getInstance().cancelAll();
  CommandScheduler.getInstance().unregisterAllSubsystems();
  CommandScheduler.getInstance().getDefaultButtonLoop().clear(); // remove old trigger bindings
}

@AfterEach
void tearDown() {
  CommandScheduler.getInstance().cancelAll();
  CommandScheduler.getInstance().unregisterAllSubsystems();
  CommandScheduler.getInstance().getDefaultButtonLoop().clear();
  SimHooks.resumeTiming();
}

/** Runs the scheduler for a number of 20 ms loops. */
private void runLoops(int loops) {
  for (int i = 0; i < loops; i++) {
    CommandScheduler.getInstance().run();
    SimHooks.stepTiming(0.02);
  }
}
```

Each line prevents a specific failure:

| Symptom | Cause | Fix in the setup |
|---|---|---|
| A scheduled command never runs | The simulated robot starts disabled | `DriverStationSim.setEnabled(true)` and `notifyNewData()` |
| `withTimeout` tests pass sometimes and fail other times | Timers read the real clock | `SimHooks.pauseTiming()` and `stepTiming(0.02)` |
| Tests pass one at a time but fail together | The scheduler is a singleton that keeps commands, subsystems, and bindings between tests | `cancelAll`, `unregisterAllSubsystems`, and clearing the button loop |
| `UnsatisfiedLinkError` | WPILib's native libraries aren't loaded | Run tests through Gradle, which sets them up |

With that setup, a test reads like a script of loops:

```java title="Example: a trigger binding test"
@Test
void holdingTheButtonRunsTheRoller() {
  boolean[] held = {false};
  new Trigger(() -> held[0]).whileTrue(roller.runAtVolts(6.0));

  runLoops(1);
  assertEquals(0.0, roller.getAppliedVolts());
  held[0] = true;
  runLoops(1);
  assertEquals(6.0, roller.getAppliedVolts());
  held[0] = false;
  runLoops(1);
  assertEquals(0.0, roller.getAppliedVolts());
}
```

## What not to test

- **WPILib itself.** Don't test that `withTimeout` works. Test that *your* command requires the right subsystem, stops its motor when interrupted, and is bound to the right input.
- **Tuning values.** A test that checks a PID gain equals 0.01 breaks every time someone tunes the mechanism, and it never catches a bug.
- **Real mechanism behavior.** Whether the flywheel actually reaches 2,500 RPM is a question for the robot and its logs.

:::exercise id="u07-commands-wpi"
Finish the command factories in `Roller` so that `RollerTest` passes. The test uses the setup from this lesson to check requirements, interruption, timeouts, default commands, trigger bindings, and disabled behavior.

This exercise uses the real WPILib scheduler, so it needs WPILib's native libraries. Run it through Gradle:

```shell
./gradlew test --tests "frc.training.u07.RollerTest"
```
---hint
`runAtVolts`: `startEnd(() -> setVolts(volts), () -> setVolts(0.0))`. Because it's the subsystem's own factory method, the command requires the roller automatically.
---hint
`ejectFor`: reuse `runAtVolts(-6.0)` and add `withTimeout(seconds)`. When the timeout interrupts the command, its end action stops the roller.
---hint
`stopCommand`: a default command should keep running, so use `run(...)`, not `runOnce(...)`.
:::

:::quiz
? You want to check the shift rules that decide when our HUB is active. What is the cheapest good test?
+ A plain JUnit test of a pure function that takes match time, game data, and alliance
- A test that runs the whole robot in simulation for 140 seconds
- A practice match with a stopwatch
- A test that runs the CommandScheduler for 7,000 loops
> Pure logic needs no simulator. Pull it out of the subsystem and test it directly.

? A test schedules a command and runs the scheduler, but `execute()` never runs. What is the most likely cause?
+ The simulated robot is disabled, so the scheduler won't run the command
- JUnit can't run commands
- The command has too many requirements
- HAL was initialized twice
> Call `DriverStationSim.setEnabled(true)` and `DriverStationSim.notifyNewData()` in the setup.

? A test of `withTimeout(0.5)` passes on your laptop but sometimes fails on a slower computer. What fixes it?
+ Pause simulated time and step it by 0.02 s per loop with `SimHooks`
- Increase the timeout to 5 seconds
- Add `Thread.sleep(500)` to the test
- Run the test twice
> With simulated time, a loop always takes exactly 20 ms, no matter how fast the computer is.

? Your command tests pass when run one at a time but fail when the whole suite runs. Why?
+ The CommandScheduler is a singleton, and commands, subsystems, or bindings from earlier tests are still registered
- JUnit runs tests in random order, which breaks WPILib
- Each test needs its own copy of WPILib
- Gradle only supports one command test per project
> Reset the scheduler before and after each test.

?tf `Intake` has a one-argument constructor that makes testing it without a turret easier.
= true
> `new Intake(io)` supplies "not resetting" and "flywheel at speed."

? When testing a subsystem's state machine, what should the test replace?
+ The IO implementation, with a fake that reports chosen inputs
- The subsystem's `periodic()` method
- The CommandScheduler
- The robot's `Constants.currentMode`
> AdvantageKit's IO layer is the seam between logic and hardware.
:::
