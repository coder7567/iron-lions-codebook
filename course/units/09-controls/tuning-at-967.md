---
summary: How our robot exposes tunable numbers, which ones actually change while the robot is running, and a pit process that produces values worth committing.
objectives:
  - Use `LoggedNetworkNumber` tunables and find them on the dashboard
  - Tell which of our tunables are live and which are frozen at boot
  - Run a tuning session that produces a number you can defend
  - Get tuned values back into the code before they are lost
files:
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## Tunables

A tunable is a constant you can change from the dashboard without redeploying code. Ours are AdvantageKit's `LoggedNetworkNumber`s: a name, a default value, and a `.get()`.

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final LoggedNetworkNumber driveKp" lines=8

Two things make this better than a plain NetworkTables entry. The default lives in the code, so a robot that boots with no dashboard still works. And AdvantageKit **records the value in the log**, so a replay uses the numbers the robot actually used, and a log from three weeks ago tells you what the gains were that day.

Our tunables cluster into groups by name: `Tuning/Drive/...` for the drivetrain, plus turret and vision values registered at the top level, such as `turretP`, `flywheelTolerance`, `Testing Flywheel Speed`, `hoodIDLEPosition`, and `The Constant of Reality`.

## Live or frozen?

This is the detail that wastes afternoons. A tunable only takes effect if something calls `.get()` **after** you change it.

| Read | Effect | Examples |
|---|---|---|
| Every loop, inside `periodic` or a command's lambda | **Live.** Change it, and the robot responds immediately | `DEADBAND`, `hoodIDLEPosition`, `testingFlywheelSpeed`, `testingHoodAngle`, `flywheelTolerance`, `ToFRealityConstant`, the vision filter limits |
| Once, while building a configuration or a command | **Frozen at boot.** Changing it does nothing until robot code restarts | `driveKp`, `driveKd`, `turnKp`, `turnKd`, `turretP`, `turretD`, and the heading controller's `ANGLE_KP`, `ANGLE_KD`, and profile constraints |

::source file="src/main/java/frc/robot/subsystems/turret/Turret.java" from="io.setHoodAngle(TurretConstants.hoodIDLEPosition.get())" lines=1

That one is live: `applyState` calls `.get()` every loop. Compare it with the turret's PID gains, which are read once while `TurretIOSpark` builds its configuration and then live inside the SPARK.

:::tip Make a gain live when you plan to tune it
If you are about to spend a practice session on the turret's kP, add a few lines to `periodic` that re-apply the configuration when the tunable changes:

```java title="A pattern for live controller gains"
if (TurretConstants.turretP.get() != lastTurretP) {
  lastTurretP = TurretConstants.turretP.get();
  io.setTurretPid(lastTurretP, TurretConstants.turretD.get());
}
```

Re-configure only on change, never every loop: each call is a blocking CAN transaction, and persisting it would write flash. Remove or leave the hook depending on how often the mechanism gets retuned.
:::

## A tuning session that produces a real number

:::steps
1. **Write down the goal.** "The hood holds its setpoint within 0.01 rotations while the robot drives" is a goal. "Make the hood better" is not.
2. **Put the robot in a safe state.** Blocks or a clear space, bumpers on, current limits on, and the Superstructure in `TESTING` so the mechanism runs from tunables instead of match logic.
3. **Change one value at a time**, and say out loud what you expect to happen before you press enter.
4. **Watch the log, not the mechanism.** Graph the setpoint against the measurement in AdvantageScope. Rise time, overshoot, and settling are visible there and invisible to your eyes.
5. **Record every trial** in the build log: date, mechanism, value, what happened, battery voltage.
6. **Stop when the goal is met.** A tuning session ends because the mechanism meets a written goal, not because it feels good.
7. **Commit the number** into the constants file the same day, with a short commit message that says what it fixed.
:::

:::danger Dashboard values do not survive
A tunable lives in NetworkTables. Reboot the robot, redeploy code, or swap the battery on a bad day, and it is back to the default in the source file. Every value that matters ends its life as a committed constant. A tuning session with nothing committed is a story, not a change.
:::

## What not to tune

- **Don't tune to fix a logic bug.** If the setpoint in the log is wrong, no gain will save you. Check the state machine first.
- **Don't tune around a mechanical problem.** A binding arm, a loose chain, or a worn belt shows up as inconsistent behavior between runs. Gains cannot make a mechanism repeatable.
- **Don't tune two things at once.** Two changes and one improvement teaches you nothing.
- **Don't tune at a competition without a plan.** The best time to tune is a practice day, with a log and a written goal. In the pit between matches, prefer the value that worked this morning.

:::team The constant of reality
`TurretConstants.ToFRealityConstant` is a tunable that scales the shot's time of flight, and its comment spells out a four-step measurement: drive at a constant speed, shoot, record where the FUEL actually entered, and compute the time-of-flight error from the miss distance divided by the robot's speed. That is what a good tuning procedure looks like: a number you can derive from a measurement someone else can repeat, not a value you nudge until it looks right.
:::

:::quiz
? What makes a tunable "live"?
+ Something calls `.get()` on it every loop, so a new value takes effect immediately
- It is stored in NetworkTables
- It has a default value in code
- It appears on the Elastic dashboard
> Values read once, during configuration, are frozen until robot code restarts.

? You change `turretP` on the dashboard and nothing happens. Why?
+ The gain was read once while building the SPARK configuration, and now lives inside the controller
- `turretP` is not a tunable
- The turret is in `IDLE`
- The dashboard writes are rejected while enabled
> Controller-side gains need a reconfigure or a code restart.

? Why does AdvantageKit log tunable values?
+ So a log shows which numbers the robot actually used, and a replay uses the same ones
- To restore them after a reboot
- To keep the dashboard in sync
- To prevent two people editing them at once
> The log is the record; NetworkTables is temporary.

? The mechanism behaves differently on two runs with identical gains. What should you suspect first?
+ Something mechanical or electrical: binding, a loose belt, or a sagging battery
- The proportional gain is too low
- The derivative gain is too high
- The dashboard value didn't apply
> Tuning assumes repeatable hardware. Fix repeatability first.

? Where does a tuned value need to end up?
+ Committed in the constants file, the same day
- Saved on the dashboard
- Written on the whiteboard in the shop
- Recorded in the match log
> Dashboard values disappear on the next reboot or deploy.

?tf The right time to discover a tuning problem is at a competition, where the real field conditions are.
= false
> Practice days give you logs, time, and a spare battery. Competitions give you six minutes between matches.
:::
