---
summary: SysId drives a mechanism through known voltage patterns and fits gains from the log. Here is how our routines are wired, how to run them safely, and how to read what comes out.
objectives:
  - Explain what quasistatic and dynamic tests measure
  - Read our `SysIdRoutine` setup and know what its defaults are
  - Run a characterization safely and collect a usable log
  - Choose between SysId and our simpler built-in characterization commands
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/commands/DriveCommands.java
---

## Measuring instead of guessing

Every gain in [Feedforward](course:09-controls/feedforward) describes a physical fact about the robot: how much voltage it takes to start moving, to hold a speed, to accelerate. SysId is WPILib's tool for measuring those facts. It runs the mechanism through four known voltage patterns, logs what happened, and fits the model from the data.

| Test | What it does | What it measures |
|---|---|---|
| **Quasistatic forward** | Voltage rises slowly from 0 | kS and kV, with acceleration nearly zero |
| **Quasistatic reverse** | The same, in the other direction | kS and kV in reverse |
| **Dynamic forward** | A sudden step to a fixed voltage | kA, from how fast it speeds up |
| **Dynamic reverse** | The same step, in the other direction | kA in reverse |

Quasistatic means "slow enough that acceleration doesn't matter." Every volt above kS goes into holding speed, so voltage plotted against velocity is a straight line whose intercept and slope are the gains you want. The dynamic test does the opposite: it changes velocity fast, so the extra voltage tells you about mass.

## Our setup

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="// Configure SysId" lines=10

The `Config` takes four arguments, and the first three are `null`, which means WPILib's defaults: a **1 volt per second** ramp, a **7 volt** step, and a **10 second** timeout per test. The fourth argument is what makes the routine usable with AdvantageKit: every state change is written to the log as `Drive/SysIdState`, so the analyzer can tell which part of the log belongs to which test.

The `Mechanism` says how to drive the thing and which subsystem it requires. `runCharacterization` sends an open-loop voltage to all four drive motors while holding the modules straight ahead.

The four routines appear in the auto chooser alongside our real autos:

::source file="src/main/java/frc/robot/RobotContainer.java" from="autoChooser.addOption(" occurrence=3 lines=10

Motor data comes from **URCL**, the unofficial REV logging library, started in `Robot`:

::source file="src/main/java/frc/robot/Robot.java" from="// Initialize URCL" lines=2

URCL records every SPARK's position, velocity, applied output, and current straight into the log, which is what the SysId analyzer reads.

## Running one safely

:::steps
1. **Pick the space.** A quasistatic drive test rolls several meters; a dynamic test is a 7 volt step, which is fast. Use a long clear stretch, or put the robot on blocks when you only need the motors.
2. **Set the field up.** Bumpers on, battery charged, nothing in the path, a spotter at each end, and someone holding the Driver Station with a hand on Disable.
3. **Select the routine** in the auto chooser and read it back out loud.
4. **Enable Autonomous**, watch the robot, and **disable** as soon as the test finishes or anything looks wrong.
5. **Repeat** for all four tests without restarting robot code, so they land in one log.
6. **Pull the log** off the USB stick and open it in AdvantageScope, which can hand the data to the SysId analyzer.
7. **Write the numbers down**, along with the date, the battery, and the surface. Gains measured on carpet do not transfer to a concrete shop floor.
:::

:::danger Two safety rules
Current limits stay on during characterization. They change the measured gains slightly and they keep a mistake from becoming a burned motor.

Never run a dynamic test on a mechanism with a hard stop unless you have checked that 7 volts for 10 seconds can't drive it into that stop. For the turret, hood, or intake arm, lower the step voltage and the timeout first.
:::

## Our simpler alternatives

SysId is thorough, and sometimes it is more than you need. Two commands in `DriveCommands` do smaller jobs without a log analyzer:

| Command | What it does | Output |
|---|---|---|
| `feedforwardCharacterization` | Ramps voltage at 0.1 V/s and fits a line | Prints kS and kV to the console |
| `wheelRadiusCharacterization` | Spins the robot in place and compares gyro rotation with wheel rotation | Prints the measured wheel radius |

The wheel radius one is easy to overlook and quietly important. Every odometry distance is computed by multiplying wheel radians by `wheelRadiusMeters`. Tread wears down over a season; a wheel that is 2 mm smaller than the constant makes odometry drift by about 4% on every path. Re-measure after a tread change, and before a competition.

:::info Why it prints instead of logging
Both commands print with `System.out.println`, which lands in the Driver Station console and the Riolog. That is fine for a pit workflow, but it means the result is gone when the console scrolls. Copy the numbers into the constants file and commit them the same day; a value that only exists in someone's screenshot isn't a value the robot has.
:::

:::quiz
? What does a quasistatic test measure?
+ kS and kV, by moving slowly enough that acceleration barely matters
- kA, by accelerating as hard as possible
- The wheel radius
- The maximum current draw
> Slow ramps isolate the friction and velocity terms.

? What does the dynamic test add?
+ kA, because a sudden voltage step reveals how quickly the mechanism can change speed
- The gear ratio
- The encoder's conversion factor
- The battery's internal resistance
> Acceleration is what a step test is good at exposing.

? Our `SysIdRoutine.Config` passes `null` for its first three arguments. What does that mean?
+ Use WPILib's defaults: a 1 V/s ramp, a 7 V step, and a 10 second timeout
- Disable those tests
- Read the values from the dashboard
- Run without current limits
> Know the defaults before you run a test on a mechanism with hard stops.

? Why does the routine log `Drive/SysIdState`?
+ So the analyzer can tell which section of the log belongs to which test
- To show the driver which test is running
- Because SysId requires a string output
- To trigger the next test automatically
> Without the state markers, the log is one undifferentiated blob.

? Why should the wheel radius be re-measured during the season?
+ Tread wear shrinks the wheel, and every odometry distance is computed from that constant
- The gear ratio changes with use
- The gyro drifts
- PathPlanner requires a fresh measurement each event
> A few percent of error compounds over a whole autonomous path.

?tf You should turn current limits off during characterization so the measurement isn't distorted.
= false
> Limits stay on. They protect the hardware, and the gains you measure should match the conditions the robot actually runs in.
:::
