---
summary: A repeatable way to find robot bugs: reproduce, observe, localize by layer, change one thing, verify, write it down. Plus what to do when you have six minutes between matches.
objectives:
  - Follow a debugging method instead of guessing
  - Localize a problem to logic, control, configuration, hardware, or environment
  - Run competition triage under time pressure
  - Write a finding down so the next person doesn't rediscover it
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/Robot.java
---

## The method

:::steps
1. **Write down the symptom** in the words of whoever saw it. "The turret pointed at the wall for a second after we crossed midfield" is a symptom. "The turret is broken" is not.
2. **Reproduce it**, or find it in a log. A bug you can't produce on demand is one you can only fix by luck.
3. **Observe, don't guess.** Open the log and look at the values around the moment. Most wrong guesses would have been settled in thirty seconds by a graph.
4. **Localize by layer** (below). Cut the problem in half rather than reading code from the top.
5. **Form one hypothesis**, stated so it can be wrong: "the passing map was built for the blue alliance and never rebuilt."
6. **Change one thing** and test it.
7. **Verify the fix on the real robot**, with the original symptom in front of you.
8. **Write it down**: what it was, how it showed up, what fixed it, and what would have caught it sooner.
:::

Steps 3 and 6 are where teams lose the most time. Guessing is fast and usually wrong; changing three things at once means you can't tell which one worked.

## Localize by layer

| Layer | Looks like | Where to look |
|---|---|---|
| **Logic** | The robot does the wrong thing confidently | `Wanted State`, `CurrentState`, the state machine's inputs |
| **Control** | The right command, badly executed: slow, overshooting, oscillating | Setpoint against measurement |
| **Configuration** | One device behaves differently from its peers | CAN IDs, inversions, conversion factors, gains, offsets |
| **Hardware** | It stops, sputters, or only fails under load | Currents, voltages, connection flags, alerts |
| **Environment** | Only at events, or only on one field | Alliance color, FMS connection, game data, lighting, battery |

The fastest cut is usually logic against everything else: if the **setpoint** in the log is wrong, stop looking at the mechanism. If the setpoint is right and the measurement doesn't follow, stop reading state-machine code.

## Four case studies from our robot

**A state that never happens.** The turret behaves oddly while paused outside our alliance zone. The state machine's decision looks fine on a quick read. Replay the match, add a `recordOutput` for the chosen branch, and watch `PAUSEDPASSING` never appear. Layer: logic. Finding F1.

**A module drops out.** The robot jerks occasionally, and `Drive/Module2/driveConnected` blinks false in the log. No amount of gain tuning will help. Layer: hardware, and the alert already said so in the pit.

**A brownout.** The robot goes limp during a spin-up while accelerating. Bus voltage dips, `TotalCurrent` spikes, and the Driver Station reports the brownout. Layer: hardware and configuration, meaning current limits.

**Autos end short.** The robot consistently stops before its final waypoint. `Odometry/Robot` tracks `Odometry/Trajectory` closely, so the follower is doing its job on a pose that is wrong. Layer: configuration, meaning wheel radius or module zeros.

Each one was localized by a graph before anyone read code.

## Competition triage

At an event you get about six minutes between matches, and the robot has to be on the field for the next one. The order changes:

:::steps
1. **Ask whether it must be fixed now.** A robot that works with a known annoyance beats a robot with a fresh, untested change.
2. **Check the simple things first**, in this order: battery, connectors, CAN chain, alerts on the dashboard, the auto selection.
3. **Look at the last match's log** for the ten seconds around the symptom. Not the whole log.
4. **Prefer a change you can undo.** A dashboard value beats a deploy; a deploy beats a rewrite.
5. **If you deploy, re-verify the basics**: enable, drive a few feet, run the mechanism once, confirm the auto selection.
6. **Tell the drive team exactly what changed**, in one sentence.
:::

:::danger The rule that saves matches
Never deploy code you have not run, no matter how small the change, unless not deploying is worse. "It's one line" is how a robot ends up disabled in a quarterfinal. If you must, have a second person read the diff out loud before it goes on the robot.
:::

## Writing it down

A finding is worth writing down when the next person would otherwise rediscover it. Keep it short and structured:

> **Symptom.** Turret aimed at the HUB while paused near midfield, instead of at the passing target.
> **Evidence.** Replay of Iowa match 32; `Turret State` shows `PAUSEDSHOOTING` past the alliance zone line.
> **Cause.** Both branches of `case PAUSED` yield `PAUSEDSHOOTING`, so `PAUSEDPASSING` is unreachable.
> **Fix.** Second branch yields `PAUSEDPASSING`. One line.
> **Would have caught it.** Asking "can every state in this enum happen?" during review.

That last line is the one that makes the team better instead of just fixing the robot, and it is why this course has a [code audit](course:reference/code-audit) at all: every finding in it is a piece of this exact form.

:::exercise id="u11-analyze"
Write the log questions you keep asking: loop timing with overruns, when a condition first became true, the worst tracking error between a setpoint and a measurement, and how often a mechanism was actually within tolerance.

These four functions are the arithmetic behind "was the flywheel ready when we shot?" and "did we overrun the loop during autonomous?"
---hint
Loop durations are the gaps between consecutive timestamps, so a run of N timestamps has N − 1 loops. Guard against fewer than two samples.
---hint
`firstTrue` returns `Double.NaN` when the condition never happened. Use `Double.isNaN` to check for it; comparing with `==` always fails.
---hint
`fractionWithin` counts samples within tolerance and divides by the total. Decide what an empty series should return before you write it.
:::

:::quiz
?order Put the debugging method in order.
1. Write down the symptom in the reporter's words
2. Reproduce it, or find it in a log
3. Observe the values around the moment
4. Localize it to a layer
5. Change one thing and test
6. Verify on the real robot and write it down
> Guessing before observing is the most common way to lose an afternoon.

? The log shows the turret's setpoint jumping to a wrong angle. Which layer should you investigate?
+ Logic: whatever computed the setpoint
- Control: the turret's PID gains
- Hardware: the turret motor
- Environment: the field's lighting
> A wrong setpoint means the decision was wrong before the controller ever saw it.

? The setpoint is correct, but the measurement lags far behind and never gets there. Which layer?
+ Control, or possibly hardware if the mechanism is binding
- Logic
- The dashboard layout
- The log's metadata
> Correct command, poor execution, is the control signature.

? It's between matches and a mechanism is misbehaving. What is the first question?
+ Whether it has to be fixed before the next match at all
- Which gain to change
- Whether to rewrite the subsystem
- Whether to swap the roboRIO
> A known annoyance beats an untested change.

? Why does a written finding include "what would have caught this sooner"?
+ It turns one bug fix into a change in how the team works
- It is required for the engineering notebook
- It helps the driver
- It makes the log smaller
> That line is the difference between fixing a robot and improving a team.

?tf A one-line change is safe to deploy between matches without running it.
= false
> "It's one line" is how a robot ends up disabled in a playoff match.
:::
