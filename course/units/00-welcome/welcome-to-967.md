---
summary: What Iron Lions programmers are responsible for, how this course is built around our real robot code, and how to move through it.
objectives:
  - Describe what the software subteam owns on Team 967
  - Explain how lessons, checks, exercises, unit tests, and capstones fit together
  - Find the team's robot code and the reference pages you will use every week
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/RobotContainer.java
---

## Welcome to the software subteam

Team 967, the **Iron Lions**, has competed in the FIRST Robotics Competition since 2002 as Linn-Mar High School's FRC team in Marion, Iowa. That is 25 seasons, nine trips to the FIRST Championship, and regional wins in 2004, 2011, 2013, and 2019.

In 2026 the robot played **REBUILT**. At the Minnesota Bluff Country Regional (March 4–7) it went 6–5 in qualifications and played in the playoffs on Alliance 8. At the Iowa Regional (March 25–28) it ranked 12th of 53, captained Alliance 7, and won the **Industrial Design Award**.

Every one of those matches ran on Java written by students. That is the job you are signing up for.

:::team The code you will study
The 2026 robot's code lives in [FRC-IronLions-967/Rebuilt-2026](https://github.com/FRC-IronLions-967/Rebuilt-2026). This course quotes it constantly and pins every quote to commit `fe04405`, so line numbers always match GitHub. When a lesson says "line 110 of `Robot.java`", you can open that exact line.
:::

## What the software subteam owns

Drivers push sticks and buttons, but **code decides what every motor does**. In autonomous, code is the only thing driving. Here is what that means on the 2026 robot:

| Job | Where it lives in Rebuilt-2026 | Unit |
|---|---|---|
| Drive anywhere, facing any direction | `subsystems/drive` (swerve drive) | 10 |
| Know where the robot is on the field | `Drive` odometry plus `subsystems/vision` (AprilTags) | 10, 13 |
| Aim and shoot FUEL, even while moving | `subsystems/turret` (turret, hood, flywheel) | 14 |
| Collect FUEL without jamming | `subsystems/intake` | 8 |
| Coordinate mechanisms so they never collide | `subsystems/Superstructure.java` | 8 |
| Score in autonomous | PathPlanner autos in `src/main/deploy/pathplanner` | 12 |
| Give drivers good controls and feedback | `RobotContainer.java` bindings and rumble | 7 |
| Find out why something went wrong | AdvantageKit logs and replay | 11 |

You do not need to understand any of that yet. By the end of the course, you will be able to read, change, and test every row.

## How the course is built

The course runs in **seventeen units**, from Unit 0 (this one) through Unit 16. Early units assume you have never written code. Later units assume you finished the earlier ones.

Every unit uses the same kinds of pages:

- **Lessons** explain one idea at a time, then show where it appears in our code.
- **Checks** at the end of a lesson ask a few questions. You get feedback right away and can try again.
- **Exercises** are coding problems in a real GradleRIO project. JUnit tests tell you when your code works.
- **Unit tests** close each unit. Score 80% or better to mark the unit complete.
- **Capstones** in Unit 16 are real projects on team code, reviewed the way the team reviews pull requests.

Here is a first taste of real team code. It runs 50 times every second while the robot is on:

::source file="src/main/java/frc/robot/Robot.java" from="public void robotPeriodic()" to="CommandScheduler.getInstance().run();" title="Robot.java · robotPeriodic()" highlight="10"

Line 110 hands control to WPILib's command scheduler, which runs every subsystem and command on the robot. [Unit 7](course:07-command-based/subsystems-commands-scheduler) explains exactly what happens inside that one call.

:::info We review our own code
Good teams find their own bugs before a match does. The [Rebuilt-2026 Code Audit](course:reference/code-audit) lists real problems found while writing this course, from a state that can never be reached to a logged value that reads the wrong motor. You will learn to spot problems like these yourself in Units 8 and 15.
:::

## How to move through it

:::steps
1. **Take the [Placement Check](course:00-welcome/placement-check).** If you already know some Java or FRC, it tells you which units you can skim.
2. **Set up your computer with [Unit 1](course:01-tools/install-the-2026-toolchain).** Do this with a mentor nearby the first time.
3. **Work through lessons in order.** Most take 15–50 minutes. Type the examples yourself instead of copying them.
4. **Do the exercises in VS Code.** Red tests are normal. Green tests mean your code works.
5. **Pass the unit test** before moving on. If you miss the pass mark, the explanations show which lessons to revisit.
6. **Ask early.** If you are stuck for more than 20 minutes, ask a mentor or an experienced student. Getting unstuck quickly is a skill, not a weakness.
:::

Your progress (completed lessons, check answers, unit test scores, and passing exercises) is saved in **this browser on this device**. To move to another computer, open [Your progress](#/progress) and export a progress file.

## Keep these pages handy

- **Search** everything with <kbd>Ctrl</kbd> <kbd>K</kbd> (or <kbd>/</kbd>).
- [967 Robot Reference](course:reference/robot-reference) lists CAN IDs, controller bindings, tunable values, and log keys.
- [Troubleshooting Guide](course:reference/troubleshooting) maps symptoms like "robot won't enable" to causes and fixes.
- [Glossary](course:reference/glossary) defines words like *odometry*, *feedforward*, and *AprilTag*.

:::quiz
? On the 2026 robot, what does code written by the software subteam control?
- Only what happens during the autonomous period
- Only the dashboard layout the drive team sees
+ Everything the robot's motors do, in both autonomous and teleop
- Nothing during teleop, because drivers control the motors directly
  ~ Drivers press buttons, but code decides what each button does.
> Drivers give inputs, but code turns those inputs into motor outputs: the joystick drive command, the Superstructure states, the turret aim. In autonomous, code does everything.

?tf Line numbers in this course's code excerpts match the team's `Rebuilt-2026` repository at a fixed commit.
= true
> Excerpts are copied from the repository when the course is built and pinned to commit `fe04405`, so the numbers match GitHub at that commit.

?order Put the parts of a unit in the order you will do them.
1. Read the lessons
2. Answer the checks at the end of each lesson
3. Solve the coding exercises
4. Pass the unit test
> Each step builds on the one before it: understand the idea, check that you understood it, use it in code, then prove it across the whole unit.

? Where is your course progress saved?
- On the team's GitHub
+ In this web browser, on this device
- On the roboRIO
- In your school account
> Progress is stored locally in your browser. Use **Export progress file** on the progress page before switching computers.
:::
