---
summary: A line-by-line reading of DriveCommands.joystickDrive (axis signs, a circular deadband, input squaring, field-relative conversion, and the red-alliance flip), followed by a diagnosis of the Move Forward auto.
objectives:
  - Explain why `RobotContainer` negates each controller axis
  - Compute the deadband and squaring output for any stick position
  - Convert field-relative speeds to robot-relative speeds by hand
  - Explain the red-alliance flip and why field-relative driving depends on a correct heading
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## From stick to supplier

The drive's default command gets its inputs from three lambdas:

::source file="src/main/java/frc/robot/RobotContainer.java" from="drive.setDefaultCommand(" lines=6

Every axis is negated. Xbox controllers follow an old flight-stick convention: pushing a stick **forward** reads **negative** Y, and pushing it **left** reads **negative** X. WPILib's field frame points +X away from the blue alliance wall, +Y to the left (seen from the blue driver station), and positive rotation counterclockwise. Negating each axis lines the two up:

| The driver... | Axis reading | Supplier value | Meaning in the field frame |
|---|---|---|---|
| Pushes the left stick forward | `getLeftY()` = −1 | x = +1 | +X, away from the driver station |
| Pushes the left stick left | `getLeftX()` = −1 | y = +1 | +Y, to the driver's left |
| Pushes the right stick left | `getRightX()` = −1 | omega = +1 | Counterclockwise |

Suppliers are lambdas, so the command reads the controller fresh every loop.

## Step 1: linear velocity with a circular deadband

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="private static Translation2d getLinearVelocityFromJoysticks" lines=14

**Deadband.** Sticks rarely rest at exactly zero. `MathUtil.applyDeadband(value, 0.1)` returns 0 for anything within 0.1 of center. It rescales everything beyond that so 0.1 through 1 maps smoothly to 0 through 1: the result is (|value| − 0.1) / 0.9, with the sign kept.

**Circular, not per-axis.** The deadband is applied to the stick's *distance from center*, `Math.hypot(x, y)`, not to x and y separately. A per-axis deadband creates a plus-shaped dead zone: pushing diagonally a little does nothing, and motion feels like it snaps to the axes. Measuring the magnitude gives a round dead zone that treats every direction the same.

**Squaring.** Squaring the magnitude gives fine control near center while still reaching full speed:

| Stick magnitude | After deadband | After squaring | Speed at 4.2 m/s max |
|---|---|---|---|
| 0.10 | 0.000 | 0.000 | 0 m/s |
| 0.25 | 0.167 | 0.028 | 0.12 m/s |
| 0.50 | 0.444 | 0.198 | 0.83 m/s |
| 0.75 | 0.722 | 0.522 | 2.19 m/s |
| 1.00 | 1.000 | 1.000 | 4.20 m/s |

**Direction.** The last statement builds a pose facing the stick's direction, moves it forward by the magnitude, and keeps the resulting translation. That is a roundabout way to write (magnitude × cos θ, magnitude × sin θ).

:::info Past the edge of the stick
Some controllers report a magnitude slightly above 1 at the diagonals, which makes the requested speed a little higher than the maximum. `Drive.runVelocity` handles it: `desaturateWheelSpeeds` scales every module down together whenever any module would exceed 4.2 m/s.
:::

## Step 2: rotation

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="// Apply rotation deadband" lines=6

Rotation uses the same deadband and squaring on one axis. `omega * omega` alone would turn a left turn into a right turn, because squaring removes the sign. `Math.copySign` puts the sign back.

The result is scaled by the maximum angular speed:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public double getMaxAngularSpeedRadPerSec()" lines=3

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double trackWidth" lines=3

The modules sit on a 20 in square, so each module is √(0.254² + 0.254²) ≈ 0.359 m from the center. When the robot spins in place with every wheel at 4.2 m/s, it turns at 4.2 ÷ 0.359 ≈ **11.7 rad/s**, about 670° per second.

## Step 3: field-relative to robot-relative

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="// Convert to field relative speeds & send command" lines=10 highlight="8,10"

The `ChassisSpeeds` built here is **field-relative**: "move toward +X." The modules need **robot-relative** speeds: "move toward the robot's front." `ChassisSpeeds.fromFieldRelativeSpeeds(speeds, heading)` rotates the velocity vector by the negative of the robot's heading θ:

```text
vx_robot =  vx_field · cos θ + vy_field · sin θ
vy_robot = −vx_field · sin θ + vy_field · cos θ
omega    =  omega (rotation speed is the same in both frames)
```

Worked example: the robot faces +Y (θ = 90°), and the driver pushes forward, asking for 4.2 m/s toward +X.

- vx_robot = 4.2 · cos 90° + 0 = 0
- vy_robot = −4.2 · sin 90° + 0 = −4.2 m/s

The robot drives toward its own right side, and its right side faces field +X, so it moves exactly where the driver asked.

Then `Drive.runVelocity` takes over:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void runVelocity(ChassisSpeeds speeds)" lines=6

It corrects for the robot turning during a 20 ms step (`discretize`), converts the speeds into four module states, and desaturates them. [Unit 10](course:10-swerve/kinematics) covers these steps.

## Step 4: the red-alliance flip

From the red driver station, "forward" is −X. `joystickDrive` adds π to the heading before converting whenever the Driver Station reports the red alliance. That rotates the whole joystick frame by 180°, so pushing forward drives toward −X.

Worked example: a red robot faces away from its driver, so θ = 180°. The driver pushes forward, asking for vx_field = 4.2 m/s. The conversion uses 180° + 180° = 360°, so vx_robot = 4.2 · cos 360° = +4.2 m/s. The robot drives out its front, toward −X, away from the red driver.

The alliance check runs **inside the lambda**, every loop. That's important, because the alliance is unknown when `RobotContainer` creates the command at boot.

:::warning Field-relative driving is only as good as the heading
`drive.getRotation()` comes from the pose estimator. The heading starts at 0 when the code boots, and it changes only when:

- the gyro measures the robot rotating,
- the pose is reset, for example by a PathPlanner auto that resets odometry to its starting pose, or
- vision measurements that saw several tags gradually correct it. `AprilTagVision` gives single-tag measurements an angular standard deviation of 99999, so they don't affect heading.

Suppose a red robot boots facing away from its driver, and nothing resets its pose. The estimator believes the heading is 0° while the real heading is 180°. The flip then makes "forward" drive the robot **toward** the driver. Check field-relative driving in the pit before any match you start without an auto, and consider adding a driver button that resets the heading, as the AdvantageKit template does.
:::

## Diagnosing the Move Forward auto

The auto chooser includes this option:

::source file="src/main/java/frc/robot/RobotContainer.java" from="Move Forward" lines=2

Run its inputs through the four steps. Here x = 0 and y = −√(1/4.2) ≈ −0.488:

1. The magnitude is 0.488. After the deadband it is (0.488 − 0.1) ÷ 0.9 = 0.431.
2. Squared, it is 0.186.
3. The direction is atan2(−0.488, 0) = −90°, which points along field −Y.
4. Scaled by 4.2, the speed is **0.78 m/s along field −Y**.

The square root suggests the goal was exactly 1 m/s: squaring √(1/4.2) gives 1/4.2, and multiplying by 4.2 gives 1. But the deadband rescales the magnitude *before* squaring. The value is also passed as `y`, so the robot moves sideways across the field, not away from the alliance wall. Because the command is `Commands.run`, it never finishes; it drives until autonomous ends. This is finding F17 in the [code audit](course:reference/code-audit).

A robot-relative command avoids the deadband, the alliance flip, and the heading problem:

```java title="Suggested fix: drive out the robot's front at 1 m/s for 2 seconds"
autoChooser.addOption(
    "Move Forward",
    Commands.run(() -> drive.runVelocity(new ChassisSpeeds(1.0, 0.0, 0.0)), drive)
        .withTimeout(2.0)
        .finallyDo(drive::stop));
```

:::exercise id="u07-joystick"
Recreate the math of `joystickDrive` in plain Java without WPILib: the circular deadband, squaring, field-to-robot conversion, and the red-alliance flip. One test checks this lesson's Move Forward numbers. `applyDeadband` is provided and behaves like `MathUtil.applyDeadband`.
---hint
Get the magnitude with `Math.hypot(x, y)` and the direction with `Math.atan2(y, x)`. Apply the deadband to the magnitude and square it, then rebuild the vector as `magnitude * Math.cos(direction)` and `magnitude * Math.sin(direction)`.
---hint
To convert from field-relative to robot-relative, rotate by the negative heading: `vxRobot = vx * cos(-θ) - vy * sin(-θ)` and `vyRobot = vx * sin(-θ) + vy * cos(-θ)`.
---hint
On the red alliance, add π to the heading before converting. Nothing else changes.
:::

:::quiz
?num The driver pushes the left stick halfway, a magnitude of 0.5. How fast does the robot drive?
= 0.83 ± 0.01 m/s
> (0.5 − 0.1) ÷ 0.9 = 0.444. Squared, that's 0.198, and 0.198 × 4.2 = 0.83 m/s.

? Why does `RobotContainer` pass `-controller.getLeftY()` for x?
+ Pushing the stick forward reads negative Y, and forward should mean +X
- Negative values make the robot slower
- The field's X axis points toward the driver station
- WPILib requires suppliers to return negative numbers
> Controller axes use a flight-stick convention, so negating lines them up with the field frame.

? What problem does applying the deadband to the stick's magnitude avoid?
+ A plus-shaped dead zone that ignores small diagonal inputs and makes motion snap to the axes
- The robot exceeding its maximum speed
- The robot spinning when the driver only wants to translate
- Stick drift on the rotation axis
> A magnitude deadband is round, so every direction behaves the same.

?num A robot's heading is 90°. The field-relative command is vx = 2 m/s, vy = 0. What robot-relative vy is sent to the drive?
= -2 ± 0.01 m/s
> vy_robot = −vx · sin 90° + vy · cos 90° = −2 m/s. The robot moves toward its right side, which faces +X.

? A red robot boots facing away from its driver. No auto runs, and vision hasn't corrected the heading. The driver pushes the stick forward. What happens?
+ The robot drives toward the driver, because the estimator thinks its heading is 0° instead of 180°
- The robot drives away from the driver
- The robot doesn't move until vision sees a tag
- The robot spins in place
> The flip assumes the pose's heading is correct. With a wrong heading, the conversion is off by 180°.

?num According to the analysis in this lesson, how fast does the Move Forward auto drive?
= 0.78 ± 0.01 m/s
> The deadband rescales the 0.488 magnitude to 0.431 before squaring, so the result is 0.186 × 4.2 ≈ 0.78 m/s.

?tf `joystickDrive` checks the alliance color once, when `RobotContainer` creates the command.
= false
> It checks inside the `Commands.run` lambda, every loop.
:::
