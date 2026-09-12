---
summary: How a camera on a coprocessor becomes data in robot code, what our two cameras are and where they point, and the settings that live outside the repository.
objectives:
  - Describe the path from photons to a pose observation
  - Read our camera constants as a physical description of the robot
  - Explain `getAllUnreadResults` and the latency choice our code makes
  - Name what lives on the coprocessor and why that is a risk
files:
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java
  - src/main/java/frc/robot/RobotContainer.java
---

## The chain

| Stage | Where it runs | What it produces |
|---|---|---|
| Exposure and capture | The camera | A frame |
| Tag detection | PhotonVision, on a coprocessor | Corners of each visible tag |
| Pose solving | PhotonVision | Camera-to-tag transforms, ambiguity, and a multi-tag solve when several tags are visible |
| Publishing | NetworkTables | A result with a timestamp |
| Reading | `AprilTagIOPhotonVision` on the roboRIO | `PoseObservation` records in our inputs |
| Filtering and fusing | `AprilTagVision` | One pose handed to the drive's estimator |

The roboRIO does none of the image processing. That is the point of a coprocessor: a camera at 30 frames per second with a 20 ms robot loop would leave no time for anything else.

## Our two cameras

::source file="src/main/java/frc/robot/subsystems/vision/VisionConstants.java" from="public static final String AprilTagCamera1Name" lines=8

Read the numbers as a physical description. Both cameras are:

- **9.733 inches behind** the robot's center (negative x),
- **9.733 inches to one side** (camera 1 to the right, camera 2 to the left),
- **9.314 inches up**,
- **pitched 30 degrees upward** (a negative pitch tilts the camera up in WPILib's convention),
- **yawed ±135 degrees**, so they look backward and outward, away from each other.

Two cameras pointed back and out cover a wide arc behind the robot. That matters for REBUILT because the HUB tags are what the robot most needs while it lines up a shot, and the turret side of the robot faces them.

The name strings must match the camera names in the PhotonVision web interface **exactly**. A renamed camera on the coprocessor produces a robot that never sees a tag, with no error anywhere in the code.

:::team Camera positions are tunables that never change
Each number is wrapped in a `LoggedNetworkNumber` (`ATC1x`, `ATC1yaw`, and so on) but read with `.get()` while the class initializes, so changing one on the dashboard does nothing until the code restarts. The upside is real, though: the values are **logged**, so any log tells you what mounting the robot believed it had. If the camera moves, change the constant and redeploy.
:::

## Reading results

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="var results = camera.getAllUnreadResults();" lines=3

`getAllUnreadResults()` returns every result that arrived since the last call, which at 30 frames per second and a 50 Hz loop is usually zero or one, and occasionally two. Our code takes **only the last one** and drops the rest.

That is a deliberate trade: the newest frame is the least stale, and a dropped frame is one fewer pose measurement. Teams that want every measurement loop over the whole list instead, which gives the estimator more data at the cost of processing older frames. Either is defensible; ours favors freshness and simplicity.

Each result carries its own **timestamp** from the coprocessor, which is what lets the pose estimator apply a measurement at the moment it was taken rather than the moment it arrived. Vision data is always late; timestamps are how that lateness stops being a problem.

## What lives outside the repository

| Lives in the repo | Lives on the coprocessor |
|---|---|
| Camera names and mounting transforms | Exposure, brightness, and gain |
| Filtering thresholds | Resolution and frame rate |
| Standard deviations | Detection decimation and blur settings |
| Which tag layout to use | Pipeline selection and calibration |

**Camera calibration is the important one.** PhotonVision needs a calibration for the resolution it runs at, and that calibration lives on the coprocessor. A reflashed coprocessor, a swapped camera, or a changed resolution means recalibrating, and nothing in Git will remind you.

Two habits prevent a bad weekend: export the PhotonVision settings after any change and commit them somewhere in the team's repository, and add "cameras report calibrated, both connected, tags detected" to the pit checklist.

:::quiz
? Why does tag detection run on a coprocessor instead of the roboRIO?
+ Image processing at 30 frames per second would not fit in a 20 ms robot loop
- The roboRIO has no USB ports
- WPILib forbids image processing
- The coprocessor is closer to the camera
> The roboRIO reads results, not images.

? Our cameras are mounted with a yaw of ±135 degrees and a pitch of −30 degrees. Where do they point?
+ Backward and outward, tilted upward
- Forward and slightly down
- Straight up
- Toward each other
> A negative pitch tilts the camera up in WPILib's convention.

? A camera is renamed in the PhotonVision interface but not in `VisionConstants`. What happens?
+ The robot never receives results from it, with no error in the code
- The code fails to compile
- PhotonVision renames it back
- The camera falls back to a default pipeline
> Camera names are a string contract between two systems.

? `getAllUnreadResults()` returns two results in one loop. What does our code do?
+ Uses the newest one and drops the other
- Uses both
- Uses the oldest one
- Throws them both away
> It favors freshness; averaging both is a legitimate alternative.

? Why does each result carry a timestamp?
+ So the pose estimator can apply the measurement at the time the picture was taken, not when it arrived
- To sort results from two cameras
- To detect a disconnected camera
- Because NetworkTables requires it
> Vision data is always late. Timestamps make the lateness harmless.

?? Which of these live on the coprocessor rather than in our repository? Select all that apply.
+ Camera calibration
+ Exposure and resolution settings
+ Pipeline selection
- Camera mounting transforms
> Anything not in Git needs a checklist item and an exported backup.
:::
