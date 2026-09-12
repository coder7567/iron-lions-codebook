---
summary: Repeat work with while, for, and for-each loops; control them with break and continue; and keep every loop fast enough for a 20 ms robot cycle.
objectives:
  - Write while, for, and for-each loops and predict how many times each runs
  - Use break and continue, and build totals with accumulator variables
  - Explain why robot code must never wait inside a loop for something to happen
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/util/SparkUtil.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
---

## Why loops

Robots do the same work many times: four swerve modules, two cameras, dozens of odometry samples, a retry until a motor controller answers. A **loop** repeats a block of code so you write it once.

## The for loop

Use `for` when you know how many times to repeat. The drive sends a setpoint to each of its four modules:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Send setpoints to modules" lines=4

A `for` header has three parts, separated by semicolons:

```java
for (int i = 0; i < 4; i++) {
//   start      keep going  after each pass
//   here       while true
    modules[i].runSetpoint(setpointStates[i]);
}
```

`i` takes the values 0, 1, 2, and 3. When `i` becomes 4, `i < 4` is false and the loop ends. Arrays count from 0, so four modules are `modules[0]` through `modules[3]`.

:::warning Off-by-one errors
Writing `i <= 4` would try `modules[4]`, which does not exist, and throw an `ArrayIndexOutOfBoundsException`. That crashes the robot program. Loops over arrays almost always use `i < array.length`.
:::

## The for-each loop

When you just need every item, and not its index, **for-each** is shorter and cannot go out of bounds. Here the drive adds up current from every module:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public double getTotalCurrent()" lines=7

Read `for (var i : modules)` as "for each module `i` in `modules`." The name `i` is misleading here, because it holds a module, not an index. `module` would be a better name.

`totalCurrent` is an **accumulator**: start it at 0 before the loop and add to it on every pass.

## The while loop

Use `while` when you do not know in advance how many passes you need. It repeats **as long as** its condition is true:

```java
int attempts = 0;
while (attempts < 5 && !configured) {
    configured = tryToConfigure();
    attempts++;
}
```

If the condition is false at the start, the body never runs at all.

## break and continue

- **`break`** exits the loop immediately.
- **`continue`** skips the rest of *this* pass and moves to the next one.

`SparkUtil.tryUntilOk` retries a motor controller command up to five times and stops early the moment it succeeds:

::source file="src/main/java/frc/robot/util/SparkUtil.java" from="public static void tryUntilOk" lines=10

Vision uses `continue` to throw away bad measurements without nesting the rest of the loop inside an `if`:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="for (int cameraIndex = 0; cameraIndex < inputs.length; cameraIndex++) {" lines=15

For each camera, and each pose observation from that camera, the code checks several reasons to reject it. If any is true, `continue` jumps straight to the next observation.

## Nested loops

A loop inside a loop runs the inner loop completely for **every** pass of the outer loop. Odometry reads several samples per cycle, and each sample has data from four modules:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="int sampleCount = sampleTimestamps.length;" lines=14

With 2 samples and 4 modules, the inner body runs 2 × 4 = 8 times. [Unit 10](course:10-swerve/odometry) explains why the drive collects several samples per cycle.

## The loop you must never write

In a normal program, waiting in a loop is fine. **In robot code it is a disaster:**

```java
// NEVER do this inside periodic(), a command, or any robot code:
while (Math.abs(turret.getTurretAngle() - target) > 0.05) {
    // wait for the turret to arrive...
}
```

WPILib calls your code every 20 ms and expects it to finish quickly. While this loop spins, nothing else runs: the scheduler cannot update, sensors are not read, and the motor command that would move the turret never gets sent. The loop waits forever for something it is preventing. The Driver Station reports **loop overruns** and the robot stops responding.

:::tip The robot loop is your loop
Robot code gets repetition for free, because WPILib calls `periodic()` 50 times a second. Instead of waiting, **check once per cycle** and remember progress in a field, like `jamCount` in `Intake`. [Unit 6](course:06-robot-foundations/robot-lifecycle) and [Unit 8](course:08-state-machines/intake-state-machine) build on this idea.
:::

:::exercise id="u02-loops"
Write loops that summarize sensor samples, then build a simplified version of the intake's jam detector.

- `sum`, `average`, `max`, `countAbove`, and `firstIndexAbove` each need one loop.
- `longestRunAbove` needs a counter that grows on high samples and resets to 0 on low ones.
- `jamDetectedAt` reports the index where a streak first reaches `count`, just like `jamCount >= jamMinCount` in `Intake`.
---hint
For `max`, start with `values[0]`, not `0`. If every sample is negative, starting at 0 gives the wrong answer. Handle the empty array first.
---hint
For `firstIndexAbove`, you can `return i;` from inside the loop as soon as you find a match. Return `-1` after the loop.
---hint
For `jamDetectedAt`, update the streak first, then check `if (streak >= count) return i;` in the same pass.
:::

:::quiz
?num How many times does the body run? `for (int i = 0; i < 4; i++)`
= 4 ± 0
> `i` is 0, 1, 2, and 3, and the loop stops when `i` reaches 4.

?code What does this print?
```java
int total = 0;
for (int s = 0; s < 2; s++) {
    for (int m = 0; m < 4; m++) {
        total++;
    }
}
System.out.println(total);
```
= 8
> The inner loop runs 4 times for each of the 2 outer passes.

? What does `continue` do inside a loop?
- Ends the loop immediately
+ Skips the rest of the current pass and starts the next one
- Restarts the loop from the first pass
- Pauses the loop for one robot cycle
> `break` ends the loop; `continue` only skips ahead to the next pass.

? Why must robot code never wait in a `while` loop for a mechanism to reach its target?
+ The loop blocks the 20 ms robot cycle, so sensors are not updated and motor commands are never sent
- Java does not allow while loops in subsystems
- `while` loops are slower than `for` loops
- The turret will overheat
> Robot code is called 50 times a second. Check progress once per cycle instead of waiting.

?tf A for-each loop like `for (var module : modules)` can accidentally read past the end of the array.
= false
> For-each visits exactly the items in the array, so it cannot go out of bounds. Index-based loops are where off-by-one errors happen.
:::
