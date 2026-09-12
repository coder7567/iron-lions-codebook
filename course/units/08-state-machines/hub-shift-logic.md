---
summary: REBUILT turns one HUB off at a time, on a schedule the robot has to work out from the match clock and one character of game data. Here is the rule, our implementation, and where it goes wrong.
objectives:
  - State REBUILT's shift schedule and what the game data character means
  - Read `updateHubStatusAndPeriod` and predict its result for any match time
  - Explain finding F19 and why an approximate match clock limits what you can automate
  - Rehearse shift timing in practice mode
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## The rule

In REBUILT, both HUBs are active during autonomous, the transition at the start of teleop, and the end game. In between, teleop is divided into four **shifts**, and in each shift only one alliance's HUB accepts FUEL.

About three seconds after autonomous ends, the FMS sends one character of **game data**: `R` or `B`. It names the alliance whose HUB is **inactive first**, which is the alliance that scored more FUEL in autonomous. That alliance is then active in shifts 2 and 4; the other alliance is active in shifts 1 and 3.

| Teleop clock | Period | Whose HUB is active |
|---|---|---|
| 140 to 130 | Transition | Both |
| 130 to 105 | Shift 1 | The alliance *not* named in the game data |
| 105 to 80 | Shift 2 | The alliance named in the game data |
| 80 to 55 | Shift 3 | The alliance not named |
| 55 to 30 | Shift 4 | The alliance named |
| 30 to 0 | End game | Both |

Shooting into an inactive HUB scores nothing, so the robot needs to know which period it's in, and so does the driver.

## Reading the game data

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="// Update autoWinColor based on game data" lines=10

Two habits here are worth copying. The code reads `charAt(0)` rather than comparing whole strings, and every `switch` has a `default` branch, because game data is a string from outside our code. Before the FMS sends it, it's empty. In a practice match without an FMS, it stays empty all match. `periodic()` also converts a `null` into `""` before anything touches it, so no other line has to worry about null.

An empty string falls through to `false`, which means "blue is inactive first." That is a guess, not a fact, and it is why the rumble warning is advisory rather than something the robot acts on by itself.

## The schedule in code

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="// No alliance? hub inactive." lines=17

The early exits come first, and each one answers a question the later code would otherwise get wrong:

- **No alliance yet?** The Driver Station hasn't connected, so nothing is known. Inactive, no timer.
- **Autonomous?** Both HUBs are active, and the timer counts down the auto period.
- **Not teleop?** Disabled between periods. Inactive, no timer.

Then the ladder:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="// Determine hub status and period timer based on match time" lines=21

Each branch answers both questions at once: is our HUB active, and how many seconds are left in this period? The second number is what a dashboard can turn into a countdown, so the driver knows whether to start lining up a shot or keep collecting.

`shift1Active` is the whole alliance rule in one line: red is active in shift 1 unless red is the one inactive first, and blue is the opposite. Every other shift alternates from there.

:::warning Finding F19: the flag and the return value disagree
The method **returns** the right answer, and `periodic()` logs that return value. But the field `hubActive`, which `getRumble()` reads, is assigned **only inside the teleop ladder**. The three early exits return `false` or `true` without touching it.

So in autonomous, or while disabled, `getRumble()` keeps comparing against whatever `hubActive` was at the end of the last teleop period. It's a small bug, because rumble hardly matters while disabled, but it's the kind that gets copied into code where it matters more.

The fix is to assign the field on every path, or better, to stop keeping a field at all: make the method pure, return a small record with both answers, and store it once per loop.
:::

## The match clock is approximate

`DriverStation.getMatchTime()` is not an official timer. The FMS sends an approximate countdown, and WPILib's documentation is explicit that it should not be used for anything critical. Near a boundary, the robot can believe it is in shift 2 for a moment while the field still says shift 1.

That is fine for the design in this code, because the match clock drives a rumble and a dashboard number, not an automatic behavior. It would not be fine for "stop the flywheel when our HUB goes inactive." Two useful habits:

- Let the driver be the one who acts on advisory information.
- If a decision must be exact, tie it to something the robot can measure, not to an approximate clock.

:::tip Rehearsing the shifts
The Driver Station's practice mode can reproduce a REBUILT match: 20 seconds of autonomous, 140 seconds of teleop, and a 30-second end game warning. Practice mode never sends game data, so to rehearse both cases, temporarily fake it while testing and remove the fake before you commit. Watching `Hub Active` and `Period Time` on the dashboard through a full practice match is the fastest way to confirm the schedule.
:::

:::exercise id="u08-hub"
Rewrite the HUB schedule as a pure function: same rules, same boundaries, no `DriverStation` calls, and no fields left over between calls. It returns both answers at once, so there is nothing to keep stale.

The tests walk both alliances through both game-data values, every boundary time, and the cases where information is missing.
---hint
Handle the exits in the same order as our code: unknown alliance, then autonomous, then anything that isn't teleop. Each returns immediately.
---hint
`shift1Active` is one line: on red it's `!redInactiveFirst`, and on blue it's `redInactiveFirst`.
---hint
Write the ladder from the largest match time down, and pair each branch's active flag with `matchTime` minus that branch's boundary. The end game returns `matchTime` itself.
:::

:::quiz
? The game data is `R`. Whose HUB is active in shift 1?
+ Blue's, because `R` names the alliance that is inactive first
- Red's
- Both
- Neither
> The named alliance sits out shift 1 and is active in shifts 2 and 4.

?num Our alliance's HUB is active, and the teleop clock reads 92 seconds. How many seconds remain in the current period?
= 12 ± 0.5 s
> 92 is inside shift 2, which ends at 80.

? Why do both `switch` blocks that read game data have a `default` branch?
+ Game data comes from outside our code, and it can be empty or unexpected
- Java requires a default in every switch
- To handle a third alliance
- Because the FMS sends lowercase letters
> Treat any external input as possibly missing or malformed.

? During autonomous, what does `getRumble()` compare against?
+ A stale `hubActive` field, because the autonomous path returns early without updating it
- The correct value, because the method returns true in autonomous
- Nothing; rumble is disabled in autonomous
- The game data character
> That's finding F19: the return value and the field disagree.

? Why shouldn't the robot automatically stop shooting when its HUB goes inactive?
+ The match clock from the FMS is approximate, so an automatic cutoff could misfire near a boundary
- Because the rules forbid automation
- Because the flywheel takes too long to restart
- Because the Superstructure has no access to the match time
> The code warns the driver instead, and the driver decides.

?tf With no game data (an empty string), the code behaves as though blue's HUB is inactive first.
= true
> An empty string makes `redInactiveFirst` false, so `shift1Active` is false for blue and true for red: exactly what a `B` would produce. It is a fallback, not information, which is another reason the robot only warns the driver.
:::
