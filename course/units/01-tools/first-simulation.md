---
summary: Run the real 2026 robot program on your laptop, drive it with a controller, and watch it move in AdvantageScope, with no robot required.
objectives:
  - Start the simulator and switch between disabled, teleop, and autonomous
  - Connect a controller or keyboard joystick and drive the simulated robot
  - Watch the robot's pose and swerve states live in AdvantageScope
files:
  - src/main/java/frc/robot/Constants.java
  - src/main/java/frc/robot/RobotContainer.java
  - simgui-ds.json
---

## Why simulate?

The team has one competition robot and dozens of things to try. **Simulation runs the exact same robot program on your laptop**, with physics models standing in for motors and sensors. You can test logic, autos, and dashboards at home, at lunch, or while the build team has the robot apart.

Our code decides at startup whether it is on a real roboRIO:

::source file="src/main/java/frc/robot/Constants.java" from="public static final Mode simMode" lines=2

On a laptop `RobotBase.isReal()` is `false`, so `currentMode` becomes `SIM`. Then `RobotContainer` builds simulated hardware instead of real hardware:

::source file="src/main/java/frc/robot/RobotContainer.java" from="case SIM:" lines=18

`ModuleIOSim`, `TurretIOSim`, and friends pretend to be motors. The `Drive`, `Turret`, and `Intake` subsystems cannot tell the difference. That trick, swapping the **IO layer**, is the heart of [Unit 11](course:11-logging/advantagekit-architecture).

## Start the simulator

:::steps
1. Open `Rebuilt-2026` in WPILib VS Code and make sure it builds.
2. Open the Command Palette and run **WPILib: Simulate Robot Code**.
3. When asked which extensions to use, check **Sim GUI** and click OK.
4. Wait for the **Robot Simulation** window to appear. The first launch is slow.
:::

The Sim GUI window has several panels. The ones you need now:

- **Robot State:** buttons for **Disconnected**, **Disabled**, **Autonomous**, **Teleoperated**, and **Test**. The robot starts disabled, exactly like on the field.
- **System Joysticks:** controllers and keyboards your laptop can see.
- **Joysticks:** the six Driver Station slots. Our driver controller must be in **slot 0**.

## Connect something to drive with

**Best option: a real Xbox controller.** Plug it in, then drag it from **System Joysticks** onto **Joystick[0]**. Now the robot code sees exactly what it sees on the field.

**No controller?** Drag **Keyboard 0** onto **Joystick[0]**. The team's `simgui-ds.json` maps it like this:

| Keys | Joystick input | What our code does with it |
|---|---|---|
| **W / S** | Axis 1 (left stick Y) | Drive forward and backward |
| **A / D** | Axis 0 (left stick X) | Drive left and right |
| **E / R** | Axis 2 (left trigger) | Left trigger: Superstructure **IDLE** |
| **Z X C V** | Buttons 1–4 (A, B, X, Y) | Not bound in `RobotContainer` |

:::note What the keyboard cannot do
An Xbox controller's right stick X is axis 4 and its right trigger is axis 3. Keyboard 0 only defines axes 0–2, so you cannot rotate or trigger SHOOTING from the keyboard. For full control, use a real controller.
:::

Now click **Teleoperated**. Push **W** or the left stick forward. The robot is driving, just invisibly. Time to see it.

:::warning Simulation drives like a real robot
The simulator enforces the same rules as the field: nothing moves while **Disabled**, and commands stop when you disable. If "nothing happens," check that you clicked Teleoperated and that your controller is in slot 0.
:::

## Watch it in AdvantageScope

Our code publishes everything it logs to NetworkTables while simulating. AdvantageScope turns that into graphs and a 3D field.

:::steps
1. Run **WPILib: Start Tool** and choose **AdvantageScope**.
2. In AdvantageScope, choose **File → Connect to Simulator**.
3. Click the **+** tab button and add a **2D Field** tab. Set the field to the 2026 game.
4. From the sidebar, drag `RealOutputs/Odometry/Robot` onto the field as a robot.
5. Drive in teleop and watch the robot move. Push the left stick sideways and it strafes, because it is swerve.
6. Add a **Swerve** tab and drag in `RealOutputs/SwerveStates/Measured` to see all four wheels steer.
:::

## Run an autonomous routine

The auto chooser is published as `Auto Choices`. You can pick an option from the Sim GUI's **NetworkTables** view under `SmartDashboard/Auto Choices`, or from Elastic connected to your own computer. Choose **Just Preload** or **Depot**, click **Autonomous** in Robot State, and watch the path. In AdvantageScope, drag `RealOutputs/Odometry/Trajectory` onto the field to draw the planned path next to the robot.

:::team In our code: a sim gap you can fix later
In simulation, pressing SHOOTING spins nothing. `TurretIOSim` never applies voltage to its flywheel model, so the simulated flywheel stays at 0 RPM, `shooterSpedUp()` stays false, and the feeder never runs. It is finding **F6** in the [Code Audit](course:reference/code-audit), and a good first contribution once you finish [Physics Simulation](course:11-logging/simulation).
:::

## Stop the simulator

Close the Robot Simulation window, or click in the VS Code terminal and press **Ctrl + C**. Always stop one simulation before starting another, because two copies fight over the same network ports.

:::quiz
? Why does our code use simulated hardware when you run it on a laptop?
- A setting in the Sim GUI tells it to
+ `Constants.currentMode` is `SIM` whenever `RobotBase.isReal()` is false, so `RobotContainer` builds the simulated IO classes
- Laptops cannot compile the real hardware classes
- PathPlanner switches it into simulation
> The mode is decided in `Constants`, and `RobotContainer`'s switch picks `ModuleIOSim`, `TurretIOSim`, and the other sim implementations.

? You clicked Teleoperated and pushed the stick, but nothing moves. What should you check first?
+ That your controller or keyboard is assigned to Joystick slot 0
- That the roboRIO is plugged in
- That AdvantageScope is open
- That you are on the red alliance
> Our driver controller is `new CommandXboxController(0)`, so it reads slot 0. With nothing in that slot, the stick values are zero.

?tf With the team's keyboard mapping, you can trigger the SHOOTING state from Keyboard 0.
= false
> SHOOTING is bound to the right trigger, axis 3. Keyboard 0 only maps axes 0–2, so use a real controller.

?text Which AdvantageScope key shows the robot's estimated position on the field? (Type the full key.)
= RealOutputs/Odometry/Robot | /Odometry\/Robot/
> `Drive.getPose()` is annotated with `@AutoLogOutput(key = "Odometry/Robot")`. AdvantageKit publishes outputs under `RealOutputs`.
:::
