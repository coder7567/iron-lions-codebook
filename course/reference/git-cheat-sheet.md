---
summary: The git commands this team actually uses, the workflow around them, and how to get out of the situations that scare people.
---

Everything here assumes the team's repository and the branch-and-pull-request workflow the repository
already shows. When in doubt, ask before force-pushing anything.

## Every day

```bash
git status
```

```bash
git switch -c turret-pause-fix
```

```bash
git add -p
```

```bash
git commit -m "Fix PAUSED branch so PAUSEDPASSING is reachable"
```

```bash
git push -u origin turret-pause-fix
```

| Command | What it does |
|---|---|
| `git status` | What is changed, staged, and which branch you are on. Run it constantly |
| `git switch -c <name>` | Create a branch and move to it |
| `git switch main` | Move back |
| `git add -p` | Stage changes piece by piece, which is also a free review of your own work |
| `git commit -m "..."` | Record the staged changes |
| `git push -u origin <name>` | Publish the branch and set it to track |
| `git pull` | Bring in what others pushed |

## Before you start work

```bash
git switch main && git pull
```

Branching from a stale `main` is the most common cause of a painful merge. Ten seconds here saves an
hour later.

## Looking around

| Command | Use |
|---|---|
| `git log --oneline -20` | The last twenty commits |
| `git log --oneline -- src/main/java/frc/robot/subsystems/turret/Turret.java` | The history of one file |
| `git log -S "PAUSEDPASSING" --oneline` | Every commit that added or removed that text |
| `git show <sha>` | What one commit changed |
| `git blame <file>` | Who last touched each line, and in which commit |
| `git diff` | Unstaged changes |
| `git diff --staged` | What you are about to commit |

`git log -S` is the one people do not know about, and it is how this course found when the pause states
were added and when the full-field mode was removed.

## Commit messages

The repository's own history shows what happens without a convention: `iowa`, `Match 9`,
`some iowa`, and `working-Nick` are real commit messages in it. They were written under time pressure
and they cost nothing at the time. They cost something now, because nobody can tell what changed.

A message worth writing has a subject line that finishes the sentence "this commit will…":

```text
Fix PAUSED branch so PAUSEDPASSING is reachable

Both branches yielded PAUSEDSHOOTING, so the turret aimed at the HUB
while paused outside our alliance zone. Found by replaying Iowa match 32.
```

- Subject in the imperative, under about 60 characters.
- A blank line, then the why, if the why is not obvious.
- One behavior change per commit.

## Pull requests

:::steps
1. Push your branch.
2. Open a pull request describing **what changed, why, and how you tested it**.
3. Ask a specific person to review it.
4. Respond to comments by changing code or explaining; both are fine.
5. Merge when it is better than what is on `main`, not when it is perfect.
6. Delete the branch.
:::

For a change that touches the robot, "how you tested it" should say what you actually ran: unit tests,
simulation, or the robot itself.

## Getting out of trouble

| Situation | Fix |
|---|---|
| Changed a file and want it back | `git restore <file>` |
| Staged something by mistake | `git restore --staged <file>` |
| Last commit message is wrong, not pushed | `git commit --amend` |
| Committed to `main` by accident, not pushed | `git branch my-work && git reset --hard origin/main && git switch my-work` |
| Need to park work and switch tasks | `git stash`, later `git stash pop` |
| Want the state from before everything | `git switch --detach <sha>` to look; branch from it if you want to keep it |
| A merge conflict | Open the file, keep the right lines, delete the markers, `git add` it, then finish the merge |
| Something is truly lost | `git reflog` lists where HEAD has been; almost nothing committed is ever gone |

:::danger Two rules with sharp edges
**Never `git push --force` to a shared branch**, especially `main`. If you think you need it, ask
first. `--force-with-lease` is the safer version when a rewrite is genuinely required.

**Never commit secrets or huge binaries.** Git keeps them forever, in every clone.
:::

## Conflicts, concretely

A conflict looks like this in the file:

```text
<<<<<<< HEAD
    public static final double turretOffsetChange = 0.10;
=======
    public static final double turretOffsetChange = 0.02;
>>>>>>> operator-trim
```

Decide which value is right (or write a third), delete all three marker lines, save, `git add` the
file, and continue. Conflicts are not errors; they are git telling you two people changed the same
line and it will not guess.

The exercises project ships a script that creates exactly this conflict on purpose, so you can practice
resolving one before it happens on a deadline.

## For an event

```bash
git tag event-iowa-known-good
```

```bash
git push origin event-iowa-known-good
```

Then, if a change goes wrong:

```bash
git switch --detach event-iowa-known-good && ./gradlew deploy
```

Tag the known-good commit before you leave, and know the rollback by heart. Rehearse it once in the
shop; the pit is the wrong place to read documentation.
