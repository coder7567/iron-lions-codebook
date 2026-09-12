---
summary: Eight projects that finish this course by improving the real robot. Each one is scoped to fit an off-season, has a clear definition of done, and fixes something this course found.
objectives:
  - Choose a capstone that matches your interests and the team's needs
  - Scope a project so it can actually be finished and verified
  - Verify a change the way this course has taught
  - Hand the work over so someone else can maintain it
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/RobotContainer.java
---

## How to use this page

Each project below is real work on the team's robot, sized for one person over a few weeks of
off-season, with a definition of done you can hold yourself to. Pick one, tell the software lead,
branch, and go.

Every brief follows the same shape: what it is, what you should have read first, and how you will know
you are finished. **A capstone is not done when the code compiles. It is done when someone else can
tell it works.**

## 1. Make vision trustworthy

**Fix findings F13 and F14.** Weight fused camera measurements by 1/σ² instead of 1/σ, give each
camera its own standard-deviation baseline, stop blending two cameras' timestamps into one, and clear
the vision inputs when a camera disconnects.

- **Read first:** [Trusting Measurements](course:13-vision/trusting-measurements) and
  [The Vision Pipeline](course:13-vision/vision-pipeline).
- **Difficulty:** hardest of the eight, and the one that most changes how the robot behaves.
- **Done when:** the fusion math has unit tests covering the equal-camera and unequal-camera cases; a
  disconnected camera produces no observations in the log; and a practice-field run shows vision
  corrections as small nudges rather than jumps.

## 2. Close the replay hole

**Give the Driver Station an IO layer.** `Superstructure` and `Turret` read `getMatchTime`,
`getGameSpecificMessage`, and `getAlliance` directly, so the hub-shift logic cannot be replayed and
cannot be tested.

- **Read first:** [AdvantageKit Architecture](course:11-logging/advantagekit-architecture) and
  [Designing for Testability](course:15-quality/designing-for-testability).
- **Done when:** a replayed match log reproduces `Hub Active` and `Period Time` exactly; the shift
  rules have unit tests; and finding F19's stale field is gone because there is no field.

## 3. Give the drive team two buttons

**Bind the X-stop and a heading reset.** `Drive.stopWithX()` exists and nothing calls it, and there is
no way to fix a wrong heading from the Driver Station.

- **Read first:** [Driving Well](course:10-swerve/driving-well) and
  [Triggers and Bindings](course:07-command-based/triggers-and-bindings).
- **Difficulty:** the smallest project here, and a good first pull request.
- **Done when:** both buttons work, the heading reset runs while disabled, the drive team has used them
  in a practice session, and they are written on the controller map.

## 4. Start the test suite

**Port three of this course's exercises into the robot repository** as tests against the real classes:
the hub schedule, the intake's jam logic, and the turret's state selection.

- **Read first:** [Unit Testing Robot Code](course:15-quality/unit-testing-robot-code).
- **Done when:** `./gradlew test` runs them from `src/test/java`, they pass against the real code (or
  document the finding they expose), and a GitHub Actions workflow runs them on every push.

## 5. Make the logs identifiable

**Fix finding F12.** Add the `gversion` plugin so `BuildConstants` is generated at build time, and stop
committing the generated file.

- **Read first:** [Telemetry and Logs](course:11-logging/telemetry-and-logs).
- **Difficulty:** low, and it improves every log the team records from then on.
- **Done when:** a fresh build's log metadata shows the actual commit, branch, and date, and the
  generated file is git-ignored.

## 6. Make simulation tell the truth

**Fix findings F5, F6, and F7.** Give the simulated flywheel voltage, fill in the intake's currents and
arm setpoint, correct the inverted `intakeSafe` comparison, and stop the simulated IO classes from
extending the hardware ones.

- **Read first:** [Simulation](course:11-logging/simulation) and
  [Reviewing State Machines](course:08-state-machines/reviewing-state-machines).
- **Done when:** a full shooting cycle can be driven in simulation, jam recovery can be triggered in
  simulation, and the interlock behaves the same way it does on the robot.

## 7. Design an auto end to end

**Add one new autonomous routine**, from strategy to a tested routine: budget it, draw the path, place
the commands, test it in simulation, then five times on a practice field.

- **Read first:** all of [Unit 12](course:12-autonomous/auto-strategy).
- **Done when:** it finishes inside 20 seconds, five runs land within a repeatable window, its starting
  pose is marked and documented, and the drive team knows when to pick it.

## 8. Re-measure the shooter

**Rebuild the shot map and the time-of-flight table** with a documented procedure, then measure the
reality constant properly.

- **Read first:** [Collecting Shot Data](course:14-shooting/collecting-shot-data).
- **Done when:** new tables are committed with the date and conditions in the message, the old ones are
  preserved, and a written procedure in the repository lets someone else repeat the session.

:::tip Scoping rules that keep a capstone finishable
**One finding, one branch.** Two fixes in one branch means neither gets merged while the other is
argued about.

**Write the definition of done before the first commit.** If you cannot describe how you will know it
works, the project is not scoped yet.

**Ask for a review at half done**, not at the end. A wrong assumption caught at the halfway point costs
an evening; caught at the end it costs the project.
:::

:::try Pick one this week
Clash in the Corn is in early October and kickoff is in January. That is the window where these
projects are possible and nobody is under match pressure. Every one of them makes the 2027 robot easier
to build, because the code that survives a season is the code the next season starts from.
:::

:::quiz
? What makes a capstone finished?
+ Someone else can tell that it works
- The code compiles and is merged
- The branch is pushed
- The lead approves the plan
> A definition of done written before the first commit is what makes that checkable.

? Which capstone most changes how the robot behaves on the field?
+ Fixing the vision fusion weighting and disconnect handling
- Fixing the build metadata
- Binding the X-stop and heading reset
- Porting three tests
> It changes how far the estimator moves toward every vision measurement.

? Which is the best first pull request for a newer programmer?
+ Binding the X-stop and a heading reset button
- Rewriting the vision fusion
- Porting the robot to the 2027 control system
- Re-measuring the shot map alone
> Small, visible, useful, and verifiable with the drive team.

? Why is "one finding, one branch" a scoping rule?
+ Two fixes in one branch means neither merges while the other is being argued about
- Git cannot merge two changes
- Reviews are limited to one file
- It makes the diff smaller
> The same reason a commit should hold one behavior change.

? When should you ask for a review of a capstone?
+ At about half done, so a wrong assumption costs an evening rather than the project
- Only when it is finished
- Before writing any code
- After it has been tested on the robot
> Reviews are cheapest before the work is finished.

?? Which projects would make a 2027 port easier? Select all that apply.
+ Porting exercises into a real test suite
+ Giving the Driver Station an IO layer
+ Fixing the build metadata
- Re-measuring the shot map
> Tests, seams, and identifiable logs all survive a package rename; a shot map is game-specific.
:::
