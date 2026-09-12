---
summary: How packages organize our code into folders, how imports bring in classes from WPILib and vendor libraries, and how to read an import list to know where a class comes from.
objectives:
  - Match a package name to its folder and a class to its fully qualified name
  - Use regular imports, static imports, and fully qualified names
  - Identify which library a class comes from by its package prefix
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
---

## Packages are folders with names

Every Java class belongs to a **package**, declared on the first line of the file. The package name must match the folder path:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="package frc.robot.subsystems.drive;" lines=11

`package frc.robot.subsystems.drive;` means the file lives in `src/main/java/frc/robot/subsystems/drive/`. The class's **fully qualified name** is the package plus the class name: `frc.robot.subsystems.drive.Module`.

Packages keep large codebases organized, and they let two classes share a simple name. The course's exercise class `frc.training.u03.ShooterSetpoint` and the robot's `frc.robot.subsystems.turret.ShooterSetpoint` never collide, because their packages differ.

## Imports

To use a class from another package by its short name, **import** it:

```java
import edu.wpi.first.math.geometry.Rotation2d;   // now you can write Rotation2d
```

You do **not** need imports for:

- classes in the **same package**. `Module` uses `ModuleIO` without importing it, because both are in `frc.robot.subsystems.drive`.
- the `java.lang` package, which includes `String`, `Math`, `System`, `Integer`, and `Double`.

### Static imports

A **static import** brings in static members, so you can use them without the class name:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="import static edu.wpi.first.units.Units.*;" lines=4

Because of `import static frc.robot.subsystems.drive.DriveConstants.*;`, `Drive` can write `maxSpeedMetersPerSec` instead of `DriveConstants.maxSpeedMetersPerSec`. The trade-off is readability: a reader cannot tell at a glance where a bare name comes from. Use static imports for constants a file uses constantly, and write the class name everywhere else.

### Fully qualified names

You can skip the import and write the full name inline. `ModuleIOSpark` does this in places, even for classes it already imports:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="tryUntilOk(" lines=6

`ResetMode` and `PersistMode` are already imported at the top of that file, so the long `com.revrobotics...` prefixes add noise without adding meaning. Fully qualified names are only necessary when two imported classes share a simple name.

## Read the imports, know the library

An import list tells you which library each class comes from. Learn these prefixes and you will know where to look up any class in our code:

| Package prefix | Library | Documentation to search |
|---|---|---|
| `java.`, `javax.` | Java standard library | Oracle Java 17 API docs |
| `edu.wpi.first.` | WPILib | WPILib Java API docs |
| `com.revrobotics.` | REVLib | REVLib Java API docs |
| `org.littletonrobotics.junction.` | AdvantageKit | AdvantageKit docs |
| `org.littletonrobotics.urcl.` | URCL (REV logging) | URCL README |
| `com.pathplanner.lib.` | PathPlannerLib | PathPlanner docs |
| `org.photonvision.` | PhotonLib | PhotonVision docs |
| `com.studica.frc.` | Studica NavX library | Studica docs |
| `frc.robot.` | **Our code** | This repository |

:::info Coming in 2027
WPILib's 2027 release, built for the new Systemcore controller, renames its Java packages from `edu.wpi.first.*` to `org.wpilib.*`. The WPILib project importer is designed to convert them automatically, but you will see both styles in examples for a while. [Preparing for 2027](course:16-season/systemcore-2027) covers the migration.
:::

## Keep imports tidy

- VS Code shows **unused imports** in gray. Run **Organize Imports** (Shift + Alt + O) to remove them and sort the rest.
- Unused imports do not break anything, but they mislead readers. `DriveCommands` imports `AutoBuilder` and `InstantCommand` without using either, so a reader might hunt for autonomous code that is not there.
- Avoid wildcard imports like `import edu.wpi.first.math.geometry.*;` in regular code. Explicit imports document exactly what a file depends on. `AllianceFlipUtil` uses a wildcard for geometry, and it is readable only because that file uses nearly every geometry class.

:::quiz
?text What is the fully qualified name of the `Turret` class in `src/main/java/frc/robot/subsystems/turret/Turret.java`?
= frc.robot.subsystems.turret.Turret
> Package plus class name: `frc.robot.subsystems.turret` + `.Turret`.

? Why can `Module` use `ModuleIO` without an import statement?
- `ModuleIO` is in `java.lang`
+ Both classes are in the same package, `frc.robot.subsystems.drive`
- Interfaces never need imports
- VS Code adds the import invisibly
> Classes in the same package can refer to each other by simple name.

? A file contains `import static frc.robot.subsystems.drive.DriveConstants.*;`. What does that allow?
+ Using `DriveConstants` static members like `maxSpeedMetersPerSec` without writing `DriveConstants.`
- Creating `DriveConstants` objects
- Changing the constants at runtime
- Importing every class in the `drive` package
> Static imports bring in static fields and methods, not classes.

? You see `import com.revrobotics.spark.SparkFlex;`. Which library's documentation explains `SparkFlex`?
- WPILib
+ REVLib
- AdvantageKit
- PathPlannerLib
> The `com.revrobotics` prefix belongs to REV Robotics' library.

?tf Removing an unused import changes how the robot behaves.
= false
> Imports only let you write short names. Unused ones have no effect on the compiled program, but they mislead readers.
:::
