---
summary: How the FRC Driver Station and the Field Management System control the robot, the match information code can read, and the alliance-color traps our code has already hit.
objectives:
  - Use the Driver Station's tabs, joystick ordering, and practice mode for 2026 timing
  - Read match information from DriverStation, including alliance, match time, and game data
  - Handle alliance changes and "not known yet" values correctly
files:
  - src/main/java/frc/robot/subsystems/Superstructure.java
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## The Driver Station

The **FRC Driver Station** (DS) is the Windows program that connects to the robot, enables it, and sends controller data every 20 ms. The robot cannot move without it.

| Tab | Use it for |
|---|---|
| **Operation** | Choose the mode (Teleoperated, Autonomous, Practice, Test), enable and disable, and see the robot's battery voltage |
| **Diagnostics** | Connection lights for Ethernet, radio, robot, and field; restart robot code or reboot the roboRIO |
| **Setup** | Team number (967), dashboard choice, and practice mode timing |
| **USB Devices** | Which controller is in which slot |
| **CAN/Power** | CAN utilization, bus faults, and brownout counts |

Messages from robot code, including exceptions and loop overrun warnings, appear in the DS **message console** on the right.

:::danger Keys every programmer must know
**Enter** disables the robot. **Space** is emergency stop: it disables the robot until the roboRIO is rebooted. Keep one hand near the keyboard whenever the robot is enabled.
:::

### Controller slots

Our code reads the driver on slot 0 and the operator on slot 1. In the **USB Devices** tab, drag controllers into the right order. The DS remembers devices, but a controller unplugged and plugged back in can change slots. **F1** rescans USB devices. Check the order before every match.

### Practice mode for 2026

Practice mode runs a timed match without a field. Set its timing in the Setup tab to match REBUILT: **20 s** autonomous, **110 s** teleop, and **30 s** end game. Otherwise match-time-based code like our HUB shift logic will be wrong during practice.

## The Field Management System

At competitions, the **Field Management System** (FMS) takes over. It tells every Driver Station when to enable, which mode to run, which alliance and station each robot is in, the match number, and in 2026 the game data letter. You cannot enable the robot yourself during a match. The FMS does it for everyone at once.

## What code can read

WPILib's `DriverStation` class exposes the data the DS and FMS provide:

| Method | Returns |
|---|---|
| `getAlliance()` | `Optional<Alliance>`, empty until known |
| `getLocation()` | `OptionalInt` station 1–3 |
| `isAutonomousEnabled()`, `isTeleopEnabled()`, `isDisabled()` | The current mode |
| `getMatchTime()` | Approximate seconds left in the current period |
| `getGameSpecificMessage()` | The 2026 game data letter, or `""` |
| `isFMSAttached()` | Whether this is a real field match |
| `getMatchNumber()`, `getEventName()` | Match information for logs |

AdvantageKit records all of this automatically every cycle, so logs always show what the robot believed about the match.

Our Superstructure uses several of these together:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="// No alliance? hub inactive." lines=17

## Alliance traps

### Unknown at boot

When the robot boots, the Driver Station may not be connected yet, so `getAlliance()` is empty. Code that reads the alliance **once, at startup** can lock in the wrong answer for the whole match.

That is exactly what happened with the turret's passing map. The `Turret` constructor fills in its passing targets using alliance-flipped field positions, but constructors run before the alliance is known, so it computes blue-side values. `Superstructure` patches this by rebuilding the map whenever the alliance changes:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="if (previousAlliance != DriverStation.getAlliance().orElse(previousAlliance))" lines=4

It works, but it depends on remembering to call `redoPassingFunction()` for every alliance-dependent value. The rebuilt map also forgot the hood offset on three entries, which is finding **F4** in the [Code Audit](course:reference/code-audit).

:::tip Compute alliance-dependent values when you use them
Prefer methods that flip positions every time they are called, like `TurretConstants.hub()`, which calls `AllianceFlipUtil.apply(hub)`. They are always right, no matter when the alliance becomes known.
:::

### Practice versus the field

In practice mode, the alliance comes from the station chosen on the Operation tab. Test both alliances before an event, or red-alliance bugs will appear for the first time in a qualification match.

## Driver Station logs

The DS saves its own logs of voltage, CPU, CAN, packet loss, and messages. After a strange match, open them in the **DS Log Viewer** or in **AdvantageScope** alongside the robot's AdvantageKit log. Together they answer "was it the code, the radio, or the battery?"

:::quiz
? Which key disables the robot, while still allowing it to be enabled again without a reboot?
+ Enter
- Space
- Escape
- F1
> Space is emergency stop, which requires a roboRIO reboot before enabling again.

? Our operator controller does nothing, but the driver controller works. What should you check first?
+ That the operator controller is in slot 1 in the Driver Station's USB Devices tab
- The CAN bus wiring
- The Elastic layout
- The robot's alliance color
> `RobotContainer` reads the operator on slot 1. Controllers can change slots when replugged.

?tf At a competition, the drive team enables the robot for each match by pressing Enable in the Driver Station.
= false
> The Field Management System enables and disables every robot on the field.

? Why can `DriverStation.getAlliance()` be empty right after the robot boots?
+ The Driver Station may not have connected and reported the alliance yet
- The FMS assigns alliances after the match ends
- It only works in autonomous
- The roboRIO stores the alliance on USB
> Code must handle the "not known yet" case, not just red and blue.

? Why does the `Turret` need `redoPassingFunction()` when the alliance changes?
+ Its constructor computes alliance-flipped passing targets before the alliance is known, so they start as blue-side values
- PathPlanner requires it
- The hood angle changes with battery voltage
- The alliance changes every shift
> Values computed once at construction do not update on their own. Compute alliance-dependent values when used.
:::
