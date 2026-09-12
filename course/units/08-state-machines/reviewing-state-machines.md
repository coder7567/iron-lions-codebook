---
summary: A repeatable way to review state-machine code: draw the diagram from the code, run a ten-question checklist, and check that simulation still matches the robot.
objectives:
  - Draw a state diagram from unfamiliar subsystem code
  - Run a review checklist that catches unreachable states, stale fields, and misplaced outputs
  - Explain how simulation drifts away from the real robot, using findings F5, F6, and F7
  - Write review comments that are specific and easy to act on
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSim.java
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSim.java
---

## State machine bugs don't crash

Most of the findings in this unit have the same shape: the code runs, the robot moves, nothing throws an exception, and one branch quietly never happens. `PAUSEDPASSING` is unreachable. The current averaging logs nothing. `hubActive` goes stale outside teleop. None of these would show up in a stack trace, and only one of them is visible in a match.

So reviewing state machines is not about looking for crashes. It's about asking whether the states in the code are the states in your head.

## Step 1: draw the diagram from the code

Reading a state machine top to bottom rarely reveals its shape. Drawing does. It takes about five minutes:

:::steps
1. **List the states.** Copy the `CurrentState` enum constants onto paper, one box each.
2. **Find every decision.** In `updateState`, each `yield` or `return` is an arrow *into* a state. Write the condition on the arrow.
3. **Mark the entries.** Which wanted states can reach each box?
4. **Find the outputs.** In `applyState`, each `case` is what the box *does*. Write one line inside the box.
5. **Circle the problems.** A box with no incoming arrow is unreachable. A box with no `case` has no outputs. An arrow with no way back is a trap.
:::

Try it on `Turret`. Six boxes come from the enum, five get arrows, and `PAUSEDPASSING` ends up with outputs and no way in. That is finding F1, and the drawing finds it in a minute even though the code has looked fine to everyone who has read it since March.

## Step 2: the checklist

| # | Question | What it catches | Seen in our code |
|---|---|---|---|
| 1 | Can every state actually happen? | Unreachable states | F1 |
| 2 | Does every state set every output it should? | Motors left at their previous values | — |
| 3 | Is the decision a switch expression with no `default`? | States nobody handles after an enum grows | — |
| 4 | Are counters and timers reset when leaving the state that owns them? | Stale evidence triggering later | Intake resets `jamCount` |
| 5 | Is every field a consumer reads updated on every path? | Stale flags | F19 |
| 6 | Do reads happen in `updateInputs` and writes in the output methods? | Hidden outputs, replay differences | F16 |
| 7 | Does the transition need repeated evidence? | False triggers from one noisy reading | Intake's 50-loop count |
| 8 | Are the wanted state, the current state, and the transition inputs all logged? | Bugs you can't diagnose from a log | Turret logs both states |
| 9 | Does the simulated IO behave like the real one? | "It worked in sim" | F5, F6, F7 |
| 10 | Is there a way out of every state, including by driver action? | Traps during a match | Every driver button sets a state |

You don't need all ten for a one-line change. Questions 1, 2, and 5 are worth asking every single time a state is added.

## Step 3: check simulation against the robot

Simulation is only useful while it tells the truth. Two of our sim IO layers have drifted:

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSim.java" from="public void updateInputs(TurretIOInputs inputs)" lines=16 highlight="10,11"

| Input | On the robot | In simulation |
|---|---|---|
| `intakeSafe` | Within 0.05 rad of the idle position | **More than** 0.05 rad from it: inverted |
| `resetting` | More than 0.1 rad from the setpoint | More than π rad from the setpoint: almost never true |
| `flywheelSpeed` | Measured from the encoder | From a `FlywheelSim` that is never given voltage, so it stays at 0 |

The consequences are worth spelling out, because they are the reason a sim result can be misleading:

- The intake-safe interlock is **backwards** in simulation, so an `IDLE` handoff that looks correct on a laptop may be wrong on the robot.
- `shooterSpedUp()` compares 0 RPM against setpoints of 1850 to 2700, so it is false whenever the turret is shooting. **The feeder never runs in simulation.**
- `IntakeIOSim` never fills currents or the arm setpoint, so jam recovery can't happen either (finding F6).

Both sim classes also extend their Spark counterparts and call `super()` (finding F7), which constructs real `SparkFlex` objects for hardware that isn't connected. A sim IO that implements the interface directly is simpler and can't accidentally inherit hardware behavior.

:::tip A two-line rule for sim IO
Write the threshold once and share it. If `intakeSafe` is "within `turretTolerance` of `turretIDLEPosition`," put that comparison in one place both IO classes call. Copy-pasted comparisons are where `<` becomes `>`.
:::

## How our state machines grew

Reading the repository's history explains a lot about its shape:

| Date | Commit | What changed |
|---|---|---|
| 2026-02-22 | `f20834f` | Turret gains a passing state |
| 2026-02-25 | `e8e89b0` | A testing state for bench tuning |
| 2026-03-11 | `6dd0a6b` | Intake gains jam counters and unjam |
| 2026-03-13 | `65727f9` | Pause states, including the F1 line |
| 2026-03-16 | `1ca2ec5` | Rumble when scoring into an inactive HUB |
| 2026-03-19 | `4dbac3d` | The full-field state is removed |

Two lessons come out of that list. **States accumulate under time pressure**, usually one per problem discovered at an event, which is exactly when nobody has time to redraw the diagram. And **removing a state is as valuable as adding one**: the full-field behavior became part of passing, so deleting it removed a whole column of possibilities from the code.

## Writing the review comment

A useful review comment names the state, the condition, and the consequence:

> In `Turret.updateState`, both branches of `case PAUSED` yield `PAUSEDSHOOTING`, so `PAUSEDPASSING` is unreachable even though `applyState` handles it. Paused outside our alliance zone, the turret aims at the HUB instead of the passing target. Should the second branch yield `PAUSEDPASSING`?

Compare that with "this looks wrong." The first version can be acted on during a build meeting; the second starts an argument. Three habits make the difference: quote the exact code path, say what the robot does because of it, and end with a question when you're inferring intent.

:::try Review the code you just learned
Pick `Intake.java`, and without rereading this unit, draw its diagram and run questions 1 through 5 on it. Then check your drawing against the one in [The Intake State Machine](course:08-state-machines/intake-state-machine). Anything you found that isn't in this course is worth bringing to a software meeting: this code has been read by a lot of people, and it still has findings in it.
:::

:::quiz
? Why do state-machine bugs rarely show up as crashes?
+ A branch that never runs looks exactly like code that works
- WPILib catches exceptions in subsystems
- The scheduler retries failed states
- Enums can't throw exceptions
> That's why reviews ask "can this happen?" instead of only reading for errors.

?order Put the steps for drawing a state diagram from code in order.
1. List the enum constants as boxes
2. Turn each `yield` in the decision into an arrow with its condition
3. Write each `applyState` case inside its box
4. Circle boxes with no arrows in and boxes with no outputs
> The drawing makes unreachable states and missing outputs obvious.

? Which checklist question would have caught finding F19, the stale `hubActive` field?
+ "Is every field a consumer reads updated on every path?"
- "Can every state actually happen?"
- "Does the transition need repeated evidence?"
- "Is there a way out of every state?"
> The early returns skip the assignment that the teleop ladder makes.

? In simulation, `TurretIOSim` reports `intakeSafe` as true when the turret is **more than** 0.05 rad from its idle position. What does that mean for testing?
+ The intake handoff is backwards in simulation, so `IDLE` behavior verified there says nothing about the robot
- Simulation is fine, because the tolerance is small
- The turret refuses to move in simulation
- The intake never deploys in simulation
> The real IO uses `<`. A copied comparison with a flipped operator is easy to write and hard to spot.

? Why does the feeder never run in simulation?
+ The simulated flywheel is never given voltage, so its speed stays at 0 and `shooterSpedUp()` is false
- The feeder motor isn't created in simulation
- The intake is disabled in simulation
- `Logger.processInputs` blocks feeder outputs during simulation
> Sim fidelity gaps turn into "works in sim, fails on the robot," or the reverse.

?? Which are good habits when reviewing a state machine? Select all that apply.
+ Draw the diagram before reading line by line
+ Name the exact code path and the resulting robot behavior in the comment
+ Ask a question when you're inferring what the author intended
- Approve state changes without the diagram when the team is at an event
> Event pressure is exactly when a five-minute drawing pays for itself.
:::
