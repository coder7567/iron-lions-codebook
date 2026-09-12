---
summary: Show that you can set up, build, simulate, and test 967 robot code on your own computer.
---

This unit test covers every Unit 1 lesson. Work through it without notes first. When you submit, each question shows an explanation, so a miss tells you exactly what to review.

:::exam Unit 1 test: tools and setup
? You are in `C:\Users\ada\Documents` and want to open the team repository folder. Which command is correct?
+ `cd Rebuilt-2026`
- `cd ..\Rebuilt-2026`
- `open Rebuilt-2026`
- `Rebuilt-2026.exe`
> `cd` changes folders, and the relative path `Rebuilt-2026` is correct from `Documents`.

? What does `..` mean in a path?
- The current folder
+ The folder one level above the current folder
- The root of the drive
- Every folder at once
> `.` is the current folder and `..` is its parent.

? In PowerShell, why must you type `.\gradlew build` instead of `gradlew build`?
+ PowerShell does not run programs from the current folder unless the path starts with `.\`
- `gradlew` only works with a backslash on Windows
- The dot tells Gradle to build in debug mode
- It is a shortcut for `git`
> This is a safety feature of the shell. On macOS and Linux the same idea is written `./gradlew`.

?? Which of these tools come with the WPILib 2026 installer? (Select all that apply.)
+ AdvantageScope
+ Elastic
+ SysId
- FRC Driver Station
- REV Hardware Client
> The Driver Station ships with FRC Game Tools, and the REV Hardware Client is a separate REV download.

? What is the REV Hardware Client used for on our robot?
- Drawing autonomous paths
+ Setting SPARK CAN IDs, updating firmware, and testing motors
- Deploying Java code
- Viewing AdvantageKit logs
> Every SPARK MAX and SPARK Flex must have the CAN ID the code expects, such as 12 for the turret.

?tf The FRC Driver Station runs on Windows, macOS, and Linux.
= false
> The Driver Station is Windows-only. Other operating systems can simulate and write code, but a Windows laptop is needed to drive the real robot.

? Why should every laptop build Rebuilt-2026 once with internet before an event?
- Deploying requires internet
+ Vendor libraries like REVLib and AdvantageKit download on the first build and are cached for offline use
- WPILib VS Code checks its license online
- Git cannot commit offline
> WPILib's own libraries install offline, but vendor libraries are fetched from the internet the first time.

? Which file stores the team number that deploys use?
- `build.gradle`
+ `.wpilib/wpilib_preferences.json`
- `settings.gradle`
- `src/main/deploy/elastic-layout.json`
> `wpilib_preferences.json` contains `"teamNumber": 967`, and GradleRIO reads it when deploying.

? What happens to files in `src/main/deploy/`?
+ They are copied to `/home/lvuser/deploy` on the roboRIO, where code like PathPlanner reads them
- They are compiled into Java classes
- They are ignored by Gradle
- They are uploaded to GitHub automatically
> Our PathPlanner autos and paths and the Elastic dashboard layout live there.

?text Which WPILib command palette command runs the robot program on your laptop? (Type the command name after "WPILib: ".)
= Simulate Robot Code | /simulate\s+robot\s+code/i
> **WPILib: Simulate Robot Code** starts the simulator, and the Sim GUI extension opens the Robot Simulation window.

? In simulation, the driver controller must be in which Joystick slot for our code to read it?
+ 0
- 1
- 3
- Any slot works
> `RobotContainer` creates `new CommandXboxController(0)`, so it reads slot 0. The operator controller reads slot 1.

? Our code knows it is running in simulation because:
- The Sim GUI sends a message to `RobotContainer`
+ `RobotBase.isReal()` returns false on a laptop, so `Constants.currentMode` becomes `SIM`
- Simulation code lives in a different repository
- `build.gradle` has a simulation flag set to true
> `Constants.currentMode = RobotBase.isReal() ? Mode.REAL : simMode`, and `simMode` is `SIM`.

? A JUnit failure says `expected: <967> but was: <0>`. What does it mean?
- The test expected 0 and your code returned 967
+ The test expected 967 and your code returned 0
- The code did not compile
- JUnit is misconfigured
> JUnit prints the expected value first and then the actual value your code produced.

? Which folder of the exercises project should you edit while solving exercises?
+ `src/main/java`
- `src/test/java`
- `solutions/java`
- `build`
> Starters are in `src/main/java`. Tests define correctness and should not be changed to make them pass.

?code In the exercises project, what exact Gradle task name runs the tests? (One word.)
= test
> `.\gradlew test` compiles your code and runs every JUnit test. Add `--tests` to filter.

? The editor shows red errors after you switched branches, but **WPILib: Build Robot Code** succeeds. What should you do?
- Delete the project and clone it again
+ Trust the build, and run **Java: Clean Java Language Server Workspace** to refresh the editor
- Commit the red errors so a mentor can see them
- Reinstall WPILib
> The build is the source of truth. The language server sometimes needs a refresh after big changes.
:::
