---
summary: What our simulated cameras model, what they can and can't prove, and the five upgrades that would make this the strongest subsystem on the robot instead of the one with the most findings.
objectives:
  - Run vision in simulation and explain what it models
  - Decide which vision questions simulation can answer
  - Prioritize the known vision findings by what they cost
  - Plan for a coprocessor and library change in 2027
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagIOSim.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## Simulated cameras

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOSim.java" from="public class AprilTagIOSim extends AprilTagIOPhotonVision" lines=1

PhotonVision ships a simulator. `VisionSystemSim` holds the tag layout and the robot's pose; `PhotonCameraSim` renders what a camera at a given mounting would see and publishes results the same way a real camera does. Our sim IO extends the real IO, feeds the simulator the drivetrain's simulated pose, and then lets the **real** parsing code run:

```java title="The whole sim implementation"
@Override
public void updateInputs(AprilTagIOInputs inputs) {
  visionSim.update(poseSupplier.get());
  super.updateInputs(inputs);
}
```

That is a genuinely good design: the transform math, the filtering, and the fusion in simulation are the same code the robot runs. What differs is only where the images come from.

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOSim.java" from="var cameraProperties = new SimCameraProperties();" lines=3

The camera model is the **default** one, not ours. Default properties describe a generic camera: a resolution, a field of view, and no calibration error, latency, or noise. So simulation answers geometry questions honestly and optical ones not at all.

| Simulation can answer | Simulation cannot answer |
|---|---|
| Does the transform chain produce the right pose? | Will the camera actually detect that tag? |
| Does the filter reject what it should? | Does glare wash out the tag at that angle? |
| Does the fusion math behave with two cameras? | How much latency does the real pipeline add? |
| Does the estimator converge, and how fast? | Is the camera calibrated correctly? |
| What happens when a camera sees nothing? | Does motion blur ruin a moving shot? |

Calibrating `SimCameraProperties` to match the real camera's resolution, field of view, latency, and noise turns several rows from the right column into the left one. That is upgrade three below.

## The upgrades, in order

:::steps
1. **Fix the fusion math (F13).** Per-camera baselines, weights of 1/σ², and either per-camera timestamps or one measurement per camera instead of a blended one. This is the change that most affects how the robot actually behaves.
2. **Handle a disconnected camera (F14).** Clear the observations and `hasTarget` in an `else` branch, and initialize `targetInfo` to an empty array. A stale measurement repeated every loop is worse than no measurement.
3. **Calibrate the simulated camera.** Give `SimCameraProperties` the real resolution, field of view, latency, and noise so simulation results transfer.
4. **Give each camera its own estimator.** PhotonLib's `PhotonPoseEstimator` per camera, each pushing its own timestamped measurement, removes the blending problem entirely and is less code than what we have.
5. **Derive the HUB from the layout (F15).**

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="private static final Translation2d hub" lines=1

The HUB is hard-coded at (4.625, 4.0) while the tag layout puts its center at about y = 4.035. Computing it from the tags the layout already holds removes a hand-typed number from the aiming path, and it would update itself if the layout ever did.
:::

Items 1 and 2 are an afternoon each and change match behavior. Items 3 through 5 are off-season work that makes the next season easier.

## What changes in 2027

The control system changes, and vision changes with it:

- **SystemCore replaces the roboRIO**, with different networking and different coprocessor options.
- **Packages move** from `edu.wpi.first.*` to `org.wpilib.*`, so imports change everywhere.
- **PhotonVision's vendordep will be rebuilt** for the new season and the new hardware.

The thing that protects us is the boundary this unit is built around. `AprilTagVision` never mentions PhotonVision: it holds `AprilTagIO` references and works with our own `PoseObservation` records. When the library changes, `AprilTagIOPhotonVision` is the file that changes, and the filtering, weighting, fusion, and everything downstream stay exactly as they are.

That is the payoff of an IO layer, and it is worth remembering the next time writing one feels like ceremony.

:::tip A vision pit checklist
- Both cameras appear in the PhotonVision interface and report the expected resolution.
- Each camera has a calibration for that resolution.
- The robot logs show `isConnected` true for both, with observations arriving.
- The pose on the dashboard agrees with where the robot actually is, within a few centimeters.
- Camera mountings are tight, and nothing has been bumped since the last match.
:::

:::quiz
? What does `AprilTagIOSim` add to the real IO implementation?
+ It feeds a simulated camera the robot's simulated pose, then lets the real parsing code run
- It replaces the transform math with a simpler version
- It generates poses directly, bypassing PhotonVision
- It disables filtering
> Same code above the camera; only the image source changes.

? Our simulated cameras use default `SimCameraProperties`. What does that mean for simulation results?
+ Geometry and logic are trustworthy; optical behavior like detection quality, latency, and noise is not modeled
- Nothing is trustworthy
- Everything matches the real robot
- Only multi-tag solves are simulated
> Calibrating the properties is what moves rows from "cannot answer" to "can."

? Which vision fix most changes how the robot behaves in a match?
+ The fusion weighting and per-camera standard deviations (F13)
- Calibrating the simulated camera
- Deriving the HUB position from the tag layout
- Adding a third camera
> It changes how far the estimator moves toward every fused measurement.

? Why is a disconnected camera that keeps reporting its last observation worse than one that reports nothing?
+ The same stale measurement is fed to the estimator every loop, which drags the pose confidently toward a position the robot has left
- It uses more CAN bandwidth
- It triggers an alert storm
- It causes a NullPointerException
> Repetition makes a stale value look like agreement.

? When PhotonVision's library changes for 2027, which file in our vision package has to change?
+ `AprilTagIOPhotonVision`, because it is the only file that mentions PhotonVision
- Every file in the vision package
- `AprilTagVision` and `VisionConstants`
- None; the vendordep handles it
> That isolation is the reason the IO layer exists.

? Why prefer deriving the HUB's position from the tag layout over a hard-coded constant?
+ The layout is already the authority on field geometry, and a derived value updates itself when the layout does
- It is faster at runtime
- Hard-coded constants can't be logged
- The turret requires a `Pose3d`
> Our hard-coded 4.0 differs from the tag-derived 4.035, which is finding F15.
:::
