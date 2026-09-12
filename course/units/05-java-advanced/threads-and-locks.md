---
summary: Why the drive samples encoders on a background thread, how threads share data safely with queues and locks, and the rules that keep multithreaded robot code from failing randomly.
objectives:
  - Explain what a thread is and why SparkOdometryThread samples at 100 Hz
  - Recognize race conditions and prevent them with locks and thread-safe queues
  - Follow thread-safety rules that keep robot code deterministic and replayable
files:
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/Robot.java
---

## Two things at once

A **thread** is a separate path of execution inside one program. Every Java program starts with one thread. For robot code, that main thread runs the 20 ms robot loop: the scheduler, every subsystem's `periodic()`, and every command.

Some jobs need a different rhythm. Our drive wants encoder and gyro readings **100 times per second**, twice as often as the robot loop, and at evenly spaced times. The robot loop cannot provide that, so the drive uses a second thread.

## The odometry thread

`SparkOdometryThread` uses WPILib's `Notifier`, which calls a method on its own thread at a fixed period:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="private static SparkOdometryThread instance = null;" lines=19

`notifier.startPeriodic(1.0 / DriveConstants.odometryFrequency)` runs `run()` every 10 ms. Each time, it reads every registered encoder and the gyro, stamps the samples with the time, and puts them into queues. The main loop later drains those queues in `ModuleIOSpark.updateInputs`, which is why each loop can contain two samples instead of one.

More samples, taken at accurate times, give the pose estimator a better picture of how the robot moved, especially during quick turns.

## The danger: race conditions

When two threads touch the same data without coordination, the result depends on exact timing. That is a **race condition**. Even `count++` is unsafe, because it is really three steps (read, add, write), and two threads can interleave them:

```text
odometry thread:  read count (5)          write 6
main thread:                 read count (5)          write 6    ← one increment lost
```

Race bugs are the worst kind: they appear rarely and randomly, pass every test on your laptop, and show up at an event as a pose that jumps once a match.

## Locks

A **lock** lets only one thread at a time run a section of code. Our drive shares one `ReentrantLock` between both threads:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="public Queue<Double> registerSignal(SparkBase spark, DoubleSupplier signal)" lines=12

The pattern is always the same: `lock()`, then `try`, then `unlock()` in `finally`, so the lock is released even if something throws. The main thread takes the same lock while it reads every module:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="odometryLock.lock(); // Prevents odometry updates while reading data" lines=7

The queues themselves are `ArrayBlockingQueue`s, which are already safe to share between threads. So why lock too? Because `Drive` needs a **consistent set**: gyro samples and all four modules' samples from the same instants. The lock guarantees the odometry thread cannot add a new sample halfway through the main thread reading them.

:::warning Hold locks briefly
While one thread holds a lock, the other waits. Never do slow work, such as configuring motors, printing, or waiting, while holding a lock the robot loop needs. And always take multiple locks in the same order everywhere, or two threads can wait on each other forever. That is a **deadlock**.
:::

Java also has the `synchronized` keyword, which locks an object for the length of a method or block. It does the same job with less control.

## Thread-safety rules for robot code

1. **Keep robot logic on the main thread.** Subsystems, commands, and state machines should only run from the scheduler.
2. **Background threads should only sample and hand off.** Read sensors, timestamp, put data in a thread-safe queue, and nothing else.
3. **Assume library objects are not thread-safe** unless their documentation says they are.
4. **Share through thread-safe structures,** like `ArrayBlockingQueue`, or protect access with a lock.
5. **Pass data from threads through logged inputs.** AdvantageKit replay only works if everything the robot logic uses arrives through inputs. Odometry samples become input arrays like `odometryDrivePositionsRad`, so replay sees exactly what the thread measured.

## Thread priority

`Robot.robotPeriodic()` contains a commented-out option:

::source file="src/main/java/frc/robot/Robot.java" from="// Optionally switch the thread to high priority" lines=3

Raising the main thread's real-time priority can reduce loop timing jitter, but a high-priority thread that runs too long can starve other important threads on the roboRIO. The template leaves it off by default, which is the right choice unless logs show timing problems and a mentor agrees to try it.

:::exercise id="u05-sampler"
Write the handoff structure between a sampling thread and the robot loop.

- `offer` is called by the background thread and adds a timestamped sample. When the buffer is full, drop the **oldest** sample and count it.
- `drain` is called by the main loop and returns every sample, oldest first, then empties the buffer.
- Every public method must be safe to call from two threads at once. Use a `ReentrantLock` with `try`/`finally`.

One test starts a real background thread that offers 20,000 samples while the main thread drains, and checks that nothing is lost, duplicated, or reordered.
---hint
Store samples in a structure you control, such as two `ArrayDeque<Double>` objects for timestamps and values. Protect every read and write with the same lock.
---hint
In `drain`, copy both deques into arrays and clear them while holding the lock, then return after unlocking.
:::

:::quiz
? Why does our drive read encoders on a separate thread?
+ To sample at 100 Hz with accurate timestamps, faster and more evenly than the 20 ms robot loop
- Because the main thread cannot talk to CAN devices
- To make the robot code use less memory
- Because PathPlanner requires a second thread
> More, better-timed samples make odometry more accurate.

? Two threads both run `count++` 1,000 times on a shared int with no lock. What is the final value?
- Always 2,000
+ Possibly less than 2,000, because increments can be lost when the threads interleave
- Always 1,000
- It does not compile
> `count++` is read, add, write. Without a lock, updates can overwrite each other.

? Why does `SparkOdometryThread` unlock in a `finally` block?
+ So the lock is released even if reading a sensor throws an exception
- `finally` blocks run on a separate thread
- Locks can only be released there
- It makes the lock faster
> A lock that is never released freezes every other thread waiting for it.

? The queues are already thread-safe `ArrayBlockingQueue`s. Why does `Drive` still take `odometryLock`?
+ To read the gyro and all four modules as one consistent set of samples
- `ArrayBlockingQueue` is not actually thread-safe
- To make the queues larger
- Locks are required for AdvantageKit logging
> Each queue is safe alone, but the lock keeps the whole group of queues in step.

?tf Background threads in robot code should run subsystem logic, like state machines, to spread out the work.
= false
> Keep logic on the main thread. Background threads should only sample data and hand it off safely.
:::
