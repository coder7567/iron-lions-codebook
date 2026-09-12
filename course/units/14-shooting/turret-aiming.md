---
summary: How a target's field position becomes a turret setpoint: the angle math, the operator's trim, the arc the turret cannot reach, and the clamps that keep it inside its travel.
objectives:
  - Compute a turret angle from a robot pose and a target position
  - Explain the offset, the wrap, and the deadzone shift in `setTurretAngle`
  - Explain what the driver's rumble is telling them
  - Trace an aiming problem to the pose, the target, or the mechanism
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## From a pose to an angle

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private void calculationToTarget(Translation2d target)" lines=14 highlight="2,3,4,5"

Four lines do the aiming:

1. **Adjust the target** for the robot's motion ([the next lesson](course:14-shooting/shoot-on-the-move)).
2. **`robotToTarget`** is the vector from the robot to that adjusted point, in the field frame.
3. **`.getAngle().minus(pose.getRotation())`** converts the field-frame angle into the robot's frame by subtracting the robot's heading.
4. **`MathUtil.angleModulus`** wraps the result into −π to π, so the turret is asked for the short way around.

Worked example. The robot is at (2.0, 4.0) facing 90°, and the HUB is at (4.625, 4.0):

- `robotToTarget` = (2.625, 0.0), whose angle is **0°** in the field frame.
- Subtract the heading: 0° − 90° = **−90°**. The target is off the robot's right side.
- Wrapped, that is still −90°, or −1.571 rad.

Everything else in this lesson happens to that number on its way to the motor.

## What the IO layer does with it

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="public void setTurretAngle(double angle)" lines=10 highlight="3,4,6,8,9,10"

| Step | Effect |
|---|---|
| `angle += turretOffset` | Adds the mechanism's zero offset (π) plus whatever trim the operator has dialed in |
| `MathUtil.inputModulus(angle, -PI, PI)` | Wraps into one turn |
| `inDeadzone = angle < 2.022 && angle > 1.6` | Flags a request the turret cannot reach |
| `if (angle > 2.022 && angle < PI) angle -= 2 * PI` | Reaches a far-forward angle by going the other way around |
| `MathUtil.clamp(angle, -4.261, 1.6)` | Keeps the setpoint inside the mechanism's travel |

The turret's travel runs from −4.261 rad to +1.6 rad, which is a bit less than a full turn. What is missing is the arc between **1.6 and 2.022 rad**: about 24°, directly past the forward soft limit. Aim there and the clamp puts the turret at 1.6 and it sits pointing near, but not at, the target.

That is what the rumble means. `inDeadzone` feeds `Turret.getRumble()`, which feeds the Superstructure's rumble condition, which buzzes the driver's controller. The message is not "something is broken"; it is **"move the robot a little and I can hit this."**

## The operator's trim

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="public static final double turretOffsetChange" lines=1

Each operator bumper press shifts `turretOffset` by ±0.05 rad, about 2.9°, and every setpoint afterward carries it. It is the mechanism's answer to "we're consistently missing to one side."

Three things to know about it:

- It is **added before the wrap**, so a trim can push a request into or out of the deadzone.
- It **resets to π when code restarts**, because it lives in the IO object.
- It is **logged** as `turretOffset`, so a log tells you what trim was dialed in during a match.

If the robot needs the same trim every match, that is not a trim; that is a calibration that belongs in the constants.

## When aiming looks wrong

Aiming has three inputs, and a miss comes from exactly one of them:

| Suspect | How to check | Typical cause |
|---|---|---|
| **The pose** | `Odometry/Robot` against where the robot actually is | Vision disagreement, bad starting pose, wheel slip |
| **The target** | `Calculations/target` and `DistanceToHub` | The hard-coded HUB y of 4.0 versus the tag-derived 4.035 (finding F15), or the wrong alliance |
| **The mechanism** | `Turret/turretAngle` against `turretSetAngle` | The boot-time seed (finding F23), the trim, a mechanical bump |

Check them in that order, because a wrong pose makes the other two look wrong too. `FieldBasedTurret`, which the Superstructure logs as the turret angle plus the robot's heading, is the fastest single number for "where is the turret actually pointing on the field?"

:::exercise id="u14-aim"
Write the aiming chain: the field angle to a target, the robot-relative angle, the deadzone test, and the setpoint transformation with its offset, wrap, far-side shift, and clamp.

The tests include the case that surprises people: a request inside the deadzone is silently clamped to the soft limit, which is why the rumble exists.
---hint
`fieldAngleTo` is one `Math.atan2(targetY - robotY, targetX - robotX)`. Note the argument order: y first.
---hint
`robotRelativeAngle` subtracts the heading and wraps. Do both, in that order, and the provided `wrap` handles the second part.
---hint
`toTurretSetpoint` follows the four steps in the class comment exactly, in order. The deadzone check happens on the wrapped angle, before the far-side shift.
:::

:::quiz
?num The robot is at (2.0, 4.0) facing 90 degrees. The HUB is at (4.625, 4.0). What turret angle does the code compute, in degrees?
= -90 ± 1
> The field angle is 0 degrees; subtracting the 90 degree heading puts the target off the robot's right side.

? What does `MathUtil.angleModulus` do in `calculationToTarget`?
+ Wraps the angle into −π to π so the turret takes the short way around
- Converts degrees to radians
- Clamps the angle to the soft limits
- Applies the operator's trim
> Wrapping and clamping are different jobs; both happen, in different places.

?num How wide is the arc the turret cannot reach?
= 0.422 ± 0.005 rad
> From the 1.6 rad soft limit to 2.022 rad, about 24 degrees.

? The driver's controller rumbles while the turret is trying to aim. What is it saying?
+ The requested angle is in the unreachable arc; moving the robot slightly would fix it
- The flywheel is not at speed
- The intake is jammed
- Vision lost the tags
> `inDeadzone` is one of the three rumble conditions.

? An operator presses the trim bumper four times in one direction. How much does the aim shift?
+ 0.2 rad, about 11 degrees, on every setpoint until the code restarts
- Nothing until the turret re-homes
- 0.05 rad total
- It shifts the robot's heading estimate
> Each press is 0.05 rad, and the offset lives in the IO object.

? Shots consistently miss to the same side all weekend. What is the right fix?
+ Find the cause (pose, target constant, or mechanism zero) and change the constant, rather than dialing the same trim every match
- Add more entries to the shot map
- Raise the flywheel tolerance
- Increase the turret's kP
> A trim you apply every match is a calibration wearing a disguise.
:::
