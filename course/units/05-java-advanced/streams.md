---
summary: Read and write the stream pipelines our IO code uses to turn odometry queues into arrays, and know when a plain loop is the better choice.
objectives:
  - Build a stream pipeline from a source, intermediate operations, and a terminal operation
  - Use mapToDouble, map, filter, sorted, toArray, collect, count, and average
  - Choose between streams and loops for readability and performance
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/drive/GyroIONavX.java
---

## Pipelines instead of loops

A **stream** processes a sequence of values as a pipeline:

1. a **source**, such as a list, an array, or a queue,
2. zero or more **intermediate operations** that transform the stream, such as `filter`, `map`, or `sorted`, and
3. one **terminal operation** that produces a result, such as `toArray`, `collect`, `count`, or `average`.

```java
List<String> flexNames = devices.stream()          // source
    .filter(d -> d.getController().equals("SparkFlex"))  // keep some
    .map(CanDevice::getName)                        // transform each
    .sorted()                                       // order them
    .collect(Collectors.toList());                  // terminal: build a list
```

Nothing happens until the terminal operation runs. Then each value flows through the pipeline in order.

## Where our code uses streams

Each loop, a swerve module turns the samples its odometry thread collected into arrays for logging and pose estimation:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="// Update odometry inputs" lines=11

Read the first statement as a pipeline:

- `timestampQueue.stream()` is the source, a `Stream<Double>`.
- `.mapToDouble((Double value) -> value)` unboxes each `Double` into a primitive `double`, producing a `DoubleStream`.
- `.toArray()` is the terminal operation, producing a `double[]`.

The turn positions use `.map(...)` to build a `Rotation2d` from each raw value, then `.toArray(Rotation2d[]::new)`. That argument is a constructor reference for "make a `Rotation2d` array of the right size."

The gyro does the same with its yaw samples, negating each angle because the NavX measures clockwise-positive:

::source file="src/main/java/frc/robot/subsystems/drive/GyroIONavX.java" from="inputs.odometryYawTimestamps =" lines=8

## Operations worth knowing

| Operation | Kind | Does |
|---|---|---|
| `filter(predicate)` | intermediate | Keeps values where the predicate is true |
| `map(function)` | intermediate | Replaces each value with the function's result |
| `mapToDouble(function)` | intermediate | Like `map`, producing a `DoubleStream` |
| `sorted()` | intermediate | Orders values |
| `limit(n)` | intermediate | Keeps the first n values |
| `toArray()` | terminal | Array of the results |
| `collect(Collectors.toList())` or `toList()` | terminal | A `List` of the results |
| `count()` | terminal | How many values, as a `long` |
| `average()`, `sum()`, `max()` | terminal (DoubleStream) | Numeric summaries; `average()` returns an `OptionalDouble` |
| `anyMatch(predicate)` | terminal | Whether any value matches |

`Arrays.stream(doubleArray)` makes a `DoubleStream` from an array.

## Rules that avoid surprises

- **A stream can be used only once.** Calling a second terminal operation on the same stream throws an exception. Build a new stream instead.
- **Do not change outside state from inside a stream.** Lambdas in `map` and `filter` should compute and return values, not add to lists or change fields.
- **`average()` on an empty stream is empty.** Use `.orElse(0.0)` for a default.
- **`toList()` returns an unmodifiable list.** Use `collect(Collectors.toList())` if you need to add to it later.

## Streams or loops?

Streams read well for "filter, transform, collect" steps like the ones above. A plain `for` loop is clearer when you need an index, need to update several things at once, or need to stop early. Our `Drive` odometry code uses nested loops for exactly those reasons.

:::tip Performance in the robot loop
Streams create a few small objects each time they run. At 50 Hz with a handful of samples, that cost is tiny, and `ModuleIOSpark` uses streams happily. In code that processes thousands of values per loop, measure before assuming either choice is faster. [Loop Performance and Allocation](course:15-quality/loop-performance) shows how to measure.
:::

:::exercise id="u05-streams"
Write each method as one stream pipeline.

- `drain` copies a queue into an array and clears it, like `ModuleIOSpark`.
- `rotationsToWheelRadians` maps every value with the drive's conversion.
- `namesOfController` chains `filter`, `map`, and `sorted`.
- `averageAbove` filters a `DoubleStream`, then averages with a default.
- `countMultiTag` counts records by one of their accessors.
---hint
For a `Queue<Double>`: `queue.stream().mapToDouble(Double::doubleValue).toArray()`, then `queue.clear()`.
---hint
`Arrays.stream(values).filter(v -> v > threshold).average().orElse(0.0)` handles the empty case in one line.
:::

:::quiz
?order Put the parts of a stream pipeline in order.
1. Source, like `list.stream()`
2. Intermediate operations, like `filter` and `map`
3. Terminal operation, like `toArray()`
> Nothing runs until the terminal operation asks for a result.

? In `timestampQueue.stream().mapToDouble((Double value) -> value).toArray()`, what does `mapToDouble` do?
+ Unboxes each `Double` object into a primitive `double`, so `toArray()` can return `double[]`
- Doubles every value
- Sorts the timestamps
- Removes duplicate timestamps
> `mapToDouble` turns a `Stream<Double>` into a `DoubleStream` of primitives.

?code What does this print?
```java
double avg = java.util.Arrays.stream(new double[] {30, 40, 50, 10})
    .filter(v -> v > 35)
    .average()
    .orElse(0.0);
System.out.println(avg);
```
= 45.0
> Only 40 and 50 pass the filter, and their average is 45.0.

?tf You can call two terminal operations on the same stream object, such as `count()` and then `toArray()`.
= false
> A stream is consumed by its first terminal operation. Create a new stream for another result.

? When is a plain `for` loop usually clearer than a stream?
+ When you need an index, update several things at once, or stop early
- Whenever you process a list
- Never; streams are always clearer
- Only when the list is empty
> Streams shine for simple filter-map-collect steps. Loops are better for complex, stateful work.
:::
