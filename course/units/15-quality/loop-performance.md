---
summary: Twenty milliseconds is the whole budget. What spends it, what an overrun actually costs, how our code already protects the loop, and how to measure instead of guess.
objectives:
  - Explain what happens when a loop runs long
  - Name the operations that cost real time on a roboRIO
  - Find the expensive parts of our code
  - Measure loop time and section time rather than guessing
files:
  - src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
---

## The budget

`LoggedRobot` runs every 20 milliseconds. In that window the robot reads every sensor, logs every input, runs five subsystem periodic methods, polls triggers, runs commands, and writes outputs to eighteen CAN devices.

When it does not finish in time, nothing crashes. The next loop simply starts late, and the watchdog prints a warning listing how long each part took:

```text title="What an overrun looks like in the console"
Watchdog not fed within 0.020000s
    Drive.periodic(): 0.014231s
    Turret.periodic(): 0.008112s
    ...
```

Those per-part lines are **epochs**, and the CommandScheduler adds one for every subsystem automatically. They are the first thing to read when the console starts complaining.

## What an overrun costs

| Effect | Why it matters |
|---|---|
| Commands run late | A 30 ms loop means the drivetrain gets its command 10 ms late, every loop |
| Control loops see stale data | Our SPARK-side loops keep running, which softens this considerably |
| Odometry samples queue | The 100 Hz thread keeps sampling into 20-deep queues, so about 200 ms of slack before samples are lost |
| Vision measurements arrive later | Their timestamps are still correct, so the estimator handles it |
| Everything gets less predictable | Timing-dependent behavior becomes intermittent, which is the worst kind of bug |

A single overrun is nothing. A loop that overruns every time something specific happens is a real defect, and the pattern in the log usually names the cause.

## What actually costs time

| Operation | Cost | Where it shows up |
|---|---|---|
| **Configuring a motor controller** | Milliseconds, blocking on CAN | `setCurrentLimit` at every mode change (finding F8) |
| **Persisting settings to flash** | Milliseconds, plus limited write cycles | The same call |
| **Building strings** | Surprisingly high on the roboRIO's JVM | Log keys built per loop; the build sets `-XDstringConcat=inline` for this reason |
| **Allocating in a loop** | Garbage collection pauses at bad moments | Anything that makes objects every cycle |
| **Image processing** | Tens of milliseconds | Not on our roboRIO; it lives on the coprocessor |
| **Reading many CAN signals** | Each read waits for its frame | Mitigated by our status-frame periods |

Notice that most of the list is CAN, not computation. A roboRIO can do plenty of arithmetic in 20 ms. What it cannot do is wait on a bus twenty times.

## What our code already does

**It moved odometry off the main loop.** `SparkOdometryThread` samples at 100 Hz on a `Notifier`, so the main loop never waits for those reads; it takes a lock and drains a queue.

**It budgets status frames.** Only the odometry signals are published at 10 ms; everything else sits at 20 ms, so the bus carries what it needs and no more.

**It preallocates in the turret.**

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="//Optimization variables to decrease runtime" lines=7

Those fields are really local variables, hoisted to avoid allocating on every loop. It is a legitimate technique, and it has a cost worth naming: the class now has six fields that hold no meaningful state between loops, which makes it harder to read and easier to accidentally depend on. Modern JVMs handle short-lived objects well, so this is the kind of optimization worth **measuring before keeping**.

## Measuring instead of guessing

:::steps
1. **Read the watchdog output.** It already tells you which subsystem is slow, for free.
2. **Log the loop time.** AdvantageKit records timestamps; graphing the gap between them shows the distribution rather than only the overruns.
3. **Time a suspicious section.** Wrap it with `Timer.getFPGATimestamp()` and record the difference as an output, then look at the graph rather than the peak.
4. **Correlate with actions.** If overruns happen only at mode transitions, you have found F8 without reading any code.
5. **Change one thing and re-measure.** Performance work without a before-and-after is guessing with extra steps.
:::

:::warning The two rules that prevent most loop problems
**Never block in a periodic method.** No sleeps, no waits, no retry loops around CAN calls, and no configuration except at startup.

**Never allocate large or numerous objects per loop.** A few small objects are fine; building an array of poses every cycle is not.
:::

:::exercise id="u15-budget"
Build the budget tracker this lesson describes: record named sections, end each loop, count overruns, and report which section costs the most across the whole run.

The last detail is the one that matters in practice: the worst section is the biggest **total**, not the biggest single loop, because a small cost paid every loop beats a large one paid once.
---hint
Keep two maps: one for the loop in progress and one for the running totals. `Map.merge(name, seconds, Double::sum)` adds to either in one line.
---hint
`endLoop` folds the current loop into the totals, clears it, counts the loop, and compares against the budget. Exactly at the budget is not an overrun.
---hint
For `worstSection`, walk the totals and keep the largest. A `LinkedHashMap` keeps insertion order, which makes ties resolve to whichever section was recorded first.
:::

:::quiz
? What happens when a robot loop takes longer than 20 ms?
+ The next loop starts late and the watchdog prints how long each part took
- The robot disables itself
- WPILib skips the next loop
- The scheduler drops the slowest command
> The per-subsystem epochs in that printout are the fastest way to find the cause.

? Which of these is the most expensive thing our robot code does in a loop?
+ Configuring a motor controller and persisting the settings to flash
- Computing swerve kinematics
- Interpolating the shot map
- Wrapping angles
> Most loop-time problems are CAN and flash, not arithmetic.

? Why does the odometry thread help loop time?
+ The 100 Hz sampling happens off the main loop, which only drains a queue
- It runs on a second processor core dedicated to odometry
- It reduces CAN traffic
- It skips samples when the loop is busy
> The main loop takes a lock and reads what the thread already collected.

? What is the cost of the turret's preallocated "optimization variables"?
+ Six fields that hold no state between loops, making the class harder to read and easier to misuse
- Higher memory use
- Slower access than locals
- They break replay
> Worth measuring before keeping; modern JVMs handle short-lived objects well.

? Overruns appear in the log only at the start of teleop and autonomous. What does that point to?
+ Work being done at mode transitions, such as reconfiguring the drive SPARKs
- A slow vision pipeline
- Garbage collection
- A disconnected module
> That correlation identifies finding F8 without reading any code.

?? Which rules keep the loop healthy? Select all that apply.
+ Never block in a periodic method
+ Do not configure motor controllers outside startup
+ Avoid allocating many objects every loop
- Log as little as possible
> Logging is cheap when it is numbers; it is expensive when it is strings.
:::
