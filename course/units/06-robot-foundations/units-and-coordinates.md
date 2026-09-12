---
summary: The units and coordinate systems every WPILib program shares, including meters, radians, and counterclockwise-positive angles, robot versus field frames, and flipping positions for the red alliance.
objectives:
  - Use WPILib's standard units and convert inputs from inches, degrees, and RPM
  - Describe the robot frame and the field frame, and tell robot-relative from field-relative motion
  - Flip blue-side field positions for the red alliance on a rotationally symmetric field
files:
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/drive/GyroIONavX.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/util/AllianceFlipUtil.java
---

## One set of units

WPILib's math classes expect **SI units**:

| Quantity | Unit | Not |
|---|---|---|
| Distance | meters | inches, feet |
| Angle | radians | degrees |
| Time | seconds | milliseconds |
| Linear speed | meters per second | ft/s |
| Angular speed | radians per second | RPM, °/s |
| Voltage | volts | percent output |

Vendor libraries sometimes use their own units. REVLib reports motor speed in RPM unless you set a conversion factor, and the NavX reports degrees. **Convert at the boundary** where data enters your code, and use SI everywhere else.

Our camera positions were measured in inches and degrees, and are converted the moment they are defined:

::source file="src/main/java/frc/robot/subsystems/vision/VisionConstants.java" from="public static final Transform3d AprilTagCamera1Transform" lines=6

WPILib also offers a typed units library (`edu.wpi.first.units`). `Drive` uses it for SysId, where `voltage.in(Volts)` turns a typed voltage measurement into a plain `double` of volts.

## Angles: counterclockwise is positive

In WPILib, a positive rotation turns **counterclockwise** when viewed from above. Rotating from +X toward +Y is positive.

Not every sensor agrees. The NavX's `getAngle()` increases **clockwise**, so our gyro code negates it:

::source file="src/main/java/frc/robot/subsystems/drive/GyroIONavX.java" from="inputs.yawPosition = Rotation2d.fromDegrees(-navX.getAngle());" lines=2

Forget that minus sign and the robot's idea of which way it turned is backward. Field-relative driving then spirals the wrong direction.

## The robot frame

The **robot frame** is attached to the robot's center:

- **+X** points **forward**, out the front of the robot.
- **+Y** points to the robot's **left**.
- **+Z** points **up**.

That is why front-left module positions are positive in both X and Y, and why our back-corner cameras have negative X. A camera pitch of −30° means tilted **up**, because positive pitch tips the camera's view down.

## The field frame

::diagram name="field-coordinates" caption="WPILib's field frame on the 2026 field, drawn to scale. The dashed line shows a 180° rotation through the field center."

WPILib's field frame puts the **origin at the corner of the blue alliance wall**:

- **+X** runs from the blue wall toward the red wall, 16.541 m long.
- **+Y** runs across the field, 8.069 m wide. It is left for a blue driver looking downfield.
- A **heading of 0** points toward the red wall.

This is sometimes called "always blue origin." Every pose from odometry, vision, and PathPlanner uses this one frame, **no matter which alliance you are on**.

## Robot-relative versus field-relative motion

A **robot-relative** speed says "move forward 1 m/s" in the robot's own frame. A **field-relative** speed says "move toward the red wall at 1 m/s," whichever way the robot faces. Field-relative driving is easier for drivers, because pushing the stick away from you always moves the robot away from you.

Converting requires the robot's heading. Our joystick command also rotates by π when we are red, since red drivers stand at the other end of the field:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="boolean isFlipped = DriverStation.getAlliance().isPresent()" lines=3

## Flipping for the red alliance

Our code, and PathPlanner, write field positions for the **blue** side and convert them when we are red. REBUILT's field is **rotationally symmetric**, so the conversion turns a point 180° around the field center:

```text
x_red = 16.541 − x_blue
y_red =  8.069 − y_blue
heading_red = heading_blue + π
```

::source file="src/main/java/frc/robot/util/AllianceFlipUtil.java" from="public static double applyX" lines=22

:::warning Know your field's symmetry
Not every game is rotational. The 2024 field was mirrored, which flips only X, and PathPlanner has a setting for which symmetry to use. Each season, check the official AprilTag layout. If a red-wall tag flips onto a blue-wall tag with both X and Y changed, the field is rotational.
:::

:::exercise id="u06-flip"
Write a pure-Java version of `AllianceFlipUtil` for the 2026 field.

- `flipX` and `flipY` use the field dimensions.
- `flipHeading` adds π and wraps into (−π, π].
- `flip` converts a whole pose, and flipping twice must return the original.
- `forAlliance` flips only for red, and `inAllianceZone` checks the correct end of the field.

One test uses real AprilTag positions: tag 13 on the red wall must flip onto tag 29 on the blue wall.
---hint
Java's `%` keeps the sign of the left side. After `angle % (2 * Math.PI)`, add or subtract 2π once to land in (−π, π].
---hint
At exactly π, keep π rather than turning it into −π: only subtract 2π when the value is **greater than** π.
:::

:::quiz
? The NavX reports it has rotated 90 degrees clockwise. What should the robot's WPILib heading change by?
- +90°
+ −90°
- +180°
- No change
> WPILib angles are counterclockwise-positive, so a clockwise turn is negative. That is why `GyroIONavX` negates `getAngle()`.

? In the robot frame, which direction is +Y?
- Forward
+ Toward the robot's left
- Toward the robot's right
- Up
> +X is forward, +Y is left, and +Z is up.

?num A blue-side target sits at x = 1.0 m. With rotational symmetry, what is its x coordinate on the red side?
= 15.541 ± 0.001 m
> 16.541 − 1.0 = 15.541 m.

?tf On the red alliance, our robot's odometry reports positions measured from the red alliance wall.
= false
> Every pose uses the same field frame, with the origin at the blue alliance wall, regardless of alliance.

? Why does `joystickDrive` rotate the field-relative conversion by π on the red alliance?
+ Red drivers stand at the opposite end of the field, so "away from me" points toward −X
- Red alliance robots have their gyros mounted backward
- PathPlanner requires it
- The red field is mirrored
> Rotating by π keeps "push the stick away" meaning "drive away from the driver" for both alliances.
:::
