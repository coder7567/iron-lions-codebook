---
summary: 2027 replaces the roboRIO, renames every WPILib package, and retires the dashboards we use. What that means for this codebase, why the IO layer makes it survivable, and what to do before it lands.
objectives:
  - Name the major 2027 control system and library changes
  - Predict which files in our repository a port would touch
  - Explain why the IO pattern contains most of the damage
  - Choose off-season work that pays off either way
files:
  - src/main/java/frc/robot/subsystems/drive/GyroIONavX.java
  - src/main/java/frc/robot/util/LimitSwitchManager.java
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
---

:::warning Everything here is subject to change
As of this writing the 2027 system is in alpha. Names, packages, and features move during an alpha,
and some of what follows will be wrong by kickoff. Treat it as a map of what kind of change is coming,
not as a specification, and check the current WPILib documentation before doing any of it.
:::

## What changes

| Area | 2026 | 2027 |
|---|---|---|
| Controller | roboRIO 2 | **SystemCore**: multiple CAN buses, Smart IO, an onboard IMU |
| Removed hardware interfaces | SPI, relays, analog out, interrupts and counters | Gone; several IMU models with them |
| Java | 17 | **25** |
| Packages | `edu.wpi.first.*` | **`org.wpilib.*`** |
| Commands | v2 | **v3** (`org.wpilib.commands3`) alongside v2, plus an OpMode framework |
| Dashboards | SmartDashboard, Shuffleboard | **Removed**; Telemetry and Tunables replace them, with `Selectable` for choosers |
| Path tools | PathWeaver (deprecated) | Removed; PathPlanner and Choreo continue |

The 2026 season already carries warnings pointing at this: `Command.schedule()` is deprecated in favor
of scheduling through the `CommandScheduler`, REVLib's `setReference` gives way to `setSetpoint`, and
SmartDashboard and Shuffleboard are deprecated for removal. Our code uses the deprecated
`Command.schedule()` in `Robot.autonomousInit` (finding F10), which is a one-line fix today and a
compile error later.

## What a port would touch in our repository

| File or area | Impact | Why |
|---|---|---|
| Every file's imports | **Mechanical** | The package rename is a project-wide find and replace |
| `GyroIONavX` | **Rewrite** | The NavX is on MXP SPI, and SPI does not exist on SystemCore; the onboard IMU or a CAN IMU takes its place |
| `LimitSwitchManager` | **Delete** | It is unused SPI code already (finding F20) |
| `ModuleIOSpark`, `TurretIOSpark`, `IntakeIOSpark` | **Update** | A new REVLib version, likely with CAN bus selection |
| `AprilTagIOPhotonVision` | **Update** | A new PhotonLib for the new coprocessor story |
| `Drive`'s PathPlanner wiring | **Update** | A new PathPlannerLib |
| Logging, choosers, tunables | **Update** | AdvantageKit's 2027 release, and `Selectable` instead of `SendableChooser` |
| `Superstructure`, `Turret`, `Intake` state machines | **Nothing** | They speak only in enums, records, and IO interfaces |
| Kinematics, odometry, shot maps, aiming math | **Nothing but imports** | Pure logic and WPILib math, which is being renamed rather than redesigned |

That table is the argument for the IO pattern, stated in hours of work. The subsystem that contains
every REV-specific call is one file per mechanism, and the logic that took the whole season to get
right does not move.

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="/** Updates the set of loggable inputs. */" lines=16

Six methods and an inputs class. Whatever replaces a SPARK MAX in 2027, that interface is still what
`Module` calls.

## What to do before it lands

Off-season work that pays off whether or not the port goes smoothly:

:::steps
1. **Delete the dead code.** `LimitSwitchManager` is SPI-based and unused; the Elastic notification
   helper is unused. Both are pure cost in a port.
2. **Fix the deprecated calls now.** `Command.schedule()` and REVLib's `setReference` have modern
   replacements in 2026, and fixing them is easier than fixing them under a compile error.
3. **Write the tests.** Pure-logic tests survive a package rename untouched, and they are how you will
   know the port did not change behavior.
4. **Fix the build metadata** (finding F12), so 2026 logs stay identifiable after the repository moves
   on.
5. **Tighten the IO boundary.** The Driver Station calls scattered through `Superstructure` and
   `Turret` are the places a port will hurt; routing them through an IO layer helps replay today and
   the port later.
6. **Do not start the port until the release is at least in beta.** Porting against an alpha means
   doing it twice.
:::

:::team What this course is worth in 2027
Almost all of it transfers. The game changes, the hardware changes, and the package names change, but
state machines, kinematics, odometry, control, vision math, logging, and the habits in Units 15 and 16
are the same the year after and the year after that.

The fastest way to be useful on the 2027 robot is to understand the 2026 one completely, which is the
bet this whole course makes.
:::

:::quiz
? Which of our files would need a genuine rewrite for 2027, rather than an update?
+ `GyroIONavX`, because SystemCore has no SPI port for the NavX
- `Superstructure`
- `Turret`
- `Drive`
> The IMU story changes; the state machines do not.

? Why do the state machine classes need almost no work in a port?
+ They speak only in enums, records, and IO interfaces, none of which are vendor-specific
- They are small
- They have tests
- WPILib guarantees compatibility for subsystems
> That containment is what the IO pattern buys.

? What does the 2026 deprecation of `Command.schedule()` mean for our `Robot.autonomousInit`?
+ It is a one-line fix today and a compile error later
- Nothing; deprecated methods keep working forever
- Autonomous will not run in 2026
- The scheduler ignores the call
> Finding F10, worth clearing before it becomes urgent.

? Why write tests before a port rather than after?
+ Pure-logic tests survive the rename and are how you prove the port changed no behavior
- Tests cannot be written after a port
- The new framework generates them
- It is required by WPILib
> A port is a refactor, and refactors need a characterization net.

? Why wait for at least a beta before starting the port?
+ Names and APIs move during an alpha, so porting early means doing it twice
- Alphas cannot be downloaded
- The rules forbid it
- Gradle will not build against an alpha
> Map the work now; do the work when the target stops moving.

?? Which off-season tasks help regardless of how the port goes? Select all that apply.
+ Deleting dead code
+ Fixing deprecated API calls
+ Writing tests for pure logic
+ Routing Driver Station reads through an IO layer
- Rewriting the state machines for Commands v3
> The last one is work you cannot evaluate until the framework is stable.
:::
