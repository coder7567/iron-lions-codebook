---
summary: The drive team is the software team's user. How to run a practice session with them, turn what they say into something measurable, and keep the trust that makes them tell you things.
objectives:
  - Translate driver language into measurable robot behavior
  - Run a practice session that produces decisions
  - Debrief a match in a way people will repeat
  - Keep changes predictable for the people driving
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Who is on it

| Role | Cares about |
|---|---|
| **Driver** | How the robot moves and whether it goes where they point it |
| **Operator** | Mechanisms, the state the robot is in, and the turret trim |
| **Coach** | Strategy, the alliance, and the match clock |
| **Human player** | Feeding FUEL, and the timing around it |
| **Technician** | The robot between matches, and what changed |

Every one of them is a user of your code, and each of them notices different failures. The operator is
usually the first to notice a state machine problem, because they are the one watching the robot do
something it was not asked to do.

## Translating

Drivers describe feelings. That is not imprecision; it is the only vocabulary available for
"something is wrong and I do not know the internals." Your job is the translation.

| They say | Ask | It usually means |
|---|---|---|
| "It's sluggish" | Coming out of a stop, or at speed? | Current limit, feedforward, or a low battery |
| "It's fighting me" | While turning, or while driving straight? | Module zero, or the heading controller |
| "It shoots late" | From a stop, or while moving? | Spin-up time, the feeder gate, or the flywheel tolerance |
| "It won't shoot" | Does the controller buzz? | The deadzone rumble, an inactive HUB, or a jam |
| "The controls are backwards" | Right after the match started? | The pose's heading and the alliance flip |
| "It stopped" | All at once, or one mechanism? | A brownout, or a CAN disconnect |

The follow-up question matters more than the first answer. "It's sluggish" has four causes, and one
question eliminates three of them.

## Practice sessions

A practice session without an agenda becomes twenty minutes of driving in circles. One with an agenda
produces decisions.

:::steps
1. **Write the three questions** the session is meant to answer, before the robot leaves the cart.
   "Does the new auto finish in time?" is a question. "Test stuff" is not.
2. **Assign a log watcher.** Someone at a laptop with AdvantageScope, not driving, not shagging FUEL.
3. **Run drills, not free driving.** The drills in [Driving Well](course:10-swerve/driving-well) each
   answer something specific.
4. **Stop after each drill** and ask the driver one question while it is fresh.
5. **Write the answers down** with the date, and note the battery.
6. **End with the three questions answered**, or write down what stopped you.
:::

## The debrief

Two minutes after a match, standing at the cart, in this order:

:::steps
1. **The driver speaks first**, uninterrupted. Programmers explaining before the driver has finished is
   the fastest way to stop hearing about problems.
2. **Ask for the moment**: "when in the match?" A time is worth ten adjectives, because it points at a
   place in the log.
3. **Say what you will check**, not what you will change.
4. **Report back after you look**, even when the answer is "the log looks normal." A question that
   disappears trains people to stop asking.
:::

## Keeping trust

- **Never change a binding without telling them.** Muscle memory is real, and a button that does
  something new mid-event is worse than a button that does nothing.
- **Say what changed, in one sentence,** every time you deploy. "The turret trim now resets when you
  press start" is enough.
- **Do not fix things they did not report** during an event. If it was not bothering them, it was not
  worth the risk.
- **Be honest about uncertainty.** "I think it was a brownout, and I will know after the next log" is
  better than a confident guess that turns out wrong.
- **Practice what you ship.** A change that lands the night before an event has never been driven by
  the person who has to drive it.

:::team The rumble is a conversation
Our robot already talks to the driver: the controller buzzes when the turret is shooting into an
inactive HUB, when the intake is jammed, or when the target is in the turret's deadzone. That is three
different problems on one channel.

Make sure the drive team knows all three, or the rumble becomes noise they learn to ignore. A signal
whose meaning nobody knows is worse than no signal, and adding a fourth condition to it without telling
anyone is how that happens.
:::

:::quiz
? A driver says the robot feels sluggish. What is the most useful next question?
+ "Coming out of a stop, or once you are moving?"
- "How sluggish?"
- "Which match?"
- "Do you want me to raise the speed limit?"
> One question eliminates most of the possible causes.

? Who usually notices a state machine problem first?
+ The operator, who watches the mechanisms do something they did not ask for
- The driver
- The coach
- The pit programmer
> Each role sees a different class of failure.

? What makes a practice session productive?
+ Three written questions it is meant to answer, and someone watching the log
- Maximum driving time
- Testing every subsystem
- A fresh battery for each drill
> Free driving produces impressions; drills produce decisions.

? In a debrief, why should the driver speak first and uninterrupted?
+ Explaining before they finish is the fastest way to stop hearing about problems
- They have the most authority
- It is faster
- The coach requires it
> A question that disappears trains people to stop asking.

? Why report back even when the log looked normal?
+ Otherwise the drive team learns that reporting something leads nowhere
- The log is never normal
- It is required by the checklist
- It gives the coach data
> Closing the loop is what keeps the reports coming.

?tf Fixing an unreported annoyance during an event is a good use of downtime.
= false
> If it was not bothering them, it is not worth the risk of an untested change.
:::
