---
summary: How an auto file turns into a command, how our two named commands work, why registration order matters, and what our autos leave running when autonomous ends.
objectives:
  - Register named commands correctly and explain why order matters
  - Read an auto file's tree and predict what the robot does
  - Choose between a named command in the auto and an event marker on a path
  - Spot the mistakes an auto linter can catch
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/deploy/pathplanner/autos/Depot.auto
---

## Our two named commands

::source file="src/main/java/frc/robot/RobotContainer.java" from="//Add Named Comands here" lines=6

| Name | Command | Effect |
|---|---|---|
| `start` | `setWantedStateCommand(SHOOTING)` | The Superstructure goes to `SHOOTING`: turret aims and spins, intake runs |
| `reverse` | `setWantedStateCommand(EJECTING)` | The Superstructure goes to `EJECTING`: rollers run backward |

Both are `InstantCommand`s that set a state and finish in the same loop. That is the payoff of the [state machine design](course:08-state-machines/superstructure): an auto never has to describe *how* to shoot, only that shooting should start. The state machine handles spin-up, the feeder gate, and aiming without the auto knowing any of it.

It also means a named command in our autos takes **zero time**. The auto moves on immediately, and the mechanism does its work in parallel with whatever comes next.

## The auto tree

A `.auto` file is a tree of steps. Our `Depot` auto looks like this:

```json title="src/main/deploy/pathplanner/autos/Depot.auto, abbreviated"
{
  "command": {
    "type": "sequential",
    "data": { "commands": [
      { "type": "named",    "data": { "name": "reverse" } },
      { "type": "parallel", "data": { "commands": [
        { "type": "path",       "data": { "pathName": "Depot" } },
        { "type": "sequential", "data": { "commands": [
          { "type": "wait",  "data": { "waitTime": 0.1 } },
          { "type": "named", "data": { "name": "start" } }
        ] } }
      ] } }
    ] }
  },
  "resetOdom": true
}
```

Read it as a command group, because that is exactly what PathPlanner builds from it:

1. Set the state to `EJECTING` for an instant, which clears anything stuck in the rollers.
2. Then, **at the same time**: follow the `Depot` path, and after a tenth of a second, set the state to `SHOOTING`.

The auto finishes when the path finishes. The parallel step is what makes it fast: the flywheel spins up while the robot drives.

`resetOdom` is the other important field. When true, PathPlanner calls the pose reset we handed to `AutoBuilder.configure`, setting the robot's pose to the start of the first path. **Any auto that follows a path should set it**, because path following is only as good as the pose it starts from.

## Named commands or event markers?

| | Named command, in the auto | Event marker, on the path |
|---|---|---|
| Lives in | The `.auto` tree | The `.path` file |
| Triggered by | Its position in the sequence | Reaching a point along the path |
| Best for | "Start shooting when this leg starts" | "Deploy the intake exactly at this waypoint" |
| Visible in | The auto editor | The path editor |

Markers are precise about *where*; the auto tree is precise about *when* relative to other steps. Our autos use the tree only, which is simpler to read and enough for a robot whose mechanisms are driven by states rather than by choreography.

:::danger Register before you build
`AutoBuilder.buildAutoChooser()` builds a command for **every** auto file at startup. A name that isn't registered yet becomes a command that does nothing, and PathPlanner prints a warning that scrolls past in the console.

The failure is silent on the field: the robot drives its path perfectly and never shoots. Registration happens first in our `RobotContainer` for exactly this reason, and it is the first thing to check when an auto drives correctly but does nothing else.
:::

## What our autos leave running

Every one of our autos ends with the Superstructure in a state, and most end in `SHOOTING`. Nothing resets it, because the named command finished the moment it ran.

At the end of autonomous, `Robot.teleopInit` cancels the **autonomous command**, but the Superstructure's wanted state is not a command; it is a field. So the robot enters teleop still shooting, with the turret aimed and the flywheel spinning.

During REBUILT's transition period both HUBs are active, so that is often what you want. It is still worth knowing, because:

- The driver takes over a robot that is already doing something.
- The intake is running, which matters if a FUEL is in the way.
- If the drive team expects a quiet robot at the start of teleop, they will report it as a bug.

An auto that ends with `reverse`, or a third named command that sets `IDLE`, would make the handoff explicit. That is a design decision for the team, not an obvious defect, which is why the linter in this lesson's exercise reports it as a finding rather than an error.

:::exercise id="u12-autolint"
Write a linter for autonomous routines. It walks the auto tree, collects the named commands and paths, and reports four findings: unregistered commands, following a path without resetting odometry, two paths running in parallel, and ending with the robot still shooting.

The test data is our real `Just Preload` and `Depot` autos, so the findings you produce are findings about our robot.
---hint
The step types are a sealed interface with records. Walk it recursively with `instanceof` patterns: `if (step instanceof Sequential s) { s.steps().forEach(...); }`.
---hint
Collect named commands and paths with one traversal each, appending to a list, so order comes out the way it appears in the file.
---hint
For "two paths in parallel," check each parallel step by counting the paths anywhere inside it, then recurse into its children for nested parallel steps.
:::

:::quiz
? Why does `RobotContainer` register named commands before building the auto chooser?
+ The chooser builds a command for every auto at startup, so a name registered later is already too late
- Registration order determines execution order
- PathPlanner caches commands between runs
- Named commands must be registered while disabled
> A missing registration becomes a command that does nothing, with only a console warning.

? What does the `start` named command actually do?
+ Sets the Superstructure's wanted state to `SHOOTING` and finishes immediately
- Runs the flywheel until it reaches speed
- Waits for the turret to aim, then shoots
- Follows a path to the HUB
> The state machine does the work; the auto only asks for it.

? In `Depot.auto`, what runs at the same time as the `Depot` path?
+ A short wait followed by setting the state to `SHOOTING`
- Nothing; the auto is fully sequential
- The `reverse` command
- A second path
> Overlapping spin-up with driving is where autonomous time is found.

? What does `resetOdom` do, and when should an auto set it?
+ It resets the robot's pose to the start of the first path; any auto that follows a path should set it
- It resets the gyro only, and only matters on red
- It clears the auto chooser
- It restarts the flywheel
> Path following is only as good as the pose it starts from.

? An auto drives its path perfectly but never shoots. What do you check first?
+ Whether the named command it uses is registered
- The translation PID gains
- The wheel radius
- The alliance flip
> That failure is silent except for a console warning at startup.

? What state is our robot in when teleop begins after most of our autos?
+ `SHOOTING`, because the named command set a state and nothing resets it
- `IDLE`, because `teleopInit` cancels the autonomous command
- `PAUSED`
- Whatever the driver last selected
> Canceling the auto command doesn't change a field the command already set.
:::
