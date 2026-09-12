---
summary: The PathPlanner app is where paths and autos are drawn. What each piece of a path means, how files reach the robot, and why our app settings disagree with our code.
objectives:
  - Explain the difference between a path and an auto, and where each is stored
  - Describe waypoints, rotation targets, constraints, and event markers
  - Keep the app's robot settings in sync with the code
  - Get a new path onto the robot and know that it arrived
files:
  - src/main/deploy/pathplanner/settings.json
  - src/main/java/frc/robot/subsystems/drive/DriveConstants.java
  - src/main/java/frc/robot/RobotContainer.java
---

## Paths and autos are different things

| | Path | Auto |
|---|---|---|
| What it is | A drawn route across the field | A routine built from paths, commands, and waits |
| File | `src/main/deploy/pathplanner/paths/<name>.path` | `.../autos/<name>.auto` |
| Contains | Waypoints, rotation targets, constraints, event markers | A tree of sequential and parallel steps |
| Used by | An auto, or `AutoBuilder.followPath` | The auto chooser |

Our repository has 22 paths and 9 autos. Several paths are variants of one idea (`Right NZ`, `Right Trench`, `Right Trench Race`, `Reset Right NZ`), which is normal: paths are cheap to make and easy to accumulate. Deleting the ones no auto uses is a good off-season job.

## What a path holds

| Piece | Meaning |
|---|---|
| **Waypoints** | The anchor points of the route, with control handles that shape the curve between them |
| **Rotation targets** | Which way the robot faces at a point along the path. A holonomic drive rotates independently of its direction of travel |
| **Constraints** | Max velocity, acceleration, angular velocity, and angular acceleration, either for the whole path or for a zone of it |
| **Event markers** | Points along the path that trigger commands |
| **Start and end states** | The velocity and rotation the path begins and ends with |

The most common mistake is forgetting that **rotation and travel are separate**. A path that drives from the depot to the HUB while rotating to face the HUB is one path with a rotation target, not two maneuvers.

## The settings tab, and finding F9

The app keeps its own description of the robot, and ours has drifted away from the code:

::source file="src/main/deploy/pathplanner/settings.json" from="robotTrackwidth" lines=8

| Setting | PathPlanner app | Our code | Effect of the difference |
|---|---|---|---|
| Track width | 0.546 m | 0.508 m | The app previews a slightly larger robot |
| Module offsets | ±0.273 m | ±0.254 m | Same |
| Drive gearing | 5.143:1 | 5.9:1 | The app thinks the robot is faster than it is |
| Drive current limit | 80 A | 30 A in `ppConfig` | The app thinks it can accelerate harder |
| Mass, MOI, wheel radius, COF, max speed | Match | Match | Fine |

**The robot uses the code's values**, because `AutoBuilder.configure` receives `DriveConstants.ppConfig`. The app's numbers only change what the app previews and how it generates its trajectory timing, so a path that looks feasible on screen can be a path the robot cannot follow. Fixing the settings tab to match the code is a ten-minute job that makes every future path honest.

::source file="src/main/java/frc/robot/subsystems/drive/DriveConstants.java" from="public static final RobotConfig ppConfig" lines=12

The app's default constraints, 4.0 m/s and 3.0 m/s², are also worth knowing: they are what a new path gets unless you change them, and they are below the robot's 4.2 m/s maximum, which is a sensible margin.

## From the app to the robot

:::steps
1. **Save in the app.** It writes `.path` and `.auto` files into `src/main/deploy/pathplanner/`.
2. **Commit them.** They are source files; a path that exists only on one laptop is a path the team does not have.
3. **Deploy.** GradleRIO copies the whole `src/main/deploy` folder to the roboRIO.
4. **Confirm it arrived.** A new auto shows up in the chooser on the dashboard. If it isn't there, the deploy didn't include it, or the name in the chooser is not the name you expect.
:::

:::warning Deploy does not delete
Our `build.gradle` sets `deleteOldFiles = false` for the deploy folder, so a path you rename or delete stays on the roboRIO. The chooser is built from the files *on the robot*, so a deleted auto can keep appearing. If a stale auto shows up in the list, that is why.
:::

## Habits that keep paths trustworthy

- **Name paths after what they do**, not where they were drawn: `Depot Trench` says more than `Path 4`.
- **Keep starting poses honest.** A path's start is where the robot must actually be placed. Mark it on the field with tape during practice.
- **Leave margin at the field elements.** The app draws a rectangle for the robot; bumpers, tolerance, and other robots all eat into it.
- **Re-check after any drivetrain change.** New wheels, a new gear ratio, or a heavier robot changes what the path's constraints mean.

:::quiz
? What is the difference between a `.path` file and an `.auto` file?
+ A path is a drawn route; an auto is a tree of paths, commands, and waits
- A path is for teleop and an auto is for autonomous
- They are the same format with different extensions
- A path contains named commands and an auto does not
> Autos reference paths by name.

? The PathPlanner app says the drive gearing is 5.143:1 while `DriveConstants` says 5.9:1. Which does the robot use when following a path?
+ The code's value, through `ppConfig`
- The app's value, from the settings file
- Whichever is newer
- The average
> The app's numbers only affect what it previews and how it times the trajectory it generates.

? Why does a rotation target exist separately from the path's direction of travel?
+ A holonomic drivetrain can face one way while moving another
- To set the ending heading only
- To control the turret
- Because the gyro needs a reference
> Rotation and translation are independent on a swerve robot.

? A deleted auto keeps showing up in the chooser. Why?
+ The deploy doesn't delete old files from the roboRIO, and the chooser is built from what is on the robot
- The chooser caches names in NetworkTables
- PathPlanner keeps a backup copy
- The auto is still referenced by another auto
> `deleteOldFiles = false` in the deploy configuration.

? Where do path and auto files live in the project?
+ In `src/main/deploy/pathplanner/`, which gets copied to the roboRIO on deploy
- In `build/pathplanner/`
- In the PathPlanner app's own folder
- In NetworkTables
> They are source files and belong in Git.

?? Which of these are worth doing after changing the drivetrain's gearing? Select all that apply.
+ Update `DriveConstants` and the gains tuned against it
+ Update the PathPlanner app's settings to match
+ Re-test the autos on the practice field
- Redraw every path from scratch
> The paths are still fine; what they mean in time and acceleration is what changed.
:::
