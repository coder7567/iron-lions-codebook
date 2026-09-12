---
summary: The team repository has no tests and is already set up to run them. Here is what a first test suite should cover, what good tests look like, and how to keep them running.
objectives:
  - Decide what in robot code is worth testing
  - Write tests that read clearly and fail usefully
  - Run the suite locally and in continuous integration
  - Plan the team's first test suite from code that already exists
files:
  - build.gradle
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/intake/Intake.java
---

## Where we stand

::source file="build.gradle" from="testImplementation" lines=2

JUnit 5 is already a dependency, `test` is already configured to use it, and `wpi.java.configureTestTasks(test)` already sets up WPILib's native libraries. Everything needed to run tests is in place. `src/test/java` does not exist.

That is not unusual in FRC, and it is worth being honest about why: during a build season, a test that takes an hour to write competes with a mechanism that needs code tonight. The way out is not a heroic push for coverage. It is picking the handful of things where a test pays for itself within the same season.

## What pays for itself

| Worth testing | Why |
|---|---|
| Rules with edge cases | The HUB shift schedule has six branches and two alliances; a table of cases is faster than a practice match |
| Counter and timing logic | Jam detection, debouncers, and averagers are pure logic with off-by-one failure modes |
| Coordinate and unit math | Alliance flips, angle wrapping, and conversions fail silently and cost matches |
| State machine transitions | "Can every state happen?" is a test, not just a review question |
| Filters and quality math | Vision rejection rules and standard deviations are pure functions with real consequences |

| Not worth testing | Why |
|---|---|
| That WPILib works | `withTimeout` is somebody else's test suite |
| Tuning values | A test asserting kP is 0.01 breaks on every tune and catches nothing |
| Mechanism behavior | Whether the flywheel reaches speed is a robot question |
| Anything that needs the field | Detection quality, traction, contact |

## What a good test looks like

```java title="The shape to copy"
@Test
@DisplayName("50 jam-like loops in a row detect a jam and start reversing")
void sustainedJamReverses() {
  IntakeStateMachine intake = new IntakeStateMachine();          // arrange

  CurrentState state = runLoops(intake, INTAKING, JAM_ARM_OUT, 50);  // act

  assertEquals(CurrentState.REVERSING, state);                   // assert
  assertTrue(intake.isJammed());
}
```

Four habits make tests worth having:

- **One behavior per test.** When it fails, the name tells you what broke without reading the body.
- **A `@DisplayName` in plain language.** The failure output is read by whoever is on the robot at the time, sometimes weeks later.
- **Arrange, act, assert, in that order,** with a blank line between. A test that interleaves them is hard to scan.
- **Name the constants.** `IntakeConstants.jamMinCount` in a test survives a retune; the literal `50` becomes a lie.

And one rule for what to assert: **test the behavior, not the implementation**. A test that checks "after 50 loops the state is REVERSING" survives a refactor. A test that checks "the private jamCount field equals 50" breaks the moment someone improves the code, which trains the team to delete tests.

## Running them

```shell
./gradlew test
```

```shell
./gradlew test --tests "frc.robot.subsystems.SuperstructureTest"
```

Gradle caches results, so an unchanged suite finishes instantly. A failing test prints the assertion, the expected and actual values, and the line.

**In continuous integration**, a GitHub Actions workflow that runs `./gradlew test` on every push is about fifteen lines of YAML and turns "someone should run the tests" into something that happens whether or not anyone remembers. That is the single highest-value addition to this repository's testing story, because it makes a suite that exists stay green.

:::team You have already written the team's first test suite
Every pure-logic exercise in this course is a test the robot code could have:

| Exercise | What it covers in the real code |
|---|---|
| [u08-hub](course:08-state-machines/hub-shift-logic) | `Superstructure.updateHubStatusAndPeriod` |
| [u08-intake](course:08-state-machines/intake-state-machine) | `Intake.updateState` jam detection |
| [u08-turret](course:08-state-machines/turret-state-machine) | `Turret.updateState`, with F1 fixed |
| [u13-filter](course:13-vision/vision-pipeline) | The vision rejection rules |
| [u14-aim](course:14-shooting/turret-aiming) | `setTurretAngle` and the deadzone |
| [u15-averager](course:15-quality/designing-for-testability) | The current averaging F2 was reaching for |

Porting one of them into `src/test/java` in the robot repository, against the real classes, is a contained and genuinely useful first pull request.
:::

:::quiz
? What is already true about testing in the team repository?
+ JUnit 5 and the test task are configured; there are simply no tests
- Tests exist but are excluded from the build
- WPILib does not support unit tests
- Tests require a robot to be connected
> The setup cost has already been paid.

? Which of these is worth a unit test?
+ The HUB shift schedule's branches and alliance handling
- That `withTimeout` interrupts a command
- The flywheel's kP value
- Whether shots go in from 4 meters
> Rules with edge cases and pure logic are where tests pay for themselves.

? Why assert against `IntakeConstants.jamMinCount` instead of the literal 50?
+ The test keeps describing the behavior after someone retunes the constant
- Literals are not allowed in tests
- It runs faster
- JUnit requires constants
> A test that hard-codes a tuning value becomes a lie the first time it changes.

? A test asserts the value of a private counter field. What is wrong with it?
+ It tests the implementation, so any refactor breaks it even when behavior is unchanged
- Private fields cannot be read in tests
- It will be flaky
- Nothing; it is more precise
> Tests that break during improvements teach a team to delete tests.

? What does adding a GitHub Actions workflow that runs the tests buy?
+ The suite stays green without anyone remembering to run it
- Faster test execution
- Coverage reports
- The ability to deploy from CI
> A suite nobody runs stops being true within a week.

?? Which are habits of a good test? Select all that apply.
+ One behavior per test
+ A `@DisplayName` written in plain language
+ Arrange, act, and assert kept in that order
- Asserting as many things as possible in each test
> When a test fails at an event, its name should be enough to know what broke.
:::
