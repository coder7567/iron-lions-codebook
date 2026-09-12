---
summary: Show that you can run a match day, work with the drive team, debug under a match clock, plan for 2027, and lead the software team.
---

This test covers all of Unit 16. Several questions put you in the pit with six minutes on the clock.

:::exam Unit 16 test: the season
? Why tag a known-good commit before leaving for an event?
+ It is a rollback point you can deploy in two minutes without arguing about which commit was good
- Git requires a tag before deploying
- It speeds up the build
- It prevents others from pushing
> Naming the rollback point costs ten seconds.

? Why is "turret at its start position" on our pre-match checklist?
+ Its encoder is seeded at boot, so a bumped turret makes every aim wrong by that amount
- The turret needs to warm up
- It reduces startup current
- The soft limits reset at power-on
> Finding F23, turned into a physical habit.

? What is the first thing to do after a match, before anything else?
+ Pull the log off the robot
- Ask the drive team what happened
- Check the battery
- Start looking for the bug
> It takes seconds, and it is the only chance you get.

? What three things must you be able to say before deploying a change at an event?
+ What is broken, what your change does, and how you will know it worked
- The file, the author, and the reviewer
- The severity, the risk, and the rollback
- That it compiles, passes tests, and was reviewed
> Missing any of the three means the robot plays as it is.

? A SPARK is swapped between matches. What does software need to do?
+ Set its CAN ID with the REV Hardware Client; the code configures and persists the rest at boot
- Nothing at all
- Re-tune that motor's gains
- Reflash the roboRIO
> Our IO layer applies configuration at startup.

? A driver says the robot feels sluggish. What is the most useful next question?
+ "Coming out of a stop, or once you are already moving?"
- "How sluggish?"
- "Which match?"
- "Should I raise the speed limit?"
> One question eliminates most of the candidate causes.

? Who usually notices a state machine problem first?
+ The operator, watching mechanisms do something nobody asked for
- The driver
- The coach
- The pit programmer
> Each role on the drive team sees a different class of failure.

? In a debrief, why does the driver speak first, uninterrupted?
+ Explaining before they finish is the fastest way to stop hearing about problems
- They outrank the programmer
- It saves time
- The coach requires it
> Reports that go nowhere stop being made.

? Why report back even when the log looked completely normal?
+ Otherwise the drive team learns that reporting something leads nowhere
- The log is never actually normal
- It is required by the checklist
- The coach needs the data
> Closing the loop is what keeps the reports coming.

? At an event, what is the very first question when something goes wrong?
+ Whether it has to be fixed before the next match at all
- Which subsystem is at fault
- Whether the log shows an error
- Who last touched that code
> A known annoyance beats an untested change.

?order Put the free event checks in the recommended order.
1. Battery
2. Connectors and the CAN chain
3. Dashboard alerts
4. Auto selection
> Most event problems stop before anyone opens a log.

? "It worked in the shop." Which event-only cause is worth checking first on our robot?
+ Which alliance you are on, since the flip depends on the pose's heading being right
- The Java version on the laptop
- The Gradle cache
- The USB stick's format
> Field-relative driving and autos both mirror with the alliance.

? Vision poses appear throughout a match log, and vision was clearly wrong. What does finding F14 suggest?
+ A camera may have disconnected, since the inputs keep reporting the last observation
- The tags were damaged
- The ambiguity limit was too high
- The robot moved too fast for vision
> Poses in a log are not proof that a camera was working.

? What is the only acceptable software action once the robot is on the field?
+ Confirming the auto selection
- Deploying a one-line fix
- Adjusting a live tunable
- Restarting robot code
> Whatever is on the robot is what plays the match.

? Which of our files would need a real rewrite for the 2027 control system?
+ `GyroIONavX`, because SystemCore has no SPI port for the NavX
- `Superstructure`
- `Turret`
- `Drive`
> The IMU story changes; the state machines do not.

? Why do our state machine classes survive a port almost untouched?
+ They speak only in enums, records, and IO interfaces, none of which are vendor-specific
- They are small
- They already have tests
- WPILib guarantees subsystem compatibility
> That containment is exactly what the IO pattern buys.

? Why wait for at least a beta before porting to a new control system?
+ Names and APIs move during an alpha, so porting early means doing it twice
- Alphas cannot be downloaded
- The rules forbid it
- Gradle will not build against one
> Map the work now; do the work when the target stops moving.

? What makes a capstone project finished?
+ Someone else can tell that it works
- The code compiles and merges
- The branch is pushed
- The lead approved the plan
> Write the definition of done before the first commit.

? A great feature idea arrives three days before an event. What decides whether it gets built?
+ Whether anybody can verify it before the event
- Whether it is technically interesting
- Whether the lead has time
- Whether the drive team wants it
> An unverified feature is a liability wearing a feature's clothes.

?? Which documents actually survive on a software team? Select all that apply.
+ The controller map
+ The calibration log
+ The findings list
- A design document written at kickoff
> Documentation survives when it answers a question people keep asking.
:::
