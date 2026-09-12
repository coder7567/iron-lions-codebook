---
summary: Style is what lets the next person read your code in a hurry. Here is what our repository is consistent about, what it is not, and a style guide short enough that the team would actually follow it.
objectives:
  - Explain why consistency matters more than any particular style
  - Use a formatter instead of arguing about formatting
  - Write comments and javadoc that earn their place
  - Decide what to do with commented-out code
files:
  - src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## Who style is for

Not the compiler, and not the author. Style is for the person who opens this file at 9 p.m. before an event, needs to find one thing, and has never seen the file before. On a team that graduates a third of its programmers every year, that person is always about to exist.

Consistency beats correctness here. Two-space or four-space indentation does not matter; **mixing them in one project does**, because every inconsistency is a small question the reader has to answer before they can read.

## Where our repository stands

**Consistent:** package layout, the subsystem and IO naming pattern, `WantedState` and `CurrentState` in every state machine, constants collected in a `*Constants` class per subsystem, and IO methods named `setX` and `updateInputs`. Those conventions are strong enough that a new file almost writes itself.

**Inconsistent:** indentation width (most files use two spaces; `IntakeIOSpark` uses three), log key naming (`Drive/Module0` next to `Wanted State` and `CurrentState`), and javadoc coverage.

::source file="src/main/java/frc/robot/subsystems/intake/IntakeIOSpark.java" from="protected SparkMax horizontal1;" lines=4

None of that is wrong. All of it is friction.

## Let a formatter decide

The fix for formatting arguments is to stop having them. Spotless with `googleJavaFormat` is what WPILib's own projects and the AdvantageKit template use, and it is a few lines in `build.gradle`:

```groovy title="What our build is missing"
plugins {
  id "com.diffplug.spotless" version "6.25.0"
}

spotless {
  java {
    target fileTree(".") { include "**/*.java" exclude "**/build/**", "**/build-*/**" }
    toggleOffOn()
    googleJavaFormat()
    removeUnusedImports()
    trimTrailingWhitespace()
    endWithNewline()
  }
}
```

Then `./gradlew spotlessApply` formats everything and `./gradlew spotlessCheck` fails a build that drifts. Adopt it in one commit that touches every file and changes no behavior, on a quiet day, never mid-event.

## Names

| Kind | Rule | Our examples |
|---|---|---|
| Classes | Say what it is | `TurretIOSpark`, `AprilTagVision` |
| Methods | Say what it does, verb first | `setWantedState`, `updateHubStatusAndPeriod` |
| Constants | Include the units or the frame | `maxSpeedMetersPerSec`, `driveEncoderPositionFactor` |
| Booleans | Read as a question | `intakeSafe`, `hasTarget`, `isConnected` |
| Log keys | Group with a slash | `Drive/Module0/driveConnected` |

`maxSpeedMetersPerSec` is the pattern worth copying everywhere: **the unit is in the name**, so nobody has to guess and nobody has to look. Compare it with `flywheelVolts`, which holds a duty cycle from the wrong motor (finding F3). A name that lies costs more than no name.

Typos travel, too. `intakeContoller` appears three times because the second and third copies were written from the first. Harmless in a field name; a typo in a **log key or a NetworkTables topic** silently breaks a dashboard binding with no error anywhere.

## Comments that earn their place

Twelve files in the repository still carry the WPILib template's placeholder:

```java
/** Add your docs here. */
```

That is worse than nothing, because it looks like documentation. Delete it or replace it.

The best comments in this repository are the ones carrying **history and procedure**, which no amount of clean code can convey:

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="//methods for getting the correct translation based on alliance color" lines=1

That parenthetical about bad data from February 7 explains why those methods exist, and it will stop someone from "simplifying" them back into constants. The time-of-flight tuning procedure and the note that the hood offset exists because an encoder got messed up are the same kind: **they answer why, and the code can only show what.**

| Comment type | Keep? |
|---|---|
| Why a constant has its value | Yes, always |
| A procedure for re-measuring something | Yes |
| History: what went wrong and when | Yes |
| Units and frames, when the name cannot carry them | Yes |
| A restatement of the code | No |
| `// TODO` with no name or date | No; make it an issue |
| Placeholder javadoc | No |

## Commented-out code

Three kinds appear in our repository, and they deserve different treatment:

| Example | Verdict |
|---|---|
| Two earlier shot map tables, above the live one | **Keep**, with a dated comment: they are calibration history you compare against |
| Commented-out bindings in `RobotContainer` | **Delete**: git has them, and they confuse the binding list |
| The turret's commented backlash compensation | **Delete or finish**: half an algorithm is a trap for the next reader |

The general rule: commented-out code is allowed when it is **data with a story**, and not when it is **logic somebody might uncomment**.

## A style guide the team would actually follow

Short enough to fit on one page, and every item is checkable:

:::steps
1. Run `./gradlew spotlessApply` before committing.
2. Put units or frames in constant names, or in the javadoc if the name would get silly.
3. Javadoc every public method on a subsystem, saying why rather than what.
4. Log keys are `Subsystem/Thing`, no spaces, consistent capitalization.
5. No commented-out logic; delete it and let git keep it.
6. No placeholder comments; write one or remove it.
7. One behavior per commit, and a message that says what changed and why.
:::

:::quiz
? Why does consistency matter more than the particular style chosen?
+ Every inconsistency is a question the reader has to answer before they can read
- Consistent code runs faster
- The compiler enforces it
- It reduces the number of files
> Two-space or three-space does not matter; mixing them does.

? What does adopting Spotless buy the team?
+ Formatting stops being a discussion, and a build can fail when the code drifts
- Faster compilation
- Automatic javadoc
- Fewer merge conflicts on logic
> Adopt it in one commit that changes no behavior, on a quiet day.

? Why is `maxSpeedMetersPerSec` a better name than `maxSpeed`?
+ The unit is in the name, so nobody has to guess or look it up
- It sorts better
- It matches WPILib's internal naming
- Longer names are always clearer
> Compare it with `flywheelVolts`, which is neither the flywheel's nor volts.

? Which comment is worth keeping?
+ A note explaining that the alliance-aware getters exist because data taken on 2/7 was wrong
- `/** Add your docs here. */`
- A comment restating what the next line does
- `// TODO fix this` with no name or date
> Comments should answer why; the code already shows what.

? What should happen to the two older shot map tables commented above the live one?
+ Keep them, with a dated note: they are calibration history worth comparing against
- Delete them; git has the history
- Move them into a test
- Uncomment the most recent one
> Data with a story is the one kind of commented-out code worth keeping.

? Why is a typo in a log key worse than a typo in a field name?
+ It silently breaks dashboard bindings and log comparisons, with no error anywhere
- The compiler rejects it
- It makes the log file larger
- NetworkTables refuses to publish it
> A field name typo is cosmetic; a key typo is a broken contract with another system.
:::
