---
summary: What twenty seconds of autonomous is worth in REBUILT, how to budget them, and how our nine autos divide up the jobs a match can need.
objectives:
  - Budget an autonomous routine against the 20 second period
  - Explain what our existing autos do and when each is the right choice
  - Coordinate an auto with alliance partners
  - Decide when a simpler auto is the better competitive choice
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/deploy/pathplanner/autos/Just Preload.auto
---

## Twenty seconds

REBUILT gives 20 seconds of autonomous, both HUBs are active for all of it, and whichever alliance scores more FUEL in autonomous gets its HUB turned off first in teleop. So autonomous is worth more than its points: it also decides the shift order for the next two minutes.

That makes the budget tight. Every second spent is a second not spent scoring, and the clock includes things that don't look like driving:

| Cost | Typical | Why |
|---|---|---|
| Flywheel spin-up | 1 to 2 s | From stopped to within tolerance |
| Turret swing to a new target | 0.3 to 1 s | Depends on how far it turns |
| Driving between scoring spots | 1 to 3 s per leg | Trapezoid profile, not top speed |
| Intaking a FUEL | 0.5 to 1 s | Arm out, roller contact, feed |
| Settling before a shot | 0.2 to 0.5 s | Let the pose estimate and the turret catch up |

A routine that drives three legs and shoots twice has almost no slack. The exercise at the end of this lesson does this arithmetic for you, and the first thing it teaches is that **a two-leg auto is usually the honest maximum** at 4 m/s and 3 m/s².

## Our nine autos

| Auto | Shape |
|---|---|
| `Just Preload` | Eject briefly, wait, then shoot. No path at all |
| `Depot` | Reverse, then drive the Depot path while shooting |
| `NZ Depot`, `NZ Score`, `Reverse NZ Depot` | Neutral-zone variants: collect or score from the middle |
| `Right NZ`, `Right NZ Race` | The right-side lane, with a faster "race" version |
| `4646 Left`, `4646 right` | Left and right routines built around a partner's needs |

The two ends of that list are the interesting ones. `Just Preload` is the auto you run when anything is uncertain: no path, nothing to collide with, and it still contributes to the autonomous FUEL count. The `4646` pair exists because an alliance partner needed a particular side of the field, which is the most common reason an auto exists at all.

::source file="src/main/java/frc/robot/RobotContainer.java" from="//Add Named Comands here" lines=6

:::warning Just Preload does not reset odometry
`Just Preload.auto` sets `resetOdom` to false, which is reasonable for a routine with no path. But the turret aims using the **pose**, so with no reset the robot aims from wherever odometry happens to think it is, which at boot is (0, 0) facing 0° unless vision has corrected it. The shot may be computed for a position the robot has never been in.

Two ways to make it safe: let vision establish the pose before the match (start with a tag in view), or reset the pose at the start of the routine to the known starting spot. This is the kind of thing the [auto linter](course:12-autonomous/named-commands-and-events) exercise checks for.
:::

## Choosing an auto at a competition

:::steps
1. **Ask the alliance what they need.** Where are the partners starting, and which lane do they want? This is the whole reason the `4646` autos exist.
2. **Pick the simplest auto that satisfies that.** Points you reliably score beat points you might.
3. **Read the selection out loud** from the dashboard after selecting it, every match.
4. **Have a fallback.** If the field is different from practice, if a partner is unpredictable, or if a mechanism is limping, `Just Preload` is a complete auto that cannot collide with anyone.
:::

The fourth point is worth dwelling on. An auto that works 95% of the time and scores four FUEL is worth more over a weekend than one that works 60% of the time and scores seven, because the 40% often includes a collision that costs your partner's auto as well.

## Designing a new one

:::steps
1. **Write the goal in one sentence.** "Score the preload, collect two from the depot, and end in the neutral zone facing the HUB."
2. **Budget it** before drawing anything. Segment distances, spin-up, and settling time against the 20 seconds.
3. **Draw the path** in the PathPlanner app with margins for other robots.
4. **Place the commands** so mechanisms work *while* the robot drives, not after it stops.
5. **Test it in simulation**, then on the practice field, at least five times.
6. **Write down what it needs**: starting position, which partners it assumes, and what it does if vision never sees a tag.
:::

Step 4 is where most of the time is found. `Depot.auto` already does this: it starts the shot in parallel with the drive, so the flywheel spins up while the robot is moving.

:::exercise id="u12-pathmath"
Write the planning arithmetic: path length from waypoints, the time a rest-to-rest move takes, the total for a multi-leg routine with overhead, whether it fits in 20 seconds, mirroring a waypoint for the red alliance, and whether every waypoint is on the field.

The last test asks the question this lesson is about: a modest two-leg routine fits, and a greedy three-leg one doesn't.
---hint
`length` sums `Math.hypot` between consecutive waypoints. Fewer than two waypoints has no length.
---hint
A move reaches cruise speed only when the distance is at least `maxVelocity² / maxAcceleration`. Above that the time is `distance / maxVelocity + maxVelocity / maxAcceleration`; below it, the profile is a triangle and takes `2 * sqrt(distance / maxAcceleration)`.
---hint
Flipping for red mirrors **both** coordinates, because the field is rotationally symmetric: `FIELD_LENGTH - x` and `FIELD_WIDTH - y`.
:::

:::quiz
?num How many seconds does REBUILT's autonomous period last?
= 20 ± 0.5 s
> And whichever alliance scores more FUEL in it gets its HUB shut off first in teleop.

? Why is autonomous worth more than the points it scores directly?
+ The alliance that scores more FUEL in autonomous has its HUB inactive first, which sets the shift order for teleop
- Autonomous points are doubled
- It determines the alliance's playoff seed
- Autonomous is the only time the HUBs are active
> The shift schedule comes out of the autonomous result.

? An alliance partner asks for the right lane. What should you do first?
+ Choose one of our autos that stays out of that lane, even if it scores less
- Run our highest-scoring auto anyway
- Ask them to change their auto
- Disable our autonomous
> A collision costs both robots their autonomous.

? Why does `Depot.auto` start the shot in parallel with the path instead of after it?
+ The flywheel spins up while the robot drives, so no time is spent standing still
- Parallel groups are faster to schedule
- PathPlanner requires it
- To avoid resetting odometry
> Overlapping mechanism work with driving is where autonomous time is found.

? `Just Preload` doesn't reset odometry. Why does that matter for a routine with no path?
+ The turret aims from the pose, so a wrong pose means a wrong aim
- Odometry resets are required for every auto
- PathPlanner will refuse to run it
- The drive motors won't move
> Either let vision establish the pose first, or reset it at the start.

?tf A routine that scores seven FUEL 60% of the time is a better competition choice than one that scores four 95% of the time.
= false
> The failures usually cost more than the extra points, especially when they involve a partner.
:::
