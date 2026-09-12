---
summary: AdvantageScope is where a log turns into an answer. How to connect it, which tab answers which question, and the keys in our log worth knowing by heart.
objectives:
  - Connect AdvantageScope to a live robot and to a log file
  - Choose the right tab for a question
  - Find the keys our code logs for each mechanism
  - Compare two logs, including an original and its replay
files:
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## Connecting

| Source | How | When |
|---|---|---|
| **Live robot** | Connect to the robot's network and open the NT4 connection to `10.9.67.2` | In the pit, while tuning or testing |
| **Live simulator** | Connect to `localhost` | While running `simulateJava` |
| **Log file** | Open a `.wpilog` | After a match or practice |
| **Download** | AdvantageScope can pull logs off the roboRIO over the network | When you don't want to unplug the USB stick |

Live connections show you what is happening. Log files show you what happened, with a time cursor you can scrub, which is almost always the more useful of the two.

## Which tab answers which question

| Tab | Use it for |
|---|---|
| **Line Graph** | The default. Put a setpoint and its measurement on the same axis and look at the shape |
| **Field 2D** | Odometry against the field: `Odometry/Robot`, `Odometry/Trajectory`, vision poses |
| **Field 3D** | Camera poses, tag positions, anything with a Z |
| **Swerve** | The four module states as little arrows: `SwerveStates/Measured` next to `SwerveStates/SetpointsOptimized` |
| **Table** | Exact values at one moment, which beats squinting at a graph |
| **Console** | The robot's printed output, including the characterization results |
| **Statistics** | Mean, max, and standard deviation over a selected range |
| **Joysticks** | What the driver actually did, which is often not what they remember doing |

## Keys worth knowing

| Question | Keys |
|---|---|
| Where did the robot think it was? | `Odometry/Robot`, `Odometry/Trajectory`, `Odometry/TrajectorySetpoint` |
| Were the modules following their commands? | `SwerveStates/SetpointsOptimized` against `SwerveStates/Measured` |
| Was a module disconnected? | `Drive/Module0/driveConnected`, `turnConnected`, and the same for 1 through 3 |
| What was the robot being asked to do? | `Wanted State`, `CurrentState` |
| What was the turret doing? | `Turret State`, `Turret/turretAngle` against `Turret/turretSetAngle`, `DistanceToHub` |
| Was the flywheel ready? | `Turret/flywheelSpeed` against `Turret/flywheelSetSpeed` |
| Was our HUB active? | `Hub Active`, `Period Time`, `Match Time` |
| How much current were we pulling? | `TotalCurrent`, and each subsystem's current inputs |
| What did the shot solver compute? | `Calculations/target`, `TOF`, `FieldBasedTurret` |

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="DistanceToHub" lines=1

Those names are the ones our code writes today, inconsistencies and all, which is why [Telemetry and Logs](course:11-logging/telemetry-and-logs) recommends a naming convention before someone has to memorize two spellings of "state."

## A worked session

The drive team says a shot went wide late in a match.

:::steps
1. Open the log and find the moment. The `Joysticks` tab or `Wanted State` shows when the trigger was pulled.
2. On a Line Graph, drag in `Turret/flywheelSpeed`, `Turret/flywheelSetSpeed`, and `Turret/turretAngle` with `Turret/turretSetAngle`.
3. Ask three questions in order: was the flywheel at speed, was the turret on target, and was the target right? The third needs `Calculations/target` and `DistanceToHub` against the pose.
4. Switch to Field 2D and watch `Odometry/Robot` around that moment. If the pose jumped, vision and odometry disagreed, and the aim was computed from a bad position.
5. Check `Hub Active`. A perfect shot into an inactive HUB is still zero points.
:::

Each step eliminates a layer. That is the whole method: **narrow by layer until only one explanation is left**.

## Comparing two logs

AdvantageScope can open a second log as a comparison source, which is how you use [replay](course:11-logging/replay): open the match log and its `_sim` replay together, then graph the same key from both. Identical lines mean the replay reproduced the match faithfully, so anything you add to the replay is trustworthy. Lines that diverge mean an input is missing from the log, which is itself a finding worth chasing.

The same trick compares two matches: yesterday's auto against today's, or the practice field against the competition field.

:::tip Export what you measure
The Statistics tab and CSV export turn a log into data you can put in a spreadsheet. That is exactly how the shot map in [Unit 14](course:14-shooting/collecting-shot-data) gets built: drive to a distance, shoot, record the distance and the setpoint that worked, and let the table interpolate between them.
:::

:::quiz
? You want to know whether a module tracked its commanded speed. Which two keys do you graph?
+ `SwerveStates/SetpointsOptimized` and `SwerveStates/Measured`
- `Odometry/Robot` and `Odometry/Trajectory`
- `Drive/Module0/driveConnected` and `TotalCurrent`
- `Wanted State` and `CurrentState`
> Command against measurement, for the same thing, on the same axis.

? Which tab shows the four module states as arrows?
+ Swerve
- Field 3D
- Mechanism
- Statistics
> It is the fastest way to spot one module pointed differently from the others.

? The robot's pose jumps sideways mid-match in Field 2D. What does that suggest?
+ A vision measurement disagreed with odometry and corrected it sharply
- The gyro disconnected
- The log was corrupted
- The trajectory was regenerated
> Odometry drifts smoothly; jumps come from corrections.

? You open a match log and its `_sim` replay, and one value diverges between them. What does that mean?
+ Something the code depends on wasn't captured in the log, so replay isn't faithful for that value
- The replay ran too fast
- The log is corrupted
- The robot code changed between matches
> A divergence is a missing input, which is a finding in itself.

? Where do the printed results of `feedforwardCharacterization` show up?
+ The Console tab, since the command prints to standard output
- The Statistics tab
- The Line Graph tab
- They are recorded as `@AutoLogOutput`
> Which is also why those numbers should be committed the same day.

?tf Connecting live to the robot is generally more useful for diagnosing a past match than opening its log.
= false
> Live shows what is happening now. A log has the moment you care about and a cursor you can scrub.
:::
