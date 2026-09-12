---
summary: What the programmer at an event actually does, hour by hour: the kit, the pre-match checklist for this robot, the between-matches cycle, and the rules that keep a weekend from going sideways.
objectives:
  - Pack and prepare for an event as the software person
  - Run our robot's pre-match checklist from memory
  - Follow the between-matches cycle, including pulling logs
  - Decide what may be changed at an event and what may not
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/turret/TurretIOSpark.java
---

## The role

At an event, one person owns the laptop. Not the code, the laptop: the machine that can deploy, the
cable that reaches the robot, and the record of what is currently running. Everything else about the
job follows from that ownership.

The job is mostly **not** writing code. It is checklists, logs, and short conversations with the drive
team, punctuated by the occasional five-minute fix.

## The kit

| Item | Why |
|---|---|
| Laptop with the toolchain installed and **tested offline** | Event wifi is unreliable and often blocked; a first-time Gradle download will not happen |
| Charger, plus a power strip | Six matches on battery is not a plan |
| USB-A to USB-C adapters, and a spare USB cable to the roboRIO | The one you need is the one that broke |
| Ethernet cable | Deploying over a cable when the radio is unhappy |
| A spare USB stick for the robot's logs | And a labeled folder on the laptop for what you pull off it |
| A printed checklist | Because a phone dies and a screen gets borrowed |
| The team's build log or notebook | Where tuning values and calibrations get written down |

## Before you leave

:::steps
1. **Deploy the commit you intend to compete with**, run the robot, and confirm it behaves.
2. **Tag it.** `git tag event-iowa-known-good` and push the tag. This is your rollback point, and
   naming it costs ten seconds.
3. **Run the tests.** `./gradlew test` should be green before the robot goes in the trailer.
4. **Copy old logs off** the robot's USB stick and clear it, so this event's logs are the only ones on
   it.
5. **Write down the calibration values** currently in the code: module zeros, wheel radius,
   feedforward gains, and the shot map. If something gets changed at the event, you want the original.
6. **Charge everything**, including the laptop, and pack the checklist.
:::

## The pre-match checklist

This one is specific to our robot, and every item exists because of something in this course:

| Check | Why it is on the list |
|---|---|
| Battery in, strapped, and charged | Everything gets slower at 11 V |
| **USB log stick in the roboRIO** | No stick, no log, no diagnosis afterward |
| **Turret physically at its start position** | The encoder is seeded to −1.6 rad at boot (finding F23); a bumped turret aims wrong all match |
| Robot powered on and code running, with no alerts on the dashboard | Module and gyro disconnects show up here first |
| Both cameras connected | The dashboard shows it; a disconnected camera keeps reporting stale poses (finding F14) |
| **Auto selected and read aloud** | The chooser also holds SysId routines that drive at full voltage |
| Drive team briefed: which auto, which side, what the partners are doing | Most auto failures are collisions |
| Bumpers on, correct alliance color | Not your job, still your problem |

Read the list out loud with a second person. A checklist you recite from memory alone is a checklist
you skip an item on.

## Between matches

You get about six minutes. The cycle:

:::steps
1. **Pull the log** while the robot is still on the cart. It takes seconds and it is the only chance.
2. **Ask the drive team one question**: did the robot do anything you did not expect?
3. **Look at the log only if the answer was yes**, and only at the ten seconds around the moment.
4. **Decide whether to change anything**, using the rule below.
5. **If you deploy, re-verify**: enable, drive a few feet, run the mechanism once, re-select the auto.
6. **Write one line in the build log**: match number, what happened, what changed.
:::

:::danger The rule for changing code at an event
Change code only when you can say, in one sentence, **what is broken, what your change does, and how
you will know it worked**. If any of the three is missing, the robot plays the next match as it is.

A robot with a known annoyance scores points. A robot with an untested change sometimes does not move.
:::

## Things you may legitimately need to do

| Task | Notes |
|---|---|
| Re-select or re-verify an auto | Free, and the most common real task |
| Adjust a live tunable | Only the ones read every loop; write the value in the build log |
| Re-zero module offsets after a module swap | Follow the full procedure; do not eyeball it |
| Re-measure the wheel radius after a tread change | The spin test takes two minutes |
| Configure a replacement SPARK | Set the CAN ID with the REV Hardware Client; our code configures the rest at boot |
| Roll back to the tagged commit | `git checkout event-iowa-known-good` and deploy; know this by heart |
| Nothing | Frequently the correct answer |

:::tip When the robot is fine, help
Pit programmers with nothing to fix are the most useful people in the pit: scouting data entry, a
second pair of hands on a battery swap, watching another team's match to learn their auto. Idle time
is the job working correctly, not the job being unnecessary.
:::

:::quiz
? Why tag a known-good commit before an event?
+ It is a rollback point you can deploy in two minutes without deciding what "known good" meant
- Git requires a tag before deploying
- It makes the build faster
- It stops others from committing
> Naming the rollback point costs ten seconds and saves a match.

? Why is "turret at its start position" on the pre-match checklist?
+ The turret's encoder is seeded at boot, so a bumped turret makes every aim wrong by that amount
- The turret needs to warm up
- It reduces current draw at startup
- The soft limits reset on power-on
> Finding F23, made into a physical habit.

? Why pull the log before doing anything else after a match?
+ It takes seconds, and it is the only chance before the next match overwrites your attention
- Logs are deleted at the end of each match
- The FMS requires it
- It frees space on the USB stick
> You can decide later whether to read it; you cannot pull it later.

? What three things must you be able to say before deploying a change at an event?
+ What is broken, what the change does, and how you will know it worked
- Which file changed, who wrote it, and when
- The severity, the risk, and the rollback plan
- Whether it compiles, passes tests, and was reviewed
> Missing any of the three means the robot plays as it is.

? A SPARK is replaced between matches. What does the programmer need to do?
+ Set its CAN ID with the REV Hardware Client; our code applies the rest of the configuration at boot
- Nothing; it is plug and play
- Re-tune the PID gains for that motor
- Re-flash the roboRIO
> Configuration is applied and persisted at startup by the IO layer.

?tf A pit programmer with nothing to fix during a match block is a sign something is wrong.
= false
> It is the job working. There is always scouting, batteries, and another team's match to watch.
:::
