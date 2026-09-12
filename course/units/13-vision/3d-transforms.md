---
summary: Four coordinate frames, three transforms, and the two lines of algebra in our vision code that turn a tag sighting into a robot pose.
objectives:
  - Distinguish a pose from a transform and name the frame each value lives in
  - Read `Transform3d` algebra, including when and why `inverse()` appears
  - Trace both solve paths in `AprilTagIOPhotonVision`
  - Convert a 3D pose into the 2D pose the estimator wants
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
---

## Four frames

| Frame | Origin | Used for |
|---|---|---|
| **Field** | The blue alliance wall corner | Everything the robot navigates by |
| **Robot** | The center of the drivetrain, +x forward | Chassis speeds, module positions |
| **Camera** | The camera's lens, +x out of the lens | What PhotonVision measures |
| **Tag** | The center of a tag's face | The layout's poses |

Every vision value belongs to exactly one of those frames, and the bugs in vision code are almost always a value used in the wrong one.

## Pose, translation, transform

| Type | Means | Example |
|---|---|---|
| `Translation3d` | A point or an offset, three numbers | The camera is 0.25 m forward |
| `Rotation3d` | An orientation: roll, pitch, yaw | Pitched 30° up, yawed 135° |
| `Pose3d` | A position **in a frame** | The robot's pose on the field |
| `Transform3d` | A **change of frame**: how to get from A to B | robotToCamera |

The naming convention is the whole game: `robotToCamera` means "start in the robot frame, apply this, end in the camera frame." Read left to right, and a chain of transforms works if the frames line up: `fieldToRobot` plus `robotToCamera` is `fieldToCamera`.

When the frames don't line up, you need an `inverse()`. `robotToCamera.inverse()` is `cameraToRobot`, which is what lets you go from a camera's field position back to the robot's.

## The multi-tag path

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="fieldToCamera = multitagResult.estimatedPose.best;" lines=4

PhotonVision's multi-tag solve already produces `fieldToCamera`, so there is one step left:

```text
fieldToRobot = fieldToCamera + cameraToRobot
             = fieldToCamera + robotToCamera.inverse()
```

Then `new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation())` reinterprets that transform as a pose, because a transform from the field origin *is* a pose in the field frame.

## The single-tag path

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="var tagPose = VisionConstants.kTagLayout.getTagPose" lines=8

One more step, because the camera measured the tag rather than the field:

```text
fieldToTarget  = the layout's pose for this tag
cameraToTarget = what the camera measured
fieldToCamera  = fieldToTarget + targetToCamera
               = fieldToTarget + cameraToTarget.inverse()
fieldToRobot   = fieldToCamera + robotToCamera.inverse()
```

Two inverses, for the same reason each time: we have a transform pointing one way and need it pointing the other. Both lines read as "walk from the field to the tag, then backwards along the camera's measurement, then backwards along the camera's mounting."

:::warning The mounting transform has to be right
Every pose the robot computes is shifted by whatever error is in `robotToCamera`. A camera mounted an inch further back than the constant says puts every vision measurement an inch off, consistently, in a direction that rotates with the robot. It looks like a mysterious bias, not like a broken camera.

Measure the mounting from CAD when you can, verify it on the real robot, and re-measure after any collision that could have moved a camera.
:::

## Down to two dimensions

The drivetrain lives on the floor, so the pose estimator wants a `Pose2d`. `AprilTagVision` fuses in three dimensions and converts at the very end:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="consumer.accept(" occurrence=2 lines=4

`toPose2d()` keeps x, y, and yaw, and drops z, roll, and pitch. Those dropped values are not useless first: a pose floating half a meter above the carpet is a bad solve, which is exactly what the z filter checks before the conversion happens.

:::exercise id="u13-transform-wpi"
Write the transform chain with WPILib geometry: the camera's mounting transform, the robot pose from a camera pose, the camera pose from a tag sighting, and the whole single-tag solve.

This exercise uses WPILib, so run it through Gradle:

```shell
./gradlew test --tests "frc.training.u13.CameraTransformsTest"
```
---hint
`robotPoseFromCamera` is one line of algebra plus a conversion: `fieldToCamera.plus(robotToCamera.inverse())`, then build a `Pose3d` from its translation and rotation.
---hint
For `fieldToCamera`, turn the tag's `Pose3d` into a `Transform3d` from the field origin, then add the inverse of the camera-to-target measurement.
---hint
`robotPoseFromTag` should call the other two. If the chain is right, the composition is free.
:::

:::quiz
? What does the name `robotToCamera` tell you?
+ It transforms from the robot's frame into the camera's frame
- The camera's position in the field frame
- The robot's position relative to the camera
- The rotation between the two, without translation
> Read the name left to right, and chain transforms whose frames line up.

? Why does the code call `robotToCamera.inverse()`?
+ It needs `cameraToRobot` to get from the camera's field position back to the robot's
- To flip the camera's image
- To convert radians to degrees
- To handle the red alliance
> An inverse turns a transform around when the frames don't line up.

? In the single-tag path, what does `fieldToTarget.plus(cameraToTarget.inverse())` produce?
+ `fieldToCamera`: where the camera is on the field
- The tag's pose relative to the robot
- The robot's pose on the field
- The camera's mounting transform
> Walk from the field to the tag, then backwards along the camera's measurement.

? A camera is actually mounted 1 inch further back than `VisionConstants` says. What do you see?
+ Every vision pose is off by an inch in a direction that rotates with the robot, which looks like a mysterious bias
- Vision stops working entirely
- The ambiguity value rises
- Only multi-tag solves are affected
> Mounting error propagates into every measurement, consistently.

? What does `toPose2d()` drop, and why is that safe here?
+ Height, roll, and pitch, because the drivetrain is on the floor and a bad height was already filtered out
- Rotation, because the gyro provides it
- Nothing; it is a rename
- The timestamp
> The z filter uses that information before it is discarded.

?tf `Pose3d` and `Transform3d` are interchangeable, since both hold a translation and a rotation.
= false
> A pose is a position in a frame; a transform is a change of frame. Confusing them is the classic vision bug.
:::
