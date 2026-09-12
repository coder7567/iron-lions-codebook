---
summary: Where to look things up: official documentation, the libraries this robot uses, the team's own repositories, and the communities worth reading.
---

## Official documentation

| Resource | Use it for |
|---|---|
| [WPILib documentation](https://docs.wpilib.org) | The reference for everything: commands, geometry, control, simulation, and the yearly changes |
| [WPILib API javadocs](https://github.wpilib.org/allwpilib/docs/release/java/) | The exact signature of the method you are about to call |
| [FIRST Robotics Competition](https://www.firstinspires.org/robotics/frc) | Rules, the game manual, and the event schedule |
| [FRC Discord](https://discord.gg/frc) | Where WPILib and vendor developers answer questions |
| [Chief Delphi](https://www.chiefdelphi.com) | The community forum; search it before asking anywhere else |

The game manual is the authority on every rule this course mentions. When a rule and this course
disagree, the manual is right and this page is out of date.

## The libraries in our robot

| Library | Documentation | What we use it for |
|---|---|---|
| AdvantageKit | [docs.advantagekit.org](https://docs.advantagekit.org) | Logging, the IO pattern, and replay |
| AdvantageScope | [docs.advantagescope.org](https://docs.advantagescope.org) | Reading logs and watching the field |
| PathPlanner | [pathplanner.dev](https://pathplanner.dev) | Drawing paths, building autos, and following them |
| PhotonVision | [docs.photonvision.org](https://docs.photonvision.org) | AprilTag detection and pose estimation |
| REVLib | [docs.revrobotics.com](https://docs.revrobotics.com/revlib) | SPARK MAX and SPARK Flex configuration and control |
| Studica NavX | [pdocs.kauailabs.com](https://pdocs.kauailabs.com/navx-mxp/) | The gyro |
| URCL | [github.com/Mechanical-Advantage/URCL](https://github.com/Mechanical-Advantage/URCL) | Logging every REV device automatically |
| Elastic | [github.com/Gold872/elastic-dashboard](https://github.com/Gold872/elastic-dashboard) | The dashboard whose layout ships in our deploy folder |

## The team's repositories

Our organization is [FRC-IronLions-967](https://github.com/FRC-IronLions-967). The ones worth reading:

| Repository | Why |
|---|---|
| `Rebuilt-2026` | The robot this course is about |
| `967_AdvantageKitTemplate_2025` | The template this project started from, which is why `BuildConstants` still names it |
| `Reefscape_2025` | Last season's robot, for comparing how the code has changed |
| `Crescendo-2024` | Two seasons back |
| `967_SDS_Swerve_Testbed` | An early swerve testbed |
| `Rebuilt-YASS` | A 2026 off-season experiment with YAGSL |
| `2025-Offseason-L1-Bot` | An off-season robot |

Reading two seasons of the team's own code side by side teaches something no tutorial can: which ideas
the team keeps, and which ones get rewritten every year.

## Code worth reading from other teams

| Source | Why |
|---|---|
| [Mechanical Advantage, team 6328](https://github.com/Mechanical-Advantage) | The template our code derives from, and the origin of AdvantageKit; their code is the reference for the IO pattern |
| [Team 254](https://github.com/Team254) | Long-running, deeply engineered code with a very different style |
| [Team 1678](https://github.com/frc1678) | Another well-documented codebase with a superstructure-driven design |
| [Team 3015](https://github.com/frc3015) and other public repos | Skim several; conventions vary widely and seeing that is the point |

Read other teams' code the way this course reads ours: find the state machine, find the IO boundary,
and ask what they do that we do not.

## Learning Java

| Resource | Note |
|---|---|
| [Oracle's Java tutorials](https://docs.oracle.com/javase/tutorial/) | Thorough and dry; good as a reference |
| [Baeldung](https://www.baeldung.com) | Practical, example-first articles on specific features |
| This course's Units 2, 3, and 5 | Java taught with robot code as the examples |

## Tools

| Tool | Note |
|---|---|
| [WPILib VS Code](https://docs.wpilib.org/en/stable/docs/zero-to-robot/step-2/wpilib-setup.html) | The installer that brings the toolchain, the extension, and the simulator |
| [REV Hardware Client](https://docs.revrobotics.com/rev-hardware-client/) | CAN IDs, firmware, and quick motor tests |
| [Git](https://git-scm.com) and [GitHub Desktop](https://desktop.github.com) | Version control; the desktop client is fine while you learn the commands |

## Staying current

- **Read the WPILib release notes each January.** They are short, and they list exactly what changed.
- **Watch the Chief Delphi thread for the new control system**, especially through the 2027 transition.
- **Skim the vendor changelogs** when you update a vendordep; REVLib and PhotonLib both rename things
  between seasons.
- **Keep this course current.** When the robot code changes, the lessons that quote it will fail to
  build, which is the reminder to update them.
