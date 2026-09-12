---
summary: Read the annotations in our code, understand how AdvantageKit's @AutoLog writes classes for you at compile time, and fix the editor errors that generated code can cause.
objectives:
  - Explain what annotations like @Override, @SuppressWarnings, @AutoLog, and @AutoLogOutput do
  - Describe how an annotation processor generates classes such as ModuleIOInputsAutoLogged
  - Troubleshoot "cannot find symbol ...AutoLogged" errors
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - build.gradle
  - .vscode/settings.json
---

## Annotations are labels for tools

An **annotation** starts with `@` and attaches information to a class, method, field, or parameter. Annotations do not run anything by themselves. Tools read them: the compiler, testing frameworks, and code generators.

| Annotation | Read by | Meaning |
|---|---|---|
| `@Override` | Compiler | This method must override something |
| `@SuppressWarnings("unused")` | Compiler | Do not warn that this looks unused |
| `@FunctionalInterface` | Compiler | This interface must have exactly one abstract method |
| `@Test`, `@DisplayName` | JUnit | This method is a test, with a readable name |
| `@AutoLog` | AdvantageKit's annotation processor | Generate logging code for this inputs class |
| `@AutoLogOutput(key = "...")` | AdvantageKit at runtime | Log this field or method's value every loop |

`RobotContainer` suppresses a warning for a field it creates but never reads directly:

::source file="src/main/java/frc/robot/RobotContainer.java" from="// Subsystems" lines=7

`aprilTagVision` looks unused, but constructing it registers the subsystem with the scheduler, and it feeds the drive through a method reference. The annotation tells the compiler this is deliberate.

## @AutoLog: code that writes code

Every IO interface has an inputs class marked `@AutoLog`:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="@AutoLog" lines=2

Nobody on the team wrote a class called `ModuleIOInputsAutoLogged`, yet `Module` uses one:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="private final ModuleIOInputsAutoLogged inputs" lines=1

It is **generated**. During compilation, an **annotation processor** finds every `@AutoLog` class and writes a new subclass, `<Name>AutoLogged`, with methods that save every field to the log (`toLog`) and read every field back during replay (`fromLog`). That is why `Module.periodic()` can do this:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="io.updateInputs(inputs);" lines=2

`Logger.processInputs` records every field of `inputs` on the real robot, or replaces them with logged values during replay. Add a field to `ModuleIOInputs`, and it is logged automatically on the next build.

The processor is connected in `build.gradle`:

::source file="build.gradle" from="def akitJson" lines=2

The generated source appears under `build/generated/sources/annotationProcessor/java/main/` after a build. It is recreated every build, so never edit it.

## When the editor cannot find generated classes

Our VS Code settings turn off annotation processing inside the editor:

::source file=".vscode/settings.json" from="java.import.gradle.annotationProcessing.enabled" lines=1

So the editor may underline `ModuleIOInputsAutoLogged` with **cannot find symbol**, especially in a fresh clone. The Gradle build still runs the processor and works.

:::tip If you see "cannot find symbol ...AutoLogged"
1. Run `.\gradlew build` from the terminal. If it succeeds, the code is fine.
2. Run **Java: Clean Java Language Server Workspace** in VS Code and reload.
3. Check that the inputs class still has `@AutoLog` and that the generated name matches: `GyroIOInputs` becomes `GyroIOInputsAutoLogged`.
:::

## @AutoLogOutput: log a value without a log call

Outputs, meaning values the code computes, can be logged by annotating the method or field that produces them:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="Returns the current odometry pose." lines=5

Every loop, AdvantageKit calls `getPose()` and records the result under `Odometry/Robot`, which appears in AdvantageScope as `RealOutputs/Odometry/Robot`. AdvantageKit finds these annotations by looking through the objects your `Robot` class holds, such as subsystems stored in `RobotContainer`'s fields. Methods with `@AutoLogOutput` should be quick and have no side effects, because they run every loop.

:::info Annotations in tests
JUnit's annotations work the same way: `@Test` marks test methods, `@BeforeEach` runs setup before each test, and `@ParameterizedTest` with `@CsvSource` runs one test with many inputs, as in the `MatchClock` exercise.
:::

:::quiz
? Where does the class `ModuleIOInputsAutoLogged` come from?
- A team member wrote it in 2025
+ AdvantageKit's annotation processor generates it at compile time from the `@AutoLog` class
- It is downloaded with REVLib
- VS Code creates it when you open the file
> Annotation processors write new source files during compilation.

? VS Code underlines `GyroIOInputsAutoLogged` in red, but `.\gradlew build` succeeds. What is going on?
+ The editor is not running the annotation processor, but the Gradle build does, so the code is fine
- The build is lying and the code is broken
- The AdvantageKit vendordep is missing
- `@AutoLog` only works in simulation
> Our settings disable annotation processing in the editor. Trust the build, and clean the Java workspace if the errors bother you.

? What does `@AutoLogOutput(key = "Odometry/Robot")` on `getPose()` do?
+ Logs the method's return value under that key every loop
- Makes `getPose()` run faster
- Creates a new `Pose2d` class
- Resets the pose at startup
> AdvantageKit calls annotated methods each loop and records their values.

?tf Adding a new public field to an `@AutoLog` inputs class makes it logged automatically after the next build.
= true
> The generated class includes every field, so there is no logging code to update.

? What does `@SuppressWarnings("unused")` change about how the code runs?
- It removes the field from the program
+ Nothing at runtime; it only silences a compiler warning
- It makes the field static
- It logs the field
> It is a note for the compiler, not an instruction to the running robot.
:::
