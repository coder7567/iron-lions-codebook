---
summary: What actually happens between typing Java and a robot moving, covering source code, compilers, bytecode, the JVM, and the three kinds of errors you will meet.
objectives:
  - Describe the path from a .java file to code running on the roboRIO
  - Read the structure of a Java file (package, imports, class, methods, statements)
  - Tell compile errors, runtime errors, and logic errors apart
files:
  - src/main/java/frc/robot/Main.java
---

## From text to motion

A **program** is a list of instructions. You write them as text in a `.java` file, called **source code**. Computers cannot run that text directly, so Java works in two steps:

1. **Compiling.** The Java compiler (`javac`) checks your code follows Java's rules and translates it into **bytecode**, stored in `.class` files.
2. **Running.** The **Java Virtual Machine** (JVM) reads the bytecode and carries out the instructions.

On the robot, GradleRIO does the compiling on your laptop and packs everything into a **jar** file. Deploying copies the jar to the roboRIO, where a JVM running Java 17 starts your robot program.

```text
Robot.java ──javac──▶ Robot.class ──packed into──▶ robot jar ──deploy──▶ roboRIO JVM runs it
 (you write)            (bytecode)                  (GradleRIO)           (motors move)
```

Because the JVM does the same job on every computer, the *same* robot jar runs in simulation on your laptop and on the roboRIO.

## The shape of a Java file

Here is a complete, runnable Java program:

```java
package frc.training.demo;          // which folder (package) this class belongs to

import java.util.List;              // other classes this file uses

/** A tiny program that greets the team. */
public class Hello {                // a class named Hello; file must be Hello.java

  public static void main(String[] args) {   // where the program starts
    System.out.println("Hello, Iron Lions!"); // a statement: print a line
    System.out.println(967 + 1);              // prints 968
  }
}
```

The parts you will see in every file:

- **`package`** names the folder the class lives in. [Unit 3](course:03-java-objects/packages-and-imports) covers it.
- **`import`** lets you use classes from other packages by their short names.
- **`class`** is the container for code. The file name must match the public class name exactly, including capitals.
- **Methods** like `main` hold statements. `main` is where a normal Java program starts.
- **Statements** are single instructions. Most end with a **semicolon** `;`.
- **Braces** `{ }` group code into blocks. Every `{` needs a matching `}`.
- **Comments** are notes for humans that the compiler ignores: `// one line`, `/* many lines */`, and `/** Javadoc */` for documentation.

:::warning Java is case-sensitive
`System` and `system` are different names. So are `turret` and `Turret`. In our code, `Turret` is the class and `turret` is a variable holding one turret object.
:::

## Where the robot program starts

Robot programs have a `main` method too. It is tiny and you never edit it:

::source file="src/main/java/frc/robot/Main.java" from="public final class Main" to="  }"

`RobotBase.startRobot(Robot::new)` tells WPILib: "create a `Robot` object and run it forever." From then on, WPILib calls methods on `Robot` 50 times per second. That loop is what makes robot code different from a program that runs once and exits. You will study it in [Unit 6](course:06-robot-foundations/robot-lifecycle).

## Try Java without a project

The JDK includes **jshell**, a place to type Java and see the result immediately. It is perfect for testing a small idea.

```powershell
& "C:\Users\Public\wpilib\2026\jdk\bin\jshell.exe"
```

On macOS, run `~/wpilib/2026/jdk/bin/jshell`. Then try:

```text
jshell> 7 / 2
$1 ==> 3

jshell> 7.0 / 2
$2 ==> 3.5

jshell> "Team " + 967
$3 ==> "Team 967"

jshell> /exit
```

Did `7 / 2` surprise you? The [next lessons](course:02-java-basics/math-and-units) explain why, and why it matters for robots.

## Three kinds of errors

Every programmer, including the ones who wrote Rebuilt-2026, deals with all three every week.

| Kind | When you find out | Example | How to find it |
|---|---|---|---|
| **Compile error** | When you build | Missing semicolon, misspelled name, wrong type | The build prints the file, line, and message |
| **Runtime error** | While the code runs | Using an object that is `null` throws a `NullPointerException` | A stack trace in the console or Driver Station |
| **Logic error** | When the robot does the wrong thing | A missing minus sign makes "forward" drive backward | Testing, logs, and careful reasoning |

Compile errors are the friendliest, because the compiler finds them for you. Logic errors are the most dangerous, because the code runs happily while doing the wrong thing.

:::danger Runtime errors on a robot stop the robot
If robot code throws an exception that nothing catches, the program crashes. The Driver Station shows the error, the robot stops, and the code restarts, which takes several seconds. In a match that is a lost match. [Unit 3](course:03-java-objects/exceptions-null-optional) covers how our code avoids crashing.
:::

### Reading a compile error

```text
C:\...\Robot.java:43: error: ';' expected
    Logger.recordMetadata("ProjectName", BuildConstants.MAVEN_NAME)
                                                                   ^
1 error
```

Read it like a sentence: in `Robot.java`, **line 43**, Java expected a `;`, and the caret `^` points to where. Fix the **first** error first, because one mistake can cause a dozen follow-on errors.

:::quiz
?order Put the steps from source code to a moving robot in order.
1. You write `Robot.java`
2. The compiler turns it into bytecode
3. GradleRIO packs the bytecode into a jar
4. The jar is deployed to the roboRIO
5. The roboRIO's JVM runs the program
> Compiling and packaging happen on your laptop. Running happens on the roboRIO, or on your laptop in simulation.

? A class is declared `public class Superstructure`. What must its file be named?
- `superstructure.java`
+ `Superstructure.java`
- `Superstructure.class`
- Any name, as long as it ends in `.java`
> The file name must match the public class name exactly, including capital letters.

? The robot drives backward when the driver pushes forward, and there are no errors anywhere. What kind of error is this?
- Compile error
- Runtime error
+ Logic error
> The code compiled and ran without exceptions, but it does the wrong thing. Only testing reveals logic errors.

?code What does this print?
```java
System.out.println(967 + 1);
```
= 968
> `967 + 1` is arithmetic on numbers, so Java prints `968`.

?tf The compiler ignores comments.
= true
> Comments exist for people. The compiler throws them away before creating bytecode.
:::
