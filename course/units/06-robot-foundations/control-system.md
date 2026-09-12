---
summary: How the pieces of the FRC control system (battery, PDH, roboRIO, CAN bus, radio, coprocessor, and Driver Station) fit together on the 2026 robot, and what each means for code.
objectives:
  - Trace power, CAN, and network connections through the 2026 robot
  - Explain CAN IDs, termination, and status frame timing
  - Recognize brownouts and find the robot's network addresses and ports
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/GyroIONavX.java
  - src/main/java/frc/robot/Robot.java
---

## The big picture

A competition robot is a small network of computers and motor controllers. Programmers do not wire it, but debugging a robot means knowing where every signal travels.

::diagram name="control-system" caption="Simplified 2026 control system. The real robot has many more power wires; the idea is what matters."

## The components

| Part | What it does | On our robot |
|---|---|---|
| **Battery** | 12 V lead-acid battery, sealed, about 13 lb | Swapped between matches |
| **Main breaker** | 120 A master switch and fuse | The red button that turns the robot on |
| **PDH** (Power Distribution Hub) | Splits power into breaker-protected channels and measures current | Feeds every motor controller and the electronics |
| **roboRIO 2** | The robot's computer, running our Java program on NI Linux Real-Time | Also hosts the dashboard layout web server |
| **Motor controllers** | Turn roboRIO commands into motor power, with built-in encoders and control loops | 18 REV SPARK MAX and SPARK Flex |
| **Motors** | Brushless NEO, NEO 550, and NEO Vortex | Drive, turn, turret, flywheel, and intake |
| **Radio** | Connects the robot to the field network or your laptop | Configured with our team number |
| **Coprocessor** | A small computer running PhotonVision | Processes images from two AprilTag cameras |
| **NavX** | Gyro and accelerometer | Plugged into the roboRIO's MXP port |
| **Driver Station laptop** | Runs the FRC Driver Station and dashboards | Controllers plug in here, not into the robot |

## The CAN bus

**CAN** (Controller Area Network) is how the roboRIO talks to motor controllers and the PDH. It is two twisted wires, yellow (CAN High) and green (CAN Low), running in a **daisy chain** from device to device. A 120 Ω **termination resistor** at each end keeps signals clean. The roboRIO has one built in, and the PDH has a switch for the other end.

Every device has a **CAN ID**. SPARKs on the same bus must all have different IDs, but devices of different kinds, like the PDH and a SPARK, may reuse a number. Our SPARKs use IDs 1 through 18.

:::warning A bad CAN connection looks like many broken motors
Because the bus is a chain, one loose connector can cut off every device after it. If the Driver Station suddenly reports several disconnected motors at once, suspect the wiring near the first missing device before blaming code. Our `Module` class raises an alert like "Disconnected drive motor on module 2." for exactly this situation.
:::

### Status frames: how often devices report

CAN has limited bandwidth, about 1 megabit per second, shared by every device. Each SPARK sends **status frames** at configurable rates. `ModuleIOSpark` asks for drive position every 10 ms, to feed 100 Hz odometry, and everything else every 20 ms:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from=".signals" lines=8

Faster frames give fresher data but add traffic. If CAN utilization in the Driver Station's diagnostics climbs high, devices can start missing frames.

## Power and brownouts

Motors pulling hard, especially at the start of a sprint or while pushing another robot, can drag the battery voltage down. If the roboRIO sees voltage fall below its **brownout** threshold (about 6.75 V by default on the roboRIO 2), it disables motor outputs until voltage recovers. To the driver, the robot suddenly goes limp for a moment.

Code prevents brownouts with **current limits**. Our drive limits each drive motor to 30 A in teleop and 80 A in autonomous. [Unit 9](course:09-controls/protecting-mechanisms) explains the trade-offs.

## Networking

Every device on the robot network gets an address based on the team number. For team 967, the pattern is **10.9.67.x**:

| Device | Address or name |
|---|---|
| Radio | `10.9.67.1` |
| roboRIO | `10.9.67.2`, or by name `roboRIO-967-FRC.local` |
| Driver Station laptop | Assigned automatically on the robot network |
| Coprocessor | A fixed address chosen by the team, for example `10.9.67.11` |

Programs listen on **ports**:

| Service | Where | Port |
|---|---|---|
| NetworkTables 4 (dashboards, AdvantageScope live data) | roboRIO | 5810 |
| Elastic dashboard layout download | roboRIO | 5800 |
| PhotonVision web interface | coprocessor | 5800 |

Port 5800 appears twice, but on two different devices, so there is no conflict. The roboRIO's layout server is started at the end of the `Robot` constructor:

::source file="src/main/java/frc/robot/Robot.java" from="// Elastic Config" lines=2

It serves the `deploy` folder, so Elastic can download `elastic-layout.json` straight from the robot.

## Where each program runs

| Program | Runs on |
|---|---|
| Our robot code (Java) | roboRIO |
| PhotonVision | Coprocessor |
| FRC Driver Station | Windows laptop |
| Elastic and AdvantageScope | Laptop, reading data from the roboRIO |
| PathPlanner app | Laptop (autos are saved into `deploy` and run on the roboRIO) |

The NavX is the exception to "everything over CAN": it sits directly on the roboRIO's MXP expansion port and talks over SPI:

::source file="src/main/java/frc/robot/subsystems/drive/GyroIONavX.java" from="private final AHRS navX" lines=1

:::info 2027 changes this picture
The 2027 control system replaces the roboRIO with **Systemcore**, which has multiple CAN buses and an onboard IMU, and does not support SPI. Code like `NavXComType.kMXP_SPI` will need to change. See [Preparing for 2027 and Systemcore](course:16-season/systemcore-2027).
:::

:::quiz
? What happens when a loose CAN connector breaks the chain between SPARK 5 and SPARK 6?
+ Devices after the break can stop communicating, so several motors may report disconnected at once
- Only SPARK 5 stops working
- The roboRIO reboots
- The robot switches to Wi-Fi
> A daisy chain depends on every connection before each device.

?tf The power distribution hub and a SPARK MAX may both use CAN ID 1.
= true
> IDs must be unique among devices of the same kind. Our SPARKs use IDs 1 through 18.

? The driver says the robot "went limp for a second" while pushing another robot. What is a likely cause?
- A CAN ID conflict
+ A brownout: battery voltage dropped below the roboRIO's threshold, so it briefly disabled outputs
- The radio changed channels
- The roboRIO ran out of memory
> Heavy current draw drags battery voltage down. Current limits help prevent this.

?text What is the roboRIO's IP address for team 967?
= 10.9.67.2
> The pattern is 10.TE.AM.x, so 967 becomes 10.9.67.x and the roboRIO is .2.

? Why does `ModuleIOSpark` set the drive position status frame to 10 ms instead of the default slower rate?
+ Odometry samples the encoders 100 times per second, so it needs positions that fresh
- Faster frames use less CAN bandwidth
- REV requires 10 ms for brushless motors
- The Driver Station reads positions directly
> Status frame timing is a trade-off between fresh data and CAN traffic.
:::
