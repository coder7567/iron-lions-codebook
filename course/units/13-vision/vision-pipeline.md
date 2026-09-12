---
summary: A line-by-line reading of AprilTagVision.periodic: how two cameras' observations are filtered, weighted, fused, and handed to the drive, and the four places the implementation is not quite what it looks like.
objectives:
  - Trace one loop of our vision subsystem from camera inputs to a pose measurement
  - Explain each filter and the tunable behind it
  - Name the shortcuts in the current implementation
  - Explain finding F14 and what a disconnected camera does today
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java
---

## How it is wired

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="public AprilTagVision(VisionConsumer consumer" lines=14

Three things come in through the constructor:

| Argument | Ours | Why |
|---|---|---|
| `consumer` | `drive::addVisionMeasurement` | Vision **pushes** measurements into the pose estimator |
| `speedsSupplier` | `drive::getChassisSpeeds` | Measurements taken while moving fast get rejected |
| `io...` | Two `AprilTagIOPhotonVision` | Varargs, so a third camera is one more argument |

Vision never reads the drive's pose and never commands anything. It is a sensor that speaks in poses.

## One loop

**Read every camera's inputs, and log them.**

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="// Update Inputs" lines=5

Each camera gets its own log key, so `Vision/AprilTag/Camera0` and `Camera1` are separate branches in AdvantageScope, and replay feeds both back.

**Read the robot's speed once**, not per observation. The comment in the code says "for better runtime," and it is the right instinct: values that don't change within a loop should be read once.

**Filter each observation.**

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="// filtering" lines=10 highlight="2-9"

| Check | Tunable | Rejects |
|---|---|---|
| Ambiguity above 0.2 | `Tuning/Vision/maxAmb` | Single-tag solves that could be either of two poses |
| Height above 0.75 m | `Tuning/Vision/maxZError` | Solves that put the robot in the air, which means a bad solve |
| x or y outside the field | none | Impossible positions |
| Translation speed above 2.0 m/s | `Tuning/Vision/maxSpeed` | Motion blur |
| Rotation speed above 2.5 rad/s | `Tuning/Vision/maxAngularSpeed` | Motion blur while spinning |

All four tunables are read every loop, so they are genuinely live: you can tighten the ambiguity limit on the dashboard during a practice session and see the effect immediately.

**Accept what survives.**

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="acceptedPoses.add(new Pose3d(" lines=6

**Weight it by quality.**

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="double stdFactor = obs.avgTagDistance() / obs.tagCount();" lines=8

**Fuse and hand it over.** The accepted poses are averaged with those weights, yaw through a sine and cosine average, and the result goes to the drive. [Trusting Measurements](course:13-vision/trusting-measurements) covers that math and its bug.

## Four things the code does not quite say

:::warning Finding F13, in four parts
1. **Every camera uses camera 1's baselines.** The weighting lines reference `camera1linearStdDevBaseline` and `camera1angularStdDevBaseline` regardless of which camera the observation came from, so `camera2linearStdDevBaseline` (0.5) and `camera2angularStdDevBaseline` (1.0) are dead constants. Both cameras are treated as equally good.
2. **Camera 0's offsets are applied to everything.**

::source file="src/main/java/frc/robot/subsystems/vision/VisionConstants.java" from="public static final double camera0OffsetX" lines=5

Every accepted pose gets `camera0OffsetX` and `camera0OffsetY` added, whichever camera produced it, and `camera1OffsetX/Y` are unused. All four are 0.0 today, so nothing is wrong on the field; the shape of the code is wrong, and the day someone sets one of them it will be wrong in a way that is hard to see.

3. **One timestamp for two cameras.** The fused measurement carries a weighted average of the contributing timestamps. Two cameras that saw the field a few milliseconds apart become a single measurement at a time neither of them observed.
4. **Weights are 1/σ instead of 1/σ².** The next lesson works through what that costs.
:::

:::danger Finding F14: a disconnected camera keeps reporting
::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="inputs.isConnected = camera.isConnected();" lines=3

Everything after that line is inside `if (inputs.isConnected)`. When a camera drops off the network, the code takes no branch at all, so `inputs.hasTarget` and `inputs.poseObservations` **keep the values from the last loop they worked**. The subsystem then re-processes a stale observation every loop, feeding the same measurement into the pose estimator repeatedly, which is exactly the situation that makes a pose estimate drift with confidence.

There is a second half to it: `inputs.targetInfo` is only assigned inside the `hasTarget` branch, so it stays `null` until a target is seen. `AprilTagVision.getTargetInfo` loops over that array, so calling it before the first sighting throws a `NullPointerException`. Nothing calls it today, which is the only reason this hasn't bitten.

Both fixes are small: an `else` that clears the arrays and the flags when disconnected, and initializing `targetInfo` to an empty array in the inputs class.
:::

:::exercise id="u13-filter"
Implement the filter this lesson describes: the ambiguity, height, off-field, and motion checks, in our code's order, with strict comparisons so a value exactly at a limit is accepted.

You also write a `reason` method that names the first check that failed, which is the version you would actually want logged.
---hint
Write `reason` first and define `reject` in terms of it. One method holds the logic; the other is one line.
---hint
The off-field check is four comparisons: x below 0, y below 0, x past the field length, y past the field width.
---hint
Translation speed is `Math.hypot(vx, vy)`, and rotation speed uses the absolute value, because spinning either direction blurs the image.
:::

:::quiz
? What does `AprilTagVision` do with the drive's chassis speeds?
+ Rejects observations taken while the robot was moving or spinning too fast
- Predicts where the robot will be when the measurement arrives
- Converts the pose into robot-relative coordinates
- Chooses which camera to trust
> Motion blur makes a tag solve unreliable, and the speed at the time of the picture is the best available proxy.

? Why is the robot's speed read once per loop instead of once per observation?
+ It cannot change within a loop, so reading it repeatedly is wasted work
- The supplier is not thread-safe
- It would log too much data
- The value is only valid once
> The code's own comment says "for better runtime."

? A vision solve reports the robot 1.2 m above the carpet. What happens?
+ It is rejected, because a height that large means the solve is wrong
- It is accepted; z is dropped later anyway
- It is accepted with a larger standard deviation
- The camera is marked disconnected
> The z check runs before the conversion to 2D that would hide the problem.

? `camera2linearStdDevBaseline` is 0.5 while camera 1's is 0.2. Which does an observation from camera 2 actually use?
+ Camera 1's, because the weighting code references camera 1's constants for every camera
- Camera 2's
- The average of the two
- Whichever camera saw more tags
> Part of finding F13: camera 2's baselines are dead constants.

? A camera loses its network connection mid-match. What does our code report?
+ The last observation it saw, repeatedly, because nothing clears the inputs when disconnected
- No observations, because the inputs are cleared
- An empty array with `isConnected` false
- A dashboard alert
> Finding F14, and a repeated stale measurement is worse than none.

? Why is `getTargetInfo` a latent `NullPointerException`?
+ `inputs.targetInfo` is only assigned when a target is seen, so it is null until the first sighting
- It reads an array of a different length
- The tag ID may not exist in the layout
- It runs before the cameras are constructed
> Nothing calls it today, which is the only reason it hasn't failed.
:::
