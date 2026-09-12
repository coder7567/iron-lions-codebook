---
summary: FUEL leaves a moving robot carrying the robot's velocity, so the turret aims at a point the target is not. Here is the iteration that finds that point, and how the team tunes it.
objectives:
  - Explain why a moving robot must aim away from its target
  - Read the three-iteration loop in `considerChassisSpeeds` and explain what it converges to
  - Explain the time-of-flight table and the reality constant
  - Measure and tune the correction on a real robot
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## The problem

A FUEL leaving the shooter keeps whatever velocity the robot had. Drive to the right at 2 m/s, shoot, and the FUEL drifts right the whole way to the HUB. If the flight takes 1.3 seconds, it lands about 2.6 meters to the right of where the turret pointed.

The fix is to aim at a **virtual target**, offset opposite the robot's motion by velocity times flight time. Then the FUEL's own drift carries it to the real target.

The trouble is that flight time depends on distance, distance depends on where the virtual target is, and where the virtual target is depends on flight time. That is a loop, and our code solves it by iterating.

## The iteration

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public Translation2d considerChassisSpeeds(Translation2d target)" lines=18 highlight="5,10,12,13,14"

Reading it carefully:

1. **Field-relative speeds.** The chassis speeds come from the drive in the robot's frame, so `ChassisSpeeds.fromRobotRelativeSpeeds` rotates them into the field frame. Aiming happens in field coordinates, so the velocity has to be there too.
2. **Three passes.** Each pass measures the distance to the *current* virtual target, looks up the flight time, and moves the target by the **change** in flight time since the last pass.
3. **The deltas telescope.** Pass one moves the target by the whole flight time; later passes only correct for how much the flight time changed. After three passes, the target has moved by the final flight time in total.

That last point is the elegant part, and it is easy to misread. The code is not applying the correction three times; it is refining one correction three times.

Three passes is a judgment call. Each pass costs a table lookup and a little arithmetic, and the correction usually stops changing after the second. A robot with a much longer flight time, or much higher speed, might want more.

## The time-of-flight table

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="timeOfFlightMap.put(1.93, 1.22);" lines=4

Four measurements, from 1.22 seconds at 1.93 m to 1.45 seconds at 5.45 m. Like the shot map, it is measured rather than derived, and it is clamped to 0 to 10 m before lookup.

Notice how flat it is: nearly doubling the distance adds about a fifth of a second. A high arc spends most of its flight going up and coming down, and the horizontal distance changes that less than you would guess.

## The constant of reality

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="This is how we will tune this number" lines=7

`ToFRealityConstant` scales the whole correction, and the comment above it is one of the best things in the repository: a four-step procedure anyone can repeat.

1. Drive at a constant speed, sideways to the target, and shoot.
2. Record where the FUEL actually crossed the HUB's plane.
3. The flight-time error is the miss distance divided by the robot's speed.
4. The constant is (flight time + error) ÷ flight time.

That is a measurement, not a knob. A miss of 0.3 m while driving 2 m/s means the flight time was under-estimated by 0.15 s; against a 1.3 s flight, the constant becomes about 1.12.

The default is 1.0, and the comment notes 0.6 was tried at some point. A value below 1 means the model over-corrects, which is what happens if the table's flight times are too long.

:::tip Why sideways is the right test
Driving **toward** the target changes the distance during the flight, which mixes two errors together. Driving **across** it isolates the lead correction: the only thing that should move the landing point is the sideways drift. Test one thing at a time, even when the test is a robot on a carpet.
:::

:::exercise id="u14-sotm"
Implement the correction: the time-of-flight table with clamping and scaling, and the three-pass iteration that moves the aim point.

One test uses a flat table so the arithmetic is exact, and another uses the team's real table and checks that the result is self-consistent: the final aim point matches the flight time computed there.
---hint
Keep the robot's position fixed through the loop. Only the aim point moves.
---hint
Track two values: the flight time from this pass and the one from the previous pass. The target moves by `speeds * (flightTime - previous)`, which is the whole flight time on the first pass.
---hint
Clamp the distance into the table's range **before** the lookup, then multiply the result by the reality constant.
:::

:::quiz
? Why does a moving robot have to aim away from its target?
+ The FUEL keeps the robot's velocity in flight, so it drifts in the direction the robot was moving
- The turret cannot turn fast enough
- The flywheel loses speed while the robot accelerates
- Odometry lags while moving
> Aim opposite the motion, and the drift carries the shot to the target.

? Why does the correction need to iterate at all?
+ Flight time depends on distance, and moving the aim point changes the distance
- The turret needs three loops to turn
- The table has three entries
- To average out noise in the speed measurement
> It is a small fixed-point problem, solved by refining an estimate.

? After three passes, how far has the aim point moved in total?
+ By the robot's velocity times the final flight time
- By three times the velocity times the flight time
- By the velocity times the sum of all three flight times
- It depends on the distance to the target
> The per-pass deltas telescope; the code refines one correction rather than applying three.

? Why are the chassis speeds converted to the field frame first?
+ Aiming happens in field coordinates, so the velocity has to be expressed there too
- Robot-relative speeds are always zero
- To compensate for the alliance flip
- Because the table is keyed by field position
> Mixing frames is the classic source of a correction that points the wrong way.

?num Our time-of-flight table says 1.3 s at about 4 m. Driving sideways at 2 m/s, how far does the FUEL drift during the flight?
= 2.6 ± 0.1 m
> Which is why the correction exists at all.

? You shoot while driving sideways at 2 m/s and the FUEL lands 0.3 m behind where you aimed. What does the procedure in the code's comment tell you to compute?
+ A flight-time error of 0.15 s, and a reality constant of about 1.12
- A new hood offset
- A larger flywheel tolerance
- A different turret trim
> Miss distance divided by robot speed is the flight-time error.
:::
