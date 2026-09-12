---
summary: Three short self-checks that tell you which units to work through carefully, which to skim, and where to start.
objectives:
  - Measure what you already know about Java, advanced Java, and FRC software
  - Choose a starting unit with a mentor
---

## How the placement check works

This is not a grade. It helps you and your mentors decide where to start. There are three sections with five questions each. Answer honestly and **do not look things up**. Guessing correctly just sends you to lessons you are not ready for.

Each section shows your score after you answer. When you finish, use the table at the bottom.

:::tip Brand new to programming?
You can skip straight to [Unit 1](course:01-tools/files-folders-terminal) and start at the beginning. The course was written for you.
:::

## Section A: Java basics

:::quiz Section A: Java basics
?code What does this code print?
```java
int a = 7;
int b = 2;
System.out.println(a / b);
```
= 3
> Both values are `int`, so Java does integer division and throws away the remainder: 7 / 2 is 3.

?code What does this code print?
```java
System.out.println(10 % 4);
```
= 2
> `%` is the remainder operator. 10 divided by 4 is 2 with a remainder of 2.

? How many times does this loop print "hi"?
```java
for (int i = 0; i < 4; i++) {
    System.out.println("hi");
}
```
- 3
+ 4
- 5
- It never stops
> `i` takes the values 0, 1, 2, and 3. When `i` becomes 4, `i < 4` is false and the loop ends.

? What is stored in `x`?
```java
double x = 1 / 4;
```
+ 0.0
- 0.25
- 1.0
- It does not compile
> `1 / 4` is integer division, which is `0`. That `int` is then widened to `0.0`. Writing `1.0 / 4` would give 0.25.

?num What does `Math.max(-1.0, Math.min(1.0, 3.5))` return?
= 1 ± 0
> `Math.min(1.0, 3.5)` is 1.0, and `Math.max(-1.0, 1.0)` is 1.0. This pattern clamps a value between −1 and 1.
:::

## Section B: Objects and advanced Java

:::quiz Section B: Objects and advanced Java
? `GyroIO` is an interface whose methods all have `default` bodies. What does `new GyroIO() {}` create?
- A compile error, because you cannot write `new` with an interface
+ An object of an anonymous class that implements `GyroIO` using the default do-nothing methods
- A copy of the real NavX gyro
- An empty array of gyros
> Our `RobotContainer` uses `new GyroIO() {}` in simulation and replay. The anonymous class implements the interface, and because every method has a default body, nothing more is needed.

? In `new Turret(new TurretIOSpark(), drive::getPose, drive::getChassisSpeeds)`, what is `drive::getPose`?
- It calls `getPose()` once and passes the current pose
+ A method reference: a function object the turret can call later to get the latest pose
- A static field on the `Drive` class
- A comment
> Method references let the turret ask the drive for the latest pose every loop without owning the drive.

?tf A `switch` can choose between the constants of an enum such as `enum Mode { REAL, SIM, REPLAY }`.
= true
> `RobotContainer` does exactly this with `switch (Constants.currentMode)`.

? Why does `DriverStation.getAlliance()` return `Optional<Alliance>` instead of `Alliance`?
+ The alliance may not be known yet, and `Optional` makes you handle that case
- `Optional` makes the method run faster
- There are more than two alliances
- It returns `null` on purpose so the robot crashes when disconnected
> Before the Driver Station reports a color, there is no value. `Optional` forces code to decide what to do, as in `getAlliance().orElse(Alliance.Blue)`.

? What does Java generate automatically for `record TargetInfo(int tagID, double targetYaw)`?
- Nothing; you must write every method yourself
- Only setter methods
+ A constructor, accessors like `tagID()`, and `equals`, `hashCode`, and `toString`
- A database table
> Records are compact, immutable data carriers. Our vision code uses records for `TargetInfo` and `PoseObservation`.
:::

## Section C: FRC and command-based software

:::quiz Section C: FRC software
? How often does a `TimedRobot` (like our `LoggedRobot`) call its periodic methods by default?
- Once per second
- Every 5 ms
+ Every 20 ms, or 50 times per second
- Only when a button is pressed
> The default loop period is 0.02 seconds. Anything that takes longer than that causes a loop overrun.

? What must run every loop for commands and subsystem `periodic()` methods to work?
+ `CommandScheduler.getInstance().run()`
- `Robot.main()`
- `Thread.sleep(20)`
- `System.gc()`
> `Robot.robotPeriodic()` calls the scheduler each loop. Without it, no command or subsystem periodic ever runs.

? In WPILib field coordinates, which direction does +X point?
- Toward the scoring table
+ From the blue alliance wall toward the red alliance wall
- Straight up
- Whichever way the robot faces
> The origin is at the blue wall's corner, +X points toward red, and +Y points to the left when you stand at the blue wall.

? What does the P in a PID controller respond to?
- The total time since enabling
+ The current error, meaning how far the measurement is from the setpoint
- The battery voltage
- The motor's temperature
> The proportional term pushes harder the farther you are from the setpoint.

? Why do our subsystems read sensors through IO interfaces like `ModuleIO`?
- Java does not allow reading sensors any other way
+ So every input can be logged and replayed in simulation, and hardware can be swapped for simulation
- To make the code compile faster
- Because the roboRIO requires it
> AdvantageKit's IO layer means all hardware data flows through logged input objects. That allows deterministic replay and swapping in `ModuleIOSim` for simulation.
:::

## Choose your starting point

| Your results | Start here |
|---|---|
| Section A: 3 or fewer | [Unit 1](course:01-tools/files-folders-terminal), then every unit in order. |
| Section A: 4–5, Section B: 3 or fewer | Do Unit 1. Skim Unit 2 and pass [its unit test](course:02-java-basics/unit-test). Start carefully at [Unit 3](course:03-java-objects/classes-and-objects). |
| Sections A and B: 4–5, Section C: 3 or fewer | Do Unit 1. Pass the Unit 2, 3, and 5 unit tests. Start at [Unit 4](course:04-git/why-version-control), then [Unit 6](course:06-robot-foundations/control-system). |
| All three: 4–5 | Do Unit 1, then pass the Unit 2–7 unit tests to confirm. Start at [Unit 8](course:08-state-machines/state-machine-basics) and ask a mentor about a [capstone](course:16-season/capstones). |

:::warning Everyone does these, no matter where you start
- **Unit 1** setup lessons, because your computer needs the 2026 tools and the team code.
- **[How to Learn (and Stay Safe) Here](course:00-welcome/how-to-learn-here)**, because safety rules apply to experts too.
- **[The 967 Git Workflow](course:04-git/team-git-workflow)**, because it is how our team shares code.
:::
