---
summary: Arithmetic, integer division, remainders, the Math class, floating-point precision, and the unit conversions that every mechanism depends on.
objectives:
  - Predict the result of integer division, remainder, and mixed int/double math
  - Use Math methods like hypot, atan2, signum, and copySign the way our code does
  - Convert between inches, meters, degrees, radians, RPM, and rad/s without mistakes
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/RobotContainer.java
---

## Arithmetic operators

| Operator | Meaning | Example | Result |
|---|---|---|---|
| `+` | add | `4.2 + 1` | `5.2` |
| `-` | subtract | `trackWidth - 0.1` | |
| `*` | multiply | `2 * Math.PI` | `6.283…` |
| `/` | divide | `20 / 2.0` | `10.0` |
| `%` | remainder | `10 % 4` | `2` |

Java follows the usual order of operations: `*`, `/`, and `%` before `+` and `-`. Use parentheses whenever order matters, even if you think you know the rules. Parentheses are free and make intent obvious:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double driveEncoderPositionFactor" lines=4

## Integer division: the classic robot bug

When **both** sides of `/` are `int`, Java does **integer division** and throws away the remainder:

```java
7 / 2        // 3, not 3.5
1 / 4        // 0
1.0 / 4      // 0.25, one double makes the whole expression double
(double) 1 / 4   // 0.25, cast first, then divide
```

This mistake produces no error and no warning. The math is just wrong. Look at this line from our auto chooser:

::source file="src/main/java/frc/robot/RobotContainer.java" from="autoChooser.addOption(\"Move Forward\"" lines=2

`1/4.2` works because `4.2` is a `double`. If someone "cleaned it up" to `1/4`, the result would silently become `0`, and the robot would never move.

:::warning When in doubt, write the .0
Write `2.0` instead of `2` in calculations with measurements. It costs nothing and makes integer division impossible.
:::

## Remainder and negative numbers

`%` gives the remainder after division. It is great for things that wrap around, like counting loops or angles. Be careful with negative numbers, because in Java **the result takes the sign of the left side**:

```java
10 % 4     //  2
-7 % 3     // -1 (not 2)
370 % 360  // 10
-10 % 360  // -10, not 350
```

That last line is why angle-wrapping in robot code uses helpers like WPILib's `MathUtil.inputModulus` instead of a bare `%`. You will write your own in [Unit 6](course:06-robot-foundations/geometry).

## Updating variables

```java
jamCount++;              // add 1
unjamCount--;            // subtract 1
totalCurrent += amps;    // same as totalCurrent = totalCurrent + amps
voltage *= 0.5;          // same as voltage = voltage * 0.5
```

## The Math class

Java's built-in `Math` class does the heavy lifting. These all appear in Rebuilt-2026:

| Method | Returns | Used for |
|---|---|---|
| `Math.PI` | π ≈ 3.14159 | Radians everywhere |
| `Math.abs(x)` | Distance from zero | Error checks like `Math.abs(angle - setpoint) < tolerance` |
| `Math.hypot(x, y)` | √(x² + y²) | Distance from joystick X and Y, and drive base radius |
| `Math.atan2(y, x)` | Angle of the point (x, y), in radians | Direction the joystick is pushed |
| `Math.sqrt(x)` | Square root | |
| `Math.signum(x)` | −1.0, 0.0, or 1.0 | Which way a motor is spinning |
| `Math.copySign(a, b)` | `a` with the sign of `b` | Squaring a joystick value while keeping its direction |
| `Math.max(a, b)`, `Math.min(a, b)` | Larger or smaller value | Clamping outputs |

Here the drive command uses three of them to shape joystick input:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="private static Translation2d getLinearVelocityFromJoysticks" lines=9

`Math.hypot(x, y)` measures how far the stick is pushed in any direction, and `Math.atan2(y, x)` finds which direction. Squaring the magnitude makes small stick movements gentler, so drivers get precise control near the center.

:::tip atan2 argument order
It is `Math.atan2(y, x)`, **y first**. Swapping them is one of the most common geometry bugs in FRC code.
:::

## Floating-point numbers are close, not exact

`double` values are stored in binary, so most decimals are approximations:

```java
System.out.println(0.1 + 0.2);          // 0.30000000000000004
System.out.println(0.1 + 0.2 == 0.3);   // false
```

**Never compare doubles with `==`.** Compare with a tolerance instead:

```java
boolean atAngle = Math.abs(turretAngle - targetAngle) < 0.05;   // within 0.05 rad
```

That is what `TurretConstants.turretTolerance = 0.05` is for, and why every numeric JUnit test uses an allowed error, like `assertEquals(0.508, result, 1e-9)`.

## Units: the conversions robots live on

Motor controllers and sensors report in their own units. Mechanisms need SI units. Conversions connect them.

| Convert | Multiply by | Why |
|---|---|---|
| inches → meters | 0.0254 | Field and robot measurements |
| degrees → radians | π / 180 | WPILib angles are radians |
| RPM → rad/s | 2π / 60 | Motor speeds |
| motor rotations → wheel radians | 2π / reduction | Encoder counts through a gearbox |

Our drive encoder factors combine these steps. With a 5.9 : 1 reduction, one motor rotation turns the wheel 2π / 5.9 ≈ **1.065 radians**. Multiply by the wheel radius, 0.0508 m, and you get meters traveled.

:::team In our code
`driveBaseRadius` is `Math.hypot(trackWidth / 2.0, wheelBase / 2.0)`, the distance from the robot's center to a module. With modules 0.254 m from center in X and Y, that is about **0.359 m**. `Drive.getMaxAngularSpeedRadPerSec()` divides the 4.2 m/s top speed by it: about 11.7 rad/s, or nearly two full spins per second.
:::

:::exercise id="u02-units"
Write the conversions in `UnitConversions`, then combine them to compute how fast a wheel moves.

1. `inchesToMeters` and `metersToInches` use `METERS_PER_INCH`.
2. `degreesToRadians` and `rpmToRadiansPerSecond` use `Math.PI`.
3. `motorRotationsToWheelRadians(reduction)` should equal `DriveConstants.driveEncoderPositionFactor` for a reduction of 5.9.
4. `wheelSpeedMetersPerSecond` should reuse your other methods rather than repeating their math.
---hint
Avoid integer division. `rpm * 2 * Math.PI / 60` is safe because `Math.PI` is a double, but writing `60.0` removes any doubt.
---hint
Wheel speed in rad/s is the motor's rad/s divided by the reduction. Linear speed is angular speed times the radius.
:::

:::quiz
?code What does this print?
```java
System.out.println(7 / 2);
```
= 3
> Both operands are `int`, so the result is truncated to 3.

?code What does this print?
```java
System.out.println(-7 % 3);
```
= -1
> In Java, the remainder takes the sign of the left operand. That is why robot code uses helpers for angle wrapping.

? What is stored in `x` after `double x = (double) 1 / 4;`?
- 0.0
+ 0.25
- 4.0
- It does not compile
> The cast happens first, making `1.0 / 4`, which is double division.

?num `driveBaseRadius = Math.hypot(0.254, 0.254)`. What is it, in meters?
= 0.3592 ± 0.0005 m
> √(0.254² + 0.254²) = 0.254 × √2 ≈ 0.3592 m.

?tf `0.1 + 0.2 == 0.3` evaluates to `true` in Java.
= false
> Floating-point rounding makes the sum 0.30000000000000004. Compare doubles with a tolerance instead.

? Why does the joystick code use `Math.copySign(omega * omega, omega)` instead of `omega * omega`?
+ Squaring always gives a positive number, so copySign restores the direction the driver pushed
- It is faster than multiplying
- `omega * omega` would overflow
- It converts omega from degrees to radians
> Squaring −0.5 gives +0.25, which would spin the wrong way. `copySign` keeps the squared size and the original sign.
:::
