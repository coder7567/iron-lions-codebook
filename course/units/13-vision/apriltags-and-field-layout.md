---
summary: What an AprilTag is, how the field layout turns a sighting into a position, and why a two-tag solve is worth so much more than a one-tag solve.
objectives:
  - Explain how a tag plus a layout produces a robot pose
  - Load and use the field layout the way our code does
  - Compare single-tag and multi-tag solves, including ambiguity
  - Explain what tag distance and count do to accuracy
files:
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## A tag is a known landmark

An AprilTag is a black and white square whose pattern encodes a number. FRC uses the **36h11** family, printed 8.125 inches across, and the REBUILT field carries **32** of them at surveyed positions.

Knowing which tag you are looking at, and where that tag is bolted to the field, is what turns a camera into a position sensor:

1. The camera sees a tag and measures where it is **relative to the camera**: a `Transform3d` with distance and angle.
2. The field layout says where that tag is **on the field**.
3. Combining the two gives where the camera is on the field, and the camera's mounting gives where the robot is.

Without step 2 you have a bearing to an anonymous square. The layout is what makes it navigation.

## Loading the layout

::source file="src/main/java/frc/robot/subsystems/vision/VisionConstants.java" from="public static final AprilTagFieldLayout kTagLayout" lines=2

`AprilTagFields.kDefaultField` is WPILib's current default, which for 2026 is the welded REBUILT field. There is also an AndyMark variant, because the two field constructions differ by small amounts. **The difference matters**: a few centimeters of tag position error becomes a few centimeters of robot position error, all match long.

The layout is used in two more places in our code, both worth knowing:

- `AprilTagIOPhotonVision` asks it for a tag's pose during a single-tag solve.
- `VisionConstants.kTagLayout.getFieldLength()` is the field length used by the turret's passing map and by `AllianceFlipUtil`. **One source of truth for the field's size**, rather than a constant someone typed.

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="var tagPose = VisionConstants.kTagLayout.getTagPose" lines=8

Notice `tagPose.isPresent()`. `getTagPose` returns an `Optional` because the tag ID might not be in the layout: a tag from another field, a misread, or a tag the layout doesn't include. The code skips the observation rather than guessing, which is the right call.

## One tag or several

| | Single tag | Multi-tag |
|---|---|---|
| How it solves | Geometry from one square's corners | One solve using every visible tag at once |
| Failure mode | **Ambiguity**: two poses fit the same image | Far more constrained |
| Our code's trust | Linear standard deviation doubled, angular set to 99999 | Baseline values |
| PhotonVision reports | `poseAmbiguity` per target | A combined `estimatedPose` with its own ambiguity |

**Ambiguity** is the single-tag problem worth understanding. A square seen from an angle can be explained by two different camera positions, mirrored about the tag's plane. When the tag is small in frame, or seen nearly straight on, the two solutions fit almost equally well, and the reported ambiguity number rises toward 1. Our filter rejects anything above 0.2.

That is also why a single tag's heading is distrusted completely. The angular standard deviation of 99999 in our code means, in effect, "use this for position, and ignore what it says about which way we're facing." With two or more tags, the geometry pins the rotation down and the real baseline is used.

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="fieldToCamera = multitagResult.estimatedPose.best;" lines=4

## What makes a measurement good

| Factor | Effect | In our code |
|---|---|---|
| **Number of tags** | More tags constrain the solve, especially rotation | `tagCount` divides the standard deviation |
| **Distance** | Error grows with distance; a far tag is a few pixels wide | `avgTagDistance` multiplies the standard deviation |
| **Angle to the tag** | A tag seen edge-on is harder to solve | Shows up as higher ambiguity |
| **Motion blur** | A moving robot smears the tag | Rejected by the speed filters |
| **Lighting** | Glare and darkness both lose tags | An exposure setting on the coprocessor |

Our quality math is exactly the first two: `avgTagDistance / tagCount`, scaled by a baseline. Two tags at 2 meters is a much better measurement than one tag at 4 meters, and the code says so in the only language the pose estimator understands, which is a standard deviation.

:::tip Field tags at an event
Tags get bumped and damaged. If a tag looks crooked or has a peeling corner, tell field staff; it is their tag and their layout. Never patch around it in code by hard-coding a different pose, because the next field will have it right and your robot will be wrong in a new way.
:::

:::quiz
? What does the field layout add to a camera's measurement?
+ Where the tag it saw is bolted to the field, which turns a relative measurement into a position
- The camera's calibration
- The robot's heading
- The tag's family and size
> Without the layout you know where a tag is relative to you, and nothing else.

? Why does `getTagPose` return an `Optional`?
+ The detected ID might not exist in the layout, so the code must handle its absence
- Tag poses are only available in simulation
- The layout loads lazily
- The tag might be too far away
> Our code skips the observation rather than guessing.

? What is ambiguity in a single-tag solve?
+ Two different camera poses explain the same image nearly equally well
- The tag ID could not be decoded
- Two tags were confused for each other
- The camera is out of focus
> It rises when the tag is small in frame or seen straight on, and our filter rejects anything over 0.2.

? Why does our code set a single-tag observation's angular standard deviation to 99999?
+ To tell the pose estimator to use the position but ignore the heading from that measurement
- To reject the observation entirely
- Because the gyro is more accurate in degrees
- It is a placeholder that was never finished
> A single tag constrains position far better than rotation.

? Which measurement is better: two tags at 2 m, or one tag at 4 m?
+ Two tags at 2 m, by a wide margin in our math
- One tag at 4 m
- They are equivalent
- It depends on the alliance
> `avgTagDistance / tagCount` is 1.0 against 4.0, and the single-tag case doubles on top of that.

?tf If a field tag looks damaged, the fastest fix is to hard-code a corrected pose for it in `VisionConstants`.
= false
> Tell field staff. A code patch follows you to the next field, where the tag is fine.
:::
