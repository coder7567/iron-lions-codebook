---
summary: Show that you understand the robot's hardware, the 20 ms loop, the Driver Station, WPILib's units and geometry, REVLib, and our sensors.
---

This test covers all of Unit 6. Many questions describe situations you will face in the pit.

:::exam Unit 6 test: hardware and WPILib foundations
? Five SPARKs report "disconnected" at the same moment, and they are all later in the CAN chain than SPARK 9. What is the most likely cause?
+ A CAN wiring problem near SPARK 9, since the daisy chain cuts off every device after a break
- Five separate firmware failures
- A bug in the Superstructure state machine
- The battery is fully charged
> A single bad connection in a chain takes out everything downstream.

?tf The roboRIO disables motor outputs during a brownout, even though robot code keeps running.
= true
> Outputs are cut until voltage recovers. Current limits help prevent brownouts.

?text What is the IP address of team 967's radio?
= 10.9.67.1
> The radio is .1 and the roboRIO is .2 on the 10.9.67.x network.

? How many times does the `Robot` constructor run during a match?
+ Once, when the robot program starts
- Once per mode change
- Every 20 ms
- Once per command
> Init methods run once per mode; periodic methods run every loop; the constructor runs once.

? Which is the correct order inside `CommandScheduler.run()`?
+ Subsystem periodic methods, then triggers, then scheduled commands, then default commands
- Commands, then subsystems, then triggers
- Triggers, then default commands, then subsystems
- Default commands only
> Subsystems read inputs and update state first, so commands see fresh data.

? Why do calls to `configure(..., PersistMode.kPersistParameters)` inside `teleopInit()` risk a loop overrun?
+ Each call blocks while the controller answers and writes flash, all at the start of teleop
- `teleopInit()` cannot call hardware
- Persisting settings resets the CAN IDs
- It reboots the roboRIO
> Blocking work in an init method lands right when the robot must respond.

? The operator controller does nothing in a practice match. What should you check first?
+ That it is in slot 1 of the Driver Station's USB Devices tab
- The robot's CAN utilization
- Whether the NavX is calibrated
- The field's game data
> `RobotContainer` reads the operator from slot 1, and controllers can change slots when replugged.

? Why might `DriverStation.getAlliance()` return an empty Optional?
+ The Driver Station has not yet connected and reported the alliance
- The match has more than two alliances
- The robot is on the red alliance
- Optional values are always empty in teleop
> Code must handle the unknown case, especially at startup.

? In WPILib's field frame, where is the origin?
+ At the corner of the blue alliance wall
- At the center of the field
- At our own alliance's wall, whichever color we are
- At the red alliance wall
> Every pose uses the blue-origin frame regardless of alliance.

?num On REBUILT's rotationally symmetric field (16.541 m by 8.069 m), a blue-side point is at y = 6.0 m. What is its y coordinate on the red side?
= 2.069 ± 0.001 m
> Rotation flips both coordinates: 8.069 − 6.0 = 2.069.

? The NavX's `getAngle()` increases when the robot turns clockwise. What does our code do with it?
+ Negates it, because WPILib angles are counterclockwise-positive
- Uses it unchanged
- Adds 180 degrees
- Converts it to meters
> Missing that minus sign reverses every heading.

? What angle does `new Rotation2d(3.14159)` represent?
+ About 180 degrees, because the constructor takes radians
- 3.14159 degrees
- 314 degrees
- It does not compile
> Use `Rotation2d.fromDegrees(...)` whenever you are thinking in degrees.

? A robot at (2, 4) faces 90°. The target is at (4.625, 4.0). What robot-relative angle should the turret aim?
+ −90°
- 0°
- 90°
- 180°
> Field angle 0° minus heading 90° gives −90°: the target is to the robot's right.

? After setting `positionConversionFactor(2π / 5.9)` on the drive encoder, what does changing that factor later also change?
+ The units of readings, setpoints, and the meaning of the PID gains tuned against them
- Only what the dashboard displays
- Only the CAN ID
- Nothing, once the robot is tuned
> Conversion factors change the units everything on that controller uses.

? A second flywheel motor is configured with `.follow(flywheel, true)`. How should code command it?
+ Command only the leader; the follower copies its output, inverted
- Send the same setpoint to both motors every loop
- Command only the follower
- Followers cannot be inverted
> Followers mirror their leader directly over CAN.

? Which idle mode fits the turret, and why?
+ Brake, to resist being pushed off its aim
- Coast, to spin down gently
- Coast, so the soft limits work
- Idle mode does not matter for position mechanisms
> Brake mode helps hold a mechanism in place. Heavy flywheels use coast instead.

? The turret was bumped 30° by hand while the robot was off. What happens when it powers on?
+ The seeded encoder position is wrong by 30°, so aiming and soft limits are both off by that much
- The absolute encoder corrects it automatically
- The NavX recalibrates the turret
- Nothing, because the turret uses brake mode
> The turret's external encoder is relative and seeded to −1.6 rad at boot.

?? Which are good ways to handle noisy sensor data in our code? (Select all that apply.)
+ Averaging in the motor controller, like `averageDepth(2)`
+ Debouncing a condition, like the 0.5 s disconnect debouncer
+ Requiring a condition for many loops, like the jam counter
- Ignoring the sensor whenever it changes
> Each technique trades a little delay for fewer false alarms.
:::
