---
summary: Feedforward predicts the voltage a mechanism needs instead of waiting for error to build up. Here is the model our drive uses, where the gains come from, and how to sanity-check them.
objectives:
  - Explain kS, kV, kA, and kG and what each one models
  - Read the feedforward in `setDriveVelocity` and in our REVLib closed-loop configurations
  - Fit kS and kV from characterization samples and check the result against the robot
  - Spot a feedforward whose units don't match its setpoint
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
---

## Predict, then correct

PID is reactive: it needs an error before it does anything. Feedforward is predictive. You already know roughly what voltage holds a wheel at 40 rad/s, so send that voltage, and let PID handle only the difference between your prediction and reality.

The result is a loop that reaches its setpoint quickly with small gains, which means less oscillation and less noise amplification. On a well-characterized flywheel, feedforward does 90% of the work.

| Gain | Models | Units (for our drive) |
|---|---|---|
| **kS** | Static friction: the voltage needed just to start moving | volts |
| **kV** | Speed: the voltage needed to *hold* a velocity against back-EMF | volts per rad/s |
| **kA** | Mass: the extra voltage needed to *change* velocity | volts per rad/s² |
| **kG** | Gravity: constant push for an arm or elevator | volts |

Our code uses kS and kV. There is no kG anywhere, which is worth knowing: the intake arm fights gravity with proportional gain alone, and a `kS` for it exists in `IntakeConstants` but is commented out of the configuration.

## The drive's model

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setDriveVelocity(double velocityRadPerSec)" lines=10 highlight="2,3,8,9"

Two things are happening:

1. `driveKs * signum(velocity) + driveKv * velocity` is the prediction, in volts. `signum` is what makes kS push in the direction of travel; at a velocity of exactly 0, it contributes nothing.
2. That voltage is handed to the SPARK as an **arbitrary feedforward** alongside the setpoint, so the controller adds it to its own PID output. The prediction is computed on the roboRIO; the correction happens on the motor controller.

REVLib can also hold the gains itself. The flywheel does it that way:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from=".pid(TurretConstants.flywheelP" lines=4

Same model, different home. Gains stored in the controller survive as configuration; gains computed in `setDriveVelocity` can use live values from the dashboard, which matters for tuning.

## Where the numbers come from

`DriveCommands.feedforwardCharacterization` drives the wheels with a slowly rising voltage and records velocity and voltage every loop. When the command ends, it fits a line:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="double kS = (sumY * sumX2" lines=2

That is ordinary least squares for `volts = kS + kV × velocity`. The **intercept** is kS, the voltage the mechanism needs before it moves at all, and the **slope** is kV, the volts per unit of speed. The routine prints both to the console, and you copy them into `DriveConstants`.

Two cautions about this particular implementation:

- It ignores the sign of the velocity, so run it in one direction only. Mixing forward and reverse samples corrupts the intercept.
- Every sample is weighted equally, including the ones from the first moments when the wheels haven't broken free yet. Those samples pull kS up.

:::tip Sanity-check the gains before trusting them
The model predicts the free speed: at 12 V, velocity = (12 − kS) / kV. With our drive's kS = 0.12349 and kV = 0.12293, that's about 96.6 rad/s at the wheel, and multiplying by the 0.0508 m wheel radius gives about **4.9 m/s**. `maxSpeedMetersPerSec` is set to 4.2 m/s, comfortably under the prediction, which is what you want: the drive can still correct at top speed instead of running out of voltage.

If the same arithmetic had produced 3 m/s, the robot could never reach its commanded top speed, and every module would saturate while the code kept asking for more.
:::

## Feedforward-only loops

A velocity loop doesn't strictly need PID at all. The horizontal rollers are configured with no proportional or derivative gain and only a kV:

::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from=".pid(IntakeConstants.horizontal1P" lines=3

For a roller that just needs to move FUEL along, "close enough" is genuinely fine, and pure feedforward can't oscillate.

:::warning Check these units on the robot
The horizontal rollers' velocity is in RPM, because no velocity conversion factor is configured, and their kV is 1.167. Read as volts per RPM, the prediction reaches 12 V at about **10 RPM**, so any commanded speed saturates the output and the rollers effectively run open loop at full power. They are commanded at 9000 RPM, so the robot behaves the way the team expects, but not for the reason the configuration suggests.

That value looks like a kV from a different unit system. Before changing it, measure: command a mid-range speed, log `horizontal1Speed` against its setpoint, and see whether the rollers actually regulate. If they don't, converting the constant into volts per RPM (or adding a velocity conversion factor) makes the configuration mean what it says.
:::

:::exercise id="u09-feedforward"
Implement the voltage model and the least-squares fit our characterization routine uses, then use the model to predict a free speed.

The tests check the model in both directions, recover known gains from clean and noisy samples, reject bad inputs, and confirm the drive's predicted free speed of about 4.9 m/s.
---hint
`kS * Math.signum(velocity) + kV * velocity`. At velocity 0, `signum` is 0, so the whole thing is 0.
---hint
For the fit, accumulate `sumX`, `sumY`, `sumXY`, and `sumX2` in one pass, then apply the two formulas from the class comment. The denominator `n * sumX2 - sumX * sumX` is shared.
---hint
`maxVelocity` has to handle a voltage below kS: there is not enough to overcome friction, so the answer is 0, not a negative velocity.
:::

:::quiz
? What does kS model?
+ The voltage needed to overcome static friction and start moving
- The voltage needed to hold a speed
- The stall current of the motor
- The mass of the robot
> It is multiplied by the sign of the velocity so it always pushes in the direction of travel.

? Why does the drive's feedforward multiply kS by `Math.signum(velocity)`?
+ So the friction term pushes in the direction of travel and contributes nothing at zero
- To convert radians to meters
- To limit the output to 12 volts
- To handle the red alliance flip
> Without it, a reverse setpoint would fight an extra constant voltage.

?num Using our drive's kS of 0.12349 and kV of 0.12293, what velocity does the model predict at 12 V?
= 96.6 ± 0.5 rad/s
> (12 − 0.12349) / 0.12293. Multiply by the 0.0508 m wheel radius to get about 4.9 m/s.

? The characterization fit gives an intercept and a slope. Which is which?
+ The intercept is kS and the slope is kV
- The intercept is kV and the slope is kS
- The intercept is kA
- Both are kV, for the two directions
> The fit is `volts = kS + kV × velocity`.

? Why should the feedforward characterization be run in one direction only?
+ The fit ignores the sign of velocity, so mixing directions corrupts the intercept
- The motors are inverted in reverse
- The encoder can't read negative velocities
- Autonomous only runs forward
> kS applies with a sign; a single-direction fit keeps that consistent.

? A velocity loop is configured with kP = 0, kD = 0, and a kV large enough to saturate at almost any setpoint. How does it actually behave?
+ Like an open-loop command at full output
- Like a well-tuned velocity loop
- It refuses to move
- It oscillates
> That's what the horizontal rollers do today, which is fine for rollers but worth knowing.
:::
