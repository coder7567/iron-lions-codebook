---
summary: Use WPILib's geometry classes (Translation2d, Rotation2d, Pose2d, and their 3D cousins) to answer robot questions like "how far is the HUB?" and "which way should the turret point?"
objectives:
  - Create and combine Translation2d, Rotation2d, and Pose2d objects
  - Compute distance and direction to a target, and convert a field angle to a robot-relative angle
  - Wrap angles correctly and avoid the degrees-versus-radians trap
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## Three classes do most of the work

| Class | Represents | Made of |
|---|---|---|
| `Translation2d` | A position or a vector on the field | x and y, in meters |
| `Rotation2d` | An angle or heading | Stored internally as cosine and sine |
| `Pose2d` | Where the robot is and which way it faces | A `Translation2d` plus a `Rotation2d` |

```java
Translation2d hub = new Translation2d(4.625, 4.0);
Rotation2d facingLeft = Rotation2d.fromDegrees(90);
Pose2d robot = new Pose2d(2.0, 4.0, facingLeft);

double x = robot.getX();                          // 2.0
Rotation2d heading = robot.getRotation();         // 90°
double radians = heading.getRadians();            // 1.5708
```

All of these objects are **immutable**. Methods like `plus` and `minus` return new objects and never change the originals.

:::danger The degrees trap
`new Rotation2d(90)` means **90 radians**, about 5,157°. Use `Rotation2d.fromDegrees(90)` for degrees. This mistake compiles, runs, and quietly aims the turret somewhere absurd.
:::

## Translation2d: positions and vectors

A `Translation2d` can mean "a point on the field" or "an arrow from one point to another." Subtracting two points gives the arrow between them:

| Method | Result |
|---|---|
| `a.plus(b)`, `a.minus(b)` | Vector sum or difference |
| `a.times(2.0)`, `a.div(2.0)` | Scaled vector |
| `a.getNorm()` | Length of the vector |
| `a.getDistance(b)` | Distance between two points |
| `a.getAngle()` | Direction of the vector, as a `Rotation2d` |
| `a.rotateBy(r)` | The vector turned by an angle |
| `new Translation2d(distance, angle)` | A vector with a given length and direction |

## Rotation2d: angles that wrap themselves

Because a `Rotation2d` stores cosine and sine, it never gets confused by 350° versus −10°. `getRadians()` always returns a value in (−π, π].

```java
Rotation2d a = Rotation2d.fromDegrees(170);
Rotation2d b = Rotation2d.fromDegrees(-170);
a.minus(b).getDegrees();   // -20.0, not 340.0
```

For plain `double` angles, `MathUtil.angleModulus(radians)` wraps into (−π, π], and `MathUtil.inputModulus(value, min, max)` wraps into any range.

## Worked example: aiming the turret

This method in `Turret` answers "which way should the turret point, and how hard should we shoot?":

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private void calculationToTarget(Translation2d target)" lines=14

Walk through it with real numbers. The robot is at (2.0, 4.0) facing 90°, and the target is the blue HUB at (4.625, 4.0). Ignore the moving-robot adjustment for now (`adjustedTarget` equals `target` when the robot is still):

:::steps
1. **Vector to the target:** `adjustedTarget.minus(pose.getTranslation())` is (4.625 − 2.0, 4.0 − 4.0) = **(2.625, 0)**.
2. **Field direction:** `robotToTarget.getAngle()` is **0°**. The HUB is straight toward the red wall.
3. **Robot-relative direction:** `.minus(pose.getRotation())` is 0° − 90° = **−90°**. The robot faces left, so the HUB is to its right.
4. **Wrap:** `MathUtil.angleModulus(...)` keeps it in (−π, π]: **−1.571 rad**.
5. **Distance for the shot map:** `robotToTarget.getNorm()` is **2.625 m**, which picks the RPM and hood angle.
:::

Subtracting the robot's heading is the key idea. A field angle says where the target is on the field; a turret needs to know where it is *relative to the robot*.

## Pose2d, Transform2d, and Twist2d

- **`Transform2d`** is a movement in a robot's own frame: "go forward 1 m and turn 30°." `pose.transformBy(transform)` applies it.
- **`Twist2d`** is a small motion along an arc, used inside odometry.

The joystick code uses a pose-and-transform trick to build a vector with a given length and direction:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="private static Translation2d getLinearVelocityFromJoysticks" lines=14

It starts at the origin facing `linearDirection`, moves forward `linearMagnitude`, and takes the resulting position. `new Translation2d(linearMagnitude, linearDirection)` would produce the same vector more directly.

When the gyro is disconnected, `Drive` estimates rotation by accumulating a `Twist2d` computed from the wheels:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Update gyro angle" lines=9

## Geometry in 3D

Vision uses `Translation3d`, `Rotation3d`, `Pose3d`, and `Transform3d`, which work the same way with a Z axis and roll, pitch, and yaw. `pose3d.toPose2d()` drops the height and tilt, which is how vision results feed the 2D pose estimator. [3D Transforms](course:13-vision/3d-transforms) covers them in depth.

:::exercise id="u06-angles"
Write the angle helpers robot code needs constantly, using plain `double` radians.

- `wrapRadians` wraps into (−π, π], and `wrapPositive` into [0, 2π).
- `shortestDelta` finds the shortest turn from one angle to another, so 3.1 to −3.1 is a small positive turn, not a huge negative one.
- `withinTolerance` compares angles across the ±π seam.
- `toSparkTurnSetpoint` mimics how `ModuleIOSpark` adds a module's zero offset and wraps the setpoint.
---hint
`shortestDelta(from, to)` is just `wrapRadians(to - from)`.
---hint
For `wrapPositive`, after `%`, add 2π when the result is negative.
:::

:::exercise id="u06-aim-wpi"
Use WPILib's geometry classes to compute what the turret code computes.

- `fieldAngleToTarget` is the direction of the vector from the robot to the target.
- `robotRelativeAngleToTarget` subtracts the robot's heading and wraps.
- `distanceToTarget` uses `getDistance`.
- `inFieldOfView` checks whether a target is within a camera's field of view, centered on the robot's front.

This exercise uses real WPILib classes, so it needs the exercises project's WPILib dependencies. It builds in WPILib VS Code like every other exercise.
---hint
The whole turret calculation is three calls: `target.minus(robot.getTranslation()).getAngle().minus(robot.getRotation())`.
:::

:::quiz
?code What does this print?
```java
Rotation2d a = Rotation2d.fromDegrees(170);
Rotation2d b = Rotation2d.fromDegrees(-170);
System.out.println(Math.round(a.minus(b).getDegrees()));
```
= -20
> 170° − (−170°) is 340°, which is the same direction as −20°. `Rotation2d` always reports the wrapped angle.

? What does `new Rotation2d(90)` create?
- A rotation of 90 degrees
+ A rotation of 90 radians
- A compile error
- A rotation of 90 percent of a circle
> The constructor takes radians. Use `Rotation2d.fromDegrees(90)` for degrees.

?num The robot is at (1.0, 1.0) and the target is at (4.0, 5.0). How far away is the target, in meters?
= 5 ± 0.001 m
> The vector is (3, 4), and its length is √(3² + 4²) = 5.

? The robot faces 180° and the target is straight toward the red wall (field angle 0°). What robot-relative angle should the turret aim?
+ 180°, or equivalently −180°: directly behind the robot
- 0°
- 90°
- −90°
> Robot-relative angle = field angle − heading = 0° − 180°.

?tf `translationA.minus(translationB)` changes `translationA`.
= false
> WPILib geometry objects are immutable. `minus` returns a new `Translation2d`.
:::
