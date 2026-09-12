---
summary: Store many values with arrays, lists, maps, and queues; pick the right one for robot data; and avoid the classic collection mistakes.
objectives:
  - Create and use arrays, including arrays of objects like our four swerve modules
  - Use List, Map, and Queue from java.util, and explain wrapper types like Double
  - Choose an appropriate collection for a job and avoid common pitfalls
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
---

## Arrays: a fixed row of boxes

An **array** holds a fixed number of values of one type, numbered from **0**.

```java
double[] currents = new double[4];     // four doubles, all 0.0 to start
currents[0] = 12.5;                    // set the first
double first = currents[0];            // read the first
int howMany = currents.length;         // 4
```

You can also fill an array when you create it. The drive stores each module's position relative to the robot center:

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final Translation2d[] moduleTranslations" lines=7

The **order is a contract**: index 0 is front left, 1 front right, 2 back left, 3 back right. Every array about modules in `Drive` must use that same order, or the wrong corner gets the wrong command. That is why `Drive` notes `// FL, FR, BL, BR` next to `new Module[4]`.

Arrays of objects start full of `null` until you fill them. `new Module[4]` creates four empty slots; the constructor then assigns `modules[0] = new Module(flModuleIO, 0);` and so on.

:::warning Arrays cannot grow
Once created, an array's length is fixed. Need a collection that grows? Use a `List`.
:::

## Lists: arrays that grow

An `ArrayList` grows as you add. Vision collects the pose measurements it accepts each loop:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="private List<Pose3d> acceptedPoses;" lines=4

Later the constructor creates them with `acceptedPoses = new ArrayList<>();`. The key methods:

| Method | Does |
|---|---|
| `list.add(x)` | Appends `x` to the end |
| `list.get(i)` | Returns the item at index `i` |
| `list.size()` | How many items |
| `list.isEmpty()` | True when size is 0 |
| `list.clear()` | Removes everything |
| `list.remove(i)` | Removes the item at index `i` |

The type in angle brackets, `List<Pose3d>`, says what the list holds. Declaring the variable as `List` and creating an `ArrayList` means you could switch implementations later without changing the rest of the code.

### Wrapper types: `Double` versus `double`

Collections can only hold **objects**, not primitives. Java provides a wrapper class for each primitive: `Double` for `double`, `Integer` for `int`, `Boolean` for `boolean`. Conversion is automatic, which is called **autoboxing**:

```java
List<Double> samples = new ArrayList<>();
samples.add(3.5);             // the double 3.5 is boxed into a Double
double first = samples.get(0); // unboxed back to double
```

Boxing creates small objects. That is fine for most code, but not free inside a loop that runs 100 times a second. [Loop Performance](course:15-quality/loop-performance) looks at when it matters.

## Maps: look things up by key

A **Map** stores key→value pairs. You look up a value by its key instead of by position.

```java
Map<Integer, String> canNames = new HashMap<>();
canNames.put(12, "Turret");
canNames.put(9, "Flywheel leader");
String name = canNames.get(12);          // "Turret"
boolean hasHood = canNames.containsKey(11); // false
```

- **`HashMap`** is fast and keeps no particular order.
- **`TreeMap`** keeps its keys **sorted**, and can find the nearest keys below or above any value.

That second ability is exactly what a shot map needs. The turret's shot map is WPILib's `InterpolatingTreeMap`, which is built on a tree map. Keys are distances in meters, and values are setpoints:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="shooterShootingMap.put(1.03" lines=11

For a distance of 2.19 m, which is not a key, the map finds the neighbors 2.0 and 2.38 and blends their setpoints. [Unit 14](course:14-shooting/shot-maps) builds one yourself.

## Queues: first in, first out

A **Queue** hands items back in the order they were added. The odometry thread samples encoders 100 times per second and puts each reading in a queue, and the main loop later drains the queue:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="public Queue<Double> registerSignal(SparkBase spark, DoubleSupplier signal)" lines=12

`ArrayBlockingQueue<>(20)` holds at most 20 readings and is safe to use from two threads at once. [Threads, Notifiers, and Locks](course:05-java-advanced/threads-and-locks) explains the lock around it.

## Choosing a collection

| You need | Use |
|---|---|
| A fixed number of items, like 4 modules | Array |
| A growing list in order | `ArrayList` |
| Lookup by key, order unimportant | `HashMap` |
| Lookup by key with sorted keys or nearest-key search | `TreeMap` |
| Items processed in arrival order | `Queue` (`ArrayDeque`, or `ArrayBlockingQueue` across threads) |

## Collection pitfalls

- **Index out of bounds.** `list.get(list.size())` is one past the end and throws an exception.
- **Removing while looping** with for-each throws `ConcurrentModificationException`. Collect what to remove, then remove after the loop, or use an iterator.
- **Missing keys return null.** `map.get(99)` gives `null` if 99 is not a key, and using that `null` crashes.
- **Comparing wrappers with `==`.** Two `Integer` objects holding 1000 can be different objects. Use `.equals()` to compare their values.

:::exercise id="u03-canmap"
Build a CAN ID map that the build team could actually use while wiring.

- Store devices by ID, and refuse IDs outside 1–62 or already in use.
- `byController` and `ids` must come back sorted by ID.
- `lowestFreeId` returns the first open ID, or -1 when the bus is full.
- `robot2026()` recreates our robot's real map. The table is in [Meet the 2026 Robot](course:00-welcome/meet-the-2026-robot).

`CanDevice` is provided. Read it before you start.
---hint
A `TreeMap<Integer, CanDevice>` keeps keys sorted. Looping over `map.values()` or `map.keySet()` then gives devices and IDs in ID order.
---hint
Check the range and `containsKey` **before** calling `put`, and return `false` if either check fails.
---hint
For `ids()`, create `new int[devices.size()]` and fill it while looping over `keySet()`.
:::

:::quiz
? The drive's module arrays use the order FL, FR, BL, BR. What happens if one array is written in a different order?
+ Commands and measurements get matched to the wrong corners of the robot
- Java sorts the arrays automatically
- The code does not compile
- Nothing, because modules are identified by name
> Array positions are just numbers. Nothing stops mismatched orders except careful, consistent code.

?code What does this print?
```java
List<String> cameras = new ArrayList<>();
cameras.add("April_Tag_1");
cameras.add("April_Tag_2");
System.out.println(cameras.size() + " " + cameras.get(1));
```
= 2 April_Tag_2
> Two items were added, and index 1 is the second one.

? Why does `List<Double>` use `Double` instead of `double`?
- `Double` is more precise
+ Collections can only hold objects, so primitives are boxed into wrapper objects
- `double` is deprecated
- It makes the list thread-safe
> Autoboxing converts between `double` and `Double` automatically.

? Which collection lets the shot map find the two known distances on either side of 2.19 m?
- `HashMap`
- `ArrayList`
+ A tree map with sorted keys
- `ArrayBlockingQueue`
> Sorted keys make "nearest below" and "nearest above" lookups fast. WPILib's `InterpolatingTreeMap` is built on that.

?tf `map.get(key)` throws an exception when the key is missing.
= false
> It returns `null`. The crash comes later, if code uses that `null` as an object.
:::
