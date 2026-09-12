---
summary: Both shooting tables came from an afternoon of measurements. Here is how to run that session so the numbers are worth committing, and how to keep them honest through a season.
objectives:
  - Plan and run a shot-data session that produces usable numbers
  - Use the logs to record distance, speed, and hood angle together
  - Turn a data sheet into map entries and verify them
  - Decide when a table needs to be re-measured
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
  - src/main/java/frc/robot/subsystems/turret/TurretConstants.java
---

## What you are producing

Three artifacts come out of a good session, and all three belong in the repository:

| Artifact | Where it goes |
|---|---|
| Shot map entries: distance, RPM, hood angle | The Turret constructor |
| Time-of-flight entries: distance, seconds | The same place |
| The reality constant for shooting on the move | `TurretConstants.ToFRealityConstant` |

Everything else, the notes and the failed attempts, belongs in the build log where the next person can read why the numbers are what they are.

## Setting up

:::steps
1. **Mark distances on the carpet.** Tape at 1, 2, 3, 4, 5, and 6 meters from the HUB, measured to the robot's center. Six marks is enough to start; our table has eleven entries because several were refined later.
2. **Charge two batteries** and swap at the halfway point. A sagging battery changes the answer, and you want the answer for a robot that has voltage.
3. **Collect a bin of FUEL** and a person to return it. Half of a session is walking.
4. **Put the Superstructure in `TESTING`.** The flywheel speed and hood angle then come from the tunable dashboard values, which is what makes this a tuning session rather than a match.
5. **Open AdvantageScope** with `DistanceToHub`, `Turret/flywheelSpeed`, and `Turret/hoodAngle` graphed. The log is the record; the whiteboard is a draft.
:::

## The loop

For each distance mark:

:::steps
1. Park on the mark and confirm `DistanceToHub` agrees with the tape. If it does not, the pose is wrong and every number from this session will be wrong with it. Fix that first.
2. Start at the setting that worked at the previous distance.
3. Shoot three FUEL. Adjust the hood first for trajectory, then the speed for range. Changing both at once teaches you nothing.
4. When three in a row go in, write the row down: distance, RPM, hood angle, battery voltage.
5. Shoot three more without changing anything. If those miss, you found a setting that works sometimes, which is not a setting.
:::

Step five is what separates a table you can trust from a table that looked good on a Tuesday.

## Measuring flight time

Flight time needs a different method: a phone at 60 frames per second, filming the robot and the HUB in one shot. Count frames from the FUEL leaving the hood to the FUEL crossing the HUB's plane, then divide by the frame rate. Three trials per distance, averaged.

Our table has four entries between 1.93 and 5.45 meters, and they are nearly flat: 1.22 to 1.45 seconds. That flatness is itself worth knowing, because it means the shoot-on-the-move correction barely changes with distance.

## Measuring the reality constant

::source file="src/main/java/frc/robot/subsystems/turret/TurretConstants.java" from="This is how we will tune this number" lines=7

The procedure in that comment is exactly right, and it belongs on the same day as the flight-time table, because both are about the same physics. Drive sideways at a constant speed, shoot, record where the FUEL crossed the plane, divide the miss by the robot's speed to get the flight-time error, then adjust the constant by the ratio.

Do it at two speeds. If the two runs disagree, the correction is not a simple scale factor, and the flight-time table is what needs work.

## From the sheet to the code

```java title="What a session produces"
shooterShootingMap.put(2.86, new ShooterSetpoint(2100, 0.525 + TurretConstants.hoodOffset));
```

Three rules for committing:

- **Commit the same day.** A number on a whiteboard is gone by Thursday.
- **Say where it came from.** "Shot data 3/14, practice field, fresh battery" in the commit message answers every future question about the table.
- **Keep the old table.** Comment it out or leave it in the history. Our Turret constructor still carries two earlier tables commented above the live one, which is how you compare after a mechanical change.

Then verify: run the robot through the same distances using the map instead of the testing state, and confirm the shots still go in. A table tuned by hand and never tested through the real code path has one untested step left in it.

## When to re-measure

| Change | Re-measure |
|---|---|
| New hood surface, belt, or flywheel wheels | The whole shot map |
| Different or worn FUEL late in a season | Spot-check three distances |
| Re-zeroed hood encoder | Nothing, but the hood offset must go to zero in the same commit |
| A heavier robot or a new battery routine | Spot-check the far end, where voltage matters most |
| New carpet at an event | Spot-check, and expect the far end to move |

The spot-check is the habit worth building: three distances, three shots each, fifteen minutes. It either confirms the table or tells you the afternoon is needed.

:::tip Two people, two jobs
One person drives and shoots; one person watches the log and writes rows. Swapping halfway keeps both honest, and it means two people know how the tables were made. A table only one person understands is a single point of failure with a build season attached to it.
:::

:::quiz
? What is the first thing to check after parking on a distance mark?
+ That `DistanceToHub` in the log agrees with the tape measure
- That the flywheel is at speed
- That the hood is at its minimum
- That vision sees two tags
> If the pose is wrong, every number from the session is wrong with it.

? Why adjust the hood and the speed one at a time?
+ Changing both at once makes it impossible to tell which change helped
- The hood cannot move while the flywheel spins
- The dashboard accepts one change per loop
- It reduces battery drain
> The same rule as tuning a controller: one variable per trial.

? After three shots in a row go in, what does the procedure ask for next?
+ Three more with nothing changed, to prove the setting repeats
- Move to the next distance
- Write the row and swap batteries
- Lower the speed until a shot misses
> A setting that works sometimes is not a setting.

? How is flight time measured?
+ Film at a known frame rate and count frames from release to the HUB's plane
- Subtract two log timestamps
- Compute it from the flywheel speed
- Read it from the existing table
> Three trials per distance, averaged.

? You measure the reality constant at two robot speeds and get two different answers. What does that tell you?
+ The correction is not a simple scale factor, so the flight-time table needs work
- Use the average of the two
- The turret trim is wrong
- The pose estimate is drifting
> A scale factor that is not constant means the underlying model is off.

?? Which of these belong in the commit that adds new shot map entries? Select all that apply.
+ The date and place the data was taken
+ The battery condition
+ The old table, kept in history or commented out
- A photo of the whiteboard
> The commit message is where a table's provenance lives.
:::
