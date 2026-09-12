---
summary: Install everything a 967 programmer needs for the 2026 season and confirm that it works before you touch the team code.
objectives:
  - Install the WPILib 2026 suite and explain what each included tool is for
  - Install the Driver Station, REV Hardware Client, Git, and PathPlanner
  - Prove the toolchain works by building a new command-based project
---

## What you are installing, and why

FRC software is a toolchain: several programs that work together. Here is everything the 2026 robot's code needs.

| Tool | What it does for us | Platforms |
|---|---|---|
| **WPILib 2026 installer** | WPILib VS Code, Java 17, GradleRIO, and the offline libraries needed to build robot code | Windows, macOS, Linux |
| AdvantageScope *(in WPILib)* | Graphs and 3D field views of logs from our AdvantageKit code | Windows, macOS, Linux |
| Elastic *(in WPILib)* | The driver dashboard; our layout lives in `elastic-layout.json` | Windows, macOS, Linux |
| SysId, Glass, Data Log Tool *(in WPILib)* | Characterizing mechanisms, viewing NetworkTables, and downloading logs | Windows, macOS, Linux |
| **FRC Game Tools** | The Driver Station (enables the robot) and the roboRIO Imaging Tool | Windows only |
| **REV Hardware Client** | Sets SPARK MAX/Flex CAN IDs, updates firmware, and spins motors for testing | Windows (check REV's site for others) |
| **Git** | Version control for team code | Windows, macOS, Linux |
| **PathPlanner** | Draws the autonomous paths in `src/main/deploy/pathplanner` | Windows, macOS, Linux |

You do **not** need CTRE's Phoenix Tuner for the 2026 robot, because all our motor controllers are REV. Future robots may differ, so check the `vendordeps` folder of whatever project you are working on.

:::info System requirements for WPILib 2026
64-bit Windows 10 or 11, Ubuntu 22.04 or 24.04, or macOS 13.3 or newer (Intel or Apple silicon). The 2026 installer warns on Windows 10 because Windows 10 support is ending. Plan for several gigabytes of free disk space.
:::

:::warning School-managed laptops
Many school laptops block installers. Ask a mentor before you spend an hour fighting permissions. The team may have laptops set up already, or can work with IT.
:::

## Install WPILib 2026

:::steps
1. **Download the installer** from the WPILib releases page on GitHub (`github.com/wpilibsuite/allwpilib/releases`). Pick the newest **2026.x** release for your operating system. Our `build.gradle` uses GradleRIO **2026.2.1**, so any 2026.2.1 or newer 2026 release works.
2. **Open the download.** On Windows, right-click the `.iso` file and choose **Mount**, then run `WPILibInstaller.exe`. On macOS, open the `.dmg` and run the installer.
3. **Choose "Everything"** and install for your user unless a mentor says otherwise. The installer includes its own VS Code, so it will not change the VS Code you may already have.
4. **Wait.** The installer copies Java, Gradle, and the offline library repository. That is what lets you build at competitions with no internet.
5. **Open "2026 WPILib VS Code"** from the desktop shortcut or Start menu. Do not use a regular VS Code install for robot code.
:::

## Install the other tools

- **FRC Game Tools (Windows):** download from NI's FRC Game Tools page and run the installer. It may need a reboot. You get the **FRC Driver Station** and the **roboRIO Imaging Tool**.
- **REV Hardware Client:** download it from REV Robotics' software page. You will use it with a mentor when setting CAN IDs or updating SPARK firmware.
- **Git:** on Windows, install Git for Windows from `git-scm.com` and keep the defaults. On macOS, run `git --version` in Terminal. If Git is missing, macOS offers to install the command line tools.
- **PathPlanner:** install the PathPlanner app from the Microsoft Store (Windows) or its GitHub releases page. Use a 2026 version that matches our `PathplannerLib-2026.1.2.json` vendordep.
- **A GitHub account:** create one at `github.com` and give your username to a mentor so you can be added to the FRC-IronLions-967 organization.

:::note No Windows laptop?
The Driver Station is Windows-only. On macOS or Linux you can still write code, run the simulator (which has its own driver station window), open logs in AdvantageScope, and run unit tests. To drive the real robot, use a Windows laptop.
:::

## Prove it works

Before you open team code, make sure the tools can build a brand-new project. Then, when something breaks later, you will know it is the project and not your install.

:::steps
1. In WPILib VS Code, press **Ctrl + Shift + P** (**Cmd + Shift + P** on macOS) to open the Command Palette, type `WPILib: Create a new project`, and press Enter.
2. Choose **Template → Java → Command Robot**, pick a folder like `Documents/frc-practice`, name the project `HelloRobot`, and enter team number **967**.
3. Click **Generate Project**, then open it in the current window when asked.
4. Open the Command Palette again and run **WPILib: Build Robot Code**.
5. Watch the terminal. The first build can take a minute. It should finish with **BUILD SUCCESSFUL**.
:::

If the build fails, read the first error line. The most common causes are:

| Error mentions | Likely cause | Fix |
|---|---|---|
| `Could not resolve ... GradleRIO` | Project year doesn't match the WPILib you installed | Create the project with WPILib 2026 VS Code |
| `JAVA_HOME` or `Unsupported class file major version` | A different Java version is being used | Use the WPILib VS Code shortcut, which points to WPILib's Java 17 |
| Timeouts downloading | First build of a project with online-only vendor libraries | Connect to the internet and retry (not needed for this template) |

:::team Before your first event
A fresh project builds offline, but Rebuilt-2026 pulls REVLib, AdvantageKit, PathPlanner, PhotonLib, and Studica from the internet the first time. **Build the team code at least once on every laptop while you have internet**, so Gradle caches those libraries. Arenas often have no usable Wi-Fi. You will do that in [Get the Team Code and Build It](course:01-tools/get-the-team-code).
:::

:::quiz
? Which program enables and disables the real robot?
- WPILib VS Code
+ The FRC Driver Station, installed with FRC Game Tools
- AdvantageScope
- REV Hardware Client
> The Driver Station is the only way to enable a real robot. It is Windows-only and ships with FRC Game Tools.

?tf You should build robot code with the "2026 WPILib VS Code" shortcut instead of a separately installed VS Code.
= true
> The WPILib shortcut is set up with the right Java version and WPILib extension, so builds behave the same on every team laptop.

? Why build Rebuilt-2026 on every laptop while connected to the internet before an event?
- The roboRIO needs internet to receive code
+ Gradle downloads and caches vendor libraries like REVLib and AdvantageKit the first time, and arenas often lack internet
- Git refuses to work offline
- The Driver Station checks for updates before every match
> WPILib's own libraries are installed offline, but vendor libraries are downloaded on first build and cached for later.

? What does the REV Hardware Client let you do?
- Draw autonomous paths
- Replay AdvantageKit logs
+ Set SPARK CAN IDs, update firmware, and test motors
- Edit the Elastic dashboard layout
> Every SPARK must have the CAN ID our code expects. The REV Hardware Client is how those IDs get set.
:::
