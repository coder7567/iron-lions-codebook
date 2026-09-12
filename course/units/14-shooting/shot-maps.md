---
summary: A shot map turns a distance into a flywheel speed and a hood angle by interpolating between measurements. Here is ours, how it is built, and why a table beats an equation.
objectives:
  - Explain what an interpolating tree map does and why we use one
  - Read our shot map and predict the setpoint for any distance
  - Explain the hood offset and the clamps applied to every setpoint
  - Decide how many entries a table needs and where to put them
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## Measure, don't derive

You could compute a projectile's launch speed from physics. On a real robot the answer would be wrong, because the model leaves out compression of the FUEL, roller slip, backspin, air, battery voltage, and the way the ball leaves the hood. Every one of those is small, and together they are not.

So the team does what most teams do: drive to a distance, try settings until shots go in, write down what worked, and let the code interpolate between the measurements.

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="shooterShootingMap.put(1.03" lines=11

Eleven measurements from 1.03 m to 6.45 m. Between them, the map blends; outside them, it holds the nearest value.

| Distance | RPM | Hood (tuned) |
|---|---|---|
| 1.03 m | 1850 | 0.588 |
| 2.00 m | 2000 | 0.588 |
| 2.86 m | 2100 | 0.525 |
| 3.84 m | 2300 | 0.475 |
| 4.66 m | 2500 | 0.425 |
| 6.45 m | 2700 | 0.350 |

The shape is what you would expect: farther means faster and flatter. The spacing is uneven because the measurements came from wherever the robot happened to be parked during testing, which is normal and fine.

## How the interpolation works

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private final InterpolatingTreeMap<Double, ShooterSetpoint> shooterShootingMap" lines=9

An `InterpolatingTreeMap` needs two things: how to find the fraction between two keys (`InverseInterpolator.forDouble()`), and how to blend two values by that fraction. The blend here is a lambda that mixes both fields:

::source file="src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java" from="public ShooterSetpoint interpolate" lines=6

`ShooterSetpoint` also implements WPILib's `Interpolatable` with the same arithmetic, so the class knows how to blend itself. The lambda in the map's constructor does the same job, which means the interpolation logic exists twice. Either would work alone; having both is the kind of duplication worth noticing during a review.

At 3.0 m, the map finds 2.86 and 3.377 around it, computes the fraction (3.0 − 2.86) ÷ (3.377 − 2.86) = 0.27, and returns 2100 + 0.27 × 150 ≈ **2141 RPM** with a hood angle 27% of the way from 0.525 toward 0.5.

## The hood offset

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="public static final double hoodOffset" lines=3

Every hood value in the map has `hoodOffset` added, and the comment says why: "used when encoder gets messed up." The hood's absolute encoder lost its reference at some point, and rather than re-zero it and retune every shot, the team added a constant to every setpoint.

It works, and it has a cost: **the numbers in the table are no longer the numbers on the mechanism**. A new programmer reading 0.588 and then watching the dashboard show 0.879 has to discover the offset to make sense of it. If the encoder is ever re-zeroed, the offset must go to zero in the same commit, or every shot moves.

## Clamps on the way out

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private void calculationToTarget(Translation2d target)" lines=14 highlight="11,12,13"

Three clamps, each with a different job:

| Clamp | Range | Protects against |
|---|---|---|
| Distance, before the lookup | 0 to 10 m | A wild pose estimate asking for an extrapolated setpoint |
| Hood angle | 0.530 to 0.907 | Commanding the hood past its travel |
| Flywheel RPM | 0 to 6758 | A negative or impossible speed |

With the table as tuned, none of them ever binds during normal shooting: every entry is inside the hood's travel and well under the flywheel's limit. They exist for the moments when something upstream is wrong, which is exactly when you want a mechanism to refuse rather than obey.

## How many entries, and where

- **Cover the range you actually shoot from.** Ours stops at 6.45 m; past that the map returns the 6.45 m setpoint, which is a guess.
- **Put entries where the curve bends.** The hood angle changes quickly between 2 and 4 meters, and the table is densest there.
- **More entries is not automatically better.** Each one is a measurement that can be wrong, and a bad entry creates a dip that is hard to see.
- **Re-measure after mechanical changes.** New hood surface, new FUEL, or a different compression means the old table describes a robot you no longer have.

:::exercise id="u14-shotmap"
Build the shot map: an interpolating table keyed by distance, returning a flywheel speed and hood angle, with the clamps `calculationToTarget` applies.

The tests use the team's real table, including a check that every tuned hood angle is inside the mechanism's travel.
---hint
A `TreeMap<Double, Setpoint>` gives you `floorEntry` and `ceilingEntry`, which are exactly the two neighbors you need.
---hint
Handle three cases before interpolating: below the first key, above the last key, and landing exactly on a key.
---hint
The fraction is `(distance - belowKey) / (aboveKey - belowKey)`, and both fields blend with the same fraction.
:::

:::quiz
? Why does the team measure a table instead of computing launch settings from physics?
+ Compression, slip, spin, and air all matter, and measuring captures them without modeling them
- Physics is too slow to compute in a 20 ms loop
- WPILib has no physics library
- The rules forbid computed setpoints
> The map is a measurement of the real robot, which is the thing you actually have.

?num Our map has 2100 RPM at 2.86 m and 2250 RPM at 3.377 m. What does it return at 3.0 m?
= 2141 ± 5 RPM
> The fraction is about 0.27 of the way between the two entries.

? A shot is requested at 8 m, past the last entry at 6.45 m. What does the map return?
+ The 6.45 m setpoint, unchanged
- Zero
- An extrapolated setpoint
- The hood's maximum angle
> Outside the table the nearest entry is held, which is a guess rather than a measurement.

? What is `hoodOffset` for?
+ It shifts every hood setpoint to compensate for an absolute encoder that lost its reference
- It converts rotations to radians
- It compensates for gravity on the hood
- It is a per-alliance adjustment
> The cost is that the tuned numbers no longer match what the mechanism reports.

? When do the clamps in `calculationToTarget` actually change a setpoint?
+ Almost never during normal shooting; they exist for when something upstream is wrong
- On every shot past 4 m
- Whenever the robot is moving
- Only in simulation
> Every tuned entry is already inside the hood's travel and the flywheel's limit.

?tf Adding more entries to a shot map always makes it more accurate.
= false
> Each entry is a measurement that can be wrong, and a bad one creates a dip that is hard to spot.
:::
