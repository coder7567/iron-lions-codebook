---
summary: Record work with commits, including the working tree, staging area, and history, and write commit messages that will actually help the team at an event.
objectives:
  - Move changes through the working tree, staging area, and commits with status, add, diff, and commit
  - Read history with log, show, and diff
  - Write clear commit messages and undo changes safely with restore and revert
files:
  - .gitignore
  - src/main/java/frc/robot/BuildConstants.java
---

## Three places your changes live

Git tracks your work in three places:

| Place | What it is | Moves on with |
|---|---|---|
| **Working tree** | The files you are editing right now | `git add` |
| **Staging area** | Changes you have chosen for the next commit | `git commit` |
| **History** | Commits saved permanently in `.git` | `git push` to share |

Staging lets you commit **related** changes together, even if you edited several unrelated things. For example, commit the turret fix in one commit and the dashboard tweak in another.

## The everyday loop

```bash
git status                  # what changed? what is staged?
git diff                    # show unstaged changes line by line
git add src/main/java/frc/robot/subsystems/turret/Turret.java
git diff --staged           # show exactly what will be committed
git commit -m "Clamp passing hood angle to the hood's range"
git log --oneline -5        # confirm it is in history
```

**Run `git status` constantly.** It tells you which branch you are on, what is modified, what is staged, and which files are **untracked**, meaning new files Git is not recording yet.

:::warning New files are not saved until you add them
One commit in our history is literally "Put the layout on git." The dashboard layout existed on a laptop but had never been added. If that laptop had died, the layout would have been gone. When `git status` lists untracked files you care about, `git add` them.
:::

### Useful variations

- `git add -p` walks through each changed chunk and asks whether to stage it. It is great for splitting unrelated edits.
- `git add .` stages everything in the current folder. Run `git status` first so you do not commit something by accident.
- `git commit` without `-m` opens an editor, so you can write a longer message.

## What Git ignores

`.gitignore` lists files Git should never track. Our repository ignores build output and machine-specific files:

::source file=".gitignore" from=".gradle" lines=6

`build/` is recreated every time you build, and `.gradle` is Gradle's cache, so committing either would bloat the history. Logs and local simulator window settings are personal, too.

## Reading history

```bash
git log --oneline --graph --decorate -20   # compact history with branches
git show 1ca2ec5                           # everything in one commit
git log -p -- src/main/java/frc/robot/subsystems/turret/TurretConstants.java  # every change to one file
git diff Iowa-Regional main -- src/main/java/frc/robot/subsystems/turret      # what changed since the event
git blame src/main/java/frc/robot/subsystems/intake/IntakeConstants.java      # who last changed each line, and in which commit
```

In VS Code, the **Source Control** view and the **Timeline** at the bottom of the Explorer show the same information with buttons.

## Commit messages that help

At an event, someone will scroll through history looking for "when did the shooter change?" Compare what they will find:

| Helps later | Does not help |
|---|---|
| Add rumble if scoring in inactive | um i have no clue |
| make sure intake and turret don't collide | put values in idk what for |
| Add operator controller | Some testing |
| Update gear ratio | ABSOLUTELY NOTHING |
| Fix intake pid and tune to a reasonable degree in sim | testing tuesday |

All of these are real messages from Rebuilt-2026. The left column describes *what changed*. The right column describes a mood or a day of the week.

Good commit messages:

- **Start with a verb, like a command:** "Add," "Fix," "Tune," "Remove."
- **Keep the first line short,** under about 60 characters.
- **Say what and why,** not how you felt. If tuning, include the values: "Tune flywheel kV to 1.93e-3 after Iowa shot data."
- **Use a longer body when needed.** Leave a blank line after the summary, then explain the reasoning.

```text
Lower passing hood angle for red alliance

redoPassingFunction left out hoodOffset on three map entries, so red
alliance passes used a hood angle 0.291 lower than blue. Match the
constructor's values.
```

## Commit before you deploy

`BuildConstants.java` has a `DIRTY` field that records whether the code had uncommitted changes when it was built:

::source file="src/main/java/frc/robot/BuildConstants.java" from="public static final String GIT_SHA" lines=6

If you deploy uncommitted code, nobody can ever reproduce exactly what ran. **Commit, then deploy**, even if the message is "Try hood offset 0.30 for practice match". You can always clean up history later on a branch, but you cannot recover code that was never saved.

## Undoing things

| Situation | Command | Safe on shared history? |
|---|---|---|
| Throw away edits to a file you have not staged | `git restore Turret.java` | Yes, but the edits are gone for good |
| Unstage a file but keep the edits | `git restore --staged Turret.java` | Yes |
| Fix the message of your last, **unpushed** commit | `git commit --amend` | Only before pushing |
| Undo a commit that is already pushed | `git revert <sha>` | **Yes**, it adds a new commit that reverses the old one |

The team used exactly the safe option this season: `Revert "delpoy for testing"` undid a pushed commit by adding a new one, so nobody's history was rewritten.

:::danger Never rewrite shared history
Commands like `git reset --hard` followed by `git push --force` can erase teammates' commits from GitHub. On the team repository, use `git revert` to undo pushed work, and ask a mentor before any command with `--force`.
:::

## Practice

Use your exercises project from Unit 1 as a practice repository:

- [ ] In a terminal inside the exercises folder, run `git init` and then `git status`.
- [ ] Commit everything as a starting point: `git add .` then `git commit -m "Start Codebook exercises"`.
- [ ] Solve one exercise, run `git diff` to review your change, and commit it with a message that says what you did.
- [ ] Make a change you do not want, then undo it with `git restore`.
- [ ] Run `git log --oneline` and read your history.

:::quiz
?order Put these steps for saving a change to history in order.
1. Edit `Turret.java`
2. `git add src/main/java/frc/robot/subsystems/turret/Turret.java`
3. `git commit -m "Clamp turret setpoint to soft limits"`
4. `git push`
> Edit, stage, commit, then share.

? `git status` lists `elastic-layout.json` under "Untracked files." What does that mean?
- The file is committed and unchanged
+ Git sees the file but is not recording it; it will not be in any commit until you add it
- The file is ignored forever
- The file was deleted
> New files must be added before Git saves them.

? Which commit message is most useful to the team during an event?
- "stuff"
- "Friday"
+ "Raise flywheel tolerance to 1000 RPM so feeding starts sooner"
- "fixed it!!!"
> It says what changed, including the value, and why.

? A bad commit is already pushed to GitHub `main`. What is the safe way to undo it?
- `git reset --hard HEAD~1` then `git push --force`
+ `git revert <sha>` then push
- Delete the repository and clone again
- Edit the files back by hand without committing
> Revert adds a new commit that undoes the old one, without rewriting anyone's history.

?tf `BuildConstants.DIRTY = 1` means the robot code was built with uncommitted changes.
= true
> The robot then runs code nobody can exactly reproduce from Git. Commit before deploying.
:::
