---
summary: Every layer our code uses to keep a mechanism from destroying itself: soft limits, clamps, output ranges, ramp rates, current limits, interlocks, and the checks a human does in the pit.
objectives:
  - Name the ways a mechanism can hurt itself and the layer that stops each one
  - Find the protections our code applies to each mechanism
  - Explain current limits, idle modes, and brownouts
  - Bring up a new mechanism safely for the first time
files:
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## What goes wrong

| Failure | What it looks like | What stops it |
|---|---|---|
| Driving past the end of travel | A turret that winds its own wiring; a hood that stalls at its stop | Soft limits, clamps |
| Too much current | A smoking motor, a tripped breaker, a brownout | Current limits |
| Two mechanisms in the same space | The intake arm swinging into the turret | Interlocks |
| Fighting a stuck game piece | Rollers stalled at full current | Jam detection and recovery |
| A setpoint that means something unintended | Wrong units, a wrapped angle, a stale offset | Clamps, wrapping, logging |
| Losing the zero | A relative encoder seeded to the wrong position at boot | Absolute encoders, or a pit procedure |

Nothing on that list is a rare event. All of them have happened to somebody's robot at every event you will attend.

## Layers, from the motor outward

| Layer | Where it lives | On our robot |
|---|---|---|
| **Soft limits** | The controller | The turret: forward 1.6 rad, reverse −4.261 rad, both enabled |
| **Setpoint clamps** | Our code | `setTurretAngle`, `setHoodAngle` (0.530 to 0.907), `setIntakeArmAngle` (0.0305 to 0.78) |
| **Output range** | The controller's closed loop | Turret ±0.5, intake arm ±0.25 |
| **Ramp rate** | The controller | Turret 0.075 s, feeder 0.05 s |
| **Current limit** | The controller | Drive 30 A (80 A in autonomous), turn 20 A, flywheel 60 A, hood 40 A, turret 30 A, arm 40 A, intake and feeder 30 A, horizontals 20 A |
| **Idle mode** | The controller | Brake for anything that must hold a position; coast for the flywheel, hood, and intake rollers |
| **Voltage compensation** | The controller | 12 V on the drive and turn motors |
| **Interlocks** | Our code | The intake waits for `turret.intakeSafe()`; the feeder waits for `shooterSpedUp` and not `resetting` |
| **Detection and response** | Our code | Jam counters, deadzone rumble, disconnect debouncers and alerts |
| **The pit** | People | Checklists, a hand on Disable, and someone watching the mechanism |

Defense in depth is the point. The turret has four separate things standing between a bad number and a broken mechanism, and each one catches a different mistake: the clamp catches an unreasonable setpoint, the soft limit catches a clamp with the wrong constant, the output range keeps a mistake slow, and the current limit keeps it from being violent.

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="turretConfig.externalEncoder.countsPerRevolution(8192);" lines=8 highlight="4-8"

## Current limits and brownouts

A `smartCurrentLimit` tells the controller to keep the motor's current under a ceiling by reducing output. Two different problems are solved at once:

- **Motor survival.** A stalled NEO pulls well over 100 A and heats fast. A 30 A ceiling means a stall is survivable.
- **Battery survival.** Everything on the robot shares one battery. When the total draw exceeds what the battery can deliver, the voltage sags, and if it sags far enough the roboRIO disables every output to keep itself alive. The Driver Station logs it as a brownout, and the robot goes limp in the middle of a match.

The most common way to brown out is asking several big loads to start at once: four drive motors accelerating while the flywheel spins up and the intake deploys. Current limits are how you make the worst case survivable.

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final int driveMotorCurrentLimit" lines=2

Our robot raises the drive limit to 80 A for autonomous and drops it to 30 A for teleop. That's a deliberate trade: autonomous is 20 seconds at the start of a match, with a fresh battery and no other mechanisms competing for it, and path following needs acceleration. Teleop shares the battery with everything else for 140 seconds. ([The way that change is applied is finding F8](course:07-command-based/robotcontainer).)

## Brake and coast are safety choices

| Mode | Behavior when the output is 0 | Use it for |
|---|---|---|
| **Brake** | The motor resists being turned | Anything that must hold a position: the turret, the arm, the drive, the turn motors |
| **Coast** | The motor spins freely | Heavy spinning mass: the flywheel, the intake rollers |

Coasting a flywheel down is gentler on the belt and the gearbox than braking 3,000 RPM of steel. Braking a turret keeps a defense bump from dragging the aim off. Our hood coasts, which is a choice worth asking about: a hood that drifts when disabled needs its position re-established, and the absolute encoder is what makes that safe.

## Interlocks: protecting mechanisms from each other

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="turret.setWantedState(Turret.WantedState.IDLE);" lines=6

The intake arm's path crosses the turret's. Rather than mechanical clearance alone, the code enforces an order: the arm may only return home once the turret reports it is parked. The interlock lives in the **coordinator**, not in either mechanism, because only the Superstructure knows about both.

The feeder's gate is the same idea applied to a game piece instead of a collision: FUEL should not enter a flywheel that isn't at speed, so the feeder waits.

:::warning What our robot doesn't protect against yet
- **Gravity on the intake arm.** There is no kG, and `armkS` is written in `IntakeConstants` but commented out of the configuration. The arm is held up by proportional gain alone, so it sags a little under its own weight, and a bigger kP is the only lever available.
- **A turret that starts in the wrong place.** The turret's encoder is seeded to −1.6 rad at boot (finding F23), so the mechanism must physically be at its idle position when the robot powers on. An earlier version of the code had a `HOMING` state; it is gone now, and a pit checklist item stands in its place.
- **Simulation drift.** The intake-safe interlock is inverted in simulation (finding F5), so the one protection you would most want to test on a laptop is the one that behaves differently there.
:::

## Bringing up a new mechanism

:::steps
1. **Check the mechanical range by hand** with the robot disabled. Write down the encoder values at both ends.
2. **Set the clamp and the soft limits** from those measured numbers, with a little margin.
3. **Set a current limit** below the motor's continuous rating before the first move.
4. **Start with a small output range.** ±0.2 is plenty to see whether the sign is right.
5. **Verify the sensor first**: does the reading increase when the mechanism moves the way you call positive?
6. **Move it once, by hand on the dashboard**, with a person on Disable and nobody in the mechanism's path.
7. **Then raise limits gradually**, and only after the mechanism has done the right thing at a slow speed.
:::

Step 5 is the one people skip. A mechanism with an inverted sensor runs away from its setpoint at full output, and every protection in this lesson is then load-bearing.

:::exercise id="u09-guard"
Implement the four guards this lesson describes: clamping a setpoint into soft limits, limiting output range, stopping output that pushes past a limit, and a rate limiter that walks a commanded value toward its target.

Each one is a few lines, and each one is the difference between a mistake and a repair.
---hint
`clampSetpoint` and `limitOutput` are both `Math.max` around a `Math.min`. Write one and the other follows.
---hint
`stopAtLimits` has to allow escape: at the upper limit, a positive output is blocked but a negative one is not.
---hint
The rate limiter clamps the *change*, not the value: compute `input - value`, clamp it to `maxChangePerSecond * dtSeconds`, add it, and store the result.
:::

:::quiz
? What does a soft limit do that a clamp in robot code does not?
+ It runs inside the controller, so it still applies if the code sends a bad setpoint
- It stops the motor faster
- It applies while the robot is disabled
- It prevents brownouts
> Layers catch each other's mistakes; that's the point of having both.

? Why does the flywheel use coast mode while the turret uses brake?
+ A heavy spinning mass is gentler to coast down, and a position mechanism should resist being pushed
- Coast mode uses less current
- Brake mode is only for absolute encoders
- The flywheel has no encoder
> Idle mode is a mechanical decision, not a preference.

? A robot browns out when it accelerates hard while the flywheel spins up. What is the most direct fix in code?
+ Lower the current limits so the worst-case total draw fits what the battery can deliver
- Raise the PID gains so it accelerates faster
- Switch the drive motors to coast
- Increase the voltage compensation value
> Current limits are how you bound the worst case.

? Why does the intake-safe interlock live in the Superstructure rather than in the intake?
+ Only the coordinator knows about both mechanisms
- The intake has no access to sensors
- Interlocks must be in a subsystem with no hardware
- The turret updates after the intake
> An interlock belongs where both sides are visible.

? During bring-up, why check the encoder's sign before moving a mechanism under control?
+ A reversed sensor makes the controller drive away from the setpoint at full output
- The encoder needs to be zeroed first
- The gains can't be tuned without it
- The soft limits are set automatically from it
> This is the failure that makes every other protection matter.

?? Which protections does the turret have today? Select all that apply.
+ Soft limits in the controller
+ A setpoint clamp in code
+ A limited output range
+ A closed-loop ramp rate
- A homing routine that finds its zero at startup
> The homing state was removed; the encoder is seeded at boot instead, which is finding F23.
:::
