# Iron Lions Codebook — build plan & conventions

Internal working document for building/maintaining the course. Students never need this file.

## Deliverables (all live OUTSIDE the team repo — never commit/push to FRC-IronLions-967/Rebuilt-2026)

```
C:\Rebuilt_2026\iron-lions-codebook\
  README.md              how to open the site, run exercises, rebuild
  PLAN.md                this file
  course\course.json     curriculum manifest (units → lessons, order, minutes)
  course\units\<unit>\<lesson>.md   lesson sources (Markdown + directives)
  course\units\<unit>\unit-test.md  unit test (exam) per unit
  course\reference\*.md  reference pages
  course\diagrams\*.svg  inline SVG diagrams (use currentColor + CSS vars)
  tools\build.mjs        node tools/build.mjs  → regenerates site\
  site\                  built static site; open site\index.html (works from file://)
  site\artifact.html     body-fragment variant for publishing as a claude.ai Artifact
  exercises\             GradleRIO 2026 project: starters (src/main), JUnit tests (src/test),
                         reference solutions (solutions/java); ./gradlew test -Psolutions
```

## Source facts (pin everything to these)

- Team repo: https://github.com/FRC-IronLions-967/Rebuilt-2026 @ main `fe04405398eec6f862f07de3f670d015f8e9f7e5`
  (117 commits, 2026-01-11 → 2026-09-10; tag `Iowa-Regional`; branches Intake-Dev, Turret-Dev,
  Only-hood/shooter-testing, pose-estimation-dev-bot, testing(no-superstructure), Use-6328-field-constants (PR #4)).
- repopack-output.txt == checked-out source (verified 114 files, 0 diffs).
- Stack: Java 17, GradleRIO 2026.2.1, Gradle wrapper 8.11, AdvantageKit 26.0.2, PathplannerLib 2026.1.2,
  photonlib v2026.3.2, REVLib 2026.0.5, Studica (NavX) 2026.0.0, URCL 2026.0.0, WPILib New Commands.
- Derived from AdvantageKit Spark swerve template (6328); BuildConstants still says 967_AdvantageKitTemplate_2025.
- Team: FRC 967 Iron Lions, Linn-Mar High School, Marion, Iowa. Rookie 2002 (est. 2001). 25 seasons,
  9 Championship appearances, 27 awards (frc-events). Regional wins 2004, 2011, 2013, 2019 (Iowa).
  Sister FTC teams 4150 Dark Matter, 4324 Lost in Time, 10107 In Theory. Site lmrobotics.org. School colors red & black.
  Subteams: CAD/Design, Assembly/Hardware, Software/Programming, Outreach/Awards, Strategy.
- 2026 season: Minnesota Bluff Country Regional (Mar 4–7): rank 20/41, 6-5, Alliance 8, out in playoff round 2.
  Iowa Regional (Mar 25–28): rank 12/53, 5-3, Alliance 7 captain, out round 2, **Industrial Design Award**.
- Upcoming: Clash in the Corn offseason Oct 2 2026; FRC Kickoff Jan 9 2027 (2027 = Systemcore season).
- Org repos worth citing: 967_SDS_Swerve_Testbed (2023), Crescendo-2024, Reefscape_2025,
  967_AdvantageKitTemplate_2025, 2025-Offseason-L1-Bot, Rebuilt-YASS (2026 offseason, YAGSL experiment), Rebuilt-2026.

### REBUILT (2026) facts used by code
- AUTO 20 s. TELEOP 2:20 = Transition 10 s (2:20–2:10), Shift 1 2:10–1:45, Shift 2 1:45–1:20, Shift 3 1:20–0:55,
  Shift 4 0:55–0:30, End Game 0:30–0:00. Both HUBs active in AUTO, Transition, End Game.
- Game data: 'R' or 'B' ≈3 s after AUTO ends = alliance whose HUB is INACTIVE in Shift 1 (the alliance that scored
  more FUEL in AUTO). Inactive-first alliance is active in Shifts 2 & 4; other alliance active in 1 & 3. Empty string before.
  FMS sends only approximate match time. Practice mode should be set 20 s auto / 110 s teleop / 30 s endgame.
- Field 16.541 m × 8.069 m (651.2 in × 317.7 in), rotationally symmetric (tags 13↔29, 15↔31).
  kDefaultField = k2026RebuiltWelded (also k2026RebuiltAndymark). 32 tags, 36h11, 8.125 in.
- Alliance zone 158.6 in (4.03 m) deep; neutral zone 283 in (7.19 m). HUB 47×47 in (1.19 m), near face 4.03 m from
  alliance wall → blue HUB center ≈ (4.626, 4.035) from tags 18–21/24–27; red HUB ≈ (11.915, 4.035) tags 2–5/8–11.
  HUB opening front edge 72 in (1.83 m) high, 41.7 in hexagonal opening. FUEL 5.91 in (15.0 cm) foam ball, 0.448–0.500 lb.
- TRENCH clearance under arm 50.34 in wide × 22.25 in tall. BUMP 6.513 in tall, 15° ramps. TOWER rungs 27/45/63 in.
  DEPOT 42×27 in with 1 in tall barriers. OUTPOST chute (human player).
- Tower points (verify in current manual before quoting): AUTO L1 15; TELEOP L1 10, L2 20, L3 30. FUEL in active HUB scores.

### 2026 → 2027 library notes
- WPILib 2026: `Command.schedule()` deprecated → `CommandScheduler.getInstance().schedule(cmd)`; Shuffleboard &
  SmartDashboard deprecated (removed 2027); PathWeaver deprecated; `Subsystem.idle()`; `MathUtil.copyDirectionPow`,
  2D applyDeadband/copySignPow; Java compiler plugin for common errors; 2026 AprilTag maps.
- REVLib 2026: `setReference` deprecated for removal → `SparkClosedLoopController.setSetpoint(...)`; top-level
  `com.revrobotics.ResetMode`/`PersistMode`; `closedLoop.feedForward.kS/kV/...`.
- PhotonLib 2026: `new PhotonPoseEstimator(layout, robotToCam)`; `estimateCoprocMultiTagPose(result)`,
  `estimateLowestAmbiguityPose(result)` etc.; `camera.getAllUnreadResults()`.
- PathPlanner: AutoBuilder.configure(poseSupplier, resetPose, robotRelativeSpeedsSupplier, output, controller, RobotConfig,
  shouldFlip, subsystem). Register NamedCommands BEFORE building autos/chooser. Paths flip for red; origin stays blue.
- 2027 (alpha 7 as of Sep 2026 — mark as subject to change): Systemcore replaces roboRIO (multiple CAN buses, Smart IO,
  onboard IMU; removes SPI, relays, analog out, interrupts/counters, several IMU models); Java 25; packages
  `edu.wpi.first.*` → `org.wpilib.*`; Commands v3 (`org.wpilib.commands3`) beside v2; OpMode framework; Telemetry &
  Tunables replace SmartDashboard/SendableChooser (`Selectable`); Shuffleboard/SmartDashboard/PathWeaver/RobotBuilder removed.

### Robot reference (from code)
CAN: 1 FL drive · 2 FL turn · 3 BL drive · 4 BL turn · 5 BR drive · 6 BR turn · 7 FR drive · 8 FR turn (all SparkMax;
NEO drive, NEO 550 turn) · 9 flywheel leader SparkFlex (inverted) · 10 flywheel follower (follow 9, inverted) · 11 hood
SparkFlex (abs encoder) · 12 turret SparkFlex (external 8192 CPR encoder, soft limits −4.261..1.6 rad, output ±0.5)
· 13 intake roller SparkFlex (inverted) · 14 intake arm SparkFlex (abs encoder, zeroOffset 0.31, output ±0.25) · 15 feeder
SparkFlex (inverted, ramp 0.05 s) · 16 horizontal 1 SparkMax · 17 horizontal 2 SparkMax (inverted) · 18 intake follower
SparkFlex (follow 13). NavX via MXP SPI. Cameras "April_Tag_1" (x −9.733 in, y −9.733 in, z 9.314 in, pitch −30°,
yaw −135°) and "April_Tag_2" (x −9.733, y +9.733, z 9.314, pitch −30°, yaw +135°).
Driver port 0: left stick translate (field-relative, flipped for red), right stick X rotate, RT SHOOTING, RB PAUSED,
LT IDLE, LB EJECTING, Start TESTING; rumble = shooting while hub inactive || intake jammed || turret target in deadzone.
Operator port 1: LB turret offset +0.05 rad, RB −0.05 rad. Named commands: "start" → SHOOTING, "reverse" → EJECTING.
Drive: 4.2 m/s max, 20×20 in module square, wheel radius 2 in, drive reduction 5.9, turn reduction 18.75,
odometry 100 Hz, current limit 30 A teleop / 80 A auto, mass 74.088 kg, MOI 6.883.

## Code audit findings (cite by ID in lessons; full write-ups in course/reference/code-audit.md)
F1 Turret PAUSED: both branches yield PAUSEDSHOOTING, PAUSEDPASSING unreachable (Turret.java ~179-185).
F2 Superstructure current averaging compares consecutive loop timestamps (>1 s never true); toggle never bound (~99-121).
F3 TurretIOSpark flywheelVolts reads turret motor's applied output (duty cycle, wrong motor) (~134).
F4 redoPassingFunction omits hoodOffset on 3 of 4 entries (effect masked by clamp) (~384-391).
F5 TurretIOSim intakeSafe uses `>` (real uses `<`) and resetting threshold π vs 0.1 (~33-34).
F6 Sim fidelity: flywheelSim never gets voltage → shooterSpedUp false in sim; IntakeIOSim never fills armSetAngle /
   currents → jam/unjam untestable in sim.
F7 IntakeIOSim/TurretIOSim extend the Spark IO classes (construct real Spark objects in sim).
F8 setCurrentLimit → driveSpark.configure(..., kPersistParameters) ×4 at every auto/teleop init (blocking + flash write);
   ppConfig assumes 30 A while auto runs 80 A.
F9 PathPlanner GUI settings drift from DriveConstants (gearing 5.143 vs 5.9, trackwidth 0.546 vs 0.508, module ±0.273
   vs ±0.254, current 80 vs 30). Code's ppConfig is what the robot uses.
F10 Robot.autonomousInit uses deprecated Command.schedule().
F11 Pathfinding.setPathfinder called in both Robot() and Drive().
F12 BuildConstants stale & committed; build.gradle lacks gversion → wrong GitSHA/BuildDate metadata in every log.
F13 AprilTagVision: camera-1 baselines and camera0 offsets used for all cameras; weights 1/σ not 1/σ² (overconfident
   fusion: two equal cams → σ/2 not σ/√2); one averaged timestamp across cameras.
F14 AprilTagIOPhotonVision keeps stale poseObservations/hasTarget when a camera disconnects; targetInfo null until
   first connection (getTargetInfo NPE risk).
F15 TurretConstants.hub Y = 4.0 vs tag-derived 4.035.
F16 IntakeIOSpark.updateInputs sends the arm setpoint (output inside input method).
F17 "Move Forward" auto: strafes along −Y (field frame, flipped for red), ≈0.78 m/s not 1.0 (deadband rescale), never ends.
F18 joystickDriveFacingTarget lacks the red-alliance flip joystickDrive has.
F19 updateHubStatusAndPeriod leaves hubActive stale outside teleop branches; relies on approximate match time.
F20 Dead code: LimitSwitchManager (SPI — gone on Systemcore), Elastic notifications unused, pathConstraints unused,
   unused imports, commented blocks.
F22 hoodConfig.encoder.positionConversionFactor(1/36) configures the relative encoder while feedback is absolute.
F23 Turret encoder seeded to −1.6 rad at boot → turret must be at its start position at power-on (pit checklist).
F24 Horizontal rollers (16/17) configured kP=kD=0 with feedForward kV=1.167 while velocity is in RPM →
   feedforward saturates above ~10 RPM, so they effectively run open-loop at full output. Likely a units
   mismatch (kV copied from volts per rotation/sec?); verify on the robot before changing.
Improvement ideas: button to re-zero heading; derive hub from tag layout; per-camera PhotonPoseEstimator; tests for
Superstructure hub logic.

## Architecture
- No runtime deps. Build (node ≥18): Markdown subset → HTML at build time; Java/JSON/Groovy/shell highlighting at
  build time; source excerpts pulled from ../Rebuilt-2026 at build time (fails loudly if an anchor is missing).
- Output SPA: hash routes `#/<unit-slug>/<lesson-slug>`; per-unit content files `assets/content/<unit>.js` loaded via
  <script> injection (file:// friendly); `assets/search.js` index; progress in localStorage (try/catch everywhere);
  export/import progress JSON. Artifact variant: optional "Ask a mentor" (claude.use("sample")) + downloads capability
  for progress export & exercise zip; both hidden when `use()` resolves null or window.claude is absent.

### Lesson front matter
```
---
title: Commands, Factories, and Composition
summary: One sentence for lists and search.
minutes: 35
objectives:
  - Build commands with Commands.* factories
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
---
```

### Directives
- Callouts `:::note|tip|warning|danger|team|info [Optional title]` … `:::` (body is Markdown; `team` = "In our code").
- `:::details Summary text` … `:::`
- Leaf: `::source file="src/main/java/frc/robot/Robot.java" from="public void robotPeriodic()" lines=15 title="…"`
  (or `to="text"`; `occurrence=2`; `highlight="3,5-7"` relative line numbers). Links to GitHub at pinned SHA.
- Leaf: `::diagram name="robot-loop" caption="…"` → course/diagrams/robot-loop.svg inline.
- `:::quiz [title]` … `:::` using the quiz mini-language below. `:::exam pass=80` for unit tests.
- `:::exercise id="u02-units"` … `:::` — body: task Markdown, then `---hint` blocks. Starter, test and solution code are
  embedded automatically from exercises/ by id → see catalog in course/exercises.json.
- Raw HTML blocks allowed (lines starting with `<`). Tables use GFM pipes.

### Quiz mini-language
```
? Single-choice question text (Markdown). Extra lines before options are part of the prompt (fences OK).
- wrong option
  ~ optional feedback shown if this option is picked
+ correct option
> explanation (Markdown, may span several > lines)

?? Multi-select question (one or more +)
?tf Statement text
= true
?text Short answer prompt
= answer | other accepted | /regex/i
?num Numeric prompt
= 0.0508 ± 0.0005 m
?order Put these in order (listed here in the CORRECT order; UI shuffles)
1. first
2. second
?code Predict the output (monospace short answer; whitespace-normalized exact match)
= expected output
```
Blank line separates questions.

### Exercises conventions
- Package `frc.training.<unit>` e.g. `frc.training.u02`. Starter = compiles, TODO bodies `throw new UnsupportedOperationException("TODO")`
  or return a safe default. Tests JUnit 5 only (no Mockito). Pure Java unless id ends in `-wpi` (uses only WPILib APIs
  attested by Rebuilt-2026 code or official docs).
- `course/exercises.json`: [{id, unit, title, level 1-3, starter:[paths], test, solution:[paths], wpilib:bool}]
- Verify here: javac + JUnit 5.8.2 launcher (offline) — starters compile & fail, solutions pass. WPILib ones: doc-checked only.

## Design plan — "Pit binder"
- Name: **Iron Lions Codebook**. Description: ground-up programming course for FRC 967 built on the 2026 REBUILT code.
- Color (light): ground #F4F3F2 graphite-warm paper · surface #FFFFFF · ink #1C1A1E · muted #5D5A61 · rule #DEDAD8 ·
  accent Lions red #B8142B (identity, primary action, active nav) · code ground (both themes) #17161A.
  Semantic: pass #1D7A4B, fail #B3261E w/ icon+label, warn #9A6200, info #2A5DB0. Alliance red/blue only inside field diagrams.
- Color (dark): ground #131215 · surface #1B1A1E · raised #242228 · ink #EDEAE7 · muted #A5A1A8 · rule #34313A ·
  accent #F0506A · pass #4CC38A · fail #FF7B72 · warn #E3B341 · info #7AA7FF.
- Type: display "Big Shoulders Display" 800 (unit numerals, H1, bumper-style "967"); body "Atkinson Hyperlegible Next";
  code "Atkinson Hyperlegible Mono". Fallbacks: condensed system sans / system-ui / ui-monospace. Scale 1.25.
- Layout: 3 panes — left rail units/lessons with status marks; 68ch reading column; right "On this page" ≥1280px.
  Home: thesis hero pairing the headline with a real annotated excerpt (Turret.considerChassisSpeeds), course map, how it
  works (lesson → check → exercise → unit test → capstone), season pacing (Clash in the Corn Oct 2, Kickoff Jan 9).
- Lesson meta row reads like telemetry: minutes · checks · exercises · files touched (chips → GitHub @ fe04405).

## Status checklist
- [x] build.mjs + app.js + app.css
- [x] exercises project + tests + solutions verified (53 exercises; all starters fail, all solutions pass)
- [x] U00 … U16 lessons + unit tests (110 lessons, 16 unit tests)
- [x] reference pages (9 + auto-generated exercise catalog)
- [x] build --strict clean (136 pages, 0 missing), browser check, README
- [ ] publish artifact (blocked by the permission classifier; site/ is complete and serves locally)
