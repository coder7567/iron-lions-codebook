---
summary: Show that you can read our state machines, predict what the robot does in any state, and name the findings hiding in them.
---

This test covers all of Unit 8. Several questions describe a moment in a match. Work out the state before you answer.

:::exam Unit 8 test: state machines
? What is the difference between a wanted state and a current state?
+ The wanted state is what someone asks for; the current state is what the subsystem decides to do about it
- The wanted state is used in autonomous and the current state in teleop
- They are the same value, stored twice for logging
- The wanted state comes from sensors and the current state from commands
> The split is what lets the turret turn one `SHOOTING` request into shooting or passing.

?order Put one loop of a mechanism subsystem in order.
1. `io.updateInputs(inputs)`
2. `Logger.processInputs(...)`
3. `currentState = updateState()`
4. `applyState()`
> Read, log, decide, act. Deciding never moves motors.

? You add a constant to an enum and forget it in a switch expression that has no `default`. What happens?
+ The build fails until you handle it
- It compiles and returns null at runtime
- It compiles and falls through to the first case
- The scheduler skips that subsystem
> That's the reason decisions are written as switch expressions and not switch statements.

? The Superstructure is `IDLE`, and the turret is 0.6 rad away from its idle position. What does the intake get asked to do?
+ `PAUSED`, so the arm stays where it is
- `IDLE`, so the arm goes back to rest
- `REVERSING`
- `TESTING`
> The arm sweeps through the turret's space, so going home waits for `turret.intakeSafe()`.

? What does the Superstructure ask of the turret while the robot is `EJECTING`?
+ `SHOOTING`, so the turret keeps aiming and spinning while the intake reverses
- `IDLE`
- `PAUSED`
- `EJECTING`
> The driver can go straight back to shooting without waiting for a spin-up.

?num How long must the intake see high current and low roller speed before it declares a jam?
= 1.0 ± 0.05 s
> 50 loops of evidence at 20 ms per loop.

? Why does the intake's jam condition include `!jammed`?
+ So the high current and low speed of reversing don't count as a new jam
- So testing mode can skip jam detection
- Because `jamCount` would overflow
- So the driver's rumble stops during recovery
> Counting stops once the intake is already responding.

? Which two facts must both be true before the feeder runs?
+ The turret is not resetting, and its flywheel is within tolerance of its setpoint
- The arm is out, and the HUB is active
- The robot is in our alliance zone, and the hood is at maximum
- The intake is not jammed, and the driver holds the right trigger
> Both arrive as `BooleanSupplier`s passed to the intake's constructor.

? A blue robot sits at x = 8.0 m and the driver presses the right trigger. What is the turret's current state?
+ `PASSING`
- `SHOOTING`
- `PAUSEDPASSING`
- `IDLE`
> Our alliance zone on blue ends at x = 5.0 m, so shooting becomes passing.

? A red robot sits at x = 13.0 m with a wanted state of `SHOOTING`. What is the turret's current state?
+ `SHOOTING`, because our zone on red runs from 11.541 m to the wall
- `PASSING`
- `PAUSEDSHOOTING`
- `TESTING`
> Field lines mirror with the alliance.

?text Which turret state can never be reached in the current code?
= PAUSEDPASSING | paused passing
> Both branches of `case PAUSED` yield `PAUSEDSHOOTING`. That's finding F1.

? What do the paused turret states do?
+ Hold the turret and hood aimed at a target while the flywheel stops
- Stop the turret and keep the flywheel spinning
- Move the turret to its idle position and stop everything
- Nothing; they are unreachable
> Holding aim means the next shot doesn't wait for the turret to swing back.

? The game data is `B`. Whose HUB is active in shift 1?
+ Red's
- Blue's
- Both
- Neither
> The character names the alliance that is inactive first.

?num The teleop clock reads 92 seconds. How many seconds are left in the current period?
= 12 ± 0.5 s
> 92 falls inside shift 2, which ends at 80.

? Why does the robot only rumble instead of stopping the flywheel when our HUB is inactive?
+ The match time from the FMS is approximate, so an automatic action could misfire near a boundary
- Rumble uses less current than stopping the flywheel
- The rules require driver control of scoring
- The Superstructure can't command the flywheel
> Advisory information belongs to the driver; exact decisions need something the robot can measure.

? `IntakeIOSpark.updateInputs` ends by sending the arm's setpoint to its controller. Why is that a problem?
+ Reading and acting get mixed, replay runs the same method, and every arm decision reaches the motor one loop late
- The arm setpoint is never clamped
- `updateInputs` runs only in simulation
- It makes the arm move while the robot is disabled
> That's finding F16. Moving the call into `setIntakeArmAngle` fixes all three.

?? Which of these are true about our simulation today? Select all that apply.
+ `TurretIOSim` reports `intakeSafe` with the comparison reversed
+ The simulated flywheel never receives voltage, so the feeder never runs
+ `IntakeIOSim` never fills currents, so jams can't be detected
- Simulation runs the same IO code as the robot, so it can't drift
> Findings F5, F6, and F7. Sim is only useful while it tells the truth.

?? Which questions belong on a state-machine review checklist? Select all that apply.
+ Can every state actually happen?
+ Does every state set the outputs it needs?
+ Are counters reset when leaving the state that owns them?
+ Is every field a consumer reads updated on every path?
- Does each state have a unique color on the dashboard?
> The first four catch findings F1, F2, F16, and F19.
:::
