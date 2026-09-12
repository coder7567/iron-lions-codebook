---
summary: Show that you understand our vision chain end to end: cameras, tags, transforms, filtering, trust, and the findings in the current implementation.
---

This test covers all of Unit 13.

:::exam Unit 13 test: vision
? Where does AprilTag detection run on our robot?
+ On a coprocessor running PhotonVision, which publishes results over NetworkTables
- On the roboRIO, in `AprilTagVision.periodic`
- On the Driver Station laptop
- Inside the camera itself
> The roboRIO reads results, never images.

? Our cameras are mounted with yaws of ±135° and a pitch of −30°. Where do they point?
+ Backward and outward, tilted up
- Forward and down
- Straight up
- At each other
> A negative pitch tilts a camera up in WPILib's convention.

? A camera is renamed in the PhotonVision interface but not in `VisionConstants`. What happens?
+ The robot silently receives nothing from it
- The code fails to compile
- PhotonVision falls back to the old name
- An alert appears automatically
> Camera names are a string contract between two systems.

? What does the field layout contribute to a pose measurement?
+ Where the detected tag sits on the field, which converts a relative sighting into a position
- The camera's calibration
- The robot's heading from the gyro
- The tag family and size
> Without it you have a bearing to an anonymous square.

? What is ambiguity in a single-tag solve?
+ Two camera poses explain the same image nearly equally well
- The tag ID could not be decoded
- Two tags were confused
- The exposure was wrong
> Our filter rejects anything above 0.2.

? Which measurement does our math consider better: two tags at 2 m, or one tag at 4 m?
+ Two tags at 2 m, by a wide margin
- One tag at 4 m
- They are equal
- It depends on the camera
> `avgTagDistance / tagCount` is 1.0 versus 4.0, and single-tag observations are doubled on top of that.

? What does the name `robotToCamera` mean?
+ A transform from the robot's frame into the camera's frame
- The camera's pose on the field
- The robot's position relative to the camera
- The rotation between them only
> Chain transforms whose frames line up, and use `inverse()` when they don't.

? In the single-tag solve, what does `fieldToTarget.plus(cameraToTarget.inverse())` give you?
+ Where the camera is on the field
- Where the robot is on the field
- The tag's pose relative to the robot
- The camera's mounting transform
> One more step, adding `robotToCamera.inverse()`, reaches the robot.

? A camera turns out to be mounted an inch further back than its constant says. What do you see?
+ Every vision pose is biased by an inch in a direction that rotates with the robot
- Vision stops producing measurements
- Ambiguity rises
- Only single-tag solves are affected
> Mounting error propagates into every measurement consistently.

? Why does `AprilTagVision` read the drive's chassis speeds?
+ To reject observations taken while the robot was moving or spinning fast enough to blur the image
- To predict the robot's future position
- To convert poses to robot-relative coordinates
- To choose which camera to use
> Motion blur ruins a tag solve, and speed is the available proxy.

? A vision solve places the robot 1.2 m above the carpet. What happens?
+ It is rejected, because a bad height means a bad solve
- It is accepted, since height is dropped in the 2D conversion
- It is accepted with a larger standard deviation
- It marks the camera disconnected
> The z check runs before the conversion that would hide it.

?num Two tags averaging 4 m away, with a 0.2 m baseline. What linear standard deviation does our code compute?
= 0.4 ± 0.01 m
> 0.2 × (4 / 2).

? What does the standard deviation handed to `addVisionMeasurement` control?
+ How far the pose estimator moves its estimate toward that measurement
- Whether the measurement is accepted
- How long it stays in the estimator's history
- The camera's exposure
> Vision supplies a value and a confidence; the estimator blends.

? Two cameras of equal quality (σ = 0.3 m each) agree. What should the fused standard deviation be, and what does our code report?
+ It should be about 0.21 m (σ over the square root of two); our code reports 0.15 m
- It should be 0.15 m; our code reports 0.21 m
- Both should be 0.3 m
- It should be 0.6 m
> Finding F13: our weights are 1/σ instead of 1/σ².

? Why is yaw averaged with sines and cosines?
+ A numeric average of angles near the wrap points the wrong way
- It is faster
- The estimator needs radians
- Because single-tag yaw is ignored
> Averaging 179° and −179° numerically gives 0°.

? A camera drops off the network mid-match. What does our code report today?
+ The last observation it saw, repeated every loop, because nothing clears the inputs
- Nothing, with `isConnected` false
- An empty observation array
- A dashboard alert
> Finding F14: repeating a stale value is worse than reporting none.

? Which of our vision findings most changes match behavior if fixed?
+ The fusion weighting and per-camera standard deviations
- The simulated camera's properties
- Deriving the HUB position from the tag layout
- The unused `VisionPoseObs` record
> It changes how far the estimator moves toward every fused measurement.

? When PhotonVision's library changes in 2027, which file should need the work?
+ `AprilTagIOPhotonVision`, the only file that mentions PhotonVision
- Every file in the vision package
- `AprilTagVision`
- `Drive`
> That isolation is what the IO layer is for.
:::
