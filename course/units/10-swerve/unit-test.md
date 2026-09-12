---
summary: Show that you can do swerve math, read our drivetrain code, diagnose a drivetrain symptom, and know what to calibrate.
---

This test covers all of Unit 10. Several questions expect arithmetic with our robot's real numbers.

:::exam Unit 10 test: swerve drive
? What does each swerve module control, and how?
+ Wheel speed through a velocity loop on the drive motor, and wheel direction through a position loop on the turn motor
- Both through one motor and a differential
- Speed with the turn motor, direction with the drive motor
- Direction only; the chassis sets the speed
> Two motors, two sensors, two loops, four times over.

? What is the difference between a `SwerveModuleState` and a `SwerveModulePosition`?
+ A state carries a speed; a position carries distance traveled and feeds odometry
- A state is field-relative; a position is robot-relative
- A position includes the gyro heading
- They are the same class with different names
> Control consumes states; odometry consumes positions.

?num Our modules sit on a 20 inch square. What is the drive base radius, in meters?
= 0.359 ± 0.002 m
> hypot(0.254, 0.254), which also sets the 11.7 rad/s maximum rotation speed.

?num The driver asks for 1 m/s forward and 1 rad/s counterclockwise. What speed does the front left module get?
= 0.79 ± 0.02 m/s
> hypot(1 − 0.254, 0.254). The front right gets hypot(1.254, 0.254) instead.

? Why does `desaturateWheelSpeeds` scale every module by the same factor?
+ It preserves the ratios, so the robot travels in the requested direction at a lower speed
- It protects the motors from over-current
- The SPARKs require equal setpoints
- It compensates for battery sag
> Clipping only the fastest module would change the direction of travel.

? What does `ChassisSpeeds.discretize(speeds, 0.02)` correct for?
+ The arc traced when the robot translates and rotates during the same 20 ms step
- Encoder quantization
- The red-alliance flip
- CAN latency
> It matters most when translating and spinning hard at once.

? A module at 0° is asked for 170° at 2 m/s. What does optimization command?
+ −10° at −2 m/s
- 170° at 2 m/s
- −10° at 2 m/s
- 190° at 2 m/s
> A module never turns more than 90°; it reverses the wheel instead.

?num A module still has 60° to turn. What factor does cosine scaling apply to its speed?
= 0.5 ± 0.01
> cos(60°). At 90° the commanded speed is zero.

? Why does the drivetrain sample odometry at 100 Hz on its own thread?
+ Smaller steps keep the per-sample approximation small, and a separate thread can sample faster than the robot loop
- The SPARKs can't publish at 50 Hz
- To reduce CAN traffic
- Because the NavX requires it
> Odometry is a running sum, so step size drives accumulated error.

? Why does the odometry thread discard an entire sample when one SPARK reports an error?
+ Mixing fresh and stale wheel readings corrupts the pose more than skipping the sample
- The queue would overflow
- REVLib throws an exception otherwise
- To keep the timestamps aligned with vision
> All-or-nothing sampling, with one lock and one timestamp per sample.

? The gyro disconnects mid-match. What happens?
+ Heading comes from module deltas through kinematics; the robot drives, but heading error grows with wheel slip
- Odometry stops
- The robot disables
- The pose estimator uses vision alone
> The fallback is worse than a gyro and better than nothing.

? `GyroIONavX` negates the NavX angle. Why?
+ The NavX counts clockwise as positive and WPILib counts counterclockwise as positive
- To convert degrees to radians
- Because the sensor is mounted upside down
- To match PathPlanner's convention
> Without the minus sign, every field-relative behavior inverts.

? Why isn't `Module` a subsystem?
+ Only `Drive` commands the modules, so there is nothing for requirements to protect
- Subsystems can't be stored in arrays
- It would slow down odometry
- WPILib forbids nested subsystems
> `Drive` calls each module's `periodic` itself, inside the odometry lock.

? What makes `new ModuleIO() {}` a usable implementation in replay mode?
+ Every interface method has a do-nothing default, and the inputs are filled from the log
- It extends `ModuleIOSpark`
- AdvantageKit generates it at build time
- It reads a simulated SPARK
> The IO layer is the seam where logged inputs are injected.

? The PathPlanner GUI and `DriveConstants` disagree about the gearing. Which does the robot use?
+ The code's `ppConfig`, because that is what `AutoBuilder.configure` receives
- The GUI's settings file, because it ships in the deploy folder
- Whichever was saved last
- Neither; PathPlanner measures it at runtime
> The GUI's copy only changes what the GUI previews. That's finding F9.

? What two measurements does the wheel radius characterization compare?
+ How far the gyro says the robot turned, and how far the wheels say they rolled
- Commanded velocity against measured velocity
- Two encoders on the same wheel
- Battery voltage against current
> Each wheel travels a circle of radius `driveBaseRadius`.

?num The wheel radius constant is 2% too large. How far off is odometry after a 5 m auto path?
= 0.1 ± 0.02 m
> Radius error scales every distance by the same factor.

? A driver reports that autos consistently finish about 20 cm to one side. Which calibrations are the best suspects?
+ Wheel radius and module zero offsets
- Feedforward kS and the drive current limit
- The joystick deadband and squaring
- The turret offset and hood angle
> Both make the robot travel a different path than the one it believes it took.

?? Which changes would improve the drive team's experience on our robot today? Select all that apply.
+ Binding `stopWithX()` to a driver button
+ Adding a heading-reset button that works while disabled
+ Running a full-match drill on one battery before each event
- Raising the teleop drive current limit to 80 A without testing
> The first two are a few lines each; the third is the drill that finds brownouts.
:::
