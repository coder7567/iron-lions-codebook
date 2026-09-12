---
summary: What the software lead actually does across a season: bringing people in, deciding what gets built, protecting the schedule, and handing the team to the next person in better shape than you found it.
objectives:
  - Onboard a new programmer without absorbing your whole week
  - Decide what to build and what to refuse
  - Run the season's phases with the right priorities in each
  - Hand over the role so the team survives your graduation
files:
  - src/main/java/frc/robot/RobotContainer.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## The job

The software lead writes less code than anyone expects. The work is deciding what gets built, making
sure two people understand each subsystem, and keeping the robot deployable on the days it matters.

A useful test: **if you were sick for a week in February, would the robot still work?** Everything in
this lesson is aimed at making the answer yes.

## Bringing someone in

A new programmer's first month decides whether they stay. The pattern that works:

:::steps
1. **Give them this course and a deadline.** Units 1 through 7 in the first few weeks, with the
   exercises, is a realistic pace for someone new to Java.
2. **Pair them with a returning member** for their first real change, and let the newer person hold the
   keyboard.
3. **Their first task should be small, visible, and theirs.** Binding the X-stop button beats "help
   with vision." They should be able to point at the robot and say what they did.
4. **Review it carefully and kindly.** The first review teaches them what the team's standards are, and
   whether feedback here is safe.
5. **Then give them a subsystem.** Ownership creates the motivation to understand something completely.
:::

The failure mode to avoid is the one where the lead does everything because it is faster. It is faster,
for about six weeks, and then the team has one person who can fix the turret and that person has
homework.

## Deciding what gets built

Every season produces more ideas than build days. Sort them:

| Category | Rule |
|---|---|
| **Must work** | Drive, score the main game piece, one reliable auto. These get the practice time |
| **Should work** | The second scoring mode, additional autos, driver aids |
| **Nice** | Automation of things a driver already does well |
| **No** | Anything that cannot be tested before the next event |

The hardest lead skill is saying no to a good idea because there is no time to test it. A feature that
works in the shop and has never been driven in a match is a liability wearing a feature's clothes.

Two questions decide most of these:

- **Who verifies it, and when?** If the answer is "we will see at the event," it is a no.
- **What breaks if it fails mid-match?** A driver aid that fails should degrade into manual control,
  not into a robot that cannot move.

## The season, by phase

| Phase | Priority | Watch out for |
|---|---|---|
| **Off-season** (now through kickoff) | Fix findings, write tests, improve simulation, train new members | Rewriting things that work |
| **Weeks 1 to 3** | Drivetrain moving, logging on, IO layers stubbed for mechanisms that do not exist yet | Waiting for hardware instead of writing against IO interfaces |
| **Weeks 4 to 6** | Mechanisms, state machines, the first autos | Tuning before the mechanism is mechanically repeatable |
| **The last week before an event** | Practice time, tuning, checklists, no new features | The "one more thing" that costs the practice day |
| **Events** | Reliability, logs, drive team support | Deploying anything untested |
| **Between events** | Fix what the event exposed, and only that | Ambitious rewrites with two weeks left |

Our 2026 season is a good illustration: a strong Iowa Regional showing, an Industrial Design Award, and
two playoff exits in round two. The gap between that and a deeper run is almost always reliability and
autonomous consistency, which are the things that get built in the phases nobody finds exciting.

## Documentation the team will actually keep

Three artifacts, all short:

- **The controller map.** One page, both controllers, updated the day a binding changes. The drive team
  reads it more than any other document.
- **The calibration log.** Date, what was measured, the value, and why. Answers the question that comes
  up at every event.
- **The findings list.** What is known-broken and what it costs. This course's code audit is that
  document; keep it alive rather than starting a new one.

Notice what is not on the list: a design document nobody reads, and per-method comments that restate
the code. Documentation survives when it answers a question somebody asks repeatedly.

## Handing it over

Start six weeks before you leave, not the week after your last event:

:::steps
1. **Name your successor early** and tell them. Surprise leads do not have time to learn.
2. **Give them the pit role at the next event** while you stand behind them.
3. **Transfer the accounts and the keys**: repository access, dashboards, the laptop image, and any
   licenses.
4. **Walk the codebase together**, subsystem by subsystem, and record which parts only you understand.
5. **Fix those parts** or document them, because they are the team's single points of failure.
6. **Leave the findings list current**, so the next lead starts with a map instead of an archaeology
   project.
:::

:::team The measure of a lead
Not the robot's performance in your last season, which depends on twenty people and a game you did not
choose. The measure is whether the software team is stronger the year **after** you leave than the year
you led it.

That is decided by how many people you taught, how much of the codebase more than one person
understands, and whether the next lead inherits notes or a mystery.
:::

:::quiz
? What is the best test of whether a software team is healthy?
+ Whether the robot would still work if the lead were out for a week in February
- Whether the code compiles without warnings
- How many features shipped
- Whether the lead wrote the most code
> Everything in this lesson is aimed at making that answer yes.

? What should a new programmer's first real task look like?
+ Small, visible, and theirs, so they can point at the robot and say what they did
- A subsystem rewrite, to learn quickly
- Documentation, until they know the code
- Whatever the lead does not want to do
> Ownership is what creates the motivation to understand something completely.

? A great idea arrives three days before an event. What decides whether it gets built?
+ Whether anyone can verify it before the event
- Whether it is technically interesting
- Whether the lead has time to write it
- Whether the drive team likes it
> A feature that has never been driven in a match is a liability.

? Which phase of the season is the wrong time for new features?
+ The last week before an event
- Weeks 1 to 3
- The off-season
- Between events, for problems the event exposed
> That week belongs to practice, tuning, and checklists.

? Which documents actually survive on a team?
+ The controller map, the calibration log, and the findings list
- A design document and per-method comments
- The engineering notebook alone
- Meeting minutes
> Documentation survives when it answers a question people keep asking.

? When should handover start?
+ About six weeks before you leave, including a pit role at an event with you watching
- After your last event
- At the start of your last season
- When your successor asks
> Surprise leads do not have time to learn.
:::
