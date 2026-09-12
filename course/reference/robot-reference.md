---
summary: Every number that describes the 2026 robot, in one place: CAN map, sensors, drivetrain, turret, intake, vision, field geometry, controls, and the software stack.
---

Everything here is read out of the team repository at commit `fe04405`. When the code changes, this
page is wrong until someone updates it, so check the source before betting a match on a number.

## CAN map

| ID | Device | Controller | Motor | Notes |
|---|---|---|---|---|
| 1 | Front left drive | SPARK MAX | NEO | 5.9:1, brake |
| 2 | Front left turn | SPARK MAX | NEO 550 | 18.75:1, inverted, absolute encoder |
| 3 | Back left drive | SPARK MAX | NEO | |
| 4 | Back left turn | SPARK MAX | NEO 550 | |
| 5 | Back right drive | SPARK MAX | NEO | |
| 6 | Back right turn | SPARK MAX | NEO 550 | |
| 7 | Front right drive | SPARK MAX | NEO | |
| 8 | Front right turn | SPARK MAX | NEO 550 | |
| 9 | Flywheel leader | SPARK Flex | Vortex | Inverted, coast, 60 A |
| 10 | Flywheel follower | SPARK Flex | Vortex | Follows 9, inverted |
| 11 | Hood | SPARK Flex | | Absolute encoder, coast, 40 A |
| 12 | Turret | SPARK Flex | | External 8192 CPR encoder, brake, 30 A, output ±0.5 |
| 13 | Intake roller | SPARK Flex | | Inverted, coast, 30 A |
| 14 | Intake arm | SPARK Flex | | Absolute encoder, brake, 40 A, output ±0.25 |
| 15 | Feeder | SPARK Flex | | Inverted, brake, 30 A, 0.05 s ramp |
| 16 | Horizontal 1 | SPARK MAX | | Coast, 20 A |
| 17 | Horizontal 2 | SPARK MAX | | Inverted, coast, 20 A |
| 18 | Intake follower | SPARK Flex | | Follows 13 |

Sensors: **NavX** over MXP SPI at 100 Hz, four **absolute turn encoders**, an **absolute hood
encoder**, an **absolute arm encoder** (zero offset 0.31), an **external turret encoder**, and two
**PhotonVision cameras**.

## Drivetrain

| Quantity | Value |
|---|---|
| Track width and wheelbase | 20 in (0.508 m) |
| Module positions | ±0.254 m in x and y, order FL, FR, BL, BR |
| Drive base radius | 0.359 m |
| Wheel radius | 2 in (0.0508 m) |
| Drive reduction | 5.9:1 |
| Turn reduction | 18.75:1 |
| Max linear speed | 4.2 m/s |
| Max angular speed | about 11.7 rad/s |
| Odometry rate | 100 Hz, on its own thread |
| Drive current limit | 30 A teleop, 80 A autonomous |
| Turn current limit | 20 A |
| Module zero rotations | FL −1.671, FR 1.603 − π, BL −0.047, BR 1.549 rad |
| Drive gains | kP 0.01, kD 0.005, kS 0.12349, kV 0.12293 |
| Turn gains | kP 1.0, kD 0 |
| Deadband | 0.1, applied to the stick's magnitude |
| PathPlanner | Mass 74.088 kg, MOI 6.883, wheel COF 1.2, translation and rotation PID 5.0 |

## Turret and shooter

| Quantity | Value |
|---|---|
| Turret gear ratio | 180 / 44 |
| Turret travel | −4.261 to 1.6 rad (soft limits) |
| Unreachable arc | 1.6 to 2.022 rad, flagged as the deadzone |
| Idle position | −1.6 rad, seeded at boot |
| Intake-safe tolerance | 0.05 rad |
| Starting offset | π, plus ±0.05 rad per operator bumper press |
| Turret gains | kP 1.0, kD 0, output ±0.5, ramp 0.075 s |
| Hood travel | 0.530 to 0.907 rotations (0.239 and 0.616 plus the 0.291 offset) |
| Hood gains | kP 5.0, kD 0 |
| Flywheel gains | kP 0.001, kS 0.2, kV 0.00193 |
| Flywheel tolerance | 1000 RPM below the setpoint counts as ready |
| Testing values | 3000 RPM and 0.6 rotations, both tunable |

**Shot map** (distance in meters to RPM and hood, before the 0.291 offset):

| m | 1.03 | 1.77 | 2.00 | 2.38 | 2.86 | 3.377 | 3.84 | 4.06 | 4.66 | 5.56 | 6.45 |
|---|---|---|---|---|---|---|---|---|---|---|---|
| RPM | 1850 | 1950 | 2000 | 2050 | 2100 | 2250 | 2300 | 2400 | 2500 | 2600 | 2700 |
| Hood | 0.588 | 0.588 | 0.588 | 0.55 | 0.525 | 0.5 | 0.475 | 0.45 | 0.425 | 0.4 | 0.35 |

**Time of flight:** 1.93 m → 1.22 s, 3.92 m → 1.3 s, 4.13 m → 1.3 s, 5.45 m → 1.45 s. Reality constant
defaults to 1.0.

**Passing map** (keyed by field x, mirrored per alliance): zone edge → 2000 RPM, midfield → 2600,
opposite zone edge → 3250, far wall → 5000, all at hood 0.25 plus the offset. Passing targets are
(1, 6) and (1, 2), chosen by the robot's y against the 4.0 m center line.

## Intake

| Quantity | Value |
|---|---|
| Arm travel | 0.0305 to 0.78 rotations, zero offset 0.31 |
| Arm positions | Rest 0.15, intake 0.77 |
| Arm gains | kP 1.0, output ±0.25 |
| Roller speed | 5000 RPM |
| Feeder speed | 4500 RPM |
| Horizontal speeds | 9000 RPM each |
| Jam detection | Above 35 A and below 1000 RPM for 50 loops (1.0 s) |
| Unjam | 10 reversing loops (0.2 s), requires the arm within 0.25 of its setpoint |

## Vision

| Quantity | Value |
|---|---|
| Cameras | `April_Tag_1` and `April_Tag_2` |
| Mounting | 9.733 in back, ±9.733 in sideways, 9.314 in up, pitch −30°, yaw ∓135° |
| Tag layout | `AprilTagFields.kDefaultField` (2026 welded), 32 tags, 36h11, 8.125 in |
| Filters | Ambiguity 0.2, height 0.75 m, on-field bounds, speed 2.0 m/s, rotation 2.5 rad/s |
| Standard deviations | Linear 0.2 m, angular 0.524 rad, scaled by average tag distance over tag count |
| Single tag | Linear doubled, angular set to 99999 |

## Field and game

| Quantity | Value |
|---|---|
| Field | 16.541 m by 8.069 m, rotationally symmetric |
| Blue HUB | About (4.626, 4.035); the code uses (4.625, 4.0) |
| Alliance zone depth | 5.0 m from the wall |
| Autonomous | 20 s, both HUBs active |
| Teleop | 140 s: transition above 130, shifts at 130, 105, 80, 55, end game below 30 |
| Game data | `R` or `B` names the alliance whose HUB is inactive first |
| Trench clearance | 22.25 in tall |

## Controls

| Input | Port | Action |
|---|---|---|
| Left stick | Driver, 0 | Field-relative translation, flipped for red |
| Right stick x | Driver, 0 | Rotation |
| Right trigger | Driver, 0 | `SHOOTING` |
| Right bumper | Driver, 0 | `PAUSED` |
| Left trigger | Driver, 0 | `IDLE` |
| Left bumper | Driver, 0 | `EJECTING` |
| Start | Driver, 0 | `TESTING` |
| Left bumper | Operator, 1 | Turret trim +0.05 rad |
| Right bumper | Operator, 1 | Turret trim −0.05 rad |

**Rumble** fires when the turret is `SHOOTING` while our HUB is inactive, when the intake is jammed, or
when the turret target is in the deadzone.

## Autos and paths

Nine autos: `4646 Left`, `4646 right`, `Depot`, `Just Preload`, `NZ Depot`, `NZ Score`,
`Reverse NZ Depot`, `Right NZ`, `Right NZ Race`, plus PathPlanner's `None`. Twenty-two paths.
Named commands: `start` sets `SHOOTING`, `reverse` sets `EJECTING`.

The chooser also holds four SysId routines, a wheel radius characterization, a feedforward
characterization, and `Move Forward`.

## Software stack

| Component | Version |
|---|---|
| Java | 17 |
| GradleRIO | 2026.2.1 |
| Gradle wrapper | 8.11 |
| AdvantageKit | 26.0.2 |
| PathPlannerLib | 2026.1.2 |
| PhotonLib | v2026.3.2 |
| REVLib | 2026.0.5 |
| Studica (NavX) | 2026.0.0 |
| URCL | 2026.0.0 |

## Log keys worth knowing

`Odometry/Robot`, `Odometry/Trajectory`, `SwerveStates/Setpoints`, `SwerveStates/SetpointsOptimized`,
`SwerveStates/Measured`, `Drive/Module0` through `Module3`, `Drive/Gyro`, `Wanted State`,
`CurrentState`, `Turret State`, `Turret/turretAngle`, `Turret/turretSetAngle`, `Turret/flywheelSpeed`,
`DistanceToHub`, `FieldBasedTurret`, `Calculations/target`, `TOF`, `Hub Active`, `Period Time`,
`Match Time`, `TotalCurrent`, `Vision/AprilTag/Camera0` and `Camera1`.
