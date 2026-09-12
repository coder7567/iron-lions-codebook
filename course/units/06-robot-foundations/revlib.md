---
summary: Create, configure, command, and read REV SPARK MAX and SPARK Flex motor controllers with the REVLib 2026 API, the way every mechanism on our robot does.
objectives:
  - Build SparkMaxConfig and SparkFlexConfig objects and apply them with configure
  - Command motors with duty cycle, voltage, and closed-loop setpoints, and read encoders and current
  - Choose conversion factors, followers, inversion, and idle modes correctly, and know when to persist settings
files:
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/util/SparkUtil.java
---

## The controllers and motors

Every motor on the 2026 robot runs through a REV motor controller:

| Controller | Class | Motors on our robot |
|---|---|---|
| SPARK MAX | `SparkMax` | NEO drive motors, NEO 550 turn motors, horizontal rollers |
| SPARK Flex | `SparkFlex` | NEO Vortex: flywheel, hood, turret, intake, arm, feeder |

Both extend `SparkBase`, which is why `ModuleIOSpark` stores its controllers as `SparkBase` fields. Creating one needs the CAN ID and the motor type:

```java
SparkFlex turret = new SparkFlex(12, MotorType.kBrushless);
```

All our motors are brushless, so the type is always `MotorType.kBrushless`. Choosing brushed for a brushless motor can damage it.

## Configuration objects

REVLib 2026 configures a controller by building a **config object** and applying it all at once. Setters chain, and related settings are grouped:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="turret = new SparkFlex(12, MotorType.kBrushless);" lines=23

| Group | Examples from our code |
|---|---|
| Top level | `idleMode(IdleMode.kBrake)`, `smartCurrentLimit(30)`, `inverted(true)`, `closedLoopRampRate(0.075)`, `voltageCompensation(12.0)` |
| `.encoder` | Built-in encoder: `positionConversionFactor`, `velocityConversionFactor` |
| `.absoluteEncoder` | `inverted`, `zeroOffset`, conversion factors |
| `.externalEncoder` | `countsPerRevolution(8192)` for the turret's through-bore encoder |
| `.closedLoop` | `feedbackSensor`, `pid(p, i, d)`, `outputRange`, `positionWrappingEnabled`, `feedForward.kS/kV` |
| `.softLimit` | `forwardSoftLimit`, `reverseSoftLimit`, and enable flags |
| `.signals` | How often status frames are sent |

### Applying a config

```java
turret.configure(turretConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
```

- **`ResetMode.kResetSafeParameters`** resets settings to defaults before applying yours, so nothing left over from last season sneaks in.
- **`PersistMode.kPersistParameters`** saves the settings to the controller's flash memory so they survive a power cycle. **`kNoPersistParameters`** applies them without saving.

`configure` sends many CAN messages and waits for replies. The drive retries it if the controller does not answer:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="tryUntilOk(" lines=7

:::warning Persist at boot, not during a match
Configure and persist once, in the constructor. Changing settings later, like our mode-dependent current limit, should use `kNoPersistParameters`, and should not happen in a way that blocks the loop. See finding **F8** in [The Robot Lifecycle](course:06-robot-foundations/robot-lifecycle).
:::

## Units and conversion factors

By default a SPARK's encoder reports **rotations** and **RPM**. Conversion factors change the units of both **readings and setpoints**:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="// Configure drive motor" lines=18

With `driveEncoderPositionFactor = 2π / 5.9`, `getPosition()` returns **wheel radians**, and velocity setpoints are in wheel radians per second. PID gains are then in those units too: `driveKp` means volts of output per rad/s of error. Change a conversion factor and every gain tuned against it changes meaning.

## Commanding a motor

| Method | Meaning | Our code |
|---|---|---|
| `set(value)` | Duty cycle, −1 to 1 | `intake.set(0)` in `IntakeIOSpark.stopIntake()` |
| `setVoltage(volts)` | A voltage, compensated for battery sag | `driveSpark.setVoltage(output)` for characterization |
| `getClosedLoopController().setSetpoint(value, type)` | The SPARK runs its own control loop toward a target | Turret position, flywheel velocity |

REVLib 2026 renamed the closed-loop call from `setReference` to **`setSetpoint`**; older examples online still show `setReference`, which is deprecated. The drive also adds its own feedforward voltage to each velocity setpoint:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="public void setDriveVelocity(double velocityRadPerSec)" lines=10

[Control on the Motor Controller](course:09-controls/onboard-control) explains how those gains work together.

## Reading the controller

| Method | Returns |
|---|---|
| `getEncoder().getPosition()` / `.getVelocity()` | Built-in encoder, in converted units |
| `getAbsoluteEncoder().getPosition()` | Absolute encoder on the data port |
| `getExternalEncoder().getPosition()` | External quadrature encoder (SPARK Flex) |
| `getOutputCurrent()` | Motor current in amps |
| `getAppliedOutput()` | Duty cycle actually applied, −1 to 1 |
| `getBusVoltage()` | Battery voltage at the controller |
| `getLastError()` | `REVLibError.kOk` if the last call worked |

Applied output is a fraction, not volts. Multiply by bus voltage to get volts, as the drive does:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="new DoubleSupplier[] {driveSpark::getAppliedOutput" lines=2

:::team In our code: two configs worth rereading
- `TurretIOSpark` logs `inputs.flywheelVolts = turret.getAppliedOutput();`. That reads the **turret** motor, not the flywheel, and it is a duty cycle, not volts (finding **F3**).
- The hood's closed loop uses its **absolute** encoder, but the conversion factor is set on the **relative** encoder with `hoodConfig.encoder.positionConversionFactor(1.0/36.0)`, so it has no effect on the hood's feedback (finding **F22**).

Configuration code is easy to skim and easy to get subtly wrong. Read it slowly.
:::

## Followers

Two motors on one mechanism can share a command. The flywheel's second motor **follows** the first, inverted because it faces the other way:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="flywheelFollower = new SparkFlex(10, MotorType.kBrushless);" lines=7

A follower copies its leader's output directly on the CAN bus. Only command the leader, and still give the follower its own current limit.

## Inversion and idle mode

- **`inverted(true)`** flips which direction counts as positive, so "positive" means "the useful direction" for the mechanism.
- **Idle mode** is what the motor does when commanded to zero: **brake** shorts the windings to resist motion, while **coast** lets it spin freely.

| Mechanism | Idle mode in our code | Why |
|---|---|---|
| Drive | brake | Stop quickly and hold position |
| Turret, arm | brake | Hold aim and hold the arm up |
| Flywheel | coast | Spin down gently instead of stopping a heavy wheel |
| Intake and horizontal rollers | coast | Nothing to hold; less stress |
| Hood | coast | Its closed loop holds position while enabled |

## Before the code runs

Use the **REV Hardware Client** to set each controller's CAN ID and update firmware to a version compatible with REVLib 2026. A controller with old firmware may refuse configuration calls, and `getLastError()` or the Driver Station console will say so.

:::quiz
? What does `ResetMode.kResetSafeParameters` do when passed to `configure`?
+ Resets settings to defaults before applying the new config, so old settings cannot linger
- Saves the settings to flash memory
- Resets the CAN ID to 0
- Stops the motor safely
> Starting from defaults makes the config object a complete description of the controller.

? The drive encoder's position conversion factor is 2π / 5.9. What unit does `getPosition()` return?
- Motor rotations
- Meters
+ Wheel radians
- Degrees
> One motor rotation is 2π/5.9 radians of wheel rotation, so positions come back in wheel radians.

? Which REVLib 2026 method sends a closed-loop target to a SPARK?
- `setReference`
+ `setSetpoint`
- `setTarget`
- `setPID`
> `setReference` is deprecated in REVLib 2026 in favor of `SparkClosedLoopController.setSetpoint`.

?tf `getAppliedOutput()` returns the voltage applied to the motor.
= false
> It returns duty cycle from −1 to 1. Multiply by `getBusVoltage()` to estimate volts.

? Why is the flywheel configured with `IdleMode.kCoast`?
+ So the heavy wheel spins down gently instead of being braked hard when commanded to zero
- Coast mode makes the flywheel faster
- Brake mode is not allowed on SPARK Flex
- To hold the flywheel's position
> Braking a heavy flywheel stresses the mechanism and draws large currents.
:::
