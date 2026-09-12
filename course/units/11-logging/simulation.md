---
summary: What our simulation does, what it models faithfully, and the three places it quietly disagrees with the real robot.
objectives:
  - Run the simulator and drive the robot in it
  - Explain what each simulated IO models
  - Name the parts of our simulation that don't match the robot, and why they matter
  - Decide what is worth testing in simulation and what isn't
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSim.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSim.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java
  - build.gradle
---

## Running it

`./gradlew simulateJava` starts the robot code on your computer. Two extras are enabled in `build.gradle`:

```groovy title="From build.gradle"
wpi.sim.addGui().defaultEnabled = true
wpi.sim.addDriverstation()
```

The sim GUI shows joysticks, the robot's mode, and raw device state; the Driver Station window lets you enable teleop and autonomous. Drag a controller into a joystick slot, enable teleop, and the drivetrain responds exactly the way it would on the robot, because everything above the IO layer is the same code.

For anything visual, AdvantageScope connected to the simulator gives you the field view, module states, and every logged value live.

## What the simulated IO models

| IO | Models | How |
|---|---|---|
| `ModuleIOSim` | Drive and turn motors | Two `DCMotorSim` physics models, driven by a WPILib `PIDController` and the same feedforward the robot uses |
| `AprilTagIOSim` | Cameras | Generates observations from the simulated pose |
| `TurretIOSim` | The flywheel | A `FlywheelSim`, with turret and hood angles assumed to reach their setpoints instantly |
| `IntakeIOSim` | The rollers | A `FlywheelSim` for the intake, with the arm assumed to reach its setpoint instantly |

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSim.java" from="driveSim =" lines=8

The drivetrain's simulation is the good one. It runs a real physics model at 20 ms, with gearing, moment of inertia, and voltage limits, and it closes the same loops the SPARKs close on the robot. Driving in simulation feels close enough to the real robot that you can find logic bugs, tune command flow, and test autos.

## Where it disagrees with the robot

Three differences matter enough to name, because each one makes a simulation result **wrong rather than approximate**.

:::danger Finding F5: the intake-safe interlock is inverted
::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSim.java" from="inputs.resetting = Math.abs" lines=2

The real IO reports `intakeSafe` when the turret is **within** tolerance of its idle position; the simulated IO reports it when the turret is **more than** a tolerance away. The Superstructure's `IDLE` handoff therefore behaves backwards in simulation. A test of that interlock on a laptop proves nothing about the robot.

`resetting` has the same problem with a different threshold: π instead of 0.1 rad, so it is essentially never true in simulation.
:::

:::danger Finding F6: the flywheel never spins, so the feeder never runs
`TurretIOSim.setFlyWheelSpeed` stores the setpoint and never gives the `FlywheelSim` any voltage, so `flywheelSpeed` stays at 0. `shooterSpedUp()` compares that against setpoints of 1850 to 2700 RPM and is false whenever the turret is shooting, so the intake's feeder gate never opens. **You cannot watch the robot shoot in simulation.**

`IntakeIOSim` never fills `intakeCurrent` or `armSetAngle` either, so jam detection and recovery can't happen there either.
:::

:::warning Finding F7: the sim IO classes construct real hardware
::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java" from="public class IntakeIOSim extends IntakeIOSpark" lines=1

Both `IntakeIOSim` and `TurretIOSim` extend their Spark counterparts and call `super()`, which constructs real `SparkFlex` and `SparkMax` objects for hardware that isn't there. It works, because REVLib tolerates it, and it means any behavior you didn't explicitly override silently comes from the hardware class.

Implementing `IntakeIO` directly makes the simulation smaller, faster, and honest about what it models.
:::

## What simulation is good for

| Worth simulating | Not worth simulating |
|---|---|
| Command flow and state machines | Whether a shot actually goes in |
| Autonomous path geometry and named-command sequencing | Mechanism tuning |
| Button bindings and interlocks (once F5 is fixed) | Current draw and brownouts |
| Vision pose math against a known pose | Camera exposure, lighting, and tag detection quality |
| Anything you would otherwise test by putting a robot on blocks | Anything involving friction, a game piece, or another robot |

The honest summary for our robot today: **the drivetrain simulates well, and the scoring mechanisms don't.**

## Fixing our simulation

Four changes, roughly in order of value:

1. Give `flywheelSim` voltage in `TurretIOSim.setFlyWheelSpeed`, so `shooterSpedUp` can become true and the feeder gate can be exercised.
2. Fill `inputs.armSetAngle` and a modeled `intakeCurrent` in `IntakeIOSim`, so jam detection and recovery become testable.
3. Fix the `intakeSafe` and `resetting` comparisons to match the real IO, ideally by sharing one helper between both classes.
4. Implement the IO interfaces directly instead of extending the Spark classes.

Each is small. Together they turn a simulator that can only test driving into one that can test the whole robot, which is what makes off-season and pre-ship work possible without the robot.

:::quiz
? What does `ModuleIOSim` actually model?
+ Motor physics with `DCMotorSim`, closed by the same kind of loops the SPARKs run
- A recording of the real drivetrain
- Nothing; it returns the setpoints unchanged
- Only the encoders
> The drivetrain is the well-modeled part of our simulation.

? Why can't you see the robot shoot in simulation today?
+ The simulated flywheel never receives voltage, so `shooterSpedUp()` is false and the feeder gate never opens
- The turret has no simulated encoder
- The Superstructure disables shooting in simulation
- `FlywheelSim` doesn't support Vortex motors
> Finding F6: the sim stores the setpoint but never drives the model.

? In simulation, the turret is 1 rad away from its idle position. What does `intakeSafe` report?
+ True, because the simulated comparison is inverted relative to the robot
- False, like the robot
- True on the robot and false in simulation
- It's not implemented in simulation
> Finding F5. A result verified in simulation would be backwards on the robot.

? Why is `IntakeIOSim extends IntakeIOSpark` a problem?
+ It constructs real motor controller objects, and any behavior not overridden comes from the hardware class
- It makes simulation slower than real time
- Simulation IO can't use inheritance
- It breaks replay
> An IO that implements the interface directly only does what it says it does.

? Which of these is worth testing in simulation?
+ Whether a named command fires at the right point in an auto path
- Whether the flywheel's kP is well tuned
- Whether the robot browns out during a spin-up
- Whether the intake grips FUEL reliably
> Simulate logic and sequencing; measure physics on the robot.

?tf Once the four listed fixes are made, simulation could test the intake's jam recovery.
= true
> Filling in current and the arm setpoint is exactly what that path needs.
:::
