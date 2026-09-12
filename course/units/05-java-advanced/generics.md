---
summary: Read and write generic types, from List<Pose3d> and Supplier<Pose2d> to the bounded type behind our shot map, and build a reusable interpolating table.
objectives:
  - Read generic types in our code, including nested and bounded ones
  - Write a generic class and a generic method
  - Explain type bounds like V extends Interpolatable<V>, and why primitives need wrapper types
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## Types with parameters

A **generic** type takes other types as parameters, written in angle brackets. You have been using them since Unit 3:

| Type | Reads as |
|---|---|
| `List<Pose3d>` | a list of `Pose3d` |
| `Map<Integer, CanDevice>` | a map from `Integer` keys to `CanDevice` values |
| `Supplier<Pose2d>` | something that supplies a `Pose2d` |
| `Optional<Alliance>` | maybe an `Alliance` |
| `Queue<Double>` | a queue of `Double` |

Generics let one class, like `ArrayList`, work for every element type while the compiler still checks types. Adding a `String` to a `List<Pose3d>` is a compile error, not a crash at a competition.

Our odometry thread keeps parallel lists of different generic types:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="private final List<SparkBase> sparks" lines=6

`List<Queue<Double>>` is a list whose items are queues of `Double`. Read nested generics from the outside in.

## Primitives and wrapper types

Type parameters must be **object types**. `List<double>` does not compile, so use `List<Double>`. Java boxes and unboxes automatically, but each boxed `Double` is a small object. In code that runs hundreds of times per second, arrays of primitives like `double[]` avoid that cost.

## The diamond

When the type is obvious from the variable, write empty angle brackets on the right and let Java fill them in:

```java
List<Pose3d> acceptedPoses = new ArrayList<>();   // same as new ArrayList<Pose3d>()
```

## Writing a generic class

A class can declare its own type parameter. By convention single capital letters are used: `T` for type, `K` and `V` for key and value.

```java
/** Holds the last value seen and whether it has changed. */
public class Latest<T> {
    private T value;
    private boolean changed;

    public void offer(T newValue) {
        changed = !java.util.Objects.equals(value, newValue);
        value = newValue;
    }

    public T get() { return value; }
    public boolean changed() { return changed; }
}

Latest<Alliance> alliance = new Latest<>();
Latest<String> gameData = new Latest<>();
```

One class, fully type-checked for every use.

## Generic methods

A single method can be generic too. The type parameter goes before the return type:

```java
public static <T> T firstOrDefault(List<T> list, T fallback) {
    return list.isEmpty() ? fallback : list.get(0);
}

Pose3d pose = firstOrDefault(acceptedPoses, new Pose3d());   // T is inferred as Pose3d
```

## Bounded types: our shot map

Sometimes a generic type needs to *do* something with its values, so it restricts which types are allowed. That is a **bound**.

WPILib's interpolating map needs to blend values between keys, so values must implement `Interpolatable`. Our `ShooterSetpoint` makes that promise:

::source file="src/main/java/frc/robot/subsystems/turret/ShooterSetpoint.java" from="public class ShooterSetpoint implements" lines=18

`implements Interpolatable<ShooterSetpoint>` reads: "a `ShooterSetpoint` knows how to interpolate toward another `ShooterSetpoint`." A declaration like `class Table<V extends Interpolatable<V>>` means "`V` can be any type that can interpolate with its own type." That looks circular, but it is exactly the promise a lookup table needs.

The turret creates its map with two type arguments and two functions:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private final InterpolatingTreeMap<Double, ShooterSetpoint> shooterShootingMap" lines=9

- `InverseInterpolator.forDouble()` answers "how far between two keys is this distance?"
- The lambda `(start, end, t) -> new ShooterSetpoint(...)` answers "how do I blend two setpoints?"

Notice that this lambda repeats the blending math already written in `ShooterSetpoint.interpolate`. Passing `ShooterSetpoint::interpolate` would avoid the duplication, which is a small cleanup worth making.

## Types as compile-time checks

Generics can even encode sizes. Vision measurement uncertainty is a 3 × 1 matrix:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void addVisionMeasurement(" lines=6

`Matrix<N3, N1>` means three rows and one column. `N3` and `N1` are marker types that exist only to be checked by the compiler, so passing a 2 × 1 matrix would be a compile error.

## Type erasure, briefly

Java checks generic types at compile time and then **erases** them. At runtime a `List<Pose3d>` is just a `List`. Two consequences you may notice:

- You cannot write `new T()` or `if (x instanceof List<Pose3d>)`.
- You cannot overload two methods that differ only by generic parameter, like `process(List<Double>)` and `process(List<String>)`.

:::exercise id="u05-interp"
Build your own generic interpolating table, the core of a shot map.

- `Blendable<T>` is provided. It works like WPILib's `Interpolatable`.
- Implement `Shot.blend`, a record that blends RPM and hood angle.
- Implement `InterpolatingTable<V extends Blendable<V>>` on top of a `TreeMap<Double, V>`:
  - an exact key returns its value,
  - a key between two others blends them by how far between they are,
  - a key outside the table returns the nearest end, and
  - an empty table returns `null`.

The tests load the team's real 2026 shooting map, and also reuse your table for a completely different type to prove it is truly generic.
---hint
`TreeMap` has `floorEntry(key)` (the largest key at or below) and `ceilingEntry(key)` (the smallest key at or above). Either may be `null` at the ends of the table.
---hint
When both neighbors exist and differ, `t = (key - lowerKey) / (upperKey - lowerKey)`, then return `lower.blend(upper, t)`.
:::

:::quiz
? What does `Map<Integer, List<CanDevice>>` hold?
+ A map from integer keys to lists of CAN devices
- A list of integers and devices
- A single CAN device with an integer ID
- A map from lists to integers
> Read generics from the outside in: a `Map` whose keys are `Integer` and whose values are `List<CanDevice>`.

?tf `List<double> samples = new ArrayList<>();` compiles.
= false
> Type parameters must be object types. Use `List<Double>`, or an array `double[]` for performance.

? What does the bound in `class InterpolatingTable<V extends Blendable<V>>` guarantee?
+ Every value type used with the table can blend with another value of the same type
- Values must be subclasses of `InterpolatingTable`
- Values must be doubles
- The table can only hold one value
> The bound lets the table call `blend` on its values safely.

? What does `Matrix<N3, N1>` tell the compiler about vision standard deviations?
+ They are a 3-row, 1-column matrix, so a different size is a compile error
- They are three separate numbers stored as strings
- The matrix is optional
- It has 3 values that must all be 1
> `N3` and `N1` are marker types that encode dimensions in the type.

?code What does this print?
```java
static <T> T firstOrDefault(List<T> list, T fallback) {
    return list.isEmpty() ? fallback : list.get(0);
}
// ...
System.out.println(firstOrDefault(List.of("April_Tag_1", "April_Tag_2"), "none")
    + " " + firstOrDefault(new ArrayList<String>(), "none"));
```
= April_Tag_1 none
> The first list has items, so its first item is returned. The second is empty, so the fallback is returned.
:::
