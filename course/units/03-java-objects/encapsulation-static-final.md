---
summary: Protect an object's data with access modifiers, share things across a whole class with static, and lock values down with final.
objectives:
  - Choose public, private, protected, or package-private access for fields and methods
  - Explain how encapsulation keeps a mechanism's limits from being bypassed
  - Use static for constants, helpers, and shared state, and final for values that must not change
files:
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
  - src/main/java/frc/robot/RobotContainer.java
---

## Access modifiers

Every field, method, and class has an **access level** that controls who can use it:

| Modifier | Who can use it | Typical use |
|---|---|---|
| `public` | Any code anywhere | Methods other classes are meant to call |
| `private` | Only code inside the same class | Fields, and helper methods |
| `protected` | The same package, plus subclasses | Fields a subclass needs (see our `TurretIOSim`) |
| *(none)* | Only classes in the same package | Rarely chosen on purpose |

The rule of thumb: **make fields private, and make methods public only when other classes need them.**

## Encapsulation: the only way in is through the rules

**Encapsulation** means an object hides its data and exposes methods that enforce rules about that data. For a robot, those rules are often *physical limits*.

Here is how the real turret sets its angle:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="turretSetAngle = MathUtil.clamp(angle" lines=3

`setTurretAngle` clamps every request between the soft limits before sending it to the motor. As long as code *must* go through `setTurretAngle`, no request can drive the turret past its wiring.

Now look at the fields of `Intake`:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public IntakeIO io;" lines=7

`io` is **public**. Any class holding the intake can reach past its state machine and command hardware directly. `RobotContainer` has leftover lines from testing that did exactly that with the turret, which also has a public `io`:

::source file="src/main/java/frc/robot/RobotContainer.java" from="// controller.povUp()" lines=4

Those lines are commented out, which was the right call for competition. They bypass `Turret`'s state machine, so the next loop's `periodic()` could immediately fight the manual command. Making `io` **private** would make the mistake impossible instead of merely commented out.

:::tip Getters are not an automatic habit
Add a getter when another class truly needs a value, like `turret.getTurretAngle()` for logging. Add a setter only when changing that value from outside is safe. Every public method is a promise you have to keep.
:::

## static: belongs to the class, not to an object

A regular field exists **once per object**. Each of the four `Module` objects has its own `index`. A `static` field or method exists **once for the whole class**.

### Constants

`DriveConstants` is a class full of `public static final` values. There is one `maxSpeedMetersPerSec` for the entire program, so you use it through the class name: `DriveConstants.maxSpeedMetersPerSec`.

### Helper methods

`static` methods need no object. `DriveCommands.joystickDrive(...)` and `SparkUtil.ifOk(...)` are called on the class name. These helper classes also hide their constructor so nobody creates useless objects:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="public class DriveCommands" lines=3

### Shared state: one of something

`SparkOdometryThread` must exist exactly once, because it is one background thread reading every motor. It keeps the single instance in a `static` field. That pattern is called a **singleton**:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="private static SparkOdometryThread instance = null;" lines=13

The constructor is `private`, so the only way to get the thread is `SparkOdometryThread.getInstance()`, which creates it the first time and returns the same one after that.

:::warning Global mutable state bites
`SparkUtil.sparkStickyFault` is a `public static boolean` that *any* code can set or clear. `ModuleIOSpark` clears it, reads several values, then checks it, and that only works because nothing else touches it between those lines. Shared, changeable static state is convenient and fragile. Prefer passing values explicitly.
:::

## final: assign once

- A **`final` local variable or field** can be assigned exactly once.
- A **`final` class** cannot be extended. `public final class Main` stops anyone from subclassing the program's entry point.
- **`static final`** together make a constant.

For objects, `final` locks the *reference*, not the object's contents:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="static final Lock odometryLock" lines=5

`modules` can never point to a different array, but the constructor still fills the array's four slots. A `final` reference to something changeable does not make it unchangeable.

## Constructors that call constructors

A class can offer several constructors. One can hand off to another with `this(...)`, so setup code lives in one place:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public Intake(IntakeIO io, BooleanSupplier turretResetting" lines=10

The one-argument version fills in defaults: never resetting, flywheel always up to speed. That is handy for testing the intake alone.

## Name your magic numbers

`new SparkFlex(12, MotorType.kBrushless)` works, but `12` is a **magic number**: a value whose meaning lives only in someone's memory. A named constant documents intent and gives you one place to change it:

```java
public static final int TURRET_CAN_ID = 12;
// ...
turret = new SparkFlex(TurretConstants.TURRET_CAN_ID, MotorType.kBrushless);
```

:::exercise id="u03-counter"
Encapsulate the intake's jam-counting pattern in a reusable class.

- Every non-constant field must be `private`. A test checks this with reflection.
- `update(condition)` extends or resets the streak and reports whether it has triggered.
- A `private static` field counts how many counters have been created. Increment it in the constructor that does the real work.
- `loopsForSeconds` converts seconds to 20 ms loops, rounding up.
---hint
The no-argument constructor already calls `this(DEFAULT_REQUIRED_LOOPS)`. Put all setup, including the created-count increment, in the `int` constructor so it only happens once per object.
---hint
`Math.ceil(seconds * 50.0)` is almost right. Floating-point rounding can make `0.5 * 50` slightly more than 25, so subtract a tiny amount like `1e-9` before rounding up.
:::

:::quiz
? Which access modifier should the `io` field of a subsystem have, so hardware can only be commanded through the subsystem's logic?
- `public`
+ `private`
- `protected`
- `static`
> Private fields force other classes through public methods, which can enforce states and limits.

? Why does `DriveCommands` have a private constructor?
+ It only contains static helper methods, so creating `DriveCommands` objects would be pointless
- Private constructors make code run faster
- It lets subclasses create it
- Java requires every class to have one
> Hiding the constructor stops anyone from writing `new DriveCommands()`.

?tf `private final Module[] modules = new Module[4];` prevents code in the class from changing which module is stored in `modules[0]`.
= false
> `final` stops `modules` from pointing to a different array. The array's contents can still change, and the constructor fills them in.

? What makes `SparkOdometryThread` a singleton?
- It extends `Thread`
+ A private constructor plus a static `getInstance()` that always returns the same object
- It is declared `final`
- It is created in `RobotContainer`
> The first call creates the instance and stores it in a static field. Every later call returns that same instance.

?code What does this print?
```java
class Counter {
    static int created = 0;
    int count = 0;
    Counter() { created++; }
}
// ...
Counter a = new Counter();
Counter b = new Counter();
a.count++;
System.out.println(Counter.created + " " + a.count + " " + b.count);
```
= 2 1 0
> `created` is static and shared, so it counts both objects. `count` is per object, so only `a`'s changed.
:::
