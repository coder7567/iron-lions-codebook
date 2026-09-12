---
summary: What a state machine is, why our subsystems are built from wanted states and current states, and how Java's enums and switch expressions keep them honest.
objectives:
  - Describe a state machine as states, transitions, inputs, and outputs
  - Explain the wanted-state / current-state split our subsystems use
  - Write `updateState` and `applyState` methods that keep deciding and acting separate
  - Use switch expressions so the compiler catches states you forgot to handle
files:
  - src/main/java/frc/robot/subsystems/intake/Intake.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## A state machine is four things

| Part | Meaning | Intake example |
|---|---|---|
| **States** | The finite set of things the mechanism can be doing | `IDLE`, `PAUSED`, `INTAKING`, `REVERSING` |
| **Transitions** | The rules that move between states | 50 loops of high current with the rollers stalled means "jammed" |
| **Inputs** | What the rules look at | The wanted state, motor current, roller speed, arm position |
| **Outputs** | What the robot does in each state | Arm angle, roller speeds, feeder speed |

The important word is **finite**. At any moment, the mechanism is in exactly one state, and you can name it. That single fact makes robot behavior explainable: when something goes wrong, you ask "what state was it in?" instead of guessing which of a dozen booleans was true.

Without state machines, mechanism code turns into a pile of flags: `isIntaking`, `wasJammed`, `armShouldBeOut`, `feederAllowed`. Four booleans have 16 combinations, and most of them are nonsense you never meant to allow. Four *states* have four.

## Wanted and current

Our subsystems split the idea in two:

| | Set by | Meaning |
|---|---|---|
| `WantedState` | Commands, the Superstructure, autos | What someone is **asking for** |
| `CurrentState` | The subsystem, every loop | What the mechanism is **actually doing** |

They are different on purpose. The driver can ask for `SHOOTING`, but the turret decides whether that means `SHOOTING` at the HUB or `PASSING` down the field, based on where the robot is. The Superstructure can ask for `INTAKING`, but the intake reverses instead when it detects a jam.

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public enum WantedState" lines=14

Notice that the two enums are not the same size. `WantedState` has a `TESTING` request that `CurrentState` doesn't have, because testing just runs the intaking behavior with jam detection turned off.

## One loop of a state machine

::diagram name="state-machine-loop" caption="Every mechanism subsystem in our code follows this shape."

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public void periodic()" lines=6

Four lines, in a fixed order:

1. **Read.** `io.updateInputs(inputs)` fills a plain object with every sensor value for this loop.
2. **Log.** `Logger.processInputs` records those inputs so a replay can feed them back later.
3. **Decide.** `updateState()` returns the current state. It reads inputs and counters; it does **not** move motors.
4. **Act.** `applyState()` sends outputs for whatever state was chosen. It does **not** make decisions.

Keeping steps 3 and 4 apart is the whole trick. Deciding is the part with the interesting logic, and because it only reads values and returns a state, you can test it without a robot, which is exactly what this unit's exercises do.

## Java tools: enums and switch expressions

Our `updateState` methods are **switch expressions**: they produce a value rather than running statements.

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private CurrentState updateState(WantedState wantedState)" lines=14

Two forms show up in our code, and they mean the same thing:

```java title="Colon form with yield, and arrow form"
// The form our subsystems use.
return switch (wantedState) {
  case IDLE:
    yield CurrentState.IDLE;
  case SHOOTING:
    yield CurrentState.SHOOTING;
};

// The shorter arrow form. No fall-through, no break.
return switch (wantedState) {
  case IDLE -> CurrentState.IDLE;
  case SHOOTING -> CurrentState.SHOOTING;
};
```

:::tip The compiler is on your side
A switch **expression** over an enum with no `default` must cover every constant. Add `CLIMBING` to `WantedState`, and the build fails until you decide what it means. A switch **statement** with a `default` branch compiles happily and silently sends your new state to the default behavior. Our `updateState` methods are expressions, and our `applyState` methods are statements with a `default`. That's a reasonable split, but it means a new state gets a decision automatically and needs its outputs written by hand.
:::

## Rules we follow

1. **`updateState` decides; `applyState` acts.** If `updateState` calls `io.setX(...)`, testing it means owning hardware.
2. **Log both states every loop.** `Logger.recordOutput("Wanted State", ...)` and `"CurrentState"` are what let you scrub a log in AdvantageScope and see exactly when a state changed.
3. **Every state needs outputs.** A state with no branch in `applyState` leaves the motors doing whatever they did last.
4. **Every state must be reachable.** If nothing can ever return a state, that state is a comment, not behavior. The turret has one: `PAUSEDPASSING` has a full `applyState` branch, but both `PAUSED` branches return `PAUSEDSHOOTING`, so it never runs. That's finding F1, and [Turret States](course:08-state-machines/turret-state-machine) fixes it.
5. **Clear counters when you leave a state.** The intake resets `jamCount` and `unjamCount` whenever the wanted state isn't `INTAKING`, so a jam from two minutes ago can't decide something now.

:::exercise id="u08-basics"
Build a small wanted-state / current-state machine for a flywheel and feeder. It should spin up before the feeder runs, the same way our intake waits for `turret::shooterSpedUp` before feeding.

Keep deciding and acting separate: `updateState` returns a state, and `applyState` turns a state into outputs.
---hint
`updateState` is a switch expression over the wanted state. For `SHOOT`, compare the measured speed against the setpoint the same way `Turret.shooterSpedUp` does: `measuredRpm - SHOT_RPM > -TOLERANCE_RPM`.
---hint
`applyState` is a switch expression over the current state that returns an `Outputs` record. Give every state its own outputs, including the feeder flag.
---hint
`periodic` is only two lines: save the result of `updateState` into the `currentState` field, then return `applyState(currentState)`.
:::

:::quiz
? Why do subsystems keep a wanted state and a current state instead of just one state?
+ Requests come from outside the subsystem, but the subsystem decides what is actually safe or sensible to do right now
- Two enums make logging faster
- WPILib requires both
- The wanted state is used in autos and the current state in teleop
> The driver asks for `SHOOTING`; the turret decides between `SHOOTING` and `PASSING` based on position.

?order Put one loop of a state-machine subsystem in order.
1. `io.updateInputs(inputs)`
2. `Logger.processInputs(...)`
3. `currentState = updateState()`
4. `applyState()`
> Read, log, decide, act. Deciding never moves motors.

? What happens when you add a new constant to `WantedState` and forget to handle it in a switch **expression** with no `default`?
+ The code doesn't compile
- It runs and returns null
- It runs and uses the first case
- It throws an exception at startup
> Exhaustiveness checking is the reason to prefer switch expressions for decisions.

? What happens when you add a new constant to `CurrentState` and forget to add a branch to `applyState`, which is a switch statement with a `default`?
+ It compiles, and the state quietly runs the default behavior
- It doesn't compile
- The scheduler skips that subsystem
- The mechanism keeps its previous outputs
> This is why the "every state needs outputs" rule is worth checking by hand.

?tf `updateState` may send motor outputs as long as it also returns a state.
= false
> Mixing the two makes the decision logic impossible to test off the robot, and it hides outputs where nobody looks for them.

? The intake resets `jamCount` and `unjamCount` whenever the wanted state is not `INTAKING`. Why does that matter?
+ Otherwise counters from an earlier state could trigger a jam response long after the situation ended
- It saves memory on the roboRIO
- Because the scheduler requires counters to be reset
- It makes the logs shorter
> State machines need their working variables cleaned up on the way out of a state.
:::
