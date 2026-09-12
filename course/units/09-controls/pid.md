---
summary: What P, I, and D each do, why our gains look so different between velocity and position loops, why the turn motors need continuous input, and why almost none of our loops use I.
objectives:
  - Explain what each PID term contributes and what its failure mode looks like
  - Read our gain table and explain why the numbers differ by orders of magnitude
  - Explain continuous input and where our code enables it
  - Tune a loop in a safe order and know when to stop
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSim.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
---

## The three terms

Every loop, the controller computes an **error**: setpoint minus measurement. The three terms each react to a different aspect of that error.

| Term | Reacts to | Effect | Too much looks like | Too little looks like |
|---|---|---|---|---|
| **P** (proportional) | How far off you are now | The main push toward the setpoint | Oscillation, buzzing, overshoot | Sluggish; stops short under load |
| **I** (integral) | How long you've been off | Erases small, persistent error | Slow oscillation, windup after a stall | Steady-state error that never closes |
| **D** (derivative) | How fast the error is changing | Damping; brakes before arriving | Jitter, because it amplifies sensor noise | Overshoot and ringing |

In code, with a 20 ms loop:

```java title="What a PID controller does each loop"
double error = setpoint - measurement;
integral += error * dt;                       // dt is 0.02 s
double derivative = (error - lastError) / dt;
double output = kP * error + kI * integral + kD * derivative;
lastError = error;
```

That's the whole algorithm. The exercise at the end of this lesson is to write it, including the two details that make a real implementation usable: an integrator range and continuous input.

## Our gains

| Loop | kP | kI | kD | Feedforward |
|---|---|---|---|---|
| Drive velocity | 0.01 | 0 | 0.005 | kS 0.12349, kV 0.12293 |
| Turn position | 1.0 | 0 | 0 | none |
| Flywheel velocity | 0.001 | 0 | 0 | kS 0.2, kV 0.00193 |
| Hood position | 5.0 | 0 | 0 | none |
| Turret position | 1.0 | 0 | 0 | none |
| Intake arm position | 1.0 | 0 | 0 | none (a kS is written but commented out) |
| Intake rollers | 0.001 | 0 | 0.0001 | kV 0.001811 |
| Feeder | 0.00001 | 0 | 0 | kV 0.001811 |
| Heading (roboRIO) | 5.0 | 0 | 0.4 | none, but it uses a motion profile |

Two patterns jump out, and both come down to units.

**Velocity loops have tiny gains.** A flywheel error is measured in RPM, and being 500 RPM low is normal during a spin-up. With kP = 0.001, that error asks for 0.5 V of correction. If you used the hood's kP of 5.0 on RPM, a 500 RPM error would ask for 2,500 V, and the controller would slam to full output on every tiny disturbance.

**Position loops have large gains.** A hood error is measured in rotations, and being 0.02 rotations off is a real miss. With kP = 5.0, that error asks for 0.1 V.

So "is kP = 1.0 a big gain?" has no answer until you know the units of the error and the units of the output. **A gain is a conversion factor with an opinion.**

## Continuous input for anything that wraps

A swerve turn motor sitting at 350° with a setpoint of 10° has an error of −340° if you subtract naively, so it would take the long way around. Continuous input tells the controller the input is circular, so it uses the short way: +20°.

On the SPARK, it's part of the closed-loop configuration:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="// Configure turn motor" lines=19 highlight="14-16"

In simulation, the same idea comes from WPILib's `PIDController`:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSim.java" from="// Enable wrapping for turn PID" lines=2

The heading controller in `joystickDriveFacingTarget` enables it too. Any loop whose measurement wraps needs this, and the symptom when it's missing is unmistakable: the mechanism occasionally takes the scenic route all the way around.

:::warning Two wrapping conventions in one robot
The SPARK is configured to wrap over **0 to 2π**, and WPILib's controllers in our code wrap over **−π to π**. Both are correct; they just have to match the range the measurement is reported in. `setTurnPosition` runs its setpoint through `MathUtil.inputModulus(..., turnPIDMinInput, turnPIDMaxInput)` for exactly this reason. When you add a wrapping loop, write down which convention you chose.
:::

## Why we don't use I

Every gain table row has kI = 0. That's a deliberate and common choice in FRC:

- **Feedforward already handles steady state.** The reason a velocity loop needs a constant push is friction and back-EMF, and [Feedforward](course:09-controls/feedforward) predicts both directly. Integral is a slow way to learn the same number.
- **Windup is dangerous.** If a mechanism is blocked (say a jammed intake), the integral keeps growing. When the blockage clears, the mechanism lurches with all of that stored output. An integrator range limits the damage, which is why the exercise makes you implement one.
- **It hides problems.** A loop that needs an integral to reach its setpoint is usually telling you that something else is wrong: a bad conversion factor, a too-small gain, friction, or a mechanical bind.

Add I only when there is a real steady-state error you can measure, feedforward can't explain, and you have clamped the integral.

## Tuning in a safe order

:::steps
1. **Get the units right first.** Confirm the measurement moves the way you expect, in the units you expect, before touching a gain.
2. **Feedforward before feedback** for velocity loops: kS and kV should get the mechanism close on their own.
3. **Raise kP** until the mechanism responds quickly and just starts to oscillate, then cut it roughly in half.
4. **Add kD** if it overshoots, a little at a time. If it starts buzzing, you have added too much.
5. **Leave kI at 0** unless you have measured a steady-state error you can't explain.
6. **Check the log, not your eyes.** Graph the setpoint against the measurement and look at the shape: rise time, overshoot, and settling.
:::

Do this with the mechanism in a state that can't hurt anyone: the robot on blocks, the turret in `TESTING`, current limits in place, and someone's hand on the disable button.

:::exercise id="u09-pid"
Write a PID controller with the same behavior as WPILib's: proportional, integral with a clamped range, derivative from the previous error, continuous input for angles, and an `atSetpoint` check with a tolerance.

The tests pin down the details that bite people: the derivative is 0 on the first loop, the integral respects its range, and continuous input takes the short way around in both directions.
---hint
Compute the error first, then wrap it when continuous input is on: the range is `maxInput - minInput`, and the wrapped error belongs in `[-range/2, range/2)`. The provided `inputModulus` helper does the wrapping.
---hint
The derivative needs a "have I run before?" flag, not a `lastError` of 0. Otherwise the first loop invents a huge rate of change and kicks the output.
---hint
Clamp the integral as you accumulate it, not afterward, so a long stall can't store a value it will never work off.
:::

:::quiz
? A flywheel holds steady 200 RPM below its setpoint and never gets closer. Which term is missing or too small?
+ Feedforward, or failing that, the integral term
- Derivative
- Continuous input
- The proportional term is too large
> Persistent, unchanging error is a steady-state problem. Our robot solves it with feedforward.

? The turret oscillates around its target, buzzing back and forth. What do you try first?
+ Lower kP, then add a little kD
- Raise kP
- Add kI
- Turn off continuous input
> Oscillation is the classic too-much-P symptom, and D damps what's left.

? Why is the flywheel's kP (0.001) so much smaller than the hood's (5.0)?
+ Flywheel error is measured in RPM and hood error in rotations, so the same physical miss produces wildly different error numbers
- The flywheel motor is stronger
- The hood loop runs faster
- The flywheel uses an absolute encoder
> A gain converts error units into output units, so the number only means something with both units in hand.

? What does continuous input do for a swerve turn motor?
+ It makes the controller take the short way around, so a 350° to 10° move is 20°, not −340°
- It prevents the module from ever turning more than 90°
- It converts radians to degrees
- It enables the absolute encoder
> Any wrapping measurement needs it; the symptom without it is a module spinning the long way.

? A mechanism with a large kI gets stuck against a hard stop for three seconds, then comes free. What happens?
+ It lurches, because the integral accumulated the whole time it was stuck
- Nothing unusual
- The controller resets automatically
- The integral term becomes negative
> That's windup. Clamp the integrator, or don't use I.

?tf Adding kD always makes a loop more stable.
= false
> Derivative amplifies sensor noise. Too much of it makes a mechanism jitter or buzz.
:::
