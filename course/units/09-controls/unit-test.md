---
summary: Show that you can read our control code, predict what a change does, tune in a safe order, and protect a mechanism.
---

This test covers all of Unit 9. Some questions give you numbers from the robot; do the arithmetic before answering.

:::exam Unit 9 test: control
? Which of these is closed-loop control?
+ `driveController.setSetpoint(40.0, ControlType.kVelocity)` with the encoder as feedback
- `driveSpark.setVoltage(6.0)`
- `Module.stop()`
- `flywheel.set(0)`
> Closed loop commands a result and keeps correcting toward it.

? The drive's velocity setpoints are in wheel radians per second. Where does that unit come from?
+ The encoder's velocity conversion factor, which divides by the 5.9:1 reduction
- WPILib's `SwerveModuleState`
- The NavX
- The PathPlanner configuration
> Conversion factors decide the units of readings, setpoints, and gains together.

? A mechanism's gearbox is swapped and `driveMotorReduction` changes. What else must be revisited?
+ The PID and feedforward gains, because the units they were tuned in have changed
- Only the wheel radius
- The CAN IDs
- Nothing
> Gains and conversion factors travel together.

? A flywheel sits 200 RPM below its setpoint and stays there. Which is the best first fix on our robot?
+ Check and improve the feedforward (kS and kV)
- Add a large kD
- Add a large kI
- Raise the current limit
> Steady-state error is what feedforward exists to remove; our loops all run kI = 0.

? The turret buzzes back and forth around its setpoint. What do you try first?
+ Lower kP, then add a little kD
- Raise kP
- Add kI
- Disable continuous input
> Oscillation is the classic too-much-proportional symptom.

? Why is the flywheel's kP (0.001) so much smaller than the hood's (5.0)?
+ Their errors are in different units: RPM for the flywheel, rotations for the hood
- The flywheel motor is more powerful
- The hood has an absolute encoder
- The hood loop runs on the roboRIO
> A gain converts error units into output units.

?num Using kS = 0.12349 and kV = 0.12293, what wheel velocity does the model predict at 12 V?
= 96.6 ± 0.5 rad/s
> (12 − kS) / kV. About 4.9 m/s at the wheel, comfortably above the 4.2 m/s commanded maximum.

? What does `Math.signum(velocity)` do in the drive's feedforward?
+ Makes the static-friction term push in the direction of travel, and contribute nothing at zero
- Converts rad/s to m/s
- Limits the output to 12 V
- Selects the PID slot
> Without it, reversing would fight an extra constant voltage.

? Our drive gains are computed on the roboRIO and passed to the SPARK as an arbitrary feedforward, while the flywheel's kS and kV live in the controller's configuration. What does the first approach buy?
+ The gains can come from live dashboard values, which helps while tuning
- Faster response
- Lower CAN usage
- It survives a controller reboot
> Controller-side gains are frozen until you reconfigure.

? Why does our drive publish the encoder position every 10 ms while other signals use 20 ms?
+ Odometry samples at 100 Hz; the rest only needs to be fresh once per robot loop
- Position messages are smaller than velocity messages
- The SPARK cannot publish velocity faster
- To leave bandwidth for the NavX
> Status frame periods are a CAN budget.

? What problem do `tryUntilOk` and `ifOk` solve?
+ CAN operations can fail silently, so configuration is retried and readings are checked before they are trusted
- They convert REVLib errors into exceptions
- They make configuration faster
- They synchronize the odometry thread
> A failed read returns a number, not an error, unless you check.

?num A 10 m move at 2 m/s and 1 m/s² takes how long as a trapezoid profile?
= 7 ± 0.1 s
> 2 s accelerating, 3 s cruising, 2 s decelerating.

? Why does `joystickDriveFacingTarget` reset its `ProfiledPIDController` in `beforeStarting`?
+ The controller keeps profile state between runs, so stale state would cause a jump
- To clear the odometry
- Because the alliance may have changed
- Because the command has no requirements
> Reset any stateful controller when its command starts.

? What does a SysId quasistatic test measure?
+ kS and kV, by ramping voltage slowly enough that acceleration barely matters
- kA
- The wheel radius
- The gear ratio
> The dynamic test is the one that reveals kA.

? You change `turretP` on the dashboard mid-practice and nothing happens. Why?
+ It was read once while the SPARK configuration was built, so the gain now lives in the controller
- The turret is in IDLE
- Tunables only apply in autonomous
- The value was out of range
> Live tunables are the ones read every loop, like `hoodIDLEPosition`.

? A tuning session ends with a great number on the dashboard and no commit. What happens next?
+ The next reboot or deploy restores the default from the source file
- The value persists in the controller's flash
- AdvantageKit writes it back into the code
- Nothing; dashboard values are permanent
> Tuned values have to end up in the constants file.

?? Which protections stand between a bad turret setpoint and a broken turret? Select all that apply.
+ A clamp in `setTurretAngle`
+ Soft limits in the controller
+ A limited closed-loop output range
+ A closed-loop ramp rate
- A homing routine at startup
> Defense in depth: each layer catches a different mistake.

? Why does the drive run an 80 A current limit in autonomous and 30 A in teleop?
+ Autonomous is short, the battery is fresh, and path following needs acceleration; teleop shares the battery with every other mechanism for 140 seconds
- The rules require a lower limit in teleop
- The motors are cooler in autonomous
- PathPlanner sets it automatically
> It is a deliberate trade, applied at each mode change (which is where finding F8 lives).
:::
