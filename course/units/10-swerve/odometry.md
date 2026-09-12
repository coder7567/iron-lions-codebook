---
summary: How wheel and gyro readings become a field position, why our robot samples them at 100 Hz on a separate thread, and what makes the estimate drift.
objectives:
  - Explain how module deltas and a heading combine into a pose
  - Explain why odometry samples faster than the robot loop, and how the thread does it safely
  - Describe what happens when the gyro disconnects
  - Name the main sources of odometry error and what to do about each
files:
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/GyroIONavX.java
---

## What odometry does

Odometry answers "where am I?" by adding up small movements. Each sample, every module reports how far its wheel rolled and which way it was pointed. Those four little vectors average into the chassis's movement in the robot's own frame, that movement is rotated into the field frame by the robot's heading, and the result is added to the previous pose.

Nothing about that is magic, and nothing about it is exact. Odometry is a running sum, so **every error is permanent**. A 1% wheel radius error doesn't average out; it accumulates over the whole match.

## Sampling faster than the robot loop

The smaller each step is, the less the "rotate by the heading" approximation costs, so our drivetrain samples at 100 Hz instead of the robot's 50 Hz. That needs its own thread:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="public void start()" lines=5

A `Notifier` runs `run()` every 10 ms. Each module registered two signals when it was created, and the gyro registered its yaw, so one pass reads them all:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="private void run()" lines=29 highlight="3,13,17-18"

Three details in that method are worth copying into any threaded code you write:

1. **One lock, taken by both sides.** `Drive.odometryLock` is held while the thread writes and while `Drive.periodic` reads. Without it, the periodic method could read half of a sample.
2. **All or nothing.** If any SPARK reports an error during the pass, the whole sample is dropped. A sample with one stale wheel is worse than no sample.
3. **One timestamp per sample**, taken from the FPGA clock at the top of the pass, so every value in the sample shares a time.

Each queue is an `ArrayBlockingQueue` of 20 entries: two robot loops' worth of samples, which is enough headroom for one slow loop and small enough that a stall can't grow without bound.

## Turning samples into a pose

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Update odometry" lines=18 highlight="1,9-16"

For each sample, the code builds the four module positions, computes the **delta** since the last sample, and remembers the new positions. Then it needs a heading for that sample:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Update gyro angle" lines=12 highlight="2-4,5-9,12"

- **With a gyro**, the heading comes from the NavX's own high-rate yaw queue, sampled by the same thread.
- **Without one**, `kinematics.toTwist2d(moduleDeltas)` extracts a rotation from the wheels alone and adds it to the running estimate. The robot stays drivable, but any wheel slip becomes heading error, and heading error becomes position error immediately.

The last line hands the sample to a `SwerveDrivePoseEstimator` with its timestamp:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="private SwerveDrivePoseEstimator poseEstimator" lines=2

A pose estimator is odometry plus a buffer of recent history, so a **late** vision measurement can be folded in at the time it was actually taken:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void addVisionMeasurement(" lines=6

That is what `AprilTagVision` calls, and [Unit 13](course:13-vision/trusting-measurements) covers how much the estimator trusts each measurement.

## The gyro

::source file="src/main/java/frc/robot/subsystems/drive/GyroIONavX.java" from="public class GyroIONavX implements GyroIO" lines=9

The NavX is constructed on the MXP SPI port and told to publish at our odometry frequency. Two things happen in its `updateInputs`:

- The instantaneous yaw is negated, because the NavX counts clockwise as positive and WPILib counts counterclockwise as positive. **That minus sign is load-bearing.** Without it, the robot's heading runs backwards and every field-relative behavior inverts.
- The high-rate queue of yaw samples is drained into an array, negated the same way, so each odometry sample can use the heading from its own moment rather than the latest one.

## Where the error comes from

| Source | What it does | What helps |
|---|---|---|
| Wheel radius wrong | Every distance scales by the same wrong factor | Re-measure with the spin test after tread changes |
| Wheel slip | Movement the encoders report but the robot didn't make | Vision corrections; don't trust odometry after a collision |
| Scrub during turns | Small, constant distance error while rotating | Sampling fast; cosine scaling reduces it |
| Gyro drift | Heading slowly rotates away from truth | Multi-tag vision corrections; a heading reset button |
| Module zeros wrong | The robot thinks it drove somewhere it didn't | Recalibrate the zeros |
| Late vision measurement applied at the wrong time | A jump in the estimate | Timestamped measurements, which the estimator handles |

The practical rule for a match: **odometry is excellent over seconds and unreliable over minutes.** That is exactly the shape vision fixes, and it is why autos reset the pose at their start instead of trusting whatever survived the last match.

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void setPose(Pose2d pose)" lines=3

:::exercise id="u10-odometry"
Implement the odometry math: convert encoder radians to meters, turn a module's distance change into a robot-frame vector, average the four into a chassis movement, apply it to a pose, and compute the wheel radius from a spin test.

The last test is the wheel radius characterization from this unit, including what a worn wheel does to the measurement.
---hint
A module delta is `(current - previous)` pointed along the module's angle: multiply by `cos(angle)` and `sin(angle)`.
---hint
To apply a robot-frame movement to a pose, rotate it by the pose's **current** heading before adding: `x' = x*cos - y*sin`, `y' = x*sin + y*cos`.
---hint
The spin test compares the arc each wheel should have traveled, `gyroDelta * driveBaseRadius`, with the wheel rotation it actually reported.
:::

:::quiz
? Why does the drivetrain sample odometry at 100 Hz instead of once per robot loop?
+ Smaller steps make the per-sample approximation smaller, so the accumulated position error is lower
- The SPARKs cannot publish more slowly
- To make the gyro readings smoother
- Because PathPlanner requires 100 Hz
> Odometry is a running sum; small steps mean small errors.

? Why does the odometry thread drop the whole sample when one SPARK reports an error?
+ A sample mixing fresh and stale wheel readings would corrupt the pose more than skipping it
- To reduce CAN traffic
- Because the queue is full
- REVLib requires it
> All-or-nothing sampling is safer than partial data.

? What is `Drive.odometryLock` protecting?
+ A sample being written by the thread while `Drive.periodic` reads the queues
- The CAN bus
- The pose estimator's internal buffer
- The gyro's SPI port
> Both sides take the same lock, so a read never sees half a sample.

? The gyro disconnects mid-match. What happens to odometry?
+ Heading comes from the module deltas through kinematics, so the robot keeps driving but heading error grows with any wheel slip
- Odometry stops updating
- The robot disables itself
- The pose estimator switches to vision only
> There is a fallback, and it is worse than a gyro but better than nothing.

? Why does `addVisionMeasurement` take a timestamp?
+ A camera measurement describes where the robot was when the picture was taken, and the estimator rewinds its history to apply it there
- To sort measurements from two cameras
- To reject old measurements
- Because WPILib logs require timestamps
> That is the difference between a pose estimator and plain odometry.

? Odometry says the robot is 40 cm from where it actually is after two minutes of a match with lots of contact. What is the most likely cause?
+ Wheel slip during contact, which odometry has no way to detect
- A software bug in kinematics
- The pose estimator's buffer overflowing
- A wrong wheel radius
> A wrong radius produces a proportional error from the first meter; slip produces a jump around contact.
:::
