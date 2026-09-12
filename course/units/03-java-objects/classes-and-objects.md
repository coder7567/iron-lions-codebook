---
summary: Build your own types with classes, create objects with new, give them state and behavior, and understand references, sharing, and null.
objectives:
  - Write a class with fields, a constructor, and methods, and create objects from it
  - Use this to tell fields from parameters
  - Explain references, shared objects, mutation, and null
files:
  - src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
---

## Classes are blueprints, objects are the things

So far you have used types Java provides: `int`, `double`, `String`. A **class** lets you define your own type by bundling:

- **fields**, the data each object remembers, and
- **methods**, the things each object can do.

An **object** is one thing built from a class. The class `Module` describes a swerve module, and the robot creates **four** `Module` objects from it, one per corner, each with its own encoders and state.

Here is a small, real class from our turret package:

::source file="src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java" from="public class ShooterSetpoint" lines=18

- `rpm` and `hoodAngle` are **fields**. Every `ShooterSetpoint` object has its own pair.
- `ShooterSetpoint(double rpm, double hoodAngle)` is a **constructor**, a special method with the class's name and no return type. It runs once, when the object is created.
- `interpolate(...)` is a **method**. It returns a new setpoint between this one and another.
- `implements Interpolatable<ShooterSetpoint>` promises WPILib this class can interpolate. [Unit 5](course:05-java-advanced/interfaces-and-polymorphism) explains interfaces.

## Creating and using objects

`new` builds an object and runs its constructor:

```java
ShooterSetpoint close = new ShooterSetpoint(1850, 0.879);
ShooterSetpoint far = new ShooterSetpoint(2700, 0.641);

System.out.println(close.rpm);              // 1850.0
ShooterSetpoint mid = close.interpolate(far, 0.5);
System.out.println(mid.rpm);                // 2275.0
```

Use a **dot** to reach an object's fields and methods: `close.rpm`, `close.interpolate(...)`.

## `this`: the object itself

Inside a class, `this` means "the object this code is running for." Constructors use it when a parameter has the same name as a field:

```java
public ShooterSetpoint(double rpm, double hoodAngle) {
    this.rpm = rpm;             // field = parameter
    this.hoodAngle = hoodAngle;
}
```

Without `this.`, `rpm = rpm;` would assign the parameter to itself and leave the field at 0.

The `Turret` constructor uses the same pattern to remember the objects it depends on:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public Turret(TurretIO io, Supplier<Pose2d> poseSupplier" lines=6

## Variables hold references, not objects

This is the most important idea in the lesson. A variable of a class type does not contain the object. It holds a **reference**, like an address pointing to where the object lives.

```java
ShooterSetpoint a = new ShooterSetpoint(2000, 0.879);
ShooterSetpoint b = a;        // copies the reference, not the object
b.rpm = 3000;                 // changes the one shared object
System.out.println(a.rpm);    // 3000.0, because a and b point to the same object
```

Compare that with primitives from [Methods](course:02-java-basics/methods): assigning an `int` copies the number. Assigning an object variable copies the arrow.

That also means **methods can change objects you pass them**. `Module.runSetpoint` says so right in its comment:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="/** Runs the module with the specified setpoint state. Mutates the state to optimize it. */" lines=10

And the drive relies on that change. It logs the setpoints before and after sending them to the modules:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Log unoptimized setpoints" lines=11

The same array is logged twice. Between the two log calls, each module *mutated* its state object. So `SwerveStates/Setpoints` and `SwerveStates/SetpointsOptimized` can show different angles, even though both log calls pass `setpointStates`.

:::warning Mutation surprises people
Changing an object that other code also holds is powerful and easy to get wrong. The team's `ShooterSetpoint` has public, non-final fields, so any code could change `rpm` on a setpoint stored in the shot map and quietly corrupt every future shot. The next lesson shows how `private` and `final` prevent that.
:::

## `==` compares references

For objects, `==` asks "are these the same object?", not "do they hold the same values?":

```java
ShooterSetpoint x = new ShooterSetpoint(2000, 0.879);
ShooterSetpoint y = new ShooterSetpoint(2000, 0.879);
System.out.println(x == y);   // false: two different objects with equal values
System.out.println(x == x);   // true
```

Comparing *values* takes an `equals` method. [Records](course:05-java-advanced/records-and-nested-classes) generate one for you.

## null: a reference to nothing

A class-type variable can hold `null`, meaning it points to no object. Fields of class types start as `null` unless assigned. `Turret` declares `private Pose2d pose;` and fills it in `periodic()`.

Calling a method or reading a field through `null` throws a **NullPointerException** and crashes the robot program:

```java
Pose2d pose = null;
pose.getX();   // NullPointerException
```

[Exceptions, null, and Optional](course:03-java-objects/exceptions-null-optional) covers the defenses.

## toString: objects that explain themselves

Every class inherits a `toString()` method that turns an object into text. The default prints something unhelpful like `ShooterSetpoint@1b6d3586`. Override it, and logging or printing the object shows real values:

```java
@Override
public String toString() {
    return "ShooterSetpoint[rpm=" + rpm + ", hoodAngle=" + hoodAngle + "]";
}
```

:::exercise id="u03-setpoint"
Write your own `ShooterSetpoint`, but make it **immutable**: its fields are `private final`, so no one can change a setpoint after creating it.

- Store both values in the constructor, and return them from `getRpm()` and `getHoodAngle()`.
- `interpolate(end, t)` returns a **new** setpoint, clamping `t` into 0..1.
- `withRpm(newRpm)` returns a **new** setpoint with the same hood angle.
- `toString()` matches the exact format in the Javadoc.

The tests use two real neighboring rows from the team's shot map: 2000 RPM at 2.0 m and 2050 RPM at 2.38 m.
---hint
Declare the fields as `private final double rpm;` and `private final double hoodAngle;`. A `final` field must be assigned in the constructor.
---hint
Inside `interpolate` you can read `end.rpm` directly. `private` means private to the class, not to the object, so another `ShooterSetpoint`'s fields are visible.
---hint
Clamp first: `double clamped = Math.max(0.0, Math.min(1.0, t));` then compute `start + (end - start) * clamped` for each value.
:::

:::quiz
? What is the difference between a class and an object?
+ A class is a blueprint that defines fields and methods; an object is one instance built from it with `new`
- A class runs on the roboRIO and an object runs on the laptop
- They are two names for the same thing
- A class holds data and an object holds methods
> `Module` is one class, and the drive creates four `Module` objects from it.

?code What does this print?
```java
ShooterSetpoint a = new ShooterSetpoint(2000, 0.9);
ShooterSetpoint b = a;
b.rpm = 2500;
System.out.println(a.rpm);
```
= 2500.0
> `b = a` copies the reference, so both variables point to one object. Changing it through `b` is visible through `a`.

? A constructor says `rpm = rpm;` instead of `this.rpm = rpm;`, and its parameter is also named `rpm`. What happens?
- It does not compile
+ The parameter is assigned to itself and the field keeps its default value, 0.0
- The field is set correctly, because Java understands what you meant
- The program throws a NullPointerException
> Inside the constructor, the plain name `rpm` refers to the parameter. Only `this.rpm` refers to the field.

?tf For two objects created separately with identical values, `x == y` is `true`.
= false
> `==` compares references. Two separately created objects are different objects even if their values match.

? Why can `SwerveStates/Setpoints` and `SwerveStates/SetpointsOptimized` show different module angles when both log calls pass the same array?
+ `Module.runSetpoint` mutates each state object between the two log calls
- The logger copies the array differently each time
- The second call reads from the motors instead
- Arrays are passed by value
> The array holds references to state objects, and `runSetpoint` changes those same objects.
:::
