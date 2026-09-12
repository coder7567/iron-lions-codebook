---
summary: The parts of the 2026 game that our code has to understand, including match timing, HUB shifts, game data, field coordinates, and alliance flipping.
objectives:
  - Explain the REBUILT match timeline and when each alliance's HUB is active
  - Interpret the game-specific message the FMS sends after AUTO
  - Locate field elements in WPILib field coordinates and explain alliance flipping
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
  - src/main/java/frc/robot/util/AllianceFlipUtil.java
---

## Why programmers read the game manual

Mechanical designers read the manual to learn what the robot may *be*. Programmers read it to learn what the robot must *know*: how long each period lasts, which targets count, where things are on the field, and what information the field sends the robot.

Every one of those rules shows up in Rebuilt-2026 as code. This lesson covers the rules our code depends on. It is not a full game summary. For scoring details and robot rules, always use the current *2026 Game Manual* and its Team Updates.

## The field in one picture

REBUILT is played on a carpeted field about **16.54 m long and 8.07 m wide** (651.2 × 317.7 inches). Each alliance has:

- an **ALLIANCE ZONE** 4.03 m deep in front of its driver station wall,
- a **HUB**, the goal FUEL is scored into, whose near face sits 4.03 m from the alliance wall,
- a **DEPOT** where FUEL waits, an **OUTPOST** where the human player feeds FUEL, and a **TOWER** to climb.

Between the alliance zones is the **NEUTRAL ZONE** (7.19 m deep). Robots cross it over two **BUMPS** or under two **TRENCHES**, whose arms leave just **22.25 inches** of clearance. That is why robot height matters to the whole team.

**FUEL** is a 5.91-inch (15.0 cm) foam ball. Thirty-two **AprilTags** (IDs 1–32) on the HUBs, walls, outposts, and trenches let cameras work out where the robot is.

## The match timeline

A match is **20 seconds of AUTO** followed by **2 minutes 20 seconds of TELEOP**. TELEOP is split into a transition, four shifts, and an end game. During the shifts, only one alliance's HUB is **active** at a time.

::diagram name="match-timeline" caption="Both HUBs are active in AUTO, the transition, and the end game. In the shifts, the alliances alternate."

Which alliance goes first is decided by AUTO. **The alliance that scored more FUEL in AUTO has its HUB go inactive first**, in Shift 1. Strong autos earn points early but cost you the first shift. That makes AUTO strategy interesting, as you will see in [Unit 12](course:12-autonomous/auto-strategy).

:::warning Match time is approximate
The field does not send the robot an official clock, only an approximate match time. Our code treats its shift boundaries (130, 105, 80, 55, and 30 seconds remaining) as good estimates, not exact truth. When the Driver Station is in practice mode, set it to 20 s AUTO, 110 s TELEOP, and 30 s END GAME so the timing matches 2026 matches.
:::

## Game data: one letter that changes the match

About three seconds after AUTO ends, the Field Management System (FMS) sends every robot a **game-specific message**. In 2026 it is a single character:

| Message | Meaning |
|---|---|
| `"R"` | The **red** alliance's HUB goes inactive first |
| `"B"` | The **blue** alliance's HUB goes inactive first |
| `""` (empty) | Nothing received yet, usually the first moments of TELEOP |

Robot code reads it with `DriverStation.getGameSpecificMessage()`. Here is how the `Superstructure` turns the letter, our alliance color, and the match time into "is our HUB active right now?":

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="boolean redInactiveFirst" to="return hubActive;"

Read it slowly. It is a direct translation of the timeline above:

- `redInactiveFirst` is `true` when the message starts with `R`.
- `shift1Active` is whether **our** HUB is active in Shift 1. Red is active in Shift 1 only if red was *not* inactive first.
- The chain of `if` statements checks the match time from latest to earliest boundary and flips between `shift1Active` and `!shift1Active`.

:::team In our code
`Superstructure` rumbles the driver's controller when the turret is in its SHOOTING state while our HUB is inactive, because that FUEL would earn nothing. The commit that added it says exactly that: *"Add rumble if scoring in inactive."* You will write and test your own version of this logic in [Unit 8](course:08-state-machines/hub-shift-logic).
:::

## Field coordinates and alliance flipping

WPILib measures the field in **meters** from an origin at the corner of the **blue** alliance wall. **+X** points from the blue wall toward the red wall, and **+Y** points to the left when you stand at the blue wall looking toward red. Positions like "the HUB" are just `(x, y)` pairs in this system.

Our code stores blue-side targets and converts them when we are red:

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="private static final Translation2d hub" lines=8

The HUB is 1.19 m wide, so its center is 4.03 + 0.595 ≈ **4.625 m** from the blue wall. The code's `hub` constant matches in X. In Y it uses 4.0 m, while the AprilTags mounted on the blue HUB put its center at about **4.035 m**. A 3.5 cm difference is small, but it is the kind of detail the [Code Audit](course:reference/code-audit) tracks.

REBUILT's field is **rotationally symmetric**: the red side is the blue side turned 180° about the field's center. To convert a blue-side point to the red side, flip **both** coordinates:

::source file="src/main/java/frc/robot/util/AllianceFlipUtil.java" from="public static double applyX" lines=11

:::details How do we know the field is rotational, not mirrored?
Check the official tag layout. Tag 13 on the red wall is at (16.533, 7.403) and tag 29 on the blue wall is at (0.008, 0.666). Subtracting from the field size gives 16.541 − 16.533 = 0.008 and 8.069 − 7.403 = 0.666. That is a 180° rotation. A mirrored field would keep the same Y value.
:::

A comment in `TurretConstants` explains why these helper methods exist: *"THIS IS WHY OUR DATA FROM 2/7 WAS BAD."* Values measured with the wrong alliance flip ruin a whole day of testing. Always ask, **"Which alliance is this number for?"**

:::quiz
?num How many seconds long is each of the four TELEOP shifts?
= 25 ± 0
> Shift 1 runs from 2:10 to 1:45, Shift 2 from 1:45 to 1:20, and so on, 25 seconds each. The code uses the boundaries 130, 105, 80, 55, and 30 seconds remaining.

? The game-specific message is `"B"` and we are on the **blue** alliance. Is our HUB active during Shift 1?
- Yes, because `B` means blue goes first
+ No, because `B` means blue's HUB is inactive in Shift 1
- It is active for the first half of the shift only
- The message does not affect Shift 1
> `B` means blue's HUB is the one that goes **inactive** first. Blue is active in Shifts 2 and 4, and red in Shifts 1 and 3.

?tf During the 30-second END GAME, both alliances' HUBs are active.
= true
> Both HUBs are active in AUTO, the transition shift, and the end game. The code returns `true` whenever `matchTime` is 30 or less.

? The blue HUB's center is at about (4.626, 4.035). Where is the red HUB's center?
- (4.626, 4.035), the same spot
- (11.915, 4.035), X flipped only, because the field is mirrored
+ (11.915, 4.034), both coordinates flipped, because the field is rotationally symmetric
- (16.541, 8.069)
> Rotating 180° flips both: X becomes 16.541 − 4.626 = 11.915 and Y becomes 8.069 − 4.035 = 4.034. The HUB sits on the field's centerline, so flipping Y barely changes it.

?text What `DriverStation` method returns the 2026 game data letter? (method name only)
= getGameSpecificMessage | getGameSpecificMessage() | DriverStation.getGameSpecificMessage()
> `DriverStation.getGameSpecificMessage()` returns an empty string until the FMS sends `R` or `B`.
:::
