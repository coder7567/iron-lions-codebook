---
summary: Every term this course uses that a new programmer would not already know, with the meaning it has on this team.
---

Terms are grouped by where you meet them. Where a word means something specific in our code, that
meaning is what is given.

## FRC and the season

**Alliance** — The three teams on one side of a match, red or blue. The alliance decides which
coordinate flip applies and which HUB is yours.

**AndyMark field / welded field** — Two ways the field is built, with slightly different tag positions.
WPILib ships a layout for each.

**Auto (autonomous)** — The first 20 seconds of a REBUILT match, with no driver input.

**Brownout** — The battery voltage sagging far enough that the roboRIO disables motor outputs to
protect itself.

**Coprocessor** — A small computer on the robot that does work the roboRIO cannot, such as running
PhotonVision.

**Driver Station** — The laptop application that connects to the robot, controls enable and disable,
and reports the alliance, the match time, and game data.

**FMS** — The Field Management System. It runs the match, sends game data, and reports an approximate
match time.

**FUEL** — REBUILT's game piece: a 5.91 inch foam ball.

**Game data** — One character, `R` or `B`, sent shortly after autonomous, naming the alliance whose HUB
is inactive first.

**HUB** — REBUILT's scoring goal. Each alliance has one, and only one is active at a time during the
teleop shifts.

**Shift** — One of four 25-second teleop periods during which only one alliance's HUB accepts FUEL.

**Teleop** — The 140 seconds of driver control after autonomous.

## Robot code structure

**AdvantageKit** — The logging framework this robot uses. It records every input so a match can be
replayed later.

**Command** — An action with a lifecycle: `initialize`, `execute`, `isFinished`, `end`.

**CommandScheduler** — The WPILib object that runs subsystem periodic methods, polls triggers, and runs
commands, once per loop.

**Default command** — The command that runs on a subsystem when nothing else requires it. Ours drives
from the joysticks.

**IO layer** — An interface (`ModuleIO`, `TurretIO`) that separates hardware from logic. It is what
makes replay, simulation, and testing possible.

**Inputs** — A plain object of sensor values filled by the IO layer each loop, logged and replayable.

**Periodic** — A method called once per 20 ms loop.

**Requirement** — The subsystem a command needs. The scheduler allows only one command per subsystem at
a time.

**Replay** — Re-running recorded inputs through the same code on a laptop, producing the same decisions.

**Subsystem** — A class owning one group of hardware. Ours are `Drive`, `AprilTagVision`, `Turret`,
`Intake`, and `Superstructure`.

**Superstructure** — Our coordinating subsystem. It owns no hardware and decides what the turret and
intake should each be doing.

**Trigger** — A condition checked every loop that can schedule or cancel commands on its edges.

**Wanted state / current state** — What someone asked for, and what the subsystem decided to do about
it. Every mechanism subsystem has both.

## Control

**Closed loop** — Commanding a result and correcting from a measurement. Open loop commands effort and
does not measure.

**Continuous input** — Telling a controller that its measurement wraps, so it takes the short way
around. Swerve turn motors need it.

**Deadband** — A region near a joystick's center treated as zero. Ours is circular: it applies to the
stick's distance from center.

**Feedforward** — A predicted output based on the setpoint. Our model is kS times the direction of
travel plus kV times velocity.

**kS, kV, kA, kG** — Feedforward gains for friction, velocity, acceleration, and gravity.

**Motion profile** — A setpoint that moves at limited velocity and acceleration instead of jumping.

**PID** — Proportional, integral, derivative: correction from the current error, the accumulated error,
and the rate of change.

**Setpoint** — The value a controller is trying to reach.

**Soft limit** — A travel limit enforced inside the motor controller.

**SysId** — WPILib's system identification routine, which measures feedforward gains from voltage ramps
and steps.

## Swerve and navigation

**Chassis speeds** — The robot's motion as vx, vy, and omega. Robot-relative unless converted.

**Desaturation** — Scaling all module speeds down together when one exceeds the maximum, so direction
is preserved.

**Field-relative** — Commanded in the field's frame rather than the robot's. Only as correct as the
pose's heading.

**Kinematics** — The math converting chassis speeds to module states and back.

**Module state / module position** — A speed and angle, versus a distance traveled and angle. Control
uses states; odometry uses positions.

**Odometry** — Estimating position by accumulating wheel and gyro measurements.

**Pose** — A position and rotation in a frame, usually the field's, whose origin is the blue alliance
wall corner.

**Pose estimator** — Odometry plus a history buffer, so late vision measurements can be applied at the
time they were taken.

**Optimize** — Flipping a module's angle by half a turn and reversing its wheel, so it never turns more
than 90 degrees.

## Vision

**Ambiguity** — How well two different camera poses explain the same single-tag image. High means
untrustworthy.

**AprilTag** — A printed square marker encoding a number. The field has 32 of them.

**Multi-tag solve** — One pose computed from several tags at once, far better constrained than a single
tag.

**PhotonVision** — The vision software running on our coprocessor.

**Standard deviation** — How much a measurement should be trusted. Smaller means the estimator moves
further toward it.

**Transform** — A change of frame, such as `robotToCamera`. A pose is a position **in** a frame; a
transform gets you **between** frames.

## Tools

**AdvantageScope** — The log viewer used to graph values, watch the field, and compare runs.

**Elastic** — The dashboard this team uses. Its layout ships in the deploy folder.

**GradleRIO** — The Gradle plugin that builds and deploys robot code.

**NetworkTables** — The publish and subscribe system between robot, dashboard, and coprocessors.

**PathPlanner** — The app and library for drawing and following autonomous paths.

**REV Hardware Client** — The tool for setting CAN IDs and updating firmware on SPARK controllers.

**URCL** — The unofficial REV logging library, which records every SPARK's data into our log.

**WPILib** — The official FRC library: commands, geometry, control, and simulation.
