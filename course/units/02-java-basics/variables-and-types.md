---
summary: Store values in variables, choose the right type for robot data, and name things so units and meaning are obvious.
objectives:
  - Declare, assign, and update variables of Java's common types
  - Choose between int, double, boolean, char, long, and String for robot data
  - Name variables and constants the way Java programmers and our codebase do
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## A variable is a labeled box

A **variable** is a name for a place in memory that holds a value. In Java every variable has a **type**, which says what kind of value it can hold.

```java
int jamCount = 0;           // declare an int named jamCount and store 0 in it
jamCount = jamCount + 1;    // read it, add 1, store the result back: now 1
jamCount++;                 // shorthand for adding 1: now 2
```

- **Declaring** creates the variable: `int jamCount`.
- **Assigning** stores a value: `= 0`. The `=` means "store into," not "equals."
- **Using** the name reads the current value.

Here are real variables from our intake. Each one remembers something between loops:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="private int jamCount = 0;" lines=3

## Java's types, in robot terms

| Type | Holds | Example in Rebuilt-2026 |
|---|---|---|
| `int` | Whole numbers, about ±2.1 billion | CAN IDs like `frontLeftDriveCanId = 1`, current limits like `30` |
| `double` | Numbers with decimals | Nearly every measurement: `maxSpeedMetersPerSec = 4.2` |
| `boolean` | `true` or `false` | `hubActive`, `jammed`, `turnInverted` |
| `char` | One character | `gameData.charAt(0)` is `'R'` or `'B'` |
| `long` | Very large whole numbers | `BUILD_UNIX_TIME = 1763843249567L` (milliseconds since 1970) |
| `String` | Text | Camera names like `"April_Tag_1"` |

`int`, `double`, `boolean`, `char`, and `long` are **primitive types**: simple values built into Java. `String` is a class, which [Unit 3](course:03-java-objects/classes-and-objects) explains, but you can use it like a value today.

:::tip Robot data is almost always double
Distances, angles, speeds, voltages, and currents are measured, not counted, so they need decimals. Use `int` for things you *count* or *identify*, like loop counts and CAN IDs.
:::

### Literals

A **literal** is a value typed directly into code. The way you write it decides its type:

```java
int canId = 12;              // no decimal point: int
double kp = 1.0e-3;          // decimal or scientific notation: double (0.001)
boolean inverted = true;
char alliance = 'R';         // single quotes: char
String camera = "April_Tag_2"; // double quotes: String
long millis = 1763843249567L;  // L suffix: long
```

`1.0e-3` means 1.0 × 10⁻³. You will see it in PID constants like `intakeP = 1.0e-3`.

## Types protect you

Java checks types when it compiles. This is a compile error:

```java
int canId = 12.5;   // error: incompatible types: possible lossy conversion from double to int
```

Java refuses to silently throw away the `.5`. That strictness catches real bugs before they reach the robot. You can force a conversion with a **cast**, `(int) 12.5`, which chops off the decimal to give `12`. Only cast when you mean it.

## Naming: the cheapest bug prevention there is

Java conventions:

| Kind of name | Style | Examples |
|---|---|---|
| Variables and methods | `camelCase` | `wheelRadiusMeters`, `getTurretAngle` |
| Classes | `PascalCase` | `DriveConstants`, `ShooterSetpoint` |
| Constants | `UPPER_SNAKE_CASE` | `METERS_PER_INCH`, `DEADBAND` |

Good names say **what** and **in what units**:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final double maxSpeedMetersPerSec" lines=5

`maxSpeedMetersPerSec` cannot be mistaken for feet per second. `odometryFrequency` has no unit in its name, so the author added a `// Hz` comment. A unit in the name is even better, because comments can be missed.

:::info Why units in names matter
In 1999, NASA lost the Mars Climate Orbiter because one team's software produced thrust data in pound-force seconds while another expected newton seconds. A robot is cheaper than a spacecraft, but a turret aimed in degrees when the code expects radians will still hit a teammate's arm.
:::

:::team In our code
Our constants mix styles. `DriveConstants` uses `camelCase` for constants like `driveMotorCurrentLimit`, but `DEADBAND` and `ANGLE_KP` use `UPPER_SNAKE_CASE`. The code works either way. Consistency within one file makes it easier to read, which is why [Unit 15](course:15-quality/style-and-documentation) covers the team's style.
:::

## `final`: values that must not change

Add `final` to a variable that should be assigned exactly once. The compiler then stops anyone from changing it by accident:

```java
final double wheelRadiusMeters = 0.0508;
wheelRadiusMeters = 0.06;   // error: cannot assign a value to final variable wheelRadiusMeters
```

Almost every value in `DriveConstants` is `public static final`, meaning a constant shared by the whole program. [Unit 3](course:03-java-objects/encapsulation-static-final) explains `public` and `static`.

## `var`: let Java figure out the type

Inside a method, you can write `var` and Java works out the type from the value:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="var turnConfig = new SparkMaxConfig();" lines=5

`turnConfig` is a `SparkMaxConfig`, because that is what `new SparkMaxConfig()` creates. Use `var` when the type is obvious from the right side. When it is not, write the type out so readers do not have to guess.

## Variables that belong to a subsystem

Variables declared inside a class but outside any method are **fields**. They live as long as the object does, so a subsystem can remember things from one 20 ms loop to the next:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private double matchTime;" lines=5

`periodTimer` starts at `0` and `hubActive` starts at `true`. `matchTime` has no initial value, so it starts at Java's default for `double`, which is `0.0`. Fields get defaults, but **local variables inside methods do not**. The compiler makes you assign them before use.

:::quiz
? Which type should store the robot's maximum speed, 4.2 m/s?
- `int`
+ `double`
- `boolean`
- `char`
> Speeds are measurements with decimals. An `int` could only store 4.

? What happens when you compile `int canId = 12.5;`?
- `canId` becomes 12
- `canId` becomes 13
+ A compile error, because a double cannot be stored in an int without a cast
- It compiles, but crashes when the robot runs
> Java refuses to silently lose the `.5`. `(int) 12.5` would compile and give 12.

?code What does this print?
```java
int jamCount = 0;
jamCount = jamCount + 1;
jamCount++;
System.out.println(jamCount);
```
= 2
> The first line stores 0, the second makes it 1, and `++` makes it 2.

? Which is the best name for a variable holding the wheel radius in meters?
- `r`
- `WheelRadius`
+ `wheelRadiusMeters`
- `wheel_radius`
> Local variables use camelCase, and including the unit prevents mix-ups.

?tf After `final double kp = 5.0;`, the statement `kp = 4.0;` compiles.
= false
> `final` means the variable can be assigned only once. The compiler rejects the second assignment.

? What is the type of `gameData.charAt(0)` when `gameData` is a `String`?
- `String`
+ `char`
- `int`
- `boolean`
> `charAt` returns a single `char`, such as `'R'`. Chars use single quotes and Strings use double quotes.
:::
