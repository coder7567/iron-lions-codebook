---
summary: The four measurements a swerve drivetrain needs, how to take each one, and how often to redo them so autonomous keeps working all season.
objectives:
  - Measure the module zero offsets and verify them
  - Run the wheel radius and feedforward characterizations and use their output
  - List the physical properties PathPlanner needs and where they come from
  - Decide when a measurement needs to be redone
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## Four numbers the drivetrain runs on

| Measurement | Constant | Redo it when |
|---|---|---|
| Module zero offsets | `frontLeftZeroRotation` and friends | A module is rebuilt, an encoder moves, or a wheel is remounted |
| Wheel radius | `wheelRadiusMeters` | Tread is replaced or worn, and before each event |
| Feedforward gains | `driveKs`, `driveKv` | Gearing, wheels, or robot weight changes noticeably |
| Mass, moment of inertia, wheel COF | `robotMassKg`, `robotMOI`, `wheelCOF` | The robot's weight or wheels change |

Everything downstream depends on these: kinematics assumes the wheels point where you think, odometry assumes the wheel is the size you think, path following assumes the robot accelerates the way you think.

## Measuring module zeros

An absolute encoder's raw reading for "straight ahead" is whatever the magnet ended up at. The offsets convert raw readings into a common zero.

:::steps
1. Disable the robot and leave it powered, so encoders still report.
2. **Set all four offsets to zero** in `DriveConstants` and deploy, so nothing is being subtracted yet.
3. Point all four wheels straight ahead with a straight edge across each pair, with the bevel gears facing the same way on all four modules.
4. Read each module's `turnPosition` on the dashboard or from a log. These are the raw offsets.
5. Write them into `frontLeftZeroRotation` and the rest, in radians, and deploy again.
6. **Verify.** With the wheels straight, all four `turnPosition` values should read about zero. Then drive slowly forward and confirm the robot tracks straight.
:::

Step 6 is the part people skip. A sign error or a swapped pair of modules looks completely normal in the constants file and completely wrong on the field.

:::info Why one of ours is written as a subtraction
`frontRightZeroRotation` is `new Rotation2d(1.603 - Math.PI)`. That module reads half a turn away from the others, which happens when a module is assembled with the encoder magnet flipped or the module mounted rotated. Subtracting π in the constant is a legitimate fix, and writing it as an expression keeps the measured number visible.
:::

## Wheel radius

`wheelRadiusCharacterization` spins the robot slowly in place and compares two things it can measure: how far the gyro says the robot turned, and how far the wheels say they rolled.

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="double wheelRadius =" lines=2

Each wheel travels along a circle of radius `driveBaseRadius`, so the distance it should have covered is `gyroDelta × driveBaseRadius`. Divide by the wheel rotation the encoder reported, and you have the radius that makes those agree.

:::steps
1. Put the robot on a surface with grip, with room to spin.
2. Select "Drive Wheel Radius Characterization" in the auto chooser and enable autonomous.
3. Let it spin several full rotations. More rotations means less measurement noise.
4. Disable. The result prints to the Driver Station console in meters and inches.
5. Compare it with the constant. A new 4 inch wheel measures near 0.0508 m; worn tread measures smaller.
6. Update `wheelRadiusMeters` and commit.
:::

A 2% radius error is a 2% odometry error: 10 cm over a 5 m autonomous path, which is the difference between scoring and missing.

## Feedforward gains

"Drive Simple FF Characterization" ramps voltage at 0.1 V/s while recording velocity, then fits kS and kV and prints them ([Unit 9](course:09-controls/feedforward) covers the math and the sanity check). Run it in one direction with plenty of room. The full SysId routines are also in the chooser when you want kA as well.

## What PathPlanner needs

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double robotMassKg" lines=3

| Property | Ours | How to get it |
|---|---|---|
| Mass | 74.088 kg (163 lb) | Weigh the robot with battery and bumpers |
| Moment of inertia | 6.883 kg·m² | Estimate from CAD, or measure with a spin test |
| Wheel coefficient of friction | 1.2 | The wheel manufacturer's number for your surface |

These feed PathPlanner's model of how hard the robot can accelerate. Too optimistic and paths ask for acceleration the robot can't deliver, so it falls behind its trajectory; too conservative and autos are slower than they need to be.

And as [Drive Architecture](course:10-swerve/drive-architecture) explains, the PathPlanner GUI keeps its own copy of these numbers, which has drifted from the code (finding F9). Update both.

## Keeping a calibration log

Keep one table in the team's build log, and fill a row every time you measure:

| Date | What | Value | Why | Who |
|---|---|---|---|---|
| 2026-02-14 | Wheel radius | 0.0508 m | New tread | |
| 2026-03-03 | FL zero | −1.671 rad | Module rebuilt | |

It takes a minute and it answers the question that always comes up at an event: "did anyone change this?"

:::quiz
?order Put the module zero procedure in order.
1. Set all four offsets to zero and deploy
2. Point every wheel straight ahead with a straight edge
3. Read each module's reported turn position
4. Write those values into the constants and deploy
5. Verify all four read about zero, then drive straight
> The verification step is the one that catches sign errors and swapped modules.

? What two measurements does the wheel radius characterization compare?
+ How far the gyro says the robot turned, and how far the wheels say they rolled
- Commanded velocity and measured velocity
- Battery voltage and current draw
- Two different encoders on the same wheel
> Each wheel travels a circle of radius `driveBaseRadius`, which turns one into the other.

?num Your wheel radius constant is 2% larger than reality. How far off is odometry after a 5 m autonomous path?
= 0.1 ± 0.02 m
> 2% of 5 m. Radius error scales every distance.

? Why is verifying the module zeros by driving straight worth the extra minute?
+ A sign error or swapped module looks perfectly normal in the constants and completely wrong on the field
- It warms up the motors
- It calibrates the gyro
- PathPlanner requires it
> Calibration mistakes rarely announce themselves.

? PathPlanner's model says the robot can accelerate harder than it really can. What do you see?
+ The robot falls behind its trajectory, and path following gets worse the more aggressive the path
- The robot refuses to run autos
- Odometry drifts
- The modules oscillate at rest
> The model decides what the path asks for; the robot decides what it delivers.

?tf Once the module zeros are measured at the start of the season, they don't need to change.
= false
> Any module rebuild, encoder replacement, or wheel remount changes them.
:::
