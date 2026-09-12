---
summary: The turret decides between shooting and passing from the robot's position on the field, holds aim while paused, and protects itself with soft limits and a deadzone. One of its states is unreachable.
objectives:
  - Predict the turret's current state from its wanted state, alliance, and field position
  - Explain the alliance-aware line checks and the passing target choice
  - Explain the soft limits, the wrap-around, the deadzone, and the operator offset
  - Describe finding F1 and write the fix
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## Four requests, six behaviors

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private CurrentState updateState()" lines=20 highlight="5-11"

| Wanted | Where the robot is | Current state |
|---|---|---|
| `IDLE` | anywhere | `IDLE` |
| `SHOOTING` | in our alliance zone | `SHOOTING` |
| `SHOOTING` | anywhere else | `PASSING` |
| `PAUSED` | in our alliance zone | `PAUSEDSHOOTING` |
| `PAUSED` | anywhere else | `PAUSEDSHOOTING`, because of finding F1 |
| `TESTING` | anywhere | `TESTING` |

And the outputs:

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="case PAUSEDSHOOTING:" lines=12

| State | Flywheel | Hood | Turret angle |
|---|---|---|---|
| `IDLE` | 0 | `hoodIDLEPosition` (tunable) | `stopTurret()`, which commands the −1.6 rad idle position |
| `PAUSEDSHOOTING` | 0 | From the shot map for the HUB distance | Aimed at the HUB |
| `PAUSEDPASSING` | 0 | From the shot map | Aimed at the passing target |
| `SHOOTING` | From the shot map | From the shot map | Aimed at the HUB |
| `PASSING` | From the passing map, by robot X | From the passing map | Aimed at the passing target |
| `TESTING` | Tunable testing speed | Tunable testing angle | Aimed at the HUB |

The paused states are the interesting design idea. The turret **keeps aiming** while the flywheel stops. FUEL is not going anywhere, the robot is quieter, it draws far less current, and the moment the driver presses the trigger again the turret is already pointed at the target. That is worth roughly a second of spin-up in a match.

## Lines on the field, and which side is ours

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private boolean isPastLine" lines=8

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="public static final double allianceZoneEnd = 5;" lines=4

Every field position in the code is written from the blue alliance's point of view and mirrored through `AllianceFlipUtil` when we're red. Two lines matter to the turret:

| Line | Blue value | Red value | Meaning |
|---|---|---|---|
| `allianceZoneEnd()` | x = 5.0 m | x = 11.541 m | The far edge of **our** alliance zone |
| `oppositeAllianceEnd()` | x = 11.541 m | x = 5.0 m | Where the **other** alliance's zone begins |

`isPastLine` flips its comparison with the alliance, so "on our side of the line" means a smaller x on blue and a larger x on red. This is the pattern for every field measurement you will write: **store one alliance's numbers, mirror at the moment of use.**

:::team The comment that tells a story
`TurretConstants` marks the alliance-aware getters with a note: the methods exist because data taken on 2/7 was bad. Hard-coded blue-side coordinates work perfectly in a blue-side practice session and fail silently on the red alliance. Mirroring at the point of use is the fix, and the comment is the reminder.
:::

## Choosing a passing target

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="private Translation2d chooseTargetBasedOnY(" lines=16

While passing, the turret throws FUEL toward one of two spots near our alliance wall: `left` at (1, 6) or `right` at (1, 2), both mirrored for red. The choice is made by comparing the robot's Y against the field's center line (`center`, 4.0 m), and the choice flips with the alliance so that "left" stays on the same physical side of the field for the driver.

The flywheel speed for a pass comes from a separate map keyed on the robot's **X** position, from 2000 RPM near our zone to 5000 RPM at the far end of the field. [Unit 14](course:14-shooting/passing) covers those tables and their rebuild on alliance change.

## Protecting the mechanism

The turret can't spin forever: cables run to the flywheel and hood. Its configuration enforces that in three layers.

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="turretConfig.externalEncoder.countsPerRevolution(8192);" lines=8

1. **Soft limits** in the SPARK: −4.261 rad to +1.6 rad, a bit under a full turn. The controller refuses to drive past them, no matter what the code asks.
2. **A clamp in software**, in `setTurretAngle`, so the setpoint is inside those limits before it is ever sent.
3. **Output limits**: ±0.5 of full output, plus a 0.075 s closed-loop ramp, so a big aim change doesn't yank the robot.

::source file="src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java" from="public void setTurretAngle(double angle)" lines=10 highlight="3,4,6,8-10"

Requested angles are wrapped into −π to π, and anything above 2.022 rad is shifted down by 2π so the turret reaches it the long way around, within its travel. What's left is a **deadzone**: the arc between 1.6 and 2.022 rad, which the turret cannot reach from either direction. Aiming there sets `inDeadzone`, which rumbles the controller so the driver can reposition the robot rather than wonder why shots are missing.

The operator's trim, ±0.05 rad per bumper press, is added *before* the wrap. The offset starts at π, which means the turret's zero and the robot's forward direction differ by half a turn, and a boot-time seed of −1.6 rad on a relative encoder assumes the turret starts at its idle position (finding F23). If the turret is bumped by hand while the robot is off, everything above is off by that much. That is why "turret at its start position before power-on" is on the pit checklist.

:::danger Finding F1: `PAUSEDPASSING` can never happen
Look again at the `PAUSED` branch. Both the "in our zone" test and the "not yet in their zone" test yield `PAUSEDSHOOTING`, so the `PAUSEDPASSING` case in `applyState` is dead code. Paused outside our alliance zone, the turret aims at the HUB from a distance where it would have been passing a moment earlier.

The likely intent, matching the `SHOOTING` branch and the passing behavior:

```java title="The fix"
case PAUSED:
  if (isPastLine(pose.getX(), TurretConstants.allianceZoneEnd())) {
    yield CurrentState.PAUSEDSHOOTING;
  } else if (isPastLine(pose.getX(), TurretConstants.oppositeAllianceEnd())) {
    yield CurrentState.PAUSEDPASSING;
  }
  yield CurrentState.IDLE;
```

This came in with commit `65727f9`, "Add pause," on 2026-03-13, in the middle of competition season. The commit added the wanted state, both current states, and both `applyState` branches. Only the one line in the decision was wrong, and nothing about the robot's behavior looks obviously broken, which is exactly why this kind of bug survives. A review question as simple as "can every state in this enum happen?" would have caught it.
:::

:::exercise id="u08-turret"
Write the turret's state selection as a pure function, with F1 fixed: `PAUSEDSHOOTING` in our zone, `PAUSEDPASSING` between the zones, and `IDLE` inside the other alliance's zone.

You also write the two alliance-aware helpers, so the tests can check blue and red positions against the same code.
---hint
`ourZoneEdgeX(isRed)` is 5.0 on blue and `FIELD_LENGTH - 5.0` on red. `theirZoneEdgeX` is the same function for the other alliance, so it can just call `ourZoneEdgeX(!isRed)`.
---hint
`onOurSide` mirrors `Turret.isPastLine`: `robotX < lineX` for blue and `robotX > lineX` for red. Strict comparisons, so a robot exactly on the line is not on our side.
---hint
The `PAUSED` case needs a block with `yield`, because it has two tests. The arrow form allows a block: `case PAUSED -> { ... yield ...; }`.
:::

:::quiz
? A blue robot at x = 8.0 m has a wanted state of `SHOOTING`. What is the turret's current state?
+ `PASSING`, because 8.0 is past the 5.0 m edge of our alliance zone
- `SHOOTING`
- `PAUSEDPASSING`
- `IDLE`
> Shooting from outside our zone becomes a pass toward our alliance wall.

? A red robot at x = 13.0 m has a wanted state of `SHOOTING`. What is the turret's current state?
+ `SHOOTING`, because our zone on red runs from 11.541 m to the wall
- `PASSING`
- `IDLE`
- `TESTING`
> The zone lines mirror with the alliance, so the same physical position means different things on each side.

? What do the paused states do differently from the idle state?
+ They keep the turret and hood aimed at a target while the flywheel stops
- They stop the turret and keep the flywheel spinning
- They stop everything, including the intake
- They aim at the HUB only during autonomous
> Holding aim means the next shot doesn't wait for the turret to swing back.

?num The turret can't reach angles between its 1.6 rad soft limit and 2.022 rad. How wide is that deadzone, in radians?
= 0.422 ± 0.001 rad
> About 24 degrees, right behind the mechanism's travel limit.

? Why does `isPastLine` change its comparison based on the alliance?
+ Field coordinates always start at the blue wall, so "toward our wall" is a smaller x on blue and a larger x on red
- Because red robots drive backward
- Because the field is longer on the red side
- Because PathPlanner reverses the axes for red
> Store one alliance's numbers, mirror them when they're used.

? What makes `PAUSEDPASSING` unreachable in the current code?
+ Both branches of the `PAUSED` case yield `PAUSEDSHOOTING`
- `PAUSEDPASSING` has no branch in `applyState`
- The enum constant is commented out
- The Superstructure never requests `PAUSED`
> The outputs exist; the decision never selects them. That's finding F1.
:::
