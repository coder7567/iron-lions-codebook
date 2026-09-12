---
summary: Show that you can explain our logging setup, use replay and simulation correctly, read a log to answer a question, and debug in a method rather than by guessing.
---

This test covers all of Unit 11.

:::exam Unit 11 test: logging, replay, and debugging
? What can an AdvantageKit log do that a log of outputs alone cannot?
+ Re-run the code later on exactly the inputs the robot saw
- Publish values to the dashboard live
- Record the match time
- Survive a power cycle
> Logging inputs is what makes replay possible.

? Where do our match logs get written on the robot?
+ To a USB stick plugged into the roboRIO, by `WPILOGWriter`
- To the Driver Station laptop
- To the deploy folder
- To NetworkTables only
> A missing stick means no log, which is why it belongs on the pit checklist.

? Which call records a value the code computed, rather than something read from hardware?
+ `Logger.recordOutput(...)`
- `Logger.processInputs(...)`
- `io.updateInputs(...)`
- `Logger.recordMetadata(...)`
> Inputs come through the IO layer; outputs are decisions.

? What does `@AutoLog` generate?
+ A subclass of the inputs class that can write every field to a log and read it back
- A dashboard layout
- A replay driver
- A NetworkTables table
> The annotation processor runs at build time; you never edit the generated file.

? In replay mode, what does `Logger.processInputs` do with the inputs object?
+ Overwrites it from the log, discarding whatever the IO layer produced
- Writes it to the log only
- Leaves it unchanged
- Publishes it to NetworkTables
> That is the single line the whole replay design rests on.

? Why can you add a new `recordOutput` and see its values for a match that already happened?
+ Outputs are recomputed during replay from the logged inputs
- AdvantageKit records every variable in memory
- The log file can be edited afterward
- The dashboard keeps history
> This is the most useful property of replay-based logging.

? `Superstructure` reads `DriverStation.getMatchTime()` directly. What does that cost?
+ Match time never reaches the log, so the hub logic does not replay faithfully
- Nothing; the Driver Station replays automatically
- The value is logged as an output instead
- Replay refuses to run
> Inputs that bypass the IO boundary are holes in replay.

? What does `setUseTiming(false)` do during replay?
+ Runs loops as fast as the computer can instead of in real time
- Removes timestamps from the log
- Disables the watchdog
- Skips loops that overran on the robot
> A full match replays in seconds.

? Which question can replay answer?
+ "Which branch of the state machine ran at that moment?"
- "Why did the CAN bus drop out?"
- "What if the driver had turned the other way?"
- "Is the battery healthy?"
> Replay reruns decisions on recorded inputs; it cannot change what happened.

? In simulation today, why does the feeder never run?
+ The simulated flywheel is never given voltage, so `shooterSpedUp()` stays false
- The intake is disabled in simulation
- The feeder motor isn't created
- The Superstructure blocks shooting in simulation
> Finding F6: the sim stores the setpoint but never drives the model.

? In simulation, the turret is far from its idle position. What does `intakeSafe` report?
+ True, because the simulated comparison is inverted compared with the robot
- False, matching the robot
- It isn't implemented in simulation
- It depends on the alliance
> Finding F5, which makes simulated interlock results backwards.

? What does finding F12 mean when you open a log from a competition match?
+ Its metadata names a November 2025 build and the wrong commit, so you can't tell which code produced it
- The inputs are missing
- The log can't be opened
- Replay will crash
> `BuildConstants` is committed and never regenerated, because the build has no `gversion` plugin.

? Where does our Elastic dashboard layout come from?
+ A JSON file in `src/main/deploy`, served by a web server on port 5800 that `Robot` starts
- Each laptop's local Elastic settings
- NetworkTables
- The USB log stick
> Versioning the layout with the code means every laptop gets the same dashboard.

? Why are the module disconnect alerts debounced for half a second?
+ A single dropped frame shouldn't raise an alert, because alerts nobody trusts get ignored
- To save CAN bandwidth
- Because `Alert` requires it
- So they only show while disabled
> An always-on or flickering indicator trains people to look away.

? You want to know whether a drive module tracked its command. Which pair do you graph?
+ `SwerveStates/SetpointsOptimized` and `SwerveStates/Measured`
- `Odometry/Robot` and `Hub Active`
- `TotalCurrent` and `Match Time`
- `Wanted State` and `CurrentState`
> Command against measurement, for the same quantity.

? The log shows a correct setpoint and a measurement that never reaches it. Which layer?
+ Control, or hardware if the mechanism is binding
- Logic
- Environment
- The dashboard
> Right command, poor execution.

? It is six minutes between matches and a mechanism is misbehaving. What is the first question to ask?
+ Whether it has to be fixed before the next match at all
- Which PID gain to change
- Whether to reflash the roboRIO
- Whether to swap the battery and redeploy
> A known annoyance beats an untested change.

?? Which habits keep code replayable and debuggable? Select all that apply.
+ Read every sensor through an IO layer
+ Log inputs you don't think you'll need
+ Copy logs off the USB stick after every event
- Keep logs small by recording only outputs
> A missing input is permanent, and a deleted log is a bug you debug twice.
:::
