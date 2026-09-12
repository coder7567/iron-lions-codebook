---
summary: Find your way around WPILib VS Code, run every WPILib command you will need, and use the navigation shortcuts that make a large codebase feel small.
objectives:
  - Open the Command Palette and run WPILib commands like Build, Deploy, Simulate, and Test
  - Jump to definitions, find references, and search across the whole project
  - Use quick fixes, rename, and formatting instead of editing by hand
---

## The window at a glance

WPILib VS Code is Visual Studio Code with the WPILib extension and Java tools already installed. The parts you will use most:

| Area | Where | Use it for |
|---|---|---|
| **Explorer** | Left sidebar, file icon | Browsing folders like `src/main/java/frc/robot` |
| **Search** | Left sidebar, magnifier | Finding text across every file |
| **Source Control** | Left sidebar, branch icon | Seeing changed files and committing (Unit 4) |
| **Testing** | Left sidebar, flask icon | Running JUnit tests one at a time or all at once |
| **Editor** | Center | Reading and writing code, with tabs for open files |
| **Panel** | Bottom | Terminal, Problems (errors and warnings), Output |
| **WPILib button** | Top right, the red **W** | A shortcut menu of WPILib commands |

## The Command Palette

Almost everything is a command. Press **Ctrl + Shift + P** (**Cmd + Shift + P** on macOS) and start typing. The WPILib commands you will use:

| Command | What it does | When |
|---|---|---|
| `WPILib: Build Robot Code` | Compiles the project and checks for errors | Constantly |
| `WPILib: Deploy Robot Code` | Builds, then sends the code to the roboRIO | Connected to a robot |
| `WPILib: Simulate Robot Code` | Runs the robot program on your laptop | Testing without a robot (next lesson) |
| `WPILib: Test Robot Code` | Runs every JUnit test | Exercises and real tests |
| `WPILib: Manage Vendor Libraries` | Adds, updates, or removes vendordeps | Rarely, with a mentor |
| `WPILib: Start Tool` | Opens AdvantageScope, Elastic, SysId, Glass, and others | Tuning and debugging |
| `WPILib: Set Team Number` | Edits `.wpilib/wpilib_preferences.json` | New projects |
| `WPILib: Open API Documentation` | Opens WPILib's Javadoc | Looking up a class |

:::warning Vendor library updates are team decisions
Updating REVLib, AdvantageKit, or PathPlanner can change how the robot behaves. Our 2026 code pins REVLib `2026.0.5`, AdvantageKit `26.0.2`, and PathPlanner `2026.1.2`. Never update a vendordep on the team repo by yourself, especially during an event.
:::

## Navigation shortcuts that matter

Rebuilt-2026 has 37 Java files. You will not remember where everything is, and you do not need to.

| Shortcut (Windows / macOS) | Action |
|---|---|
| **Ctrl + P** / **Cmd + P** | Open a file by typing part of its name, like `superstr` |
| **F12** | Go to the definition of the thing under your cursor |
| **Alt + ←** / **Ctrl + -** | Jump back to where you were |
| **Shift + F12** | Find every reference to a method, field, or class |
| **Ctrl + Shift + F** / **Cmd + Shift + F** | Search text in every file |
| **Ctrl + Shift + O** / **Cmd + Shift + O** | List the methods in the current file |
| **Ctrl + `** | Open or close the terminal |

### Try it on the team code

Once you have the team code open (next lesson), try this tour. It takes five minutes and teaches you the shape of the robot.

:::steps
1. Press **Ctrl + P**, type `RobotContainer`, and press Enter.
2. Find the line `controller.rightTrigger().onTrue(superstructure.setWantedStateCommand(WantedState.SHOOTING));`. Click on `setWantedStateCommand` and press **F12**. You land in `Superstructure.java`.
3. With your cursor on `setWantedStateCommand`, press **Shift + F12** to see everywhere it is used, including the named commands for PathPlanner.
4. Press **Alt + ←** to jump back.
5. Press **Ctrl + Shift + F** and search for `LoggedNetworkNumber`. Every result is a value that can be tuned from a dashboard.
:::

## Letting the editor write code for you

The Java extension understands your code as you type. Use it.

- **Red squiggles** are compile errors. Hover over one to read the message, or open the **Problems** panel for the whole list.
- **Ctrl + Space** suggests completions. Type `drive.get` and press it to see every getter on `Drive`.
- **Ctrl + .** (quick fix) offers fixes, most often *Import* for a class you used without importing.
- **F2** renames a variable, method, or class **everywhere it is used**. Never rename with find-and-replace, which also changes unrelated text.
- **Shift + Alt + F** formats the file. Consistent formatting makes code review easier.
- **Ctrl + /** comments or uncomments the selected lines.

:::tip Hover to read documentation
Hover over any WPILib class, like `SwerveDrivePoseEstimator`, to see its Javadoc. It is often faster than searching the web, and it always matches the library version you have installed.
:::

## When VS Code gets confused

After switching branches or changing `build.gradle`, the Java tools sometimes show errors that the build does not have. If **WPILib: Build Robot Code** succeeds but the editor is full of red, open the Command Palette and run **Java: Clean Java Language Server Workspace**, then reload when asked. The *build* is the source of truth, not the squiggles.

:::quiz
?text Which key jumps to the definition of the method under your cursor?
= F12 | /^f12$/i
> F12 goes to the definition, and Alt + ← (Ctrl + - on macOS) brings you back.

? You want to rename `getRumble()` everywhere in the project. What is the safest way?
- Find and replace `getRumble` in every file
+ Put the cursor on the method name and press F2
- Rename it in one file and fix the compile errors one at a time
- Delete the method and write a new one
> F2 is a symbol-aware rename. It changes the method and every call site, and nothing else.

? Which command runs the robot program on your laptop without a roboRIO?
- `WPILib: Deploy Robot Code`
+ `WPILib: Simulate Robot Code`
- `WPILib: Manage Vendor Libraries`
- `WPILib: Set Team Number`
> Simulation runs the real robot code on your computer with simulated hardware. The next lesson walks through it.

?tf If the editor shows red squiggles but `WPILib: Build Robot Code` reports BUILD SUCCESSFUL, the build result is the one to trust.
= true
> The language server can fall out of sync, especially after switching branches. Clean the Java workspace to refresh it.
:::
