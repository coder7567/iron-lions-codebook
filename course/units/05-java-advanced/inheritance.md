---
summary: Reuse and specialize code with extends, override methods, call super, and know when inheritance is the wrong tool, using our Robot, subsystems, and simulation IO classes as examples.
objectives:
  - Extend a class, override its methods with @Override, and call super
  - Explain how WPILib calls your overridden methods (Robot, SubsystemBase)
  - Apply the "is-a" test and recognize when composition or interfaces fit better
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSim.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## Extending a class

**Inheritance** lets a class start from another class and add to it or change it. The new class **extends** the old one:

- the original is the **superclass** (or parent),
- the new one is the **subclass** (or child).

The subclass automatically has the superclass's public and protected methods and fields, and can **override** methods to change what they do.

## The robot program is a subclass

Our `Robot` class does not write the 20 ms loop, the mode switching, or the logging hooks. It inherits all of that:

::source file="src/main/java/frc/robot/Robot.java" from="public class Robot extends LoggedRobot" lines=4

`LoggedRobot` comes from AdvantageKit and builds on WPILib's robot base classes. It already knows *when* to call `robotPeriodic()`, `autonomousInit()`, and the rest. `Robot` just **overrides** those methods to say *what* happens:

::source file="src/main/java/frc/robot/Robot.java" from="public void robotPeriodic()" lines=15

This is inheritance's best use: the framework owns the structure, and your subclass fills in the specifics. Every subsystem works the same way. `Superstructure extends SubsystemBase` and overrides `periodic()`, and WPILib's scheduler calls it every loop.

## @Override

`@Override` tells the compiler, "this method is meant to replace one from the superclass." If you misspell the name or get the parameters wrong, the compiler reports an error:

```java
@Override
public void robotPeriodc() { }   // error: method does not override or implement a method from a supertype
```

Without `@Override`, that typo would compile as a brand-new method that WPILib never calls, and your robot code would silently never run. **Always write `@Override`.**

## super: reaching the parent

Inside a subclass, `super` refers to the superclass's version of things:

- `super(...)` in a constructor runs the parent's constructor. It must be the first line.
- `super.someMethod()` calls the parent's version of a method you overrode.

Our turret simulation IO is a subclass of the real Spark IO:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSim.java" from="public class TurretIOSim extends TurretIOSpark" lines=32

- `super()` runs `TurretIOSpark`'s constructor.
- It overrides `updateInputs` and `setFlyWheelSpeed` to fake the hardware.
- It reads fields like `flywheelSetSpeed` and `hoodSetAngle` directly. Those fields are **`protected`** in `TurretIOSpark`, which is exactly what `protected` is for: visible to subclasses.

## The "is-a" test, and where this goes wrong

Inheritance should model an **is-a** relationship. A `Superstructure` *is a* subsystem. Is a simulated turret IO *a* Spark turret IO?

Look at what `super()` does: `TurretIOSpark`'s constructor creates real `SparkFlex` objects on CAN IDs 9 through 12 and configures them. So every simulation run constructs "real" motor controller objects just to inherit some fields. The simulation also inherits every method it does *not* override. Calling `setHoodAngle` in simulation, for example, runs the real Spark code path. That coupling is finding **F7** in the [Code Audit](course:reference/code-audit).

It also explains finding **F5**. Because the simulation class re-implements `updateInputs` by copying and adjusting logic, the copies drifted apart: the real class computes `intakeSafe` with `<` and the simulation with `>`.

:::tip Prefer interfaces and composition
When two classes share a *contract* but not an identity, have both implement an **interface**. When one class needs another's abilities, **hold** an object of that class instead of extending it. The [next lesson](course:05-java-advanced/interfaces-and-polymorphism) shows the interface approach, which the drive subsystem already uses: `ModuleIOSim` implements `ModuleIO` directly and never extends `ModuleIOSpark`.
:::

## Every class extends Object

If a class does not say `extends`, it extends `java.lang.Object`. That is where `toString()`, `equals()`, and `hashCode()` come from, and why you can override them in any class.

## Stopping inheritance

- A **`final` class** cannot be extended: `public final class Main`.
- A **`final` method** cannot be overridden.
- A **`private` method** is not inherited at all.

## Abstract classes

An **abstract class** cannot be created with `new`. It exists to be extended, and may declare **abstract methods** with no body that every subclass must implement. WPILib's `Command` works much like this: subclasses and command factories supply the behavior. Interfaces cover most of the same needs with fewer restrictions, so you will write abstract classes rarely.

:::quiz
? Why does `Robot` override `robotPeriodic()` instead of writing its own loop?
+ `LoggedRobot` and WPILib already run the loop and call the overridden methods at the right times
- Java does not allow loops in robot code
- `robotPeriodic()` is a keyword
- Overriding makes the code faster
> The framework owns the timing and structure, and the subclass supplies the behavior.

? What happens if you misspell `periodic` as `periodc` but include `@Override`?
+ The compiler reports an error, because nothing is being overridden
- The method runs normally
- WPILib calls it anyway
- The robot crashes at runtime
> That is the point of `@Override`. Without it, the typo would create a method nobody ever calls.

? `TurretIOSim` reads `flywheelSetSpeed`, a field declared in `TurretIOSpark`. Which access modifier makes that possible without making it public?
- `private`
+ `protected`
- `final`
- `static`
> Protected members are visible to subclasses (and to classes in the same package).

? Why is `TurretIOSim extends TurretIOSpark` a questionable design?
+ A simulated IO is not really a Spark IO, and `super()` creates and configures real Spark objects in simulation
- Subclasses cannot override methods
- Simulation classes must be final
- Java forbids extending classes from another package
> Inheriting to reuse fields ties simulation to real hardware code. Implementing a shared interface avoids that.

?tf A class that does not write `extends` still inherits `toString()` from `Object`.
= true
> Every Java class ultimately extends `java.lang.Object`.
:::
