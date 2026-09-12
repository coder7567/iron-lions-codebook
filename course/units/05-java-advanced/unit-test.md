---
summary: Show that you can read and write the advanced Java our robot code relies on, from inheritance and interfaces to lambdas, records, generics, streams, threads, and annotations.
---

This test covers all of Unit 5. Most questions use real patterns from Rebuilt-2026.

:::exam Unit 5 test: advanced Java in robot code
? `Superstructure extends SubsystemBase` and defines `public void periodic()`. Who calls `periodic()`?
+ WPILib's command scheduler, every loop, because the subclass overrides the method the framework already calls
- `RobotContainer`, once at startup
- The Driver Station
- Nobody; it must be called manually
> Inheritance lets the framework own the timing while subclasses supply behavior.

? What is the main purpose of writing `@Override` on `robotPeriodic()`?
- It makes the method run first
+ It makes the compiler fail if the method does not actually override anything, catching typos
- It makes the method static
- It logs the method
> Without it, a misspelled name would silently become a method nobody calls.

? Which statement about `TurretIOSim extends TurretIOSpark` is accurate?
+ Its `super()` call constructs and configures real Spark objects even in simulation
- It cannot access any `TurretIOSpark` fields
- It automatically keeps real and simulated behavior identical
- Java does not allow simulation classes to extend real ones
> That coupling is why implementing a shared interface is usually the better design.

? `RobotContainer` passes `new ModuleIO() {}` to `Drive` in replay mode. Why does that compile and work?
+ Every `ModuleIO` method has a default body, so an empty anonymous class is a complete implementation
- `ModuleIO` is an abstract class with a constructor
- Replay mode ignores the drive subsystem
- Anonymous classes can skip required methods
> The do-nothing implementation lets AdvantageKit supply inputs from the log.

? What does polymorphism let `Drive` do?
+ Run the same code with real, simulated, or replay IO objects, calling each object's own `updateInputs`
- Change its class at runtime
- Run on multiple roboRIOs
- Skip compiling unused methods
> `Drive` depends on interfaces, so any implementation works.

? Which functional interface matches `turret::shooterSpedUp` when passed to `Intake`?
- `DoubleSupplier`
+ `BooleanSupplier`
- `Consumer<Boolean>`
- `Runnable`
> `shooterSpedUp()` takes nothing and returns a boolean.

?code What does this print?
```java
double[] target = {1000};
DoubleSupplier supplier = () -> target[0];
target[0] = 3000;
System.out.println(supplier.getAsDouble());
```
= 3000.0
> The lambda reads the array when it runs, not when it was created.

? Why does `Turret` receive `Supplier<Pose2d>` instead of a `Pose2d`?
+ So it gets the current pose every loop instead of one value from when it was constructed
- `Pose2d` values cannot be stored in fields
- Suppliers are required by `SubsystemBase`
- To save memory
> Suppliers deliver fresh data each call.

?code What does this print?
```java
record TargetInfo(int tagID, double yaw) {}
// ...
TargetInfo a = new TargetInfo(26, 1.5);
TargetInfo b = new TargetInfo(26, 1.5);
System.out.println(a.tagID() + " " + a.equals(b));
```
= 26 true
> Accessors are named after components, and records compare by value.

? Where should a record reject an ambiguity above 1.0?
+ In its compact constructor
- In `toString`
- In a setter
- Records cannot reject values
> Validating in the compact constructor means an invalid record can never exist.

? Which declaration allows a table to call `blend` on its values?
- `class Table<V>`
+ `class Table<V extends Blendable<V>>`
- `class Table<Blendable>`
- `class Table extends Blendable`
> The bound promises every `V` implements `Blendable<V>`.

?tf `Map<Integer, double> ids = new HashMap<>();` compiles.
= false
> Type arguments must be object types: use `Double`.

?code What does this print?
```java
long count = java.util.List.of(1, 2, 3, 4, 5).stream()
    .filter(n -> n % 2 == 1)
    .map(n -> n * 10)
    .count();
System.out.println(count);
```
= 3
> 1, 3, and 5 pass the filter. Mapping does not change how many there are.

? Why does `Drive.periodic()` lock `odometryLock` while reading modules, even though the queues are thread-safe?
+ To read the gyro and every module as one consistent set, without the odometry thread adding samples partway through
- Because Java requires a lock around every loop
- To make the odometry thread run faster
- Because `ArrayBlockingQueue` cannot be read without a lock
> Each queue is safe on its own; the lock keeps the whole set in step.

? Which task belongs on a background thread in robot code?
+ Sampling encoders with timestamps and handing the data off through a thread-safe queue
- Running the Superstructure state machine
- Scheduling commands
- Binding controller buttons
> Keep logic on the main thread; background threads sample and hand off.

? Where does `ModuleIOInputsAutoLogged` come from?
+ AdvantageKit's annotation processor generates it from `@AutoLog` during compilation
- It is part of WPILib
- A teammate wrote it by hand
- VS Code generates it when the file opens
> Generated sources are rebuilt every compile. Never edit them.

?? Which are true about `@AutoLogOutput(key = "Odometry/Robot")` on `Drive.getPose()`? (Select all that apply.)
+ AdvantageKit records the method's return value every loop
+ The value appears under that key in logs and AdvantageScope
- It changes what `getPose()` returns
+ The method should be fast and free of side effects
> Annotated outputs are called each loop just to record values.
:::
