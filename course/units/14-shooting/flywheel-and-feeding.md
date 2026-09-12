---
summary: The two motors that throw FUEL, the gate that decides when a ball is allowed into them, and the tolerance that decides what "at speed" means.
objectives:
  - Describe the flywheel's motors, control mode, and idle behavior
  - Explain the feeder gate and both conditions behind it
  - Judge whether the flywheel tolerance is set well
  - Describe finding F3 and what it breaks in the logs
files:
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
---

## Two motors, one wheel

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="flywheelFollower = new SparkFlex(10" lines=6

CAN 9 leads and CAN 10 follows, inverted, because the two motors sit on opposite sides of the same shaft. Only the leader is ever commanded; the follower mirrors it over CAN. Both are set to **coast**, which is the right choice for a heavy spinning mass: braking 2,500 RPM of steel would punish the belt and the gearbox for no benefit.

The leader runs a velocity loop with a feedforward, configured on the controller itself:

| Gain | Value | Job |
|---|---|---|
| kS | 0.2 V | Overcome friction |
| kV | 0.00193 V per RPM | Hold a speed, roughly 4.8 V at 2,500 RPM |
| kP | 0.001 | Correct what the feedforward misses |

Sanity-check kV the way [Unit 9](course:09-controls/feedforward) suggests: 0.00193 × 2500 plus 0.2 is about 5 volts to hold a shooting speed, which is believable for a Vortex flywheel and leaves plenty of headroom for recovery after a shot.

## Stopping is not the same as commanding zero

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="public void setFlyWheelSpeed(double speed)" lines=8 highlight="3,4,5,6"

A setpoint of zero would ask the velocity loop to actively hold zero, fighting the wheel's momentum all the way down. Instead, the code calls `flywheel.set(0)`, which is open loop: the controller stops driving and the wheel coasts. Small detail, real consequence for the hardware.

## The feeder gate

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="private void intake(boolean resetting, boolean flywheelSpedUp)" lines=7 highlight="4"

The feeder is the last thing between a FUEL and the flywheel, and it runs only when **both** conditions hold:

| Condition | Comes from | Why |
|---|---|---|
| Not resetting | `Turret.getResetting()` | The turret is more than 0.1 rad from its setpoint, so it is still swinging |
| Flywheel at speed | `Turret.shooterSpedUp()` | Feeding a slow wheel produces a short shot and drags the speed down further |

Everything else keeps running while the gate is closed: the arm stays out, the rollers keep collecting, and FUEL stacks up behind the feeder waiting for the wheel.

::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from="feeder = new SparkFlex(15" lines=11

The feeder is inverted, runs a velocity loop that is almost pure feedforward (kP is 0.00001), and has a 0.05 second closed-loop ramp so it does not slam a ball into a wheel that is only just ready.

## What "at speed" means

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public boolean shooterSpedUp()" lines=3

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="public static final LoggedNetworkNumber flywheelTolerance" lines=1

Read the comparison carefully. It is one-sided: the measured speed only has to be **within 1,000 RPM below** the setpoint, and any amount above counts as ready. At a 2,500 RPM setpoint, that means **1,500 RPM opens the gate**.

That is a wide window. It has an obvious upside, which is that the robot starts feeding early instead of waiting for a perfect number, and an obvious cost, which is that the first shot of a burst can leave at a speed the shot map never intended.

Two things worth knowing before changing it:

- It is a **live tunable** (`flywheelTolerance`), so it can be tightened during a practice session and watched in the logs.
- The right value is a measurement, not an opinion: graph `Turret/flywheelSpeed` against `flywheelSetSpeed` during a burst and see how far the speed actually sags between shots. Tighten until shots are consistent, loosen until the robot stops waiting.

There is a second consequence of the one-sided test. When the setpoint is 0, the comparison is 0 − 0 > −1000, which is **true**, so `shooterSpedUp()` reports "ready" whenever the flywheel is off. Nothing goes wrong today, because the feeder only runs while the intake is in `INTAKING` and the turret is shooting, but a future caller that trusts that method outside those states would be surprised.

:::warning Finding F3: the logged flywheel voltage is the wrong motor
::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="inputs.flywheelVolts = turret.getAppliedOutput();" lines=1

That line reads the **turret** motor, not the flywheel, and `getAppliedOutput()` returns a duty cycle from −1 to 1 rather than volts. So the input named `flywheelVolts` is neither the flywheel's nor a voltage.

Nothing in the robot's behavior depends on it, which is why it has survived. What it costs is diagnosis: anyone trying to work out why a shot was weak, or whether the flywheel saturated, reads a number that describes a different mechanism entirely.

The fix is one line: `flywheel.getAppliedOutput() * flywheel.getBusVoltage()`, the same pattern `ModuleIOSpark` already uses for the drive motors.
:::

:::quiz
? Why do both flywheel motors use coast mode?
+ Braking a heavy spinning wheel punishes the belt and gearbox for no benefit
- Coast mode draws less current
- Brake mode is unavailable on a SPARK Flex
- So the follower can spin independently
> Idle mode is a mechanical decision.

? Why does `setFlyWheelSpeed(0)` call `flywheel.set(0)` instead of commanding a velocity of zero?
+ A zero velocity setpoint would actively fight the wheel's momentum; open loop lets it coast
- The velocity loop rejects zero
- It saves CAN bandwidth
- It resets the encoder
> Stopping and commanding zero are different requests.

?num The flywheel setpoint is 2500 RPM and the tolerance is 1000. At what measured speed does the feeder gate open?
= 1500 ± 50 RPM
> The comparison is one-sided: within 1000 RPM below the setpoint, or anything above it.

? Which two conditions must both hold for the feeder to run?
+ The turret is not resetting, and the flywheel is within tolerance of its setpoint
- The HUB is active, and the intake arm is out
- The robot is in our alliance zone, and the hood is at maximum
- Vision sees a tag, and the robot is stationary
> Both arrive as `BooleanSupplier`s from the turret.

? What does `shooterSpedUp()` return when the flywheel setpoint is 0?
+ True, because zero minus zero is greater than the negative tolerance
- False, because the wheel is not spinning
- It throws an exception
- It depends on the intake's state
> Harmless today, and a trap for a future caller.

? What is wrong with `inputs.flywheelVolts`?
+ It reads the turret motor's duty cycle, so it is neither the flywheel's nor a voltage
- It is never logged
- It reads the follower instead of the leader
- It is in radians
> Finding F3: a one-line fix that makes shot diagnosis honest.
:::
