---
summary: How to take an auto from an idea to something you trust on a competition field: simulation, the practice field, the log review, and the match-day discipline that keeps it working.
objectives:
  - Test an auto in simulation and know what that does and doesn't prove
  - Run a practice-field routine that catches the common failures
  - Review an auto in the logs
  - Follow match-day discipline for auto selection
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## Simulation first

`./gradlew simulateJava`, select the auto in the chooser, and enable autonomous. The drivetrain simulation is good enough that this proves real things:

| Simulation proves | Simulation cannot prove |
|---|---|
| The auto is selectable and builds without errors | That the robot actually scores |
| Named commands are registered (no "not registered" warnings) | Whether the flywheel reaches speed in time |
| The path geometry, start pose, and end pose | Traction, tipping, or contact with other robots |
| That the routine finishes inside 20 seconds | Battery-dependent timing |
| Trajectory following behavior at the commanded speeds | Field-to-field differences |

The second column is why simulation is step one, not the whole test. It also has a hole specific to our robot: [the simulated flywheel never spins](course:11-logging/simulation), so any part of an auto that waits on `shooterSpedUp` behaves differently there.

## On the practice field

:::steps
1. **Mark the starting pose.** Tape on the carpet, with the robot's bumper corners lined up. An auto is only as repeatable as its start.
2. **Run it once with a finger on Disable**, watching for anything unexpected in the first two seconds.
3. **Run it five times.** Once is an anecdote. Five runs show you the spread: where it ends, how long it takes, and how often it does the whole routine.
4. **Measure the end pose** against what the auto intended, with a tape measure, not by eye.
5. **Run it on a low battery.** Autos get slower as voltage drops, and a routine with no slack fails at 11.5 V.
6. **Run it after a collision.** Nudge the robot mid-run and see what it does. This is what a real match does to it.
7. **Write down the results** in the build log: date, auto, five end positions, total time.
:::

Step 3 is the one teams skip and regret. An auto that works four times out of five is a 20% failure rate, which over a twelve-match event is two or three lost autonomous periods.

## Reviewing the log

| Question | Keys |
|---|---|
| Did it follow the path? | `Odometry/Robot` against `Odometry/TrajectorySetpoint` on Field 2D |
| Was the start pose right? | `Odometry/Robot` at t = 0 against the intended starting pose |
| Did the mechanisms run? | `Wanted State`, `CurrentState`, `Turret/flywheelSpeed` |
| Did it finish in time? | The timestamp when the command ended, against the start of autonomous |
| Did anything disconnect? | `Drive/Module*/driveConnected`, `Drive/Gyro/connected` |
| Was the battery healthy? | Bus voltage through the routine |

The most common finding is that the robot tracked its trajectory perfectly while the trajectory was in the wrong place, which means the starting pose was wrong. That is a `resetOdom` problem or a placement problem, and it looks nothing like a tuning problem.

## Common failures, in the order you should suspect them

| Symptom | Likely cause |
|---|---|
| Robot drives the path but doesn't shoot | A named command isn't registered |
| Robot drives somewhere absurd | Wrong starting pose, or the alliance flip didn't match reality |
| Robot follows the path offset by a constant amount | `resetOdom` false, or the robot was placed wrong |
| Robot falls behind the trajectory | Constraints exceed what the robot can do, or the battery is low |
| Auto doesn't appear in the chooser | It wasn't deployed, or the chooser shows a stale copy on the roboRIO |
| Auto works in practice and fails at the event | Field differences, a different alliance, or a partner in the way |

## Match-day discipline

:::steps
1. **Select the auto and read it aloud** from the dashboard, every match, before the field is closed.
2. **Place the robot on its marks**, and have a second person confirm.
3. **Verify vision sees a tag** if the auto depends on the pose being corrected.
4. **After the match, note what happened** in one line: worked, partly worked, or didn't, and why.
5. **Change autos between matches only for a reason you can state**, and re-test if there is any time at all.
:::

:::danger The auto you did not test is not an auto
A routine that has never run on a real field is a hypothesis. If there is no time to test it, run the one that works. `Just Preload` scores every time and cannot collide with a partner, and a reliable small auto has won more matches than an untested large one.
:::

:::quiz
? What does testing an auto in simulation prove?
+ That it builds, its commands are registered, and its geometry and timing are plausible
- That the robot will score
- That the flywheel reaches speed in time
- That it works on a real field
> Simulation is step one because it is fast and free, not because it is sufficient.

? Why run a new auto five times instead of once?
+ Once is an anecdote; five runs show the spread in end position, timing, and reliability
- The robot needs to warm up
- PathPlanner caches the trajectory after the first run
- To average out gyro drift
> An auto that works four times in five is a 20% failure rate.

? In the log, the robot tracks its trajectory setpoint closely, but the whole trajectory is offset from the field. What is wrong?
+ The starting pose was wrong: either `resetOdom` is false or the robot was placed incorrectly
- The translation gains are too low
- The path constraints are too aggressive
- The named commands aren't registered
> Good tracking in the wrong frame is a pose problem, not a following problem.

? An auto drives correctly but no mechanism runs. What is the first suspect?
+ A named command that was never registered, which became a do-nothing command
- The intake's current limit
- The path's rotation targets
- A low battery
> It fails silently except for a console warning at startup.

? Why test an auto on a low battery?
+ Autos get slower as voltage drops, and a routine with no slack fails
- To calibrate the gyro
- Because current limits change with voltage
- To check the brownout threshold
> Timing margin is the thing a weak battery eats first.

? It is the last match of qualifications, a new auto is untested, and the old one works. Which do you run?
+ The one that works
- The new one, since it scores more
- Neither; disable autonomous
- Let the drive team decide during the match
> An untested routine is a hypothesis, and a failed auto can take a partner's with it.
:::
