---
summary: Model plain data with records, validate it in compact constructors, and organize helper types as nested classes, the way our vision and drive code do.
objectives:
  - Declare records, use their generated accessors and equality, and validate them in compact constructors
  - Explain the difference between static nested classes, inner classes, and anonymous classes
  - Decide when data belongs in a record and when it needs a regular class
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagIO.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/commands/DriveCommands.java
---

## Records: data without boilerplate

Many classes exist just to carry a few values together: a tag ID with its angle and distance, or a pose with a timestamp. Writing fields, a constructor, getters, `equals`, `hashCode`, and `toString` for each is tedious and easy to get subtly wrong.

A **record** declares all of that in one line. Our vision code uses three:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIO.java" from="public enum PoseTypes" lines=8

From `record PoseObservation(double ambiguity, Pose3d pose, double timestamp, double avgTagDistance, int tagCount)`, Java generates:

- a constructor taking all five values in order,
- a `private final` field for each,
- **accessors named after the components**: `ambiguity()`, `pose()`, `timestamp()` (no `get` prefix),
- `equals` and `hashCode` that compare values, and
- a readable `toString`, like `PoseObservation[ambiguity=0.05, pose=..., ...]`.

Records are **immutable**. Once created, their values cannot change.

## Using records

Vision reads observations through their accessors while deciding which to keep:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="rejectPose =" lines=9

Because records compare by value, two observations with identical components are `equals`, which makes records easy to use in tests and collections.

## Compact constructors: validation

A record can check its values in a **compact constructor**, which runs before the fields are assigned:

```java
public record TargetInfo(int tagID, double targetYaw, double targetPitch, double distanceToTarget) {
    public TargetInfo {
        if (tagID < 1 || tagID > 32) {
            throw new IllegalArgumentException("2026 tag IDs are 1-32, got " + tagID);
        }
    }
}
```

Now an impossible `TargetInfo` can never exist anywhere in the program.

## Records can do more

Records can have methods, static factory methods, and they can implement interfaces:

```java
public record Shot(double rpm, double hoodAngle) implements Interpolatable<Shot> {
    public static final Shot STOWED = new Shot(0, 0.907);

    public Shot withRpm(double newRpm) {            // "change" by returning a copy
        return new Shot(newRpm, hoodAngle);
    }

    @Override
    public Shot interpolate(Shot end, double t) {
        return new Shot(rpm + (end.rpm - rpm) * t, hoodAngle + (end.hoodAngle - hoodAngle) * t);
    }
}
```

What records cannot do: extend another class, or add non-static fields beyond their components.

:::info Records in AdvantageKit logs
AdvantageKit can log records inside `@AutoLog` inputs. `AprilTagIOInputs` holds `PoseObservation[] poseObservations`, so every vision observation, with every component, is recorded in the log and can be replayed.
:::

## Nested classes

A class declared inside another class is a **nested class**. It keeps a helper type next to the only code that uses it.

### Static nested classes

A `static` nested class is an ordinary class that just lives inside another class's namespace. The wheel radius characterization keeps its scratch data in one:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="private static class WheelRadiusCharacterizationState" lines=5

It is `private`, so only `DriveCommands` can use it. It exists purely so lambdas can share changing values.

`LocalADStarAK` does the same thing for its logged pathfinding data:

::source file="src/main/java/frc/robot/util/LocalADStarAK.java" from="private static class ADStarIO implements LoggableInputs" lines=4

Classes declared inside an **interface** are automatically `static`. That is why `ModuleIO.ModuleIOInputs` works without the word `static`. Enums nested in classes, like `Superstructure.WantedState`, are static too.

### Inner classes

A nested class **without** `static` is an **inner class**. Each inner object is tied to an object of the outer class and can read its fields directly. They are handy but easy to misuse, and our robot code does not use any. Prefer `static` nested classes unless you truly need the outer object.

### Anonymous classes

You met these in [Interfaces and Polymorphism](course:05-java-advanced/interfaces-and-polymorphism): `new ModuleIO() {}` defines and creates a nameless class in one expression. For interfaces with one method, a **lambda** is usually clearer.

## Record or class?

| Choose a record when | Choose a class when |
|---|---|
| The type is a bundle of values | The object has changing state, like `jamCount` |
| Equality should mean "same values" | Identity matters, like one specific subsystem |
| Immutability is a benefit | It controls hardware or runs every loop |
| Examples: `TargetInfo`, a shot setpoint, a pose observation | Examples: `Intake`, `Drive`, `ModuleIOSpark` |

:::exercise id="u05-records"
Model vision measurements as a record and work with lists of them.

- In `PoseObservation`, validate `tagCount` and `ambiguity` in the compact constructor, then add `isMultiTag`, `distanceTo`, and `withTimestamp`.
- In `Observations`, `filter` keeps on-field, low-ambiguity observations in their original order, like the first part of our vision filter, and `newest` returns the latest one as an `Optional`.
---hint
A compact constructor has no parameter list: `public PoseObservation { ... }`. Inside it, refer to the components by name, like `tagCount`.
---hint
`withTimestamp` builds a new record: `new PoseObservation(x, y, headingRadians, newTimestampSeconds, ambiguity, tagCount, avgTagDistanceMeters)`.
---hint
For `filter`, create a new `ArrayList`, loop over the input, and `add` the ones that pass. Never remove from the list you were given.
:::

:::quiz
? For `record TargetInfo(int tagID, double targetYaw, double targetPitch, double distanceToTarget)`, how do you read the tag ID?
- `info.getTagID()`
+ `info.tagID()`
- `info.tagID`
- `TargetInfo.tagID(info)`
> Record accessors are named exactly like their components, without a `get` prefix.

?code What does this print?
```java
record Shot(double rpm, double hood) {}
// ...
Shot a = new Shot(2000, 0.88);
Shot b = new Shot(2000, 0.88);
System.out.println(a.equals(b) + " " + (a == b));
```
= true false
> Records compare by value with `equals`, but `a` and `b` are still two different objects.

? Where should a record check that `tagCount` is at least 1?
+ In a compact constructor, which runs before the fields are assigned
- In a setter method
- In `toString`
- Records cannot validate their values
> Compact constructors guarantee no invalid record can ever be created.

?tf Classes declared inside an interface, like `ModuleIO.ModuleIOInputs`, are automatically static.
= true
> Nested types in interfaces are implicitly static, so they do not need an instance of anything.

? Which type is the best fit for a record?
- The `Intake` subsystem, which counts jam loops
- `ModuleIOSpark`, which configures motor controllers
+ A pose observation with ambiguity, timestamp, and tag count
- `SparkOdometryThread`, which runs a background thread
> Records suit immutable bundles of values, not objects with changing state or hardware.
:::
