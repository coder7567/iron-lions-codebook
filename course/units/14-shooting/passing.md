---
summary: When the robot is too far to score, the turret throws FUEL toward its own alliance instead. Here is how the target and the speed are chosen, and the offset bug hiding in the rebuild.
objectives:
  - Explain when the turret passes instead of shooting
  - Describe how the passing target and passing speed are chosen
  - Explain why the passing map is rebuilt when the alliance changes
  - Describe finding F4 and measure what it actually costs
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Passing is the fallback for shooting

The driver presses one button for "score." The turret decides what that means: inside our alliance zone it shoots at the HUB, and everywhere else it **passes**, throwing FUEL back toward our own end of the field where a partner or a later trip can use it.

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="case PASSING:" lines=7

Three values go out in that branch, and they come from two different places:

| Value | Comes from |
|---|---|
| Flywheel RPM | The **passing map**, keyed by the robot's x position |
| Hood angle | The passing map as well |
| Turret angle | `calculationToTarget`, aimed at the chosen passing target |

So a pass is aimed like a shot, using the same shoot-on-the-move correction, but its power comes from a table keyed on how far down the field the robot is, rather than how far it is from a target.

## Choosing a corner

`chooseTargetBasedOnY` picks between two spots near our alliance wall: `left` at (1, 6) and `right` at (1, 2) in blue coordinates. It compares the robot's y against the field's center line, and both the comparison and the targets are mirrored for the red alliance, so "the corner on this side of the field" stays the same physical place for the drive team.

Passing across the field's width would send FUEL through traffic; passing to the near corner keeps it on one side. That is a strategy decision written into code, not just geometry.

## The passing map

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="//need to entrys for the passing/fullfield" lines=5

Four entries, keyed by field x:

| Key | RPM | Meaning |
|---|---|---|
| Our alliance zone edge (5.0 m on blue) | 2000 | Just outside scoring range, a short lob |
| Midfield (8.27 m) | 2600 | Halfway |
| The other alliance zone edge (11.541 m) | 3250 | Deep in the neutral zone |
| The far wall (16.541 m) | 5000 | Full field |

The hood stays at 0.25 plus the offset for every entry, so a pass is always the same arc and only the speed changes. That is a reasonable simplification: a pass has to land somewhere useful, not inside a 41-inch opening.

Because the keys come from `TurretConstants.allianceZoneEnd()` and friends, they are **already mirrored for the alliance**, so a red robot's map is keyed 11.541, 8.27, 5.0, and 0.0. The interpolation then works in raw field coordinates for either alliance.

## Why the map gets rebuilt

Those keys are computed **in the Turret constructor**, at boot, when the Driver Station usually has not reported an alliance yet. `AllianceFlipUtil.shouldFlip()` answers false, so the map comes out blue-side no matter which alliance the robot is really on.

The Superstructure fixes that when the alliance finally arrives:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="if (previousAlliance != DriverStation.getAlliance()" lines=4

One `redoPassingFunction()` call rebuilds the table with correctly mirrored keys. It is a small, sensible patch over a real problem: values derived from the alliance cannot be computed at construction time.

:::danger Finding F4: the rebuild drops the hood offset
::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="public void redoPassingFunction()" lines=8 highlight="4,5,6,7"

Compare that with the constructor. Three of the four rebuilt entries pass `0.25` where the original passed `0.25 + hoodOffset`. Only the last keeps it.

The clamp in `setHoodAngle` softens the damage. The hood's travel starts at 0.530, so a request of 0.25 is clamped up to 0.530 while the intended 0.541 passes through untouched:

| Entry | Intended | After the rebuild | After the clamp |
|---|---|---|---|
| First three | 0.541 | 0.25 | **0.530** |
| Last | 0.541 | 0.541 | 0.541 |

The real error is 0.011 rotations, about 4 degrees of hood, on every pass after an alliance change. Small enough that nobody noticed, large enough to move where a full-field pass lands.

The quick fix is to add `+ TurretConstants.hoodOffset` to the three entries. The better fix is for the constructor and the rebuild to call one shared method, so the two lists cannot drift apart again.
:::

:::team Constants for a passing scheme that is no longer there
`TurretConstants` also holds `flywheelPassingSpeed`, `hoodPassingAngle`, `flywheelFullFieldSpeed`, `hoodFullFieldAngle`, `startNZ`, `endNZ`, `startFullField`, and `endFullField`. None of them appears in the passing map, which is built from literals. They are the remains of an earlier design, and commit `4dbac3d`, "remove fullfield," is where that design was retired.

Unused constants are cheap to keep and expensive to read: the next person to touch passing has to work out which set is live. Delete them, or use them.
:::

:::exercise id="u14-passing"
Write the passing pieces: alliance mirroring, the target choice by y, the four map keys for either alliance, and the hood clamp. The last test measures exactly what finding F4 costs once the clamp is applied.
---hint
`flipX` and `flipY` mirror through the field's length and width. `flip` applies both.
---hint
`choosePassTarget` compares against the **mirrored** center line and returns a **mirrored** target. On red, the high side picks the right-hand target.
---hint
The midfield key is not mirrored, because half the field length is the same measured from either end.
:::

:::quiz
? When does the turret pass instead of shooting at the HUB?
+ Whenever the robot is outside our alliance zone with a `SHOOTING` request
- Whenever the HUB is inactive
- Only during autonomous
- When the flywheel is below its tolerance
> The state machine turns one driver request into shooting or passing based on position.

? What is the passing map keyed by?
+ The robot's x position on the field
- The distance to the HUB
- The distance to the passing target
- The match time
> A pass is about how far down the field you are, not how far from a target.

? Why does the hood angle stay the same for every passing entry?
+ A pass only needs to land in a useful area, so only the speed has to change
- The hood cannot move while passing
- The map can only interpolate one field at a time
- It matches the height of the HUB opening
> A deliberate simplification that makes the table easy to tune.

? Why must the passing map be rebuilt when the alliance is reported?
+ Its keys are alliance-mirrored field positions computed at boot, before the alliance is known
- The flywheel gains change per alliance
- PathPlanner rebuilds it
- The tag layout changes
> Anything derived from the alliance cannot be computed in a constructor.

?num After the hood clamp, how much hood error does finding F4 actually cause?
= 0.011 ± 0.002 rotations
> A request of 0.25 is clamped up to the 0.530 minimum, against an intended 0.541.

? What is the better fix for F4?
+ Have the constructor and the rebuild call one shared method that builds the table
- Add the offset to the three entries and move on
- Remove the hood offset entirely
- Stop rebuilding the map
> Two copies of a list will drift apart again; one copy cannot.
:::
