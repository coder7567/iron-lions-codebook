---
summary: Make code choose, using comparisons, logical operators, if/else chains, the ternary operator, and both kinds of switch that appear in our robot code.
objectives:
  - Write boolean expressions with comparison and logical operators, including short-circuit checks
  - Choose between if/else chains, the ternary operator, and switch
  - Read classic switch statements and modern switch expressions with arrows and yield
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/Robot.java
---

## Booleans and comparisons

A `boolean` is `true` or `false`. Comparisons produce booleans:

| Operator | Meaning | Example |
|---|---|---|
| `==` | equal to | `jamCount == 0` |
| `!=` | not equal to | `previousAlliance != alliance` |
| `<`, `<=` | less than (or equal) | `matchTime <= 30` |
| `>`, `>=` | greater than (or equal) | `inputs.intakeCurrent > jamCurrent` |

Combine them with **logical operators**:

| Operator | Meaning | True when |
|---|---|---|
| `&&` | and | both sides are true |
| `\|\|` | or | at least one side is true |
| `!` | not | the value is false |

```java
boolean jamSuspected = inputs.intakeCurrent > IntakeConstants.jamCurrent
    && inputs.intakeSpeed < IntakeConstants.jamSpeed;
```

High current **and** low speed together mean the rollers are pushing hard but not turning, so FUEL is probably stuck.

### Short-circuiting protects you

`&&` and `||` stop as soon as the answer is known. `a && b` skips `b` when `a` is false, and `a || b` skips `b` when `a` is true. Code uses that to check for danger first:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="gameData = DriverStation.getGameSpecificMessage();" lines=4

If `gameData` is `null`, calling `gameData.isEmpty()` would crash the robot program. Because `gameData == null` comes first, the `||` never reaches `isEmpty()` when there is nothing to call it on. **Order matters.**

## if, else if, else

`if` runs a block only when its condition is true. Add `else if` and `else` to choose among options, checked top to bottom until one matches:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private boolean isPastLine(double robotX, double lineX)" lines=8

Red and blue robots face opposite directions on the field, so "past the line" means a bigger X for red and a smaller X for blue.

:::warning Order of else-if checks matters
Checks run in order and stop at the first match. The HUB timing code checks `matchTime > 130` before `matchTime > 105`. Reverse them and 140 seconds would match `> 105` first and be reported as Shift 1.
:::

:::tip Always use braces
Java lets you skip braces for a one-line `if`, but adding a second line later silently puts it *outside* the `if`. Braces on every branch avoid that trap.
:::

## The ternary operator

For "pick one of two values," the **ternary** operator `condition ? valueIfTrue : valueIfFalse` fits on one line:

::source file="src/main/java/frc/robot/subsystems/intake/Intake.java" from="io.setFeederSpeed(resetting" lines=1

Read it as: *if the turret is resetting, or the flywheel is not up to speed, feed at 0; otherwise feed at `feederSpeed`.* The ternary has the lowest precedence, so the whole `resetting || !flywheelSpedUp` is the condition. Use ternaries for simple choices, and `if` for anything longer.

## switch statements

When you compare one value against several constants, `switch` is clearer than a long `if` chain. The classic form uses `case` labels and `break`:

::source file="src/main/java/frc/robot/Robot.java" from="switch (BuildConstants.DIRTY)" lines=11

:::danger Forgetting break falls through
In a classic switch, execution continues into the next `case` unless you `break`. Without the `break` after case 0, a clean build would log "All changes committed" and then immediately overwrite it with "Uncomitted changes." The compiler does not warn you.
:::

## switch expressions: modern and safer

Newer Java has **switch expressions** that produce a value. With arrows `->` there is no fall-through and no `break`:

::source file="src/main/java/frc/robot/subsystems/drive/ModuleIOSpark.java" from="zeroRotation =" lines=8

Each module number maps to its own zero offset, and `default` covers anything else. A switch expression must handle every possible value, so the compiler catches missing cases.

The colon form uses `yield` to hand back a value. Our `Superstructure` maps each wanted state to a current state this way:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="private CurrentState updateState(WantedState wantedState)" lines=14

Switch expressions can match `char` values too, which is how the HUB logic reads the game data letter: `case 'B' -> ...` and `case 'R' -> ...`.

## Decisions that read well

- **Name booleans as yes/no questions:** `hasTarget`, `isConnected`, `jammed`, `intakeSafe()`.
- **Do not compare booleans to `true`.** Write `if (jammed)`, not `if (jammed == true)`.
- **Compare text with `.equals`, not `==`.** For Strings, `==` asks whether two variables point at the *same object*, not whether the text matches. [Unit 3](course:03-java-objects/strings-and-formatting) explains why.

:::exercise id="u02-matchclock"
Turn a match time into REBUILT periods, using the same boundaries as `Superstructure`.

- `periodName` returns `"AUTO"` in autonomous, otherwise one of `"TRANSITION"`, `"SHIFT 1"` to `"SHIFT 4"`, or `"END GAME"`.
- `shiftNumber` returns 1 to 4 during shifts and 0 otherwise.
- `secondsLeftInPeriod` returns the seconds until the current period ends, never negative.
- `warnHubChange` is true in the last 3 seconds of the transition or any shift. The real HUB lights pulse for 3 seconds before a change, so a driver warning is useful.

The tests use `@ParameterizedTest`, which runs one test method with many inputs. Each row of `@CsvSource` is one case.
---hint
Check boundaries from the top down, like the Superstructure: `matchTime > 130` first, then `> 105`, and so on.
---hint
A time of exactly 130 is **not** greater than 130, so it belongs to Shift 1. Watch `>` versus `>=`.
---hint
For `warnHubChange`, first make sure you are not in the end game (`matchTime > 30`), then compare `secondsLeftInPeriod(matchTime)` with 3.
:::

:::quiz
? Why is `gameData == null || gameData.isEmpty()` safe when `gameData` is null?
+ `||` stops after the first side is true, so `isEmpty()` is never called on null
- `isEmpty()` returns true for null values
- Java converts null to an empty string automatically
- It is not safe; it throws a `NullPointerException`
> Short-circuit evaluation skips the right side once the answer is known.

?code What does this print?
```java
boolean resetting = false;
boolean flywheelSpedUp = true;
double feed = resetting || !flywheelSpedUp ? 0 : 4500;
System.out.println(feed);
```
= 4500.0
> The condition `false || false` is false, so the ternary picks 4500. Assigned to a double, it prints `4500.0`.

? What happens in a classic `switch` statement when a `case` has no `break`?
- It does not compile
+ Execution falls through into the next case
- The switch restarts from the top
- Only the default case runs
> Fall-through is legal in classic switches. Arrow-style switch expressions do not fall through.

? Which boundary checks classify a match time of exactly **80** seconds the way our Superstructure does?
- `> 80` means Shift 2
+ 80 is not `> 80`, so it falls to the `> 55` check: Shift 3
- 80 matches `>= 80`: Shift 2
- 80 is a special case handled separately
> The code uses strict `>`. At exactly a boundary, the next check down applies.

?tf In a switch expression written with arrows (`case 0 -> ...`), you must write `break` after each case.
= false
> Arrow cases never fall through, so `break` is not needed. The colon form of a switch expression returns values with `yield`.
:::
