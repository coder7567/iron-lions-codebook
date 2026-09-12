---
summary: Show that you can read our shooting code, compute a setpoint or an aim angle by hand, explain the corrections, and name the findings in this subsystem.
---

This test covers all of Unit 14.

:::exam Unit 14 test: shooting
? Why does the team measure a shot map instead of computing launch settings from physics?
+ Compression, slip, spin, and air all matter, and measuring captures them without modeling them
- Physics is too slow for a 20 ms loop
- WPILib has no physics library
- The rules require measured tables
> The map describes the robot you actually have.

?num The map holds 2100 RPM at 2.86 m and 2250 RPM at 3.377 m. What does it return at 3.0 m?
= 2141 ± 5 RPM
> About 27% of the way between the two entries.

? A shot is requested at 8 m, past the last entry at 6.45 m. What comes back?
+ The 6.45 m setpoint, unchanged
- Zero
- An extrapolated setpoint
- The hood's maximum angle
> Outside the table, the nearest entry is held.

? What is `hoodOffset` for?
+ It shifts every hood setpoint to compensate for an absolute encoder that lost its reference
- It converts rotations to radians
- It compensates for gravity
- It differs per alliance
> The cost is that tuned numbers no longer match what the mechanism reports.

?num The robot is at (2.0, 4.0) facing 90 degrees, and the HUB is at (4.625, 4.0). What turret angle does the code compute, in degrees?
= -90 ± 1
> Field angle 0, minus the 90 degree heading.

?num How wide is the arc the turret cannot reach?
= 0.422 ± 0.005 rad
> From the 1.6 rad soft limit to 2.022 rad, about 24 degrees.

? The controller rumbles while the turret aims. What is it telling the driver?
+ The requested angle is in the unreachable arc, so moving the robot slightly would fix it
- The flywheel is not at speed
- The intake is jammed
- Vision lost its tags
> `inDeadzone` is one of the three rumble conditions.

? An operator presses the trim bumper four times one way. What changes?
+ Every turret setpoint shifts by 0.2 rad until the code restarts
- Nothing until the turret re-homes
- Only the next shot
- The robot's heading estimate
> Each press is 0.05 rad, and the offset lives in the IO object.

? Why must a moving robot aim away from its target?
+ The FUEL carries the robot's velocity and drifts during the flight
- The turret cannot turn fast enough
- The flywheel slows while the robot accelerates
- Odometry lags while moving
> Aim opposite the motion so the drift carries the shot home.

? Why does the shoot-on-the-move correction iterate?
+ Flight time depends on distance, and moving the aim point changes the distance
- The turret needs three loops to turn
- The table has three entries
- To average sensor noise
> It refines one correction three times, rather than applying three corrections.

?num Flight time is about 1.3 s at 4 m. Driving sideways at 2 m/s, how far does the FUEL drift?
= 2.6 ± 0.1 m
> Which is exactly why the correction exists.

? You shoot while driving sideways at 2 m/s and land 0.3 m behind the aim point. What does the code's own procedure compute?
+ A flight-time error of 0.15 s, so a reality constant of about 1.12
- A new hood offset
- A wider flywheel tolerance
- A different turret trim
> Miss distance divided by robot speed is the flight-time error.

? What is the passing map keyed by?
+ The robot's x position on the field
- The distance to the HUB
- The distance to the passing target
- The match time
> A pass depends on how far down the field you are.

? Why is the passing map rebuilt when the alliance is reported?
+ Its keys are alliance-mirrored field positions computed at boot, before the alliance is known
- The flywheel gains change per alliance
- The hood offset changes per alliance
- PathPlanner rebuilds it
> Anything derived from the alliance cannot be computed in a constructor.

?num After the hood clamp, how much hood error does finding F4 cause?
= 0.011 ± 0.002 rotations
> A request of 0.25 clamps up to 0.530, against an intended 0.541.

?num The flywheel setpoint is 2500 RPM with a 1000 RPM tolerance. At what measured speed does the feeder gate open?
= 1500 ± 50 RPM
> The comparison is one-sided: within 1000 below, or anything above.

? Which two conditions must hold before the feeder runs?
+ The turret is not resetting, and the flywheel is within tolerance of its setpoint
- The HUB is active, and the arm is out
- The robot is in our zone, and the hood is at maximum
- Vision sees a tag, and the robot is stopped
> Both arrive as suppliers from the turret.

? Why does `setFlyWheelSpeed(0)` use `flywheel.set(0)` rather than a velocity setpoint of zero?
+ A zero velocity setpoint would fight the wheel's momentum; open loop lets it coast
- The velocity loop rejects zero
- It saves CAN bandwidth
- It resets the encoder
> Stopping and commanding zero are different requests.

? What is wrong with the logged `flywheelVolts`?
+ It reads the turret motor's duty cycle, so it is neither the flywheel's nor a voltage
- It is never recorded
- It reads the follower instead of the leader
- It is in radians per second
> Finding F3: a one-line fix that makes shot diagnosis honest.

? During a shot-data session, what is the first thing to verify at each distance mark?
+ That `DistanceToHub` agrees with the tape measure
- That the battery is above 12 V
- That the hood is at its minimum
- That the turret is centered
> A wrong pose makes every number from the session wrong.
:::
