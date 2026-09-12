---
summary: Represent a fixed set of choices, like robot modes and mechanism states, with enums; switch over them safely; and give enums their own data and methods.
objectives:
  - Declare enums and use them in comparisons and switches
  - Explain why an exhaustive switch expression catches a forgotten state at compile time
  - Add fields, constructors, and methods to an enum, and use values(), name(), and ordinal()
files:
  - src/main/java/frc/robot/Constants.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/intake/Intake.java
---

## A type with a fixed set of values

Some things have only a few possible values: the robot runs for **real**, in **simulation**, or in **replay**. You could use numbers (0, 1, 2) or text ("real", "sim"), but nothing would stop someone from writing 7 or "simulation". An **enum** makes the set exact:

::source file="src/main/java/frc/robot/Constants.java" from="public static enum Mode" lines=10

`Mode` has exactly three values: `Mode.REAL`, `Mode.SIM`, and `Mode.REPLAY`. A variable of type `Mode` can hold nothing else, and a typo like `Mode.SIMULATION` is a compile error instead of a bug.

Enum constants are written in `UPPER_SNAKE_CASE` by convention. WPILib and REVLib use a `k` prefix instead, as in `IdleMode.kBrake`, `MotorType.kBrushless`, and `ControlType.kVelocity`. They are enums too.

## Enums describe mechanism states

Our subsystems use enums for their state machines. The Superstructure has two:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="public enum WantedState" lines=15

**WantedState** is what the driver asked for. **CurrentState** is what the robot is actually doing. They look identical here, but in `Turret` and `Intake` they differ. For example, the intake's `TESTING` wish becomes an `INTAKING` current state without jam detection. [Unit 8](course:08-state-machines/state-machine-basics) is all about this pattern.

## Using enums

Compare enum values with `==`. Every enum constant is a single shared object, so `==` is safe and correct here, unlike with Strings:

```java
if (turret.getCurrentState() == Turret.CurrentState.SHOOTING && !hubActive) {
    rumble();
}
```

Enums shine in `switch`. Inside the `case` labels you write just the constant name, without the enum's name in front:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private void applyState()" lines=32

## Exhaustive switches catch forgotten states

Suppose next season someone adds `CLIMBING` to `WantedState`. What breaks?

- `updateState` uses a **switch expression**. A switch expression must handle every enum value, so the build **fails** with an error until someone writes the `CLIMBING` case. Good, because the compiler found the gap.
- `applyState` is a **switch statement** with a `default` branch. It compiles fine, and `CLIMBING` quietly falls into `default`, setting turret and intake to IDLE. The robot would just refuse to climb, with no error anywhere.

:::tip Let the compiler help
For state machines, prefer switch expressions or switch statements without `default`, so adding a state forces you to decide what it does everywhere.
:::

## Built-in enum methods

Every enum gets these for free:

| Method | Returns | Example with `CurrentState.SHOOTING` |
|---|---|---|
| `name()` | The constant's exact name | `"SHOOTING"` |
| `toString()` | Same as `name()` unless overridden | `"SHOOTING"` |
| `ordinal()` | Position in the declaration, from 0 | `2` |
| `values()` | Array of all constants, in order | `[IDLE, PAUSED, SHOOTING, EJECTING, TESTING]` |
| `valueOf("PAUSED")` | The constant with that exact name | `PAUSED`, or an `IllegalArgumentException` if none matches |

AdvantageKit logs enums by name. That is why `Logger.recordOutput("CurrentState", getCurrentState())` shows readable text like `SHOOTING` in AdvantageScope.

:::warning Do not rely on ordinal() for saved data
Inserting a new constant in the middle shifts every later `ordinal()`. Use `ordinal()` for things like "cycle to the next mode," never for values saved to files or logs.
:::

## Enums with data and behavior

Enums are classes, so they can have fields, a constructor, and methods. Each constant passes its own values to the constructor:

```java
public enum ShotPreset {
    HUB_CLOSE(1850, 0.879),
    HUB_FAR(2700, 0.641);

    private final double rpm;
    private final double hoodAngle;

    ShotPreset(double rpm, double hoodAngle) {   // enum constructors are always private
        this.rpm = rpm;
        this.hoodAngle = hoodAngle;
    }

    public double getRpm() { return rpm; }
}

double speed = ShotPreset.HUB_CLOSE.getRpm();   // 1850.0
```

Compare that with how the intake keeps its speeds today. The state lives in `Intake`, the speeds live in `IntakeConstants`, and methods like `intake()` and `reverse()` connect the two:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="public enum WantedState" lines=14

Nothing is wrong with that design. It is common and works well. But when each state carries its own numbers, a state and its speeds cannot drift apart.

:::team In our code
`Turret.CurrentState` includes `PAUSEDPASSING`, but no code path ever produces it: both branches of the PAUSED case yield `PAUSEDSHOOTING`. The compiler cannot catch an enum value that is never *used*. That is finding **F1** in the [Code Audit](course:reference/code-audit), and you will fix a copy of it in [Unit 8](course:08-state-machines/reviewing-state-machines).
:::

:::exercise id="u03-rollermode"
Give an enum its own data and behavior.

- Fill in the real speeds for `INTAKING` and `REVERSING`.
- `isMoving()` is true when either speed is nonzero.
- `armDeployed()` is true for every mode except `STOPPED`.
- `fromText(text)` finds a mode by name, ignoring capitalization and extra spaces, and falls back to `STOPPED`.
- `next()` cycles through the modes, like a driver button that steps through options.
---hint
Compare an enum value with `this != STOPPED` inside the enum.
---hint
For `fromText`, check for `null` first, then loop `for (RollerMode mode : values())` and use `mode.name().equalsIgnoreCase(text.trim())`.
---hint
For `next()`, `values()[(ordinal() + 1) % values().length]` wraps from the last mode back to the first.
:::

:::quiz
? Why is an enum better than an `int` code for the robot's mode?
+ Only the declared values are possible, so invalid modes and typos become compile errors
- Enums run faster than ints
- Enums can be changed at runtime from the dashboard
- An `int` cannot be used in a switch
> A `Mode` variable can only hold `REAL`, `SIM`, or `REPLAY`.

? Someone adds `CLIMBING` to `Superstructure.WantedState`. Which method fails to compile until it handles the new value?
+ `updateState`, because its switch expression must cover every value
- `applyState`, because its switch has a default branch
- Both
- Neither, because Java ignores new enum values
> Switch expressions must be exhaustive. A statement with `default` quietly sends `CLIMBING` to the default branch.

?code What does this print?
```java
enum Mode { REAL, SIM, REPLAY }
// ...
System.out.println(Mode.REPLAY.ordinal() + " " + Mode.SIM.name());
```
= 2 SIM
> `REPLAY` is the third constant, so its ordinal is 2, and `name()` returns the constant's exact name.

?tf `Mode.valueOf("sim")` returns `Mode.SIM`.
= false
> `valueOf` needs the exact name, `"SIM"`. Anything else throws an `IllegalArgumentException`.

? Which statement about comparing enum values is correct?
- You must use `.equals()`, as with Strings
+ `==` is correct, because each enum constant is a single shared object
- You must compare `ordinal()` values
- Enums cannot be compared
> There is only ever one `CurrentState.SHOOTING` object, so `==` works.
:::
