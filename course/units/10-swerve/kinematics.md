---
summary: The math that turns chassis speeds into four module states and measured module states back into chassis speeds, plus the two corrections our code applies before commanding the modules.
objectives:
  - Compute module states from chassis speeds by hand for our drivetrain
  - Explain forward kinematics and where our code uses it
  - Explain desaturation and why it scales every module together
  - Explain `discretize` and the X-stop
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## Inverse kinematics: speeds to modules

Each module sits at a known place on the robot. When the chassis translates, every module moves the same way. When the chassis rotates, each module also moves **perpendicular to its own radius**, at a speed proportional to how far it is from the center. Add the two vectors and you have that module's velocity:

```text
vx_i = vx − ω · y_i
vy_i = vy + ω · x_i

speed_i = hypot(vx_i, vy_i)
angle_i = atan2(vy_i, vx_i)
```

::diagram name="swerve-kinematics" caption="Translation gives every module the same vector. Rotation gives each a different one. Kinematics adds them."

Try it with our numbers. The driver asks for 1 m/s forward and 1 rad/s counterclockwise. The front left module is at (0.254, 0.254):

- vx = 1 − 1 × 0.254 = **0.746 m/s**
- vy = 0 + 1 × 0.254 = **0.254 m/s**
- speed = hypot(0.746, 0.254) = **0.788 m/s**, angle = atan2(0.254, 0.746) = **0.328 rad** (18.8°)

The front right module is at (0.254, −0.254), so its vx is 1 + 0.254 = 1.254 m/s: the outside of a left turn moves faster. That difference is the whole reason kinematics exists.

WPILib does this for us:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void runVelocity(ChassisSpeeds speeds)" lines=19 highlight="4,5,6"

## Forward kinematics: modules to speeds

Running it backwards answers "what is the robot actually doing?" from four measured module states. With four modules there are eight measurements for three unknowns, so WPILib solves it as a least-squares problem. For our symmetric square it comes out simple:

```text
vx = average of all vx_i
vy = average of all vy_i
ω  = Σ(x_i · vy_i − y_i · vx_i) / Σ(x_i² + y_i²)
```

Our code uses it in two places, both worth knowing:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public ChassisSpeeds getChassisSpeeds()" lines=4

- **The turret** reads it through `drive::getChassisSpeeds` to lead its shots while the robot moves.
- **Vision** reads it to reject camera measurements taken while the robot is moving or spinning too fast.

Both want *measured* speeds, not commanded ones: what the robot is doing, not what it was asked to do.

## Desaturation

Kinematics will happily return a module speed of 6 m/s for a drivetrain that tops out at 4.2. If the code sent that, the module would saturate and the **other** modules would still run at their commanded speeds, so the robot would travel in a direction nobody asked for.

Desaturation scales every module's speed by the same factor so the fastest one lands exactly at the limit:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="SwerveDriveKinematics.desaturateWheelSpeeds" lines=1

The robot ends up slower than requested but moving in **exactly** the requested direction, which is what a driver wants. It is also why full stick plus full rotation gives less translation than full stick alone: the rotation demand eats part of the budget.

## Discretize: correcting for the 20 ms step

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="ChassisSpeeds discreteSpeeds = ChassisSpeeds.discretize(speeds, 0.02);" lines=1

Kinematics assumes the chassis speeds hold for an instant. Our code commands them for a whole 20 ms loop, and if the robot is rotating during that time, the straight-line command traces a slight arc, so the robot ends up a little to the side of where the math intended. Over a long path those small errors accumulate into a visible drift.

`discretize` adjusts the commanded speeds so that, after 20 ms of simultaneous translation and rotation, the robot lands where the continuous command would have put it. It matters most when translating and spinning hard at the same time, which is exactly what shoot-on-the-move driving does.

## The X-stop

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void stopWithX()" lines=8

Pointing each module at its own radius makes the wheels form an X. Rolling the robot in any direction would require at least two modules to slide sideways, so it resists being pushed. It is the standard "hold this spot" move for defense or for staying put while shooting, and our code has it ready even though no button is bound to it today. Binding it to a driver button is a small, high-value change.

:::exercise id="u10-kinematics"
Implement both directions of swerve kinematics for our drivetrain, plus desaturation.

The tests check the three cases you should be able to predict by hand (pure translation, pure strafe, pure rotation), a combined case with the numbers from this lesson, a round trip through both directions, and desaturation with and without a speed over the limit.
---hint
Inverse kinematics is two lines per module: `vx - omega * y` and `vy + omega * x`. Then `Math.hypot` for the speed and `Math.atan2(vy, vx)` for the angle, in that argument order.
---hint
For forward kinematics, turn each state back into a vector with `speed * cos(angle)` and `speed * sin(angle)` before you average.
---hint
Desaturation compares the largest **absolute** speed against the limit. When it is under the limit, return the states unchanged; otherwise scale all of them by `maxSpeed / fastest`.
:::

:::quiz
?num The driver asks for 1 m/s forward and 1 rad/s counterclockwise. What speed does the front right module get?
= 1.28 ± 0.02 m/s
> hypot(1 + 0.254, 0.254). It's on the outside of the turn, so it runs faster than the front left.

? Why does desaturation scale every module by the same factor instead of just clipping the fastest one?
+ Scaling keeps the ratios between modules, so the robot still travels in the requested direction, just slower
- Clipping would damage the motors
- The SPARK requires equal speeds
- It makes the math simpler
> Clipping one module changes the direction of travel; scaling changes only the magnitude.

? What does `ChassisSpeeds.discretize(speeds, 0.02)` correct for?
+ The arc a robot traces when it translates and rotates during the same 20 ms step
- Encoder quantization
- CAN latency
- The difference between field-relative and robot-relative speeds
> It nudges the commanded speeds so the robot lands where the continuous command intended.

? Which uses forward kinematics: the turret's shoot-on-the-move math, or the joystick drive command?
+ The turret, which reads measured chassis speeds through `drive::getChassisSpeeds`
- The joystick drive command, which converts joystick values into module states
- Both
- Neither
> Inverse kinematics commands the robot; forward kinematics measures it.

? A driver pushes the stick fully forward and also asks for full rotation. Why does the robot translate more slowly than with the stick alone?
+ The rotation demand pushes some module speeds over the limit, so desaturation scales everything down
- The gyro limits speed during rotation
- Squaring the joystick reduces the output
- The current limit drops during rotation
> There is only so much wheel speed to go around.

? What does `stopWithX` do, and why?
+ Points each module along its own radius so the wheels form an X, which resists being pushed
- Applies the brakes electrically
- Sets all module angles to zero
- Cuts power to the drive motors
> Every direction of push would require some module to slide sideways.
:::
