---
summary: Changing the shape of code without changing what the robot does, in an order that keeps you able to stop at any point, with a worked plan for one of our real findings.
objectives:
  - Define refactoring and explain what makes it safe
  - Build a safety net before changing code
  - Refactor in small, reversible steps
  - Rank our known findings by value and risk
files:
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
---

## Refactoring is not "improving"

Refactoring means changing the **structure** of code while leaving its **behavior** identical. Renaming a variable is a refactor. Extracting a method is a refactor. Fixing finding F1 is not: the robot behaves differently afterward, which makes it a bug fix.

The distinction matters because it decides how you verify the change:

| Kind of change | How you know it worked |
|---|---|
| Refactor | The robot does exactly what it did before |
| Bug fix | The robot does something different, and you can describe what |
| Feature | The robot does something new, and someone asked for it |

**Never mix them in one commit.** A commit that renames three things and also changes a threshold is a commit nobody can review and nobody can revert cleanly.

## The safety net

:::steps
1. **Write a characterization test first.** Not a test of what the code *should* do, a test of what it *does* right now, including the parts you think are wrong. That test is what tells you the refactor changed nothing.
2. **Branch.** One refactor per branch, named for what it does.
3. **Commit in small steps** that compile and pass. The ability to stop halfway and still have working code is the whole point.
4. **Re-run the tests after every step**, not at the end.
5. **Verify on the robot** before merging anything that touches a mechanism, even when the tests pass. Tests do not know about a wiring harness.
:::

Step one is the step people skip, and it is the one that makes the rest safe. For our codebase, the characterization tests are largely written already: the pure-logic exercises in this course capture what the real code currently does, F1, F4, and all.

## A worked plan: finding F16

The problem: `IntakeIOSpark.updateInputs` ends by sending the arm's setpoint to its controller, so a method named "update inputs" writes an output, and every arm decision reaches the motor one loop late.

::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from="inputs.subsystemCurrent" lines=3 highlight="3"

The plan, in commits:

:::steps
1. **Characterize.** Write a test against a fake IO that records the order of calls in a loop, capturing today's behavior: a decision made this loop reaches the controller next loop.
2. **Move the call.** Put `armController.setSetpoint(...)` at the end of `setIntakeArmAngle`, where the decision arrives, and delete it from `updateInputs`.
3. **Update the characterization test** to the new expected order, in the same commit as step 2, because behavior did change: the arm now moves one loop sooner and is no longer commanded at boot before any state has run.
4. **Check the boot path.** `armSetAngle` is initialized to `intakePosition`, so today the first `updateInputs` commands the arm out. After the change, nothing commands the arm until a state does, which is safer and is a behavior change worth naming in the commit message.
5. **Verify on the robot.** Enable, watch `armAngle` against `armSetAngle` on the first enable and during a state change, and confirm the arm no longer twitches out at startup.
:::

That is five steps for a one-line move, and four of them are about knowing whether it worked. This is the normal ratio.

## Ranked: what this codebase should change

| Finding | Value | Risk | Notes |
|---|---|---|---|
| F13 vision fusion weights | High | Medium | Changes how much the pose estimator trusts vision; test in simulation, then on the robot |
| F14 stale vision on disconnect | High | Low | An `else` branch and an array initializer |
| F1 unreachable turret state | High | Low | One line, and it changes behavior while paused |
| F12 build metadata | Medium | Very low | A Gradle plugin; makes every future log identifiable |
| F16 output in `updateInputs` | Medium | Low | The worked plan above |
| F8 persisting at mode change | Medium | Low | Change the persist mode; measure the loop before and after |
| F4 passing hood offset | Medium | Low | Fix the three entries, then unify the two builders |
| F2 and F19 dead or stale state | Low | Very low | Delete the averaging block or replace it with the exercise's class |
| F20 dead code | Low | Very low | Delete `LimitSwitchManager`; use or delete the Elastic helper |
| F5, F6, F7 simulation fidelity | Medium | Low | Off-season work that makes everything else testable |

Two patterns in that table are worth noticing. **The highest-value items are not the biggest ones**: F14 and F1 are a few lines each. And **risk tracks behavior change, not diff size**: F12 touches the build and risks nothing, while F13 is a small edit that changes how the robot navigates.

## Habits

- **Rename freely.** A better name is the cheapest refactor there is, and modern editors make it safe.
- **Delete confidently.** Git remembers. Commented-out code is a comment that lies about being code.
- **Extract when you explain.** If you find yourself explaining a block of code to someone, that block wants to be a named method.
- **Leave the campsite cleaner.** Fix the small thing you noticed while you were in there, in its own commit.
- **Stop when the tests go red and you cannot say why.** Revert to the last green commit rather than debugging forward. That is what the small steps were for.

:::quiz
? What makes a change a refactor rather than a bug fix?
+ The robot's behavior is identical afterward
- The diff is small
- No tests changed
- It was suggested in a review
> Which is also how you verify it: the robot does exactly what it did before.

? Why write a characterization test before refactoring?
+ It records what the code does today, so you can prove the refactor changed nothing
- It documents what the code should do
- It is required before merging
- It makes the refactor faster
> A characterization test captures current behavior, bugs included.

? Why commit in small steps that each compile and pass?
+ You can stop at any point and still have working code, and you can revert one step instead of everything
- Smaller commits build faster
- Gradle caches them separately
- It looks better in the history
> The ability to stop halfway is the safety property you are buying.

? Moving the arm's `setSetpoint` out of `updateInputs` changes behavior at boot. What changes?
+ The arm is no longer commanded to its deployed position before any state has run
- The arm stops responding to the state machine
- The encoder offset is reset
- The intake current limit changes
> `armSetAngle` starts at the intake position, so today the first `updateInputs` sends it.

? Which of our findings gives the most value for the least risk?
+ F14, clearing vision inputs when a camera disconnects
- F13, the fusion weighting
- F5, the simulation interlock
- F20, deleting dead code
> An `else` branch and an initializer, and it stops a stale pose from being repeated.

?tf A commit that renames variables and also changes a threshold is fine as long as both changes are correct.
= false
> Nobody can review it and nobody can revert half of it. Split it.
:::
