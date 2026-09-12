---
summary: Symptom-first troubleshooting for this robot: what to check, in what order, and which lesson explains the underlying cause.
---

Find the symptom, work down the checks in order, and stop when something is wrong. Each entry points at
the lesson that explains why.

## Cannot deploy or connect

| Symptom | Check in this order |
|---|---|
| Deploy fails to find the robot | Robot powered on, radio lights, laptop on the robot's network, then try a USB cable |
| Deploy fails partway | Battery voltage; a browning-out roboRIO drops the connection mid-deploy |
| Driver Station shows no robot code | The deploy actually succeeded; then the roboRIO's console for a startup exception |
| Code deploys but nothing runs | A crash in the `Robot` constructor, which prints to the console. Everything in ours runs there: metadata, receivers, `RobotContainer`, and the web server |
| Gradle wants to download something at an event | You did not build offline before leaving ([the runbook](course:16-season/pit-programmer-runbook)) |

## The robot will not move

| Symptom | Check in this order |
|---|---|
| Nothing moves at all | Enabled, correct mode, battery, main breaker, then the Driver Station's joystick tab |
| Some motors move | Dashboard alerts for module or gyro disconnects, then the CAN chain past the last working device |
| It moves in autonomous but not teleop | Controller in USB slot 0, and slot 1 for the operator |
| It stops mid-match | Bus voltage in the log; brownouts show up there before they show up anywhere else |
| One mechanism is dead | Its current in the log: zero current means no command or no power; high current with no motion means a mechanical bind |

## Driving feels wrong

| Symptom | Likely cause | Lesson |
|---|---|---|
| Drifts to one side driving straight | A module zero is off | [Characterizing Swerve](course:10-swerve/characterizing-swerve) |
| Controls are backwards, or rotated | The pose's heading is wrong, or the alliance flip is fighting it | [Joystick Driving](course:07-command-based/joystick-drive) |
| Twitchy near the stick's center | Deadband or squaring | [Joystick Driving](course:07-command-based/joystick-drive) |
| A module spins the long way around | Continuous input not configured on that loop | [PID](course:09-controls/pid) |
| Sluggish out of a stop | Current limit, or feedforward kS | [Feedforward](course:09-controls/feedforward) |
| Robot pushes off its spot easily | No X-stop bound; brake mode only | [Driving Well](course:10-swerve/driving-well) |

## Shooting problems

| Symptom | Check | Lesson |
|---|---|---|
| Shots consistently short or long | The shot map against the measured distance, then the hood offset | [Shot Maps](course:14-shooting/shot-maps) |
| Shots miss to one side while driving | The reality constant and the flight-time table | [Shoot on the Move](course:14-shooting/shoot-on-the-move) |
| Shots miss to one side while stopped | The pose, then the HUB constant, then the turret's zero | [Turret Aiming](course:14-shooting/turret-aiming) |
| The first shot of a burst is weak | The flywheel tolerance is wide enough to feed at 1500 RPM | [Flywheel and Feeding](course:14-shooting/flywheel-and-feeding) |
| The feeder never runs | The turret is resetting, or the flywheel never reaches tolerance | [Flywheel and Feeding](course:14-shooting/flywheel-and-feeding) |
| The controller buzzes and shots miss | The target is in the turret's deadzone: reposition the robot | [Turret Aiming](course:14-shooting/turret-aiming) |
| The turret aims at the HUB while paused far away | Finding F1 | [Turret States](course:08-state-machines/turret-state-machine) |

## Intake problems

| Symptom | Check | Lesson |
|---|---|---|
| The intake spits FUEL back out | Jam detection triggered: 50 loops above 35 A and below 1000 RPM | [The Intake State Machine](course:08-state-machines/intake-state-machine) |
| The arm does not deploy | The Superstructure's state, then whether the turret reports intake-safe | [The Superstructure](course:08-state-machines/superstructure) |
| The arm deploys briefly at startup | Finding F16: the boot value of the arm setpoint | [Code Audit](course:reference/code-audit) |
| Rollers run but nothing feeds | The feeder gate: turret resetting, or flywheel below tolerance | [Flywheel and Feeding](course:14-shooting/flywheel-and-feeding) |

## Vision problems

| Symptom | Check | Lesson |
|---|---|---|
| No vision measurements at all | Both cameras connected in the PhotonVision interface, then the camera names against `VisionConstants` | [Cameras and PhotonVision](course:13-vision/cameras-and-photonvision) |
| The pose jumps when tags appear | Standard deviations too small, or the fusion weighting | [Trusting Measurements](course:13-vision/trusting-measurements) |
| The pose drifts and vision never corrects it | Measurements being rejected: check speed, ambiguity, and height limits | [The Vision Pipeline](course:13-vision/vision-pipeline) |
| Vision worked, then quietly stopped being right | A camera disconnected and the inputs went stale (finding F14) | [The Vision Pipeline](course:13-vision/vision-pipeline) |
| Poses are consistently offset | The camera mounting transform | [3D Transforms](course:13-vision/3d-transforms) |

## Autonomous problems

| Symptom | Check | Lesson |
|---|---|---|
| The auto drives but no mechanism runs | A named command is not registered | [Named Commands](course:12-autonomous/named-commands-and-events) |
| The auto is offset from the field by a constant | `resetOdom`, or the robot was placed wrong | [Testing Autos](course:12-autonomous/testing-autos) |
| The robot falls behind its path | Constraints exceed the robot, or the battery is low | [PathPlannerLib](course:12-autonomous/pathplannerlib) |
| The auto is missing from the chooser | Not deployed, or a stale copy is on the roboRIO | [The PathPlanner App](course:12-autonomous/pathplanner-app) |
| The auto drives sideways or forever | You selected "Move Forward" (finding F17) | [Joystick Driving](course:07-command-based/joystick-drive) |
| The robot ends autonomous still shooting | Every auto ends with `start`, and nothing resets it | [Named Commands](course:12-autonomous/named-commands-and-events) |

## Dashboard and logs

| Symptom | Check | Lesson |
|---|---|---|
| No log after a match | The USB stick was not in the roboRIO | [Telemetry and Logs](course:11-logging/telemetry-and-logs) |
| The dashboard is empty | The layout was not downloaded from the robot, or NetworkTables is not connected | [Dashboards, Tunables, and Alerts](course:11-logging/dashboards-tunables-alerts) |
| A tunable does nothing | It is read once at boot, not every loop | [Tuning at 967](course:09-controls/tuning-at-967) |
| The log names the wrong commit | Finding F12 | [Telemetry and Logs](course:11-logging/telemetry-and-logs) |
| Replay does not reproduce a match | An input bypassed the IO layer, such as the Driver Station calls | [Replay](course:11-logging/replay) |

## Build and tooling

| Symptom | Fix |
|---|---|
| Gradle fails on a fresh clone | Run once online so it can fetch the wrapper and dependencies |
| Tests fail with `UnsatisfiedLinkError` | Run through Gradle, which sets up WPILib's native libraries |
| Simulation opens with no joystick | Drag a controller into a slot in the sim GUI, then enable in the Driver Station window |
| The course site shows stale content | Rebuild with `node tools/build.mjs`, and use the local server rather than opening files directly |
| A course build fails on a `::source` anchor | The robot code moved: update the anchor, which is the point of the check |

:::tip When nothing on this page matches
Go back to the [debugging method](course:11-logging/debugging-method): reproduce it, look at the log
around the moment, localize it to logic, control, configuration, hardware, or environment, then change
one thing. This page is a shortcut for problems that have happened before, not a substitute for the
method.
:::
