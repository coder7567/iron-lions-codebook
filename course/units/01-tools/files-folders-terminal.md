---
summary: The computer skills every robot programmer uses daily, including files, folders, paths, hidden files, and running commands in a terminal.
objectives:
  - Tell absolute paths from relative paths and navigate folders with cd
  - Run commands in PowerShell or a macOS/Linux terminal and stop them safely
  - Recognize the hidden folders a WPILib project depends on
---

## Files, folders, and extensions

A robot project is just a **folder of files**. Everything you will build, deploy, and test is a text file in a folder on your computer.

- A **file** holds data, like `Robot.java`.
- A **folder** (also called a *directory*) holds files and other folders.
- A file's **extension** tells programs what kind of file it is.

| Extension | What it is | Example in our repo |
|---|---|---|
| `.java` | Java source code | `src/main/java/frc/robot/Robot.java` |
| `.gradle` | Build instructions for Gradle | `build.gradle` |
| `.json` | Structured data | `vendordeps/REVLib.json`, PathPlanner `.path` files |
| `.md` | Markdown text, used for READMEs | `WPILib-License.md` |

:::tip Show file extensions
Windows hides extensions by default, so `Robot.java` might appear as just `Robot`. In File Explorer, open **View → Show → File name extensions**. On macOS, open Finder **Settings → Advanced** and check **Show all filename extensions**.
:::

## Paths: the address of a file

A **path** is the address of a file or folder.

An **absolute path** starts at the top of the drive:

```text
Windows:  C:\Users\ada\Documents\Rebuilt-2026\build.gradle
macOS:    /Users/ada/Documents/Rebuilt-2026/build.gradle
```

A **relative path** starts from wherever you are right now. If you are already inside `Rebuilt-2026`, the same file is just `build.gradle`, and the robot code is at `src/main/java/frc/robot/Robot.java`.

Two special names work everywhere:

- `.` means "this folder."
- `..` means "the folder above this one."

Java package names mirror folders. The class `frc.robot.subsystems.drive.Drive` lives in the folder `src/main/java/frc/robot/subsystems/drive/`. [Unit 3](course:03-java-objects/packages-and-imports) explains why.

## Hidden files and folders

Names starting with a dot, like `.git` or `.wpilib`, are **hidden** by default. They matter:

| Hidden item | Purpose |
|---|---|
| `.git/` | The entire version history. Never edit it by hand. |
| `.wpilib/wpilib_preferences.json` | Stores our team number, `967`, and the project year, `2026`. Deploys use it. |
| `.vscode/` | VS Code settings, including how to run WPILib unit tests. |
| `.gitignore` | Lists files Git should not track, like the `build/` folder. |

## The terminal

A **terminal** is a text window where you type commands for the computer to run. The program that reads your commands is the **shell**: **PowerShell** on Windows, **zsh** on macOS, and usually **bash** on Linux.

Programmers use terminals because commands are precise, repeatable, and easy to share. "Run `./gradlew build`" is a lot clearer than "click the third button in the menu."

In WPILib VS Code, open one with **Terminal → New Terminal**. It opens *already inside your project folder*, which saves typing.

### Commands you will use constantly

| Task | PowerShell (Windows) | macOS / Linux |
|---|---|---|
| Show where I am | `pwd` | `pwd` |
| List files here | `ls` | `ls` (add `-a` for hidden) |
| Go into a folder | `cd src` | `cd src` |
| Go up one folder | `cd ..` | `cd ..` |
| Make a folder | `mkdir notes` | `mkdir notes` |
| Clear the screen | `cls` or `clear` | `clear` |
| Build the robot code | `.\gradlew build` | `./gradlew build` |

A typical session looks like this:

```powershell
PS C:\Users\ada> cd Documents\Rebuilt-2026
PS C:\Users\ada\Documents\Rebuilt-2026> ls
PS C:\Users\ada\Documents\Rebuilt-2026> .\gradlew build
```

:::tip Two keys that save hours
- **Tab** completes names. Type `cd Reb` and press Tab to get `cd Rebuilt-2026`.
- **Up arrow** brings back your previous commands.
:::

### Why `.\gradlew` and not `gradlew`?

`gradlew` is a small script in the project folder that downloads (or finds) the right version of Gradle and runs it. For safety, shells do not run programs from the current folder unless you say so. The `.\` (Windows) or `./` (macOS/Linux) means "the one in *this* folder." On macOS or Linux, if you see `permission denied`, run `chmod +x gradlew` once.

### Stopping a command

Some commands run until you stop them, like the robot simulator. Press **Ctrl + C** in the terminal to stop the running command. On a Mac this is also **Control + C**, not Command.

## Reading errors

When a command fails, the shell tells you why. Read the message before retrying. Two common ones:

```text
gradlew : The term 'gradlew' is not recognized as the name of a cmdlet...
```
You forgot the `.\` prefix, or you are not in the project folder. Run `pwd` to check.

```text
cd : Cannot find path 'C:\Users\ada\Rebuilt' because it does not exist.
```
A typo or wrong starting folder. Use `ls` to see what is actually there, then use Tab completion.

:::danger Deleting from a terminal skips the Recycle Bin
Commands like `rm -rf` (macOS/Linux) and `Remove-Item -Recurse` (PowerShell) delete immediately and permanently. Never run a delete command you copied from somewhere without understanding exactly which path it removes. When in doubt, delete in File Explorer or Finder instead.
:::

:::quiz
? You are in `C:\Users\ada\Documents\Rebuilt-2026`. Which is a **relative** path to the build file?
- `C:\Users\ada\Documents\Rebuilt-2026\build.gradle`
+ `build.gradle`
- `/build.gradle`
- `C:\build.gradle`
> A relative path starts from where you are. The first option is absolute; the others point to the wrong place.

?text You are in `Rebuilt-2026\src`. What command moves you back up to `Rebuilt-2026`?
= cd .. | cd..
> `..` means the parent folder.

? The simulator is running in the terminal and you want to stop it. What do you press?
- Escape
+ Ctrl + C
- Ctrl + Z
- Close VS Code
> Ctrl + C sends an interrupt that stops the running command. It works in PowerShell, zsh, and bash.

? In PowerShell, which command builds the robot code from the project folder?
- `gradlew build`
+ `.\gradlew build`
- `build.gradle`
- `java build`
> PowerShell will not run a script from the current folder without the `.\` prefix. On macOS and Linux the equivalent is `./gradlew build`.

?tf The `.wpilib/wpilib_preferences.json` file stores the team number that deploys use.
= true
> Our file contains `"teamNumber": 967` and `"projectYear": "2026"`. GradleRIO reads it to find the robot at deploy time.
:::
