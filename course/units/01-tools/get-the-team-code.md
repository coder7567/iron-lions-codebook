---
summary: Clone Rebuilt-2026, open it correctly, understand every top-level file and folder, and run your first build of the real robot code.
objectives:
  - Clone the team repository with Git and open it in WPILib VS Code
  - Explain what each folder and build file in Rebuilt-2026 is for
  - Build the robot code and fix the most common first-build problems
files:
  - build.gradle
  - vendordeps/REVLib.json
  - src/main/java/frc/robot/Main.java
---

## Clone the repository

The team's code lives on GitHub at `FRC-IronLions-967/Rebuilt-2026`. **Cloning** downloads the code *and* its full history so you can see every change the team made this season.

Open a terminal in the folder where you keep projects, such as `Documents`, and run:

```bash
git clone https://github.com/FRC-IronLions-967/Rebuilt-2026.git
cd Rebuilt-2026
git log --oneline -5
```

The last command shows the five newest commits. If it prints commit messages, you have the code.

:::tip Prefer buttons?
In WPILib VS Code, open the Command Palette and run **Git: Clone**, paste the URL, and choose a folder. The result is identical. You will still learn the terminal commands in [Unit 4](course:04-git/commits-and-history), because they work the same on every computer.
:::

## Open it the right way

In WPILib VS Code, choose **File → Open Folder** and pick the `Rebuilt-2026` folder itself, the one that contains `build.gradle`. Choose **Yes, I trust the authors** when asked.

:::warning Open the project folder, not its parent
If you open `Documents` instead of `Rebuilt-2026`, WPILib commands will not find `build.gradle` and the Java tools will not understand the code. The Explorer's top item should read `REBUILT-2026`.
:::

The Java extension spends a minute importing the Gradle project. Wait for the spinner in the status bar to finish before judging any red squiggles.

## A tour of the repository

| Item | What it is |
|---|---|
| `build.gradle` | The build recipe: Java version, the GradleRIO plugin, libraries, and how to package and deploy the code |
| `settings.gradle` | Tells Gradle where WPILib's offline library repository is installed |
| `gradlew`, `gradlew.bat`, `gradle/wrapper/` | The Gradle Wrapper, which runs the exact Gradle version the project expects (8.11) |
| `vendordeps/` | One JSON file per third-party library |
| `.wpilib/wpilib_preferences.json` | Team number `967` and project year `2026` |
| `src/main/java/frc/robot/` | All robot Java code |
| `src/main/deploy/` | Files copied to the roboRIO: the Elastic dashboard layout and every PathPlanner path and auto |
| `elastic-layout.json` (top level) | An older copy of the dashboard layout; the one in `deploy/` is what the robot serves |
| `simgui-ds.json` | Simulator keyboard mappings for fake joysticks |

### The robot code packages

```text
src/main/java/frc/robot/
├── Main.java               starts the robot program (never edit)
├── Robot.java              mode changes and logging setup
├── RobotContainer.java     builds subsystems and binds controls
├── Constants.java          real, sim, or replay mode
├── BuildConstants.java     generated build info
├── commands/DriveCommands.java
├── subsystems/
│   ├── Superstructure.java coordinates turret and intake
│   ├── drive/              swerve drive, gyro, odometry thread
│   ├── intake/             intake arm, rollers, feeder
│   ├── turret/             turret, hood, flywheel, shot data
│   └── vision/             AprilTag cameras
└── util/                   alliance flipping, Spark helpers, pathfinding
```

### Vendor libraries

Each file in `vendordeps/` tells GradleRIO which library version to download:

| File | Library | Why we need it |
|---|---|---|
| `WPILibNewCommands.json` | WPILib command-based framework | Subsystems, commands, triggers |
| `REVLib.json` | REVLib 2026.0.5 | Controls every SPARK MAX and SPARK Flex |
| `AdvantageKit.json` | AdvantageKit 26.0.2 | Logging and deterministic replay |
| `URCL.json` | Unofficial REV-Compatible Logger | Records raw SPARK data into our logs |
| `PathplannerLib-2026.1.2.json` | PathPlannerLib 2026.1.2 | Follows autonomous paths |
| `photonlib.json` | PhotonLib v2026.3.2 | Reads the AprilTag cameras |
| `Studica.json`, `StudicaLib.json` | Studica 2026.0.0 | The NavX gyro |

## What the build file says

You do not need to write Gradle yet, but you should be able to read it. The dependencies block pulls in WPILib, every vendor library, and JUnit for tests:

::source file="build.gradle" from="dependencies {" lines=25

The last lines run AdvantageKit's annotation processor, which generates classes like `ModuleIOInputsAutoLogged` for you. [Unit 5](course:05-java-advanced/annotations-and-generated-code) explains how.

The `jar` block packages the robot program. It also copies the source code, vendordeps, and build file *into* the jar that gets deployed:

::source file="build.gradle" from="jar {" lines=8

:::team In our code
Because of those `backup` lines, every jar deployed to the robot contains a copy of the source that built it. If a laptop dies at an event, a mentor can recover the exact code from the roboRIO.
:::

## Your first build

With internet access, open a terminal in VS Code and run:

```powershell
.\gradlew build
```

(On macOS or Linux, use `./gradlew build`.) The first time, Gradle downloads the vendor libraries, which can take a few minutes. Later builds take seconds. The build:

1. compiles every `.java` file, stopping at the first compile errors,
2. runs the annotation processor to generate AdvantageKit classes,
3. runs any tests in `src/test/java` (this repo has none yet), and
4. packages the robot jar.

Success ends with **BUILD SUCCESSFUL**. The output goes into a `build/` folder that Git ignores.

### When the first build fails

| Message contains | Cause | Fix |
|---|---|---|
| `Could not resolve com.revrobotics...` or another vendor name | No internet on first build | Connect and rebuild. After one success, it works offline. |
| `Could not find ... GradleRIO 2026.2.1` | WPILib 2026 not installed, or an older version | Install WPILib 2026.2.1 or newer |
| `error: cannot find symbol ... AutoLogged` | Annotation processing did not run | Run `.\gradlew build` from the terminal instead of relying on editor errors |
| `Unsupported class file major version` | Wrong Java in use | Build from the WPILib VS Code terminal, which uses WPILib's Java 17 |

:::quiz
? Which folder do you choose in **File → Open Folder**?
- `Documents`
+ `Rebuilt-2026`, the folder that contains `build.gradle`
- `src/main/java`
- `.wpilib`
> WPILib and the Java tools look for `build.gradle` at the top of the folder you opened.

? What is in `src/main/deploy/`?
- The compiled robot jar
+ Files copied to the roboRIO, like the Elastic layout and PathPlanner paths and autos
- Vendor library downloads
- Git history
> GradleRIO copies everything in `src/main/deploy` to `/home/lvuser/deploy` on the roboRIO. Robot code reads PathPlanner files from there.

? Which vendor library lets our code control SPARK MAX and SPARK Flex motor controllers?
- PhotonLib
- AdvantageKit
+ REVLib
- Studica
> REVLib provides `SparkMax`, `SparkFlex`, and their configuration classes.

?tf Once Rebuilt-2026 has built successfully on a laptop, later builds on that laptop can work without internet.
= true
> Gradle caches the downloaded vendor libraries, and WPILib's own libraries come with the installer.
:::
