---
summary: Define contracts with interfaces, write default methods, create anonymous implementations, and use polymorphism, the idea that lets one Drive class run real hardware, simulation, and log replay.
objectives:
  - Declare and implement interfaces, including default methods and nested classes
  - Explain polymorphism using ModuleIOSpark, ModuleIOSim, and new ModuleIO() {}
  - Design a subsystem that depends on an interface so it can be tested with a fake
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIO.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## An interface is a contract

An **interface** lists methods a class promises to provide, without saying how. A class **implements** the interface by providing those methods:

```java
public interface GyroIO {
    void updateInputs(GyroIOInputs inputs);   // "anything that is a GyroIO can do this"
}

public class GyroIONavX implements GyroIO {
    @Override
    public void updateInputs(GyroIOInputs inputs) {
        inputs.connected = navX.isConnected();   // how the NavX does it
    }
}
```

A class can extend only one superclass, but it can implement **many** interfaces.

## Our IO interfaces

Every hardware-facing piece of Rebuilt-2026 is described by an interface. Here is the swerve module's:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIO.java" from="public interface ModuleIO" lines=39

Three things to notice:

1. **A class inside the interface.** `ModuleIOInputs` holds every reading the module needs. The `@AutoLog` annotation makes AdvantageKit generate logging code for it ([Unit 5.8](course:05-java-advanced/annotations-and-generated-code)).
2. **Default methods.** Every method has a `default` body, which here does nothing. Implementations override only what they support.
3. **No hardware anywhere.** The interface does not mention SPARK MAX, NEO, or CAN. It only describes what a module can report and do.

## Polymorphism: one Drive, three worlds

**Polymorphism** means code written against an interface works with *any* object that implements it. `Drive` takes five IO objects and never asks which kind they are:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public Drive(" lines=11

`RobotContainer` decides what to pass in:

::source file="src/main/java/frc/robot/RobotContainer.java" from="switch (Constants.currentMode) {" lines=59

| Mode | Gyro | Modules | What happens when Drive calls `updateInputs` |
|---|---|---|---|
| `REAL` | `GyroIONavX` | `ModuleIOSpark` | Reads real encoders over CAN |
| `SIM` | `new GyroIO() {}` | `ModuleIOSim` | Runs a physics model |
| `REPLAY` | `new GyroIO() {}` | `new ModuleIO() {}` | Does nothing; AdvantageKit fills inputs from a log file |

The `Drive` class is identical in all three. At runtime Java calls the right `updateInputs` for whichever object it was given. This is called **dynamic dispatch**.

### Anonymous classes

`new ModuleIO() {}` creates an object of an **anonymous class**: a class with no name that implements `ModuleIO` right there, with an empty body. Because every method in `ModuleIO` has a default, the empty class is complete and every call quietly does nothing. That is exactly what replay needs.

Notice that simulation uses `new GyroIO() {}` too. There is no simulated gyro. When the gyro reports disconnected, `Drive` estimates rotation from the wheels, which works well enough in simulation.

## Interfaces from libraries

You implement library interfaces too. `ShooterSetpoint implements Interpolatable<ShooterSetpoint>`, which promises WPILib an `interpolate` method. WPILib's interpolating map can then blend setpoints without knowing anything about shooters.

## Designing with interfaces

The IO pattern gives three superpowers:

- **Simulation:** swap in a physics implementation.
- **Replay:** swap in a do-nothing implementation and feed recorded inputs.
- **Testing:** swap in a **fake** that returns whatever the test wants and records what the subsystem commanded.

That third one is what this lesson's exercise is about. A fake lets a JUnit test ask, "When the flywheel reads 3000 RPM and the target is 3000, what voltage does the subsystem command?" No robot, no simulator, no waiting.

:::tip Rules of thumb
- Depend on **interfaces**, not concrete hardware classes.
- Keep interfaces **small and focused** on what the subsystem needs.
- Put hardware details (CAN IDs, configs) only in the implementation, like `ModuleIOSpark`.
:::

:::exercise id="u05-io"
Build both sides of an IO boundary.

**Part 1: `FlywheelIOFake`.** Implement `FlywheelIO` with fields a test can set (measured RPM, current, connected) and read (last voltage, number of commands). Clamp voltages to ±12.

**Part 2: `Flywheel`.** Depend only on `FlywheelIO`. Each `periodic()` must read inputs **first**, then either stop (target 0 or disconnected) or command the feedforward-plus-P voltage using the real 2026 flywheel gains.

The final test passes `new FlywheelIO() {}`, exactly like our replay IO, to prove your subsystem works with any implementation.
---hint
In `periodic()`, call `io.updateInputs(inputs)` before using `inputs.velocityRpm`. Otherwise you are controlling with last loop's reading.
---hint
`io.stop()` is a default method that calls `setVoltage(0.0)`, so calling it counts as a voltage command in the fake.
---hint
At 3000 RPM from rest: 0.2 + 0.00193 × 3000 + 0.001 × 3000 = 0.2 + 5.79 + 3.0 = 8.99 V.
:::

:::quiz
? What does `new ModuleIO() {}` create?
- A compile error, because interfaces cannot be instantiated
+ An object of an anonymous class implementing `ModuleIO` using its default methods
- A simulated swerve module with physics
- A copy of `ModuleIOSpark`
> Because every `ModuleIO` method has a default body, an empty anonymous class is a complete implementation.

? Why can `Drive` run unchanged on the robot, in simulation, and in replay?
+ It depends only on the `GyroIO` and `ModuleIO` interfaces, and `RobotContainer` passes in different implementations
- It checks `Constants.currentMode` inside every method
- WPILib rewrites it at build time
- Simulation and replay use a different `Drive` class
> Polymorphism: the same code calls whichever implementation it was given.

?tf A Java class can implement more than one interface.
= true
> A class extends at most one superclass but can implement many interfaces.

? Which implementation makes `Drive.periodic()` read real encoder values?
- `ModuleIOSim`
- `new ModuleIO() {}`
+ `ModuleIOSpark`
- `ModuleIOInputs`
> `ModuleIOSpark` talks to the SPARK MAX controllers over CAN.

? What is the main benefit of testing a subsystem with a fake IO?
+ The test controls exactly what the "sensors" read and checks what was commanded, with no robot or simulator
- Fakes make the robot faster
- Fakes replace the need for real hardware at competitions
- JUnit requires fakes
> Fakes turn hardware behavior into ordinary, repeatable method calls.
:::
