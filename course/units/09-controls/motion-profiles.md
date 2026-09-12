---
summary: A step setpoint asks a mechanism for infinite acceleration. Motion profiles ask for something the mechanism can actually do, and our heading controller is built on one.
objectives:
  - Explain why a step setpoint saturates a controller and what that looks like
  - Compute the phases and duration of a trapezoid profile
  - Read our `ProfiledPIDController` and explain why it must be reset before use
  - Tell the difference between limiting the setpoint and limiting the output
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## The problem with a step

Suppose the turret is at 0 rad and the code asks for 1.5 rad. The error jumps instantly to 1.5, and with kP = 1.0 the controller demands 1.5 units of output immediately. Three things follow:

1. The output saturates, so the controller is running open loop at full power until the error shrinks.
2. The mechanism accelerates as hard as the motor and gearbox allow, drawing a big current spike.
3. It arrives fast and overshoots, because nothing told it to slow down early.

A **motion profile** fixes this by replacing the step with a path: a setpoint that moves from where the mechanism is to where it should be, at speeds and accelerations the mechanism can achieve. The controller then only ever sees a small error, which is exactly the situation PID is good at.

## The trapezoid

The standard profile has three phases: accelerate at a constant rate, cruise at a maximum velocity, then decelerate to a stop. Plotted against time, velocity is a trapezoid.

| Symbol | Meaning |
|---|---|
| `maxVelocity` | The fastest the profile will ask for |
| `maxAcceleration` | How quickly the profile may change velocity |
| Ramp distance | `maxVelocity² / (2 × maxAcceleration)`, used once on the way up and once on the way down |

If twice the ramp distance is more than the whole move, the profile never reaches `maxVelocity`. It becomes a **triangle**: accelerate to a peak of `√(maxAcceleration × distance)`, then decelerate. Short moves are triangles; long moves are trapezoids.

Our heading controller's constraints are 8 rad/s and 20 rad/s². A 90° turn (1.571 rad) needs 1.6 rad of ramp distance to reach 8 rad/s and only has 1.571 rad to work with, so even a quarter turn is a triangle, peaking at about 5.6 rad/s and taking about 0.56 seconds. That is the exercise at the end of this lesson.

## Our profiled controller

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="ProfiledPIDController angleController" lines=9

A `ProfiledPIDController` is a PID controller with a profile built in: you give it a goal, and each loop it computes the profile's next setpoint and runs PID against *that* instead of the goal.

Two configuration details matter:

- **`enableContinuousInput(-Math.PI, Math.PI)`** for the same reason the turn motors need it: heading wraps.
- **The profile must be reset.** A `ProfiledPIDController` carries its current profile state between calls. If the command starts again three minutes later, the stored state still says the robot was mid-turn. That is why the command factory attaches a `beforeStarting`:

```java title="From joystickDriveFacingTarget"
.beforeStarting(() -> angleController.reset(drive.getRotation().getRadians()));
```

**Rule: any stateful controller you keep around must be reset when its command starts.** That applies to profiled controllers, integrators, slew rate limiters, and filters.

## Limiting the setpoint versus limiting the output

A profile limits the **setpoint**. There is a cruder tool that limits the **output**, and our code uses both.

| Technique | What it limits | Where we use it |
|---|---|---|
| Motion profile | How fast the setpoint may move | The heading controller; PathPlanner paths |
| Slew rate limiter | How fast a commanded value may change | `wheelRadiusCharacterization`, ramping up its turn speed |
| Closed-loop ramp rate | How fast the controller's output voltage may change | The turret (`closedLoopRampRate(0.075)`) and the feeder (`0.05`) |
| Output range | The maximum output, regardless of error | The turret (±0.5) and the intake arm (±0.25) |

Output limits are easy and safe, and they cost you predictability: the mechanism still chases a step, just gently. A profile is more work and gives you a plan, which means you can predict where the mechanism will be at any moment and how long the move takes. That prediction is what makes shoot-on-the-move and path following possible.

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="SlewRateLimiter limiter = new SlewRateLimiter" lines=1

:::info Where profiles hide in PathPlanner
A PathPlanner path is a profile in two dimensions. Its max velocity and acceleration constraints do the same job as a trapezoid's, and the generated trajectory is a setpoint that moves smoothly through space. When you tune a path that overshoots, you are doing exactly what this lesson describes, one level up. [Unit 12](course:12-autonomous/pathplannerlib) covers that.
:::

:::exercise id="u09-profile"
Implement a rest-to-rest trapezoid profile: decide whether a move is a trapezoid or a triangle, find its peak velocity and total time, and sample its position and velocity at any moment.

The last test checks the 90° heading turn from this lesson: about 0.56 seconds, and triangular.
---hint
The move is triangular when twice the ramp distance, `maxVelocity² / (2 × maxAcceleration)`, exceeds the distance. In that case, the peak velocity is `√(maxAcceleration × distance)`.
---hint
Once you have the peak velocity, the three phase durations follow: the ramp takes `peak / maxAcceleration`, and the cruise covers whatever distance the two ramps don't.
---hint
For `sample`, handle the ends first (before 0, and at or past the total time), then decide which phase `t` falls into. In the deceleration phase, work from the distance already covered and the time spent decelerating.
:::

:::quiz
? What happens when a position controller is given a step setpoint far from the mechanism's current position?
+ The output saturates and the mechanism accelerates as hard as it can, then likely overshoots
- The controller ignores the setpoint
- The mechanism moves at a constant safe speed
- The integral term prevents any motion
> A step asks for infinite acceleration; the hardware supplies whatever it can.

?num A move of 10 m has a max velocity of 2 m/s and a max acceleration of 1 m/s². How long does the whole profile take?
= 7 ± 0.1 s
> Two seconds up, two down, and 6 m of cruising at 2 m/s in between.

? When is a trapezoid profile actually a triangle?
+ When the move is too short to reach the maximum velocity
- When the acceleration limit is zero
- When the mechanism wraps around
- When the goal is behind the starting position
> Ramping up and down needs twice the ramp distance; shorter moves peak early and come straight back down.

? Why does `joystickDriveFacingTarget` reset its `ProfiledPIDController` in `beforeStarting`?
+ The controller keeps profile state between runs, so a stale state would produce a jump when the command restarts
- Because the command has no requirements
- To clear the drive's odometry
- Because the alliance may have changed
> Any stateful controller has to be reset when its command starts.

? What's the difference between a closed-loop ramp rate and a motion profile?
+ A ramp rate limits how fast the output voltage changes; a profile limits how fast the setpoint moves and predicts the whole move
- They are the same thing
- A ramp rate only works on velocity loops
- A profile runs on the motor controller
> Both smooth motion; only the profile gives you a plan you can predict against.

?tf A motion profile makes PID gains matter less, because the error stays small throughout the move.
= true
> That's the main practical benefit: the controller only ever corrects small deviations from a plan.
:::
