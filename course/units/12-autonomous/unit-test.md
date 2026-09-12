---
summary: Show that you can budget an autonomous routine, read our auto files and PathPlanner wiring, diagnose an auto from a log, and follow competition discipline.
---

This test covers all of Unit 12.

:::exam Unit 12 test: autonomous
?num How long is REBUILT's autonomous period?
= 20 ± 0.5 s
> And the alliance that scores more FUEL in it has its HUB inactive first in teleop.

? Why is autonomous worth more than the points it scores?
+ It decides which alliance's HUB is inactive first, which sets the shift order for all of teleop
- Its points count double
- It sets the playoff seed
- Both HUBs are inactive during it
> The autonomous FUEL count feeds the game data character.

? What is the difference between a `.path` file and an `.auto` file?
+ A path is a drawn route; an auto is a tree of paths, commands, and waits that references paths by name
- A path is for teleop; an auto is for autonomous
- A path holds named commands and an auto does not
- They are the same thing
> Both live in `src/main/deploy/pathplanner/`.

? Our PathPlanner app settings say the drive gearing is 5.143:1 while `DriveConstants` says 5.9:1. Which does the robot use?
+ The code's value, which reaches PathPlanner through `ppConfig`
- The app's value, from the deployed settings file
- Whichever file was saved more recently
- Neither; PathPlanner measures it at runtime
> That mismatch is finding F9: the app previews a robot we don't have.

? What does `AutoBuilder.configure` receive from the drivetrain?
+ Pose supplier, pose reset, speeds supplier, output consumer, controller, robot config, alliance flip rule, and the subsystem to require
- Only the pose supplier and output consumer
- The list of paths and autos
- The auto chooser
> Those eight arguments are the entire interface between PathPlanner and our code.

? What does the holonomic drive controller do each loop while following a path?
+ Looks up the trajectory state for now, uses its velocity as feedforward, and adds a PID correction for pose error
- Drives straight to the next waypoint
- Recomputes the trajectory
- Applies PID to the final pose only
> Feedforward from the plan, feedback for the error.

? When is the red-alliance flip decided?
+ While the auto runs, by calling the `shouldFlip` supplier
- When the path file is saved
- When `RobotContainer` is constructed
- At the end of autonomous
> An alliance that hasn't been reported yet makes a red robot run a blue path.

? Why must named commands be registered before `AutoBuilder.buildAutoChooser()`?
+ Every auto's command is built at startup, so a later registration is too late and the step becomes a no-op
- Registration order sets execution order
- The chooser sorts autos by registration
- PathPlanner requires alphabetical order
> The failure is silent except for a console warning.

? What does the `start` named command do on our robot?
+ Sets the Superstructure's wanted state to `SHOOTING` and finishes immediately
- Spins the flywheel until it is at speed
- Drives to the HUB
- Waits for the turret to finish aiming
> The state machine does the work; the auto just asks.

? In `Depot.auto`, what happens at the same time as the `Depot` path?
+ A 0.1 second wait, then setting the state to `SHOOTING`
- Nothing; the auto is fully sequential
- The `reverse` command
- A second path
> Overlapping spin-up with driving is how an auto stays inside 20 seconds.

? What does `resetOdom` do?
+ Resets the robot's pose to the start of the first path, using the reset method given to `AutoBuilder`
- Resets the gyro's yaw only
- Clears the pose estimator's vision history
- Restarts the autonomous timer
> Any auto that follows a path should set it.

? `Just Preload` has `resetOdom` false and no path. Why is that still worth a second look?
+ The turret aims from the pose, so an uncorrected pose means a wrong aim
- PathPlanner will refuse to run it
- The drive motors stay disabled
- It cannot be selected in the chooser
> Either let vision establish the pose, or reset it explicitly.

? What state is the robot in when teleop starts after most of our autos?
+ `SHOOTING`, because the named command set a state and cancelling the auto command doesn't undo it
- `IDLE`
- `PAUSED`
- Whatever the driver selects first
> Worth knowing before the drive team reports it as a bug.

? What does `LocalADStarAK` add to PathPlanner's pathfinder?
+ It records the pathfinder's results as inputs so replay follows the same routes the robot found
- It avoids other robots
- It speeds up pathfinding
- It draws paths at runtime
> Anything outside your control that affects behavior is an input.

? Which is a good use of pathfinding?
+ Driving to a fixed scoring pose from wherever the robot happens to be
- Following a precise autonomous route you run every match
- Avoiding other robots
- Replacing odometry
> The navgrid knows static field elements only.

? In a log, the robot tracks its trajectory setpoint closely but the whole trajectory is offset from the field. What is wrong?
+ The starting pose was wrong: `resetOdom` false, or the robot was placed incorrectly
- The translation gain is too low
- The constraints are too aggressive
- The gyro is disconnected
> Good tracking in the wrong frame is a pose problem.

?num Two legs of 3.0 m and 2.5 m at 4 m/s and 3 m/s², with 1.5 s of overhead each, take about how long?
= 6.8 ± 0.3 s
> Both legs are triangular at those constraints, so about 2.0 s and 1.8 s of driving plus 3 s of overhead.

? It is the last qualification match, the new auto is untested, and the old one works. Which do you run?
+ The one that works
- The new one, because it scores more
- Neither
- Whichever the drive team prefers in the moment
> An untested routine is a hypothesis, and its failure can take a partner's auto with it.
:::
