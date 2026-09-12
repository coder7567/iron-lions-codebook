---
summary: The dashboard is the robot's face during a match. What our Elastic layout shows, how alerts reach the pit, and how to design a dashboard for the two very different people who read it.
objectives:
  - Explain how our Elastic layout is stored, served, and loaded
  - Use WPILib `Alert`s and explain where they appear
  - Design separate views for the drive team and for programmers
  - Decide what belongs on a dashboard and what belongs in a log
files:
  - src/main/java/frc/robot/Robot.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/deploy/elastic-layout.json
---

## Our dashboard

The team uses **Elastic**, a dashboard whose layout is a JSON file that ships with the robot code:

::source file="src/main/deploy/elastic-layout.json" from="grid_size" lines=12

::source file="src/main/java/frc/robot/Robot.java" from="// Elastic Config" lines=2

The layout lives in `src/main/deploy`, so it deploys with the code, and `Robot` starts a small web server on port 5800 that serves that folder. In Elastic, "download layout from robot" pulls it down. That means **the layout is versioned with the code**: a laptop that has never seen this robot gets the right dashboard in one click, and a layout change is a commit like any other.

Our layout's first tab is "Testing," full of text displays bound to tuning topics such as `Tuning/Drive/Drive/PID/Kp`, `Kd`, and `Ks`. That tells you what the dashboard was built for: a programmer at a practice field, changing a number and watching the result.

## Alerts

An `Alert` is a message that appears on the dashboard when a condition is true, with a severity. Our drivetrain raises three kinds:

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="private final Alert gyroDisconnectedAlert" lines=2

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="driveDisconnectedAlert =" lines=4

They are set every loop from a debounced connection flag:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="// Update alerts" lines=3

Two design details are worth copying:

- **The message names the thing.** "Disconnected drive motor on module 2" sends someone to a specific connector. "Drive error" sends them to the whole robot.
- **The condition is debounced.** A single dropped CAN frame shouldn't light up the pit display. The 0.5 second debouncer in `ModuleIOSpark` is what keeps alerts meaningful, and an alert nobody trusts is worse than no alert.

The gyro alert adds a third: it is suppressed in simulation, because a missing gyro is expected there. **An alert that is always on is noise.**

## Two audiences, two views

| | The drive team | Programmers |
|---|---|---|
| Reads it | During a match, in two-second glances | In the pit, while changing something |
| Needs | Auto selection, alliance and HUB state, battery, alerts, whether the robot is ready | Setpoints against measurements, tunables, states |
| Hates | Numbers that need interpretation, anything that changes when it shouldn't | Nothing; more is fine |
| Failure mode | Missing something critical because the screen is busy | Not being able to find a value |

A good competition tab is almost empty: the auto chooser, one line of alerts, battery voltage, and one or two mechanism-ready indicators. Everything else belongs on a second tab, or in the log.

:::team Ready lights beat raw numbers
Our robot already computes the booleans a driver would want: `shooterSpedUp()`, `intakeSafe()`, `getRumble()`, and `Hub Active`. A competition tab with four big indicators driven by those answers "can I shoot right now?" faster than a flywheel RPM readout ever will. The rumble already says it through the controller; the dashboard can say it to the human standing behind the driver.
:::

## Tunables on the dashboard

[Tuning at 967](course:09-controls/tuning-at-967) covers the tunables themselves. Two dashboard-specific rules:

- **Keep them off the competition tab.** A text box that changes a PID gain does not belong where somebody might tap it during a match.
- **Show the value the robot is using**, not the one someone typed. For gains read once at boot, that means the dashboard is showing an intention, not a fact, which is exactly how afternoons get lost.

## Notifications, and some dead code

Our repo contains `frc/robot/util/Elastic.java`, a helper for pushing pop-up notifications to the dashboard. Nothing calls it (finding F20). It is a ready-made way to say "auto finished," "vision lost the target," or "battery below 11 V" in a way that is hard to miss.

The same finding covers `LimitSwitchManager`, which reads sixteen limit switches over SPI:

::source file="src/main/java/frc/robot/util/LimitSwitchManager.java" from="public class LimitSwitchManager" lines=3

Nothing constructs it, and the SPI port it uses does not exist on the 2027 control system. Either wire it up or delete it; code that is neither used nor removed is a question every future reader has to answer.

:::exercise id="u11-alert"
Build the alert system this lesson describes: alerts that turn on only after their condition has held for a number of loops, turn off the moment it clears, group by severity, and collapse into one summary line for the pit.

The summary format is the part worth getting right: "OK" when everything is fine, and otherwise something like "2 errors, 1 warning".
---hint
`update` is short: a false condition zeroes the counter and clears the alert; a true condition increments and sets `active` once the count reaches the threshold.
---hint
`active(Severity)` walks the alerts in the order they were added and keeps the active ones of that severity, so the returned list is stable between loops.
---hint
For the summary, loop over `Severity.values()` in declaration order, skip the zero counts, and pluralize by comparing the count with 1.
:::

:::quiz
? Where does our Elastic layout live, and how does a laptop get it?
+ In `src/main/deploy`, served by a web server on port 5800 that `Robot` starts
- In the Elastic install directory on each laptop
- On the USB stick with the logs
- In NetworkTables
> Versioning the layout with the code means every laptop gets the same dashboard.

? Why are the module disconnect alerts debounced?
+ A single dropped CAN frame would otherwise raise an alert, and alerts nobody trusts get ignored
- To reduce NetworkTables traffic
- Because REVLib requires it
- So they only appear while disabled
> A 0.5 second debouncer is the difference between a signal and noise.

? Why is the gyro alert suppressed in simulation?
+ A missing gyro is expected there, and an alert that is always on is noise
- Simulation has no dashboard
- The alert would crash the simulator
- Gyro data isn't logged in simulation
> Every always-on indicator trains people to ignore indicators.

? What belongs on the competition tab of a dashboard?
+ Auto selection, alerts, battery, and a couple of ready indicators
- Every tunable, so they can be changed quickly
- All four modules' velocities
- The full list of logged keys
> Two-second glances under pressure; anything else goes on another tab or into the log.

? A dashboard shows a PID gain that is read once while the SPARK is configured. What is it actually displaying?
+ The value someone typed, not necessarily the value the controller is using
- The controller's current gain
- The value from the last match
- Nothing; the widget would be blank
> Which is exactly how a tuning session gets lost.

?? Which of these are true about `Elastic.java` and `LimitSwitchManager` in our repo? Select all that apply.
+ Neither is used by any running code
+ `LimitSwitchManager` uses an SPI port that the 2027 control system removes
+ Unused code makes every future reader stop and check whether it matters
- They are required by the Elastic dashboard
> That's finding F20: wire it up or delete it.
:::
