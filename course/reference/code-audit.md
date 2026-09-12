---
summary: Every finding this course made in the 2026 robot code, with where it lives, what it costs, and how to fix it. The lessons cite these by number.
---

This page is the course's findings list. Each entry was read out of the repository at commit
`fe04405`, and each names a file so you can check it yourself. **Nothing here has been changed in the
team's repository**; these are for the team to fix deliberately, on its own branches.

Numbers are stable identifiers rather than a sequence, so the lessons can cite them without
renumbering when the list changes.

Three things this list is not. It is not a criticism of the people who wrote the code, which works and
won an Industrial Design Award at Iowa. It is not exhaustive; a careful reader will find more. And it
is not a to-do list in priority order, which is what the last section is for.

## Summary

| ID | Area | Finding | Match impact | Effort |
|---|---|---|---|---|
| F1 | Turret | `PAUSEDPASSING` is unreachable | Wrong aim while paused outside our zone | One line |
| F2 | Superstructure | Current averaging never averages | None; the value is never logged | Small |
| F3 | Turret IO | `flywheelVolts` reads the turret motor's duty cycle | Diagnosis only | One line |
| F4 | Turret | Passing rebuild drops the hood offset | About 4 degrees of hood after an alliance change | Small |
| F5 | Simulation | `intakeSafe` and `resetting` are inverted in sim | Misleading simulation results | Small |
| F6 | Simulation | The simulated flywheel and intake never produce data | Shooting and jams cannot be simulated | Medium |
| F7 | Simulation | Sim IO classes extend the hardware IO classes | Constructs real device objects in sim | Medium |
| F8 | Drive IO | Current limit persisted at every mode change | Blocking CAN and flash wear at the worst moment | Small |
| F9 | PathPlanner | App settings disagree with the code | Paths preview differently than they run | Small |
| F10 | Robot | Deprecated `Command.schedule()` | None today; a compile error later | One line |
| F11 | Drive | Pathfinder installed twice | None; confusing | One line |
| F12 | Build | `BuildConstants` is stale and committed | Every log names the wrong commit | Small |
| F13 | Vision | Fusion weighting, shared baselines, blended timestamps | Overconfident pose corrections | Medium |
| F14 | Vision IO | A disconnected camera keeps reporting | Stale poses fed to the estimator | Small |
| F15 | Turret | HUB y hard-coded at 4.0 versus 4.035 | 3.5 cm of aim bias | Small |
| F16 | Intake IO | An output inside `updateInputs` | One loop of arm delay; arm deploys at boot | Small |
| F17 | Autos | "Move Forward" strafes at 0.78 m/s and never ends | Surprising autonomous behavior | Small |
| F18 | Drive commands | `joystickDriveFacingTarget` lacks the red flip | Reversed controls on red, if it were bound | One line |
| F19 | Superstructure | `hubActive` goes stale outside teleop | Rumble uses a stale value when disabled | Small |
| F20 | Repository | Dead code and unused constants | Reading cost, and SPI code that 2027 removes | Small |
| F22 | Turret IO | Hood conversion factor set on the wrong encoder | None today; a trap later | One line |
| F23 | Turret IO | Turret encoder seeded at boot | Wrong aim if the turret is bumped while off | Procedure |
| F24 | Intake IO | Horizontal rollers saturate their feedforward | None today; the config lies | Small |

## Turret

### F1: `PAUSEDPASSING` can never happen

**Where:** [Turret.java, the PAUSED case](repo:src/main/java/frc/robot/subsystems/turret/Turret.java#L179-L185)

Both branches of `case PAUSED` yield `PAUSEDSHOOTING`, so the `PAUSEDPASSING` branch in `applyState` is
dead code. Paused outside our alliance zone, the turret aims at the HUB instead of the passing target.

Introduced by commit `65727f9`, "Add pause," on 2026-03-13, in the same commit that added both states.

**Fix:** the second branch yields `PAUSEDPASSING`. Covered by [u08-turret](ex:u08-turret).

### F3: `flywheelVolts` is neither

**Where:** [TurretIOSpark.updateInputs](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java#L134)

`inputs.flywheelVolts = turret.getAppliedOutput()` reads the **turret** motor, and `getAppliedOutput`
returns a duty cycle, not volts. Anyone diagnosing a weak shot reads a number describing a different
mechanism.

**Fix:** `flywheel.getAppliedOutput() * flywheel.getBusVoltage()`, matching what `ModuleIOSpark` does.

### F4: the passing rebuild drops the hood offset

**Where:** [Turret.redoPassingFunction](repo:src/main/java/frc/robot/subsystems/turret/Turret.java#L384-L391)

Three of the four rebuilt entries omit `+ TurretConstants.hoodOffset`, which the constructor includes.
The hood clamp limits the damage to 0.011 rotations, about 4 degrees, on every pass after an alliance
change.

**Fix:** add the offset, then have the constructor and the rebuild call one shared builder.

### F15: the HUB is 3.5 cm off

**Where:** [TurretConstants](repo:src/main/java/frc/robot/subsystems/turret/TurretConstants.java#L16)

The HUB is hard-coded at (4.625, 4.0). Derived from the tag layout, its center is about (4.626, 4.035).

**Fix:** compute it from `VisionConstants.kTagLayout` so it tracks the layout.

### F22: the hood conversion factor is on the wrong encoder

**Where:** [TurretIOSpark hood configuration](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java#L89)

`hoodConfig.encoder.positionConversionFactor(1.0/36.0)` configures the **relative** encoder while the
closed loop uses the **absolute** encoder. The line has no effect today and will mislead the next
person who changes hood gearing.

**Fix:** delete it, or move it to `absoluteEncoder` if a conversion is actually wanted.

### F23: the turret's zero is assumed at boot

**Where:** [TurretIOSpark constructor](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java#L115)

`turret.getExternalEncoder().setPosition(TurretConstants.turretStartingAngle)` seeds a relative encoder,
so the turret must physically be at its idle position when the robot powers on.

**Fix, short term:** the pit checklist item. **Long term:** a homing routine, or an absolute encoder.

## Superstructure

### F2: the current averaging never averages

**Where:** [Superstructure.periodic](repo:src/main/java/frc/robot/subsystems/Superstructure.java#L99-L121)

The block compares two timestamps taken one loop apart and waits for a gap greater than one second, so
`totalTimestamps` stays 0 and nothing is ever logged. `toggleCurrentLogging()` is also never bound to
anything.

**Fix:** delete the block, or replace it with a windowed averager. Covered by
[u15-averager](ex:u15-averager).

### F19: `hubActive` goes stale

**Where:** [Superstructure.updateHubStatusAndPeriod](repo:src/main/java/frc/robot/subsystems/Superstructure.java#L242-L258)

The method returns the correct value, and the logged output is right. The **field** `hubActive`, which
`getRumble()` reads, is only assigned inside the teleop ladder, so it keeps its last teleop value while
disabled or in autonomous.

**Fix:** assign on every path, or better, return a record and stop keeping a field.

## Vision

### F13: overconfident fusion

**Where:** [AprilTagVision.periodic](repo:src/main/java/frc/robot/subsystems/vision/AprilTagVision.java#L120-L215)

Four related problems: camera 1's standard-deviation baselines are used for every camera, camera 0's
offsets are applied to every observation, the fused timestamp is a weighted blend across cameras, and
the weights are 1/σ where the statistics call for 1/σ². Two equal cameras produce a fused σ/2 rather
than σ/√2, roughly 40% more confidence than the data supports.

**Fix:** per-camera baselines and offsets, inverse-variance weights, and either per-camera measurements
or a real per-camera estimator. Covered by [u13-fusion](ex:u13-fusion).

### F14: a disconnected camera keeps reporting

**Where:** [AprilTagIOPhotonVision.updateInputs](repo:src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java#L39-L41)

Everything is inside `if (inputs.isConnected)`, with no `else`, so a disconnected camera leaves the
previous loop's `hasTarget` and `poseObservations` in place. The same stale measurement is then fed to
the estimator every loop. Separately, `inputs.targetInfo` stays null until the first target is seen, so
`getTargetInfo` would throw.

**Fix:** an `else` that clears the arrays and flags, and an empty-array initializer in the inputs class.

## Drivetrain and autonomous

### F8: persisting configuration at every mode change

**Where:** [ModuleIOSpark.setCurrentLimit](repo:src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java#L224-L228)

Called four times at each of `autonomousInit` and `teleopInit`, each call blocking on CAN and writing
flash. Also, `ppConfig` describes a 30 A limit while autonomous runs at 80 A.

**Fix:** `PersistMode.kNoPersistParameters`, and decide which current limit PathPlanner's model should
describe.

### F9: two sets of drivetrain numbers

**Where:** [pathplanner/settings.json](repo:src/main/deploy/pathplanner/settings.json) versus
[DriveConstants](repo:src/main/java/frc/robot/subsystems/drive/DriveConstants.java#L117-L128)

The app stores gearing 5.143, track width 0.546, module offsets ±0.273, and an 80 A limit; the code
uses 5.9, 0.508, ±0.254, and 30 A. The robot follows the code, so the app previews a robot we do not
have.

**Fix:** update the app's settings tab to match, and re-check after any drivetrain change.

### F10: deprecated scheduling

**Where:** [Robot.autonomousInit](repo:src/main/java/frc/robot/Robot.java#L132)

`autonomousCommand.schedule()` is deprecated in WPILib 2026.

**Fix:** `CommandScheduler.getInstance().schedule(autonomousCommand)`.

### F11: the pathfinder is installed twice

**Where:** [Robot](repo:src/main/java/frc/robot/Robot.java#L93) and
[Drive](repo:src/main/java/frc/robot/subsystems/drive/Drive.java#L106)

Harmless, and a reader has to check whether the two calls differ.

**Fix:** keep the one in `Drive`, where the rest of the PathPlanner wiring lives.

### F17: "Move Forward" does neither

**Where:** [RobotContainer](repo:src/main/java/frc/robot/RobotContainer.java#L161-L162)

`joystickDrive(drive, () -> 0.0, () -> -Math.sqrt(1/4.2), () -> 0.0)` puts its value on the **y**
supplier, so the robot strafes along the field's y axis rather than driving away from the wall, and the
deadband rescale makes it 0.78 m/s instead of the intended 1.0. Being a `Commands.run`, it never ends.

**Fix:** a robot-relative command with a timeout, as shown in
[Joystick Driving](course:07-command-based/joystick-drive).

### F18: the facing-target command does not flip

**Where:** [DriveCommands.joystickDriveFacingTarget](repo:src/main/java/frc/robot/commands/DriveCommands.java#L141)

It converts field-relative speeds without the red-alliance flip that `joystickDrive` applies, so its
translation controls would be reversed on red. Nothing binds it today.

**Fix:** apply the same flip, or delete the command until it is needed.

## Intake

### F16: an output inside `updateInputs`

**Where:** [IntakeIOSpark.updateInputs](repo:src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java#L165)

The method ends by sending the arm's setpoint to its controller. Reading and acting are mixed, replay
runs the same method, every arm decision is one loop late, and because `armSetAngle` initializes to the
deployed position, the first command after boot deploys the arm.

**Fix:** move the call into `setIntakeArmAngle`.

### F24: the horizontal rollers run open loop

**Where:** [IntakeIOSpark horizontal configuration](repo:src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java#L118-L139)

Both horizontal rollers are configured with kP and kD of 0 and a feedforward kV of 1.167. With velocity
in RPM, that reaches 12 V at about 10 RPM, so any commanded speed saturates and the rollers run at full
output. The robot behaves as the team expects, but not for the reason the configuration states.

**Fix:** measure first. If a real velocity loop is wanted, convert the constant into volts per RPM or
add a velocity conversion factor.

## Simulation

### F5: inverted comparisons

**Where:** [TurretIOSim.updateInputs](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSim.java#L33-L34)

`intakeSafe` uses `>` where the real IO uses `<`, and `resetting` compares against π instead of 0.1 rad.
The intake-safe interlock therefore behaves backwards in simulation.

**Fix:** share one comparison between both implementations.

### F6: the simulated mechanisms produce no data

**Where:** [TurretIOSim](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSim.java#L41-L43) and
[IntakeIOSim](repo:src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java#L24-L36)

The simulated flywheel is never given voltage, so `shooterSpedUp()` is never true and the feeder never
runs. The simulated intake never fills currents or `armSetAngle`, so jam detection and recovery cannot
happen.

**Fix:** drive the sim models from the setpoints, and fill the inputs the logic reads.

### F7: sim IO extends hardware IO

**Where:** [TurretIOSim](repo:src/main/java/frc/robot/subsystems/turret/TurretIOSim.java#L13) and
[IntakeIOSim](repo:src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java#L14)

Both call `super()`, constructing real `SparkFlex` and `SparkMax` objects for hardware that is not
there, and inheriting any behavior they did not override.

**Fix:** implement the IO interfaces directly.

## Repository

### F12: every log names the wrong commit

**Where:** [BuildConstants.java](repo:src/main/java/frc/robot/BuildConstants.java) and
[build.gradle](repo:build.gradle)

The file is committed and never regenerated, because the build does not apply the `gversion` plugin. It
reports a November 2025 build of a project named `967_AdvantageKitTemplate_2025`, with `DIRTY = 1`.

**Fix:** add the plugin, generate the file into a build folder, and git-ignore it.

### F20: dead code

**Where:** [LimitSwitchManager](repo:src/main/java/frc/robot/util/LimitSwitchManager.java),
[Elastic](repo:src/main/java/frc/robot/util/Elastic.java),
[DriveConstants.pathConstraints](repo:src/main/java/frc/robot/subsystems/drive/DriveConstants.java#L130-L133)

`LimitSwitchManager` is unused and uses SPI, which the 2027 control system removes. The Elastic
notification helper is unused. `pathConstraints` is unused. Several passing-related constants in
`TurretConstants` describe a design that commit `4dbac3d` removed. Commented-out bindings sit in
`RobotContainer`.

**Fix:** delete, except the commented shot map tables, which are dated calibration history worth
keeping.

## Suggested order

Value against risk, for a team with off-season time:

:::steps
1. **F14**, then **F1**: small, contained, and each removes a real behavior bug.
2. **F12**: makes every future log identifiable, and costs nothing.
3. **F13**: the biggest behavior improvement, and worth simulation and practice-field verification.
4. **F16**, **F8**, **F4**: small fixes with clear before-and-after checks.
5. **F5**, **F6**, **F7**: simulation fidelity, which makes everything after it easier to test.
6. **F2**, **F19**, **F20**, **F22**, **F10**, **F11**: cleanup, best done in one or two tidy branches.
7. **F17**, **F18**, **F24**, **F15**, **F23**: decide per item whether to fix, delete, or document.
:::

[The capstone projects](course:16-season/capstones) group several of these into scoped pieces of work
with definitions of done.
