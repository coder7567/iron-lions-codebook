---
summary: Treat behavior as a value, with lambdas, method references, and functional interfaces like Supplier and BooleanSupplier, the glue that wires subsystems, commands, and triggers together.
objectives:
  - Write lambdas and method references and match them to functional interfaces
  - Explain why subsystems receive suppliers instead of plain values
  - Capture state in lambdas correctly and avoid common mistakes
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
---

## Passing behavior around

So far, arguments have been values: numbers, strings, objects. Sometimes you want to hand over **behavior**, meaning "here is how to get the robot's pose whenever you need it," not "here is the pose right now."

Java does this with **functional interfaces**: interfaces with exactly one abstract method. A **lambda** or **method reference** creates an object implementing that one method.

## Functional interfaces you will see constantly

| Interface | Method | Meaning | Example in our code |
|---|---|---|---|
| `Runnable` | `void run()` | Do something | Command actions |
| `Supplier<T>` | `T get()` | Produce a value | `Supplier<Pose2d> poseSupplier` in `Turret` |
| `DoubleSupplier` | `double getAsDouble()` | Produce a number | Joystick axes in `joystickDrive` |
| `BooleanSupplier` | `boolean getAsBoolean()` | Answer yes or no | `turretResetting` in `Intake` |
| `Consumer<T>` | `void accept(T)` | Use a value | `ifOk(..., consumer)` in `SparkUtil` |
| `DoubleConsumer` | `void accept(double)` | Use a number | Storing a Spark reading |
| `Function<T, R>` | `R apply(T)` | Turn a T into an R | Mapping with streams |

## Lambda syntax

```java
() -> 42.0                                // no parameters, returns 42.0
value -> value * value                    // one parameter
(a, b) -> a + b                           // two parameters
() -> {                                   // a block: use return for a value
    double speed = readSpeed();
    return speed > 1000;
}
```

The Java compiler figures out the parameter types from the functional interface you assign the lambda to.

## Method references

When a lambda just calls one existing method, a **method reference** says it more directly:

| Method reference | Same as the lambda | Kind |
|---|---|---|
| `drive::getPose` | `() -> drive.getPose()` | A method on a specific object |
| `CanDevice::getId` | `device -> device.getId()` | A method on whatever object is passed in |
| `Math::abs` | `x -> Math.abs(x)` | A static method |
| `Robot::new` | `() -> new Robot()` | A constructor |

`RobotContainer` wires subsystems together with method references:

::source file="src/main/java/frc/robot/RobotContainer.java" from="turret = new Turret(new TurretIOSpark()" lines=2

The turret does not own the drive. It just receives two functions: "ask the drive for the pose" and "ask the drive for its speeds." The intake receives two `BooleanSupplier`s that ask the turret questions. Nobody holds a reference to a whole other subsystem it does not need.

## Why suppliers instead of values

Look at when `Turret` uses its supplier:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public void periodic()" lines=11

`poseSupplier.get()` runs **every loop**, so the turret always aims from the robot's current position. If the constructor had been given a `Pose2d` value instead, the turret would aim from wherever the robot was at boot, forever.

:::warning Calling instead of referencing
`new Turret(io, drive.getPose(), ...)` calls `getPose()` once and passes today's answer, which does not even compile because the constructor wants a `Supplier`. `drive::getPose` passes the ability to ask later. Watch for missing `::`.
:::

## Lambdas in commands and triggers

Command-based code is built out of lambdas. The drive's default command runs this block every loop:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="public static Command joystickDrive(" lines=30

- `xSupplier`, `ySupplier`, and `omegaSupplier` are `DoubleSupplier`s. `RobotContainer` passes lambdas like `() -> -controller.getLeftY()`.
- `Commands.run(() -> { ... }, drive)` takes a `Runnable` to execute each loop, plus the subsystem it requires.

Triggers take a `BooleanSupplier` too. Here, rumble turns on and off with the Superstructure's opinion:

::source file="src/main/java/frc/robot/RobotContainer.java" from="new Trigger(superstructure::getRumble)" lines=5

## Your own functional interface

When no standard interface fits, declare one. Vision needs "accept a pose, a timestamp, and a standard deviation matrix":

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="//Feeds into the drive poseEstimator" lines=8

`@FunctionalInterface` asks the compiler to confirm there is exactly one abstract method. `RobotContainer` passes `drive::addVisionMeasurement`, whose parameters match.

## Capturing variables

A lambda can use local variables from the method around it, but only if those variables are **effectively final**, meaning never reassigned:

```java
double threshold = 0.5;
BooleanSupplier pressed = () -> controller.getRightTriggerAxis() > threshold;  // fine
threshold = 0.6;   // now the lambda above fails to compile
```

To keep **changing** state inside a lambda, store it somewhere mutable: a field, an object, or a one-element array. Our wheel radius characterization captures a small state object and updates its fields from inside lambdas:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="Commands.runOnce(" occurrence=4 lines=6

`state` itself never changes. The object it points to does.

`SparkUtil.ifOk` shows lambdas that store results into an object:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="ifOk(driveSpark, driveEncoder::getPosition" lines=8

`(value) -> inputs.drivePositionRad = value` is a `DoubleConsumer`: `ifOk` reads the Spark, checks for errors, and only then hands the value to the lambda.

:::tip Keep suppliers cheap
Suppliers and trigger conditions may be called every 20 ms, sometimes several times. Have them return values that are already computed, and never put slow work like file reads or waiting inside them.
:::

:::exercise id="u05-triggers"
Write small functions that build new `BooleanSupplier`s out of others, like WPILib's `Trigger` does.

- `and`, `or`, and `not` combine conditions, and must short-circuit.
- `above` turns a `DoubleSupplier` into a button, like `controller.rightTrigger()`.
- `risingEdge` is true only on the loop a condition becomes true, like `onTrue`. It needs memory between calls.
- `debounced` waits for a condition to stay true for several calls.

The tests use a pretend controller with method references like `pad::getRightTriggerAxis`.
---hint
Return a lambda that calls the inputs *inside* it: `return () -> a.getAsBoolean() && b.getAsBoolean();`. Calling them outside the lambda would read them only once.
---hint
For `risingEdge`, create `boolean[] previous = {false};` before the `return`, and update `previous[0]` inside the lambda. The array reference never changes, so the lambda may capture it.
:::

:::quiz
? What does `drive::getPose` create?
+ A `Supplier<Pose2d>` that calls `drive.getPose()` each time it is used
- The current pose value, computed once
- A copy of the drive subsystem
- A new method on the drive
> Method references package a method call to run later, as many times as needed.

? Which functional interface fits the lambda `() -> -controller.getLeftY()`?
- `Consumer<Double>`
+ `DoubleSupplier`
- `BooleanSupplier`
- `Runnable`
> It takes nothing and produces a `double`.

?code What does this print?
```java
int[] count = {0};
Runnable tick = () -> count[0]++;
tick.run();
tick.run();
tick.run();
System.out.println(count[0]);
```
= 3
> The lambda changes the array's contents three times. The captured array reference itself never changes.

? Why does `Turret` take a `Supplier<Pose2d>` instead of a `Pose2d`?
+ So it can get the robot's current pose every loop instead of a single value from boot
- Suppliers use less memory
- `Pose2d` cannot be passed to constructors
- PathPlanner requires it
> A value is a snapshot. A supplier gives fresh data each time it is called.

?tf A lambda can reassign a local variable from the method that created it.
= false
> Captured locals must be effectively final. Keep changing state in a field, an object, or an array.
:::
