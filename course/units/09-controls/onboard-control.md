---
summary: Almost every loop on our robot runs inside a SPARK, not on the roboRIO. What that buys, what it costs, and the configuration and CAN-bandwidth details that come with it.
objectives:
  - Explain the trade-offs between controller-side and roboRIO-side control loops
  - Read a SPARK configuration and say what each section does
  - Explain status frame periods and why our odometry signals are faster than the rest
  - Use `tryUntilOk` and `ifOk` and explain the problem they solve
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/util/SparkUtil.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## Where the loop lives

Our robot code runs every 20 ms. A SPARK runs its own closed loop about **fifty times faster**, in the controller, right next to the encoder. So when `setDriveVelocity` sends a setpoint, it isn't asking for one correction; it is handing the controller a goal that the controller keeps working on between our loops.

| | Loop on the SPARK | Loop on the roboRIO |
|---|---|---|
| Rate | About 1 kHz | 50 Hz, our loop |
| Latency to the motor | None; the loop is in the controller | One CAN round trip per correction |
| Survives a slow robot loop | Yes; the setpoint stays in effect | No; a loop overrun delays every correction |
| Gains live in | The controller's configuration | Your code |
| Can use WPILib's tools | No | Yes: profiles, state-space, feedforward classes |
| Easy to log and replay | Only what you send and read back | Everything |

That trade explains our code's split. Mechanisms that need fast, simple regulation (wheel speed, turret angle, flywheel RPM) close on the SPARK. Things that need the whole robot's picture (heading control, path following) close on the roboRIO, where the pose estimator lives.

## Reading a SPARK configuration

A configuration object is built in sections, then applied once:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="// Configure drive motor" lines=18 highlight="3-6,14-18"

| Section | Sets |
|---|---|
| Top level | Idle mode (brake or coast), smart current limit, inversion, voltage compensation |
| `.encoder` / `.absoluteEncoder` / `.externalEncoder` | Which sensor, its conversion factors, measurement period and averaging |
| `.closedLoop` | Feedback sensor, PID gains, output range, position wrapping, REVLib feedforward |
| `.softLimit` | Forward and reverse travel limits |
| `.signals` | How often the controller reports each value on the CAN bus |

Two details in the top-level section matter more than they look:

- **`voltageCompensation(12.0)`** makes the controller scale its output so a command means the same thing at 12.6 V fresh off the charger and at 11.2 V late in a match. Without it, every gain you tune drifts with the battery.
- **`smartCurrentLimit(...)`** caps the current the motor may draw. It protects the motor and the battery, and it is the main defense against brownouts. Our drive runs at 30 A in teleop and 80 A in autonomous; turn motors get 20 A; the flywheel 60 A.

## Status frames: the CAN bus is a budget

A SPARK doesn't volunteer data; it publishes each signal on a schedule, and every message shares one CAN bus with 17 other devices. Asking for everything quickly is how you saturate a bus.

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="        .signals" occurrence=1 lines=8

The position signal is set to `1000 / odometryFrequency` milliseconds, which is 10 ms for our 100 Hz odometry. Everything else stays at 20 ms, matching the robot loop. The reasoning is worth copying: **make the signals that feed odometry fast, and leave the rest at loop rate.**

Our odometry sampling runs on its own thread (`SparkOdometryThread`), which is why `Drive.periodic` takes a lock before reading module inputs. Unit 10 covers that.

## Configuration that survives a bad moment

Configuring a SPARK is a CAN transaction, and a transaction can fail: a controller that is still booting, a momentarily noisy bus, a loose connector. Our code retries instead of hoping:

::source file="src/main/java/frc/robot/util/SparkUtil.java" from="public static void tryUntilOk" lines=10

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="tryUntilOk(" occurrence=1 lines=7

Reading has the same problem in reverse: a value read from a disconnected controller is garbage, not an exception. `ifOk` checks the controller's error status before it accepts a reading, and records a sticky fault when it doesn't:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="sparkStickyFault = false;" occurrence=1 lines=9

The last line turns that fault flag into a connection status, smoothed by a 0.5 second debouncer so a single dropped frame doesn't fire an alert. `Module.periodic` turns that into an on-dashboard `Alert`, which is how a disconnected module announces itself in the pit instead of during a match.

:::team Why our turret's kP can't be tuned from the dashboard
`TurretIOSpark` configures the turret with `.pid(TurretConstants.turretP.get(), 0.0, TurretConstants.turretD.get())`. Those `.get()` calls run **once**, while the constructor builds the configuration. Changing `turretP` on the dashboard afterward changes the `LoggedNetworkNumber` and nothing else, because the gain now lives in the controller. Tuning a controller-side gain means reconfiguring the controller or restarting robot code. [Tuning at 967](course:09-controls/tuning-at-967) lists which of our tunables are live and which are frozen at boot.
:::

## Reset and persist modes

Every `configure` call takes two modes:

| Mode | Meaning | Our choice |
|---|---|---|
| `ResetMode.kResetSafeParameters` | Start from factory defaults, then apply this config | Yes: the controller's state can't surprise you |
| `PersistMode.kPersistParameters` | Write the settings into the controller's flash | Yes at boot, so a controller that reboots mid-match comes back configured |

Persisting at boot is right. Persisting **repeatedly** is not: each write blocks while the controller stores the settings, and flash memory has a limited number of write cycles. That is why `setCurrentLimit`, which runs at every mode change, is finding F8:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setCurrentLimit" lines=5

Switching that call to `PersistMode.kNoPersistParameters` keeps the runtime change and drops both costs.

:::quiz
? What is the main advantage of closing a velocity loop on the SPARK instead of the roboRIO?
+ The loop runs far faster and doesn't wait on CAN round trips for each correction
- It uses less code
- It works while the robot is disabled
- It allows higher PID gains
> The controller sits next to the encoder and runs about 1 kHz.

? Why does the heading controller in `joystickDriveFacingTarget` run on the roboRIO instead?
+ It needs the pose estimator, which only exists on the roboRIO
- Heading loops can't run on a SPARK
- The NavX is plugged into the roboRIO
- Because it uses a `ProfiledPIDController`
> Loops that need the whole robot's picture belong where that picture lives.

? Our drive encoder's position signal is published every 10 ms while everything else uses 20 ms. Why?
+ Odometry samples at 100 Hz, and the rest only needs to be fresh once per robot loop
- Position messages are smaller
- The SPARK can't publish velocity faster
- To reduce the number of devices on the bus
> Status frame periods are a CAN bandwidth budget; spend it where it buys something.

? What problem does `tryUntilOk` solve?
+ A configuration call can fail on a busy or briefly disconnected bus, so it retries a few times
- It converts REVLib errors into exceptions
- It makes configuration faster
- It prevents two threads from configuring at once
> Configuration is a CAN transaction, and transactions fail sometimes.

? What does `ifOk` protect against?
+ Accepting a garbage reading from a controller that returned an error
- Dividing by zero
- Reading an encoder that is not configured
- Losing precision on doubles
> A failed read returns a value, not an exception, so the error status has to be checked.

? Why is `PersistMode.kPersistParameters` a poor choice inside `setCurrentLimit`?
+ It writes flash on every mode change, which blocks and wears out the controller's memory
- Current limits can't be persisted
- It resets the encoder position
- It only works while disabled
> Persist at boot; apply runtime changes without persisting.
:::
