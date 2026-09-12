---
summary: Why a robot team cannot live without version control, what Git and GitHub each do, and the handful of ideas (repository, commit, branch, remote) that the rest of the unit builds on.
objectives:
  - Explain what problems version control solves for a robot team during build season and at events
  - Tell Git apart from GitHub
  - Describe repositories, commits, branches, tags, and remotes
  - Configure Git on your computer
---

## The problem

Picture a Friday at a regional. The turret aimed perfectly in Match 9. Between matches, two students each change a few numbers on different laptops. Now it misses. Which change broke it? What exactly was on the robot in Match 9? Can you get that code back in the next ten minutes?

Without **version control**, those questions have no reliable answers. With it, each one takes a single command.

## What Git gives the team

**Git** is a version control system: a program that records snapshots of your project's files and their history.

| Need | What Git provides | Where you can see it in Rebuilt-2026 |
|---|---|---|
| Know what changed, when, and why | **Commits**, snapshots with a message | 117 commits on `main`, from "Initial commit" on January 11 to September 2026 |
| Work without breaking everyone else | **Branches** | `Turret-Dev`, `Intake-Dev`, `pose-estimation-dev-bot` |
| Review before code goes to the robot | **Pull requests** on GitHub | Pull request #4, "Use-6328-field-constants" |
| Undo a bad change safely | **Revert** | The commit `Revert "delpoy for testing"` |
| Mark exactly what ran at an event | **Tags** | The tag `Iowa-Regional` |

## Git versus GitHub

These are easy to confuse:

- **Git** runs on your computer. It works offline, in the pit, with no internet.
- **GitHub** is a website that hosts Git repositories so the team can share them. It adds pull requests, code review, and issue tracking. Our code lives in the **FRC-IronLions-967** organization on GitHub.

You commit locally with Git, then **push** to GitHub to share and **pull** to get teammates' work.

## The core ideas

**Repository.** A project folder plus its complete history, stored in the hidden `.git` folder. Cloning copies the whole thing, history included.

**Commit.** A snapshot of every tracked file at one moment, with a message, an author, and a unique ID called a **SHA**, such as `fe04405`. Commits link to their parent commits, forming a history.

**Branch.** A movable label pointing at a commit. When you commit on a branch, the label moves forward. `main` is the team's shared, stable branch.

**Tag.** A label that never moves. `Iowa-Regional` will always point at the code the team competed with.

**Remote.** Another copy of the repository, usually on GitHub, named `origin` by default.

## A season, told by commits

Reading the history of Rebuilt-2026 is like reading the season's lab notebook:

| Dates (2026) | What the commits show |
|---|---|
| Jan 11–13 | Initial commit, REVLib and other vendor libraries, update to 2026 |
| Jan 14–22 | First `TurretIO`, the Superstructure, vision rework, turret redesigns |
| Jan 23–24 | The intake subsystem, including "I FINISHED THE INTAKE SUBSYSTEM" |
| Jan 31 | "auto works", and the pose-estimation branch merged |
| Feb 11–14 | Hood and intake branches merged, homing added |
| Feb 23–27 | "Odometry works", the dashboard layout committed, vision tuned, PR #4 merged |
| Mar 10–22 | Tuning after Bluff Country: backlash, unjamming, shoot-on-the-move changes, autos |
| Mar 26–31 | Iowa Regional commits, including "Match 9", then the `Iowa-Regional` tag |

When something breaks next season, this history is how you find out when and why.

:::team Git is part of competing
The build process stamps each robot jar with the Git commit it came from, and AdvantageKit writes that into every log. But in our 2026 repository, `BuildConstants.java` was never regenerated after November 2025, so every log reports the wrong commit. That is finding **F12** in the [Code Audit](course:reference/code-audit). You will see how to fix it in [The 967 Git Workflow](course:04-git/team-git-workflow).
:::

## Set up Git on your computer

Tell Git who you are. This name and email go into every commit you make.

```bash
git config --global user.name "Your Name"
git config --global user.email "your-github-noreply-address@users.noreply.github.com"
git config --global init.defaultBranch main
git config --global pull.rebase false
```

:::tip Use GitHub's private email address
GitHub gives every account a no-reply email address, found under **Settings → Emails**. Using it keeps your personal email out of the public commit history.
:::

Check your settings with `git config --global --list`.

:::quiz
? Which is true about Git and GitHub?
- They are the same program
+ Git records history on your computer; GitHub hosts repositories online for sharing and review
- GitHub works offline and Git needs internet
- Git is only for websites
> You can commit with Git in a pit with no internet, and push to GitHub later.

? What does the tag `Iowa-Regional` give the team?
+ A permanent label pointing at the exact commit the team used for that event
- A copy of the robot jar
- A branch where new features go
- A list of match results
> Tags do not move, so the team can always get back to the event code.

?tf A branch label moves forward each time you commit on that branch.
= true
> Branches are movable labels. Tags stay put.

? The turret worked in Match 9 and now misses. How does Git help most?
- It automatically fixes the tuning values
+ It lets you see exactly what changed since the working commit, and return to that commit if needed
- It emails the drive team
- It stops anyone from changing code at events
> Commits are snapshots, so you can compare them or restore one.

?text Which Git command setting stores the name that appears on your commits? (Just the key, like `a.b`.)
= user.name
> `git config --global user.name "Your Name"` sets it once for your computer.
:::
