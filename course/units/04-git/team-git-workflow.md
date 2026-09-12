---
summary: A concrete Git workflow for Team 967, covering branches and pull requests in build season, a commit-before-deploy routine at events, known-good tags, and fixing the stale build metadata in our logs.
objectives:
  - Follow the proposed 967 workflow for build season and competition days
  - Recover a known-good version of robot code quickly at an event
  - Explain how build metadata ties each log to a commit, and how to restore it
files:
  - build.gradle
  - src/main/java/frc/robot/BuildConstants.java
  - src/main/java/frc/robot/Robot.java
---

:::note A proposal to agree on
This lesson turns lessons from the 2026 history into a workflow. Talk it through with mentors and the software lead before the 2027 season, adjust it, and write down what you decide.
:::

## Build season: branches and pull requests

**`main` is always deployable.** Anything on `main` should build and should be safe to run on the robot.

:::steps
1. **Start from an up-to-date main:** `git switch main`, then `git pull`.
2. **Create a branch per task,** named for the work: `git switch -c climber-io-layer`.
3. **Commit small, commit often,** with messages that say what changed.
4. **Push the branch** and open a pull request when it works in simulation, or earlier, as a draft, to get feedback.
5. **Get one review** from a mentor or experienced student before merging.
6. **Merge, delete the branch, and pull `main`** on your laptop.
:::

Recommended settings for a mentor with GitHub admin access:

- Protect `main` so changes require a pull request with one approval during build season.
- Allow an event exception, below, so reviews never block a robot in the queue.

## Event days: speed with a safety net

Events are where history is most valuable and most often skipped. The 2026 commits around the Iowa Regional ("some iowa," "Match 9," "iowa") show real event work. This routine keeps that pace while making every change recoverable.

### Before the event

- [ ] Merge everything that is going to the event into `main` and push.
- [ ] Create an event branch: `git switch -c event/iowa-2027`, then `git push -u origin event/iowa-2027`.
- [ ] Tag the starting point: `git tag iowa-2027-start`, then `git push origin iowa-2027-start`.
- [ ] Build on the robot laptop **with internet**, so every vendor library is cached.

### Every change between matches

:::steps
1. **Two people look at it.** The programmer making the change explains it to a second person before deploying. It takes a minute and catches a surprising number of mistakes.
2. **Test what you can.** Simulate, or run the mechanism on the cart or on blocks.
3. **Commit, then deploy.** Name the match in the message: `Q24: lower hood 0.02 for far shots after misses in Q23`.
4. **Deploy, and check `git status` is clean** first, so the robot never runs uncommitted code.
5. **Push when there is internet,** from a phone hotspot or at the hotel, so the code is not only on one laptop.
:::

### Known-good tags

When the robot plays well, mark it:

```bash
git tag known-good-q18
```

If a later change goes badly and there are ten minutes until the next match, return to the known-good code, deploy it, and debug afterward:

```bash
git switch --detach known-good-q18   # check out exactly that commit
# ...deploy...
git switch event/iowa-2027           # come back later to investigate
```

### After the event

- Open a pull request from the event branch into `main`, and review what changed at the event.
- Tag the event's final code: `git tag Iowa-Regional-2027`. The team did exactly this in 2026 with `Iowa-Regional`.
- Turn anything learned into issues, like "hood angles too high at 5 m."

## Every log should name its commit

AdvantageKit writes metadata into every log file. `Robot.java` records the build's Git details:

::source file="src/main/java/frc/robot/Robot.java" from="Logger.recordMetadata(\"ProjectName\"" lines=16

Those values come from `BuildConstants.java`. The AdvantageKit template regenerates that file on every build using the **gversion** Gradle plugin, so a log from Match 9 tells you exactly which commit was running. Our 2026 `build.gradle` does not include that plugin, so the committed `BuildConstants.java` is frozen from November 2025:

::source file="src/main/java/frc/robot/BuildConstants.java" from="public static final String MAVEN_NAME" lines=9

Every 2026 log claims to be commit `f285ed0` on the old template project, built on November 22, 2025, with uncommitted changes. That is finding **F12** in the [Code Audit](course:reference/code-audit).

:::tip The fix, from the AdvantageKit template
Compare our `build.gradle` with the current AdvantageKit template project and copy its version-file setup. In recent templates that means adding the `com.peterabeles.gversion` plugin, making `compileJava` depend on `createVersionFile`, configuring `gversion` with `srcDir "src/main/java/"`, `classPackage "frc.robot"`, and `className "BuildConstants"`, and adding `src/main/java/frc/robot/BuildConstants.java` to `.gitignore` so the generated file is never committed. Check the template for the exact current syntax and plugin version.
:::

## Laptop hygiene

- **One robot laptop** at events holds the event branch, and everyone commits there or pushes from their own laptop first.
- **Never deploy from a branch nobody else can see.** If it is not pushed, it does not exist for the rest of the team.
- **`git status` before deploy, `git log -1` after.** Know exactly what is on the robot.
- **Do not store secrets in the repository.** No Wi-Fi passwords or tokens in code or commits.

:::quiz
? What does "main is always deployable" mean day to day?
+ Only work that builds and is safe to run gets merged to main; experiments live on branches
- Everyone commits directly to main as fast as possible
- Main is only updated after the season
- Main is deployed automatically every hour
> A trustworthy main is what makes every other part of the workflow work.

? It is 10 minutes before a playoff match, and a change made after Q18 made shooting worse. What is the fastest safe recovery?
- Try random constants until it works
+ Check out the `known-good-q18` tag, deploy it, and investigate after the match
- Delete the event branch
- Reinstall WPILib
> Tags let you return to exact known-good code in seconds.

?? Which belong in the between-match change routine? (Select all that apply.)
+ A second person reviews the change
+ Commit before deploying
+ Test in simulation or on the cart when possible
- Deploy uncommitted code to save time
+ Push when internet is available
> Each step is quick, and together they make every event change understandable and reversible.

? Why does every 2026 log report commit `f285ed0` from November 2025?
- AdvantageKit cannot read Git
+ `BuildConstants.java` is committed and never regenerated, because `build.gradle` lacks the gversion setup
- The roboRIO clock was wrong
- The logs were copied from the template
> Restoring the AdvantageKit template's version-file setup makes each build write fresh Git metadata.

?tf At events, a branch should only exist on the robot laptop, so it cannot be changed by others.
= false
> Push event work whenever there is internet. Code that exists on one laptop is one dropped laptop from gone.
:::
