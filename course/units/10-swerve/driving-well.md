---
summary: The drivetrain's real users are two people at a Driver Station. How to practice with them, turn their feedback into measurable changes, and review a practice session in the logs.
objectives:
  - Translate driver feedback into a specific, measurable change
  - Run practice drills that expose drivetrain problems before a match does
  - Review a practice session in the logs and know what to look for
  - Decide which drivetrain changes are worth making before an event
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## The drivetrain's users

Everything in this unit exists so that two people can put the robot where they want it, quickly, under pressure, while somebody plays defense. That is the only measure of success that counts, and it is not the same as "the code is correct."

The gap between those two is where programmers lose the drive team's trust. A robot that is technically correct and feels bad to drive gets described as "broken," and the description is fair.

## Turning feedback into a change

Drivers describe feelings. Your job is to turn the feeling into something you can measure and change.

| What the driver says | What to measure | Likely change |
|---|---|---|
| "It's slow to turn" | Time for a 90° turn in the log | Rotation scaling; the heading controller's gains and profile constraints |
| "It's twitchy near center" | Commanded speed against stick position | The deadband, or the squaring curve |
| "It drifts when I let go" | Commanded speeds after the stick returns to center | Deadband too small; brake mode; module zeros |
| "It feels sluggish out of a stop" | `driveCurrentAmps` during acceleration | Current limit; feedforward kS |
| "It pushes me around" | Contact events and pose drift | An X-stop button; drive gear ratio; weight |
| "Autos miss to the left" | `Odometry/Robot` against the trajectory | Wheel radius, module zeros, PathPlanner config |
| "It goes the wrong way when I push forward" | The pose's heading against reality | Heading reset; the red-alliance flip |

Every row has the same shape: a vague complaint becomes a number in a log, and the number picks the change.

## Drills worth running

:::steps
1. **Straight line, 5 m.** Drive forward and stop on a line. Then check odometry against the tape measure. This catches wheel radius and zero errors in one minute.
2. **Figure eight around two cones.** Continuous translation and rotation at the same time. This is where `discretize`, cosine scaling, and the heading estimate all show up.
3. **Strafe along a wall.** Any pull toward or away from the wall means a module zero is off.
4. **Spin while translating.** Ask for a full rotation while crossing the field, which is what shoot-on-the-move driving looks like.
5. **Stop and hold.** Stop on a spot with someone pushing the robot. Today that means brake mode; with an X-stop bound to a button, it means the modules form an X.
6. **Full-match simulation.** Two and a half minutes of driving with the flywheel running and the intake cycling, on one battery. This is the drill that finds brownouts.
:::

Run drill 6 before every event. It is the only one that tests the drivetrain against everything else the robot does at the same time.

## Reviewing the session

Pull the log afterward and look at five things, in this order:

| Check | Where | Healthy looks like |
|---|---|---|
| Loop overruns | The Driver Station log and the watchdog messages | Rare, and not during a specific action |
| Module connections | `driveConnected`, `turnConnected` | Solid true for the whole session |
| Battery under load | Bus voltage during acceleration | No dips near the brownout threshold |
| Command against measurement | `SwerveStates/SetpointsOptimized` against `Measured` | Measurements tracking closely, with no module lagging |
| Pose against vision | `Odometry/Robot` against vision poses | Small, gradual corrections rather than jumps |

Write down what you found, even when everything was fine. "The drivetrain was clean at 12.1 V for a full match" is a useful data point three weeks later.

## Changes worth making on our robot

Two small ones, both mentioned elsewhere in this course, would pay for themselves quickly:

- **Bind the X-stop.** `Drive.stopWithX()` exists and nothing calls it. One button gives the drive team a way to hold a spot against defense.
- **Bind a heading reset.** Field-relative driving is only as good as the pose's heading, and there is no way to fix a bad heading from the Driver Station today. A button that sets the pose's rotation to the alliance's forward direction, with `ignoringDisable(true)` so it works before the match starts, removes a whole category of "the controls are backwards" panic.

Both are a few lines in `configureButtonBindings`, and both are the kind of change a new programmer can land in their first week.

:::danger Don't change the drivetrain the night before an event
A drivetrain change invalidates practice: the drivers' muscle memory, the autos, and every number in the calibration log. If a change is worth making, make it early enough to run drill 6 afterward with the actual drive team.
:::

:::quiz
? A driver says the robot is "twitchy near the middle of the stick." What do you check first?
+ The deadband and the input squaring curve
- The wheel radius
- The gyro's mounting
- PathPlanner's mass constant
> Small-stick behavior is set by the deadband and the shaping curve.

? Autos consistently end about 20 cm to one side. Which two calibrations are the best suspects?
+ Wheel radius and module zeros
- Feedforward kS and the current limit
- The deadband and joystick squaring
- The flywheel gains and the hood offset
> Both make the robot travel a different path than it thinks it does.

? Which drill best exposes a brownout problem?
+ A full-match simulation with the flywheel and intake running on one battery
- A 5 m straight line
- A figure eight
- Strafing along a wall
> Brownouts come from everything drawing current at once.

? Why is `Drive.stopWithX()` worth binding to a button?
+ It gives the drive team a way to hold a position against contact
- It reduces CAN traffic
- It resets the module zeros
- It is required by PathPlanner
> It exists in the code today with nothing calling it.

? Why should a heading-reset command use `ignoringDisable(true)`?
+ So the drive team can fix the heading before the match starts, while the robot is still disabled
- Because resetting odometry while enabled is unsafe
- Because the gyro only reports while disabled
- So it runs during autonomous
> Commands are ignored while disabled unless they are marked otherwise.

?tf Making a drivetrain improvement the night before an event is fine as long as the code compiles and simulation looks right.
= false
> It invalidates the drive team's practice and every auto tuned against the old behavior.
:::
