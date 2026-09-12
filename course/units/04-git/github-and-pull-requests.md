---
summary: Share work through GitHub with fetch, pull, and push, then propose changes with pull requests that teammates can review before code reaches the robot.
objectives:
  - Use remotes with fetch, pull, and push, including pushing a new branch
  - Open a pull request with a description a reviewer can act on
  - Review, merge, and clean up a pull request the way the team does
---

## Remotes

A **remote** is another copy of the repository. When you clone, Git names GitHub's copy `origin`:

```bash
git remote -v
# origin  https://github.com/FRC-IronLions-967/Rebuilt-2026.git (fetch)
# origin  https://github.com/FRC-IronLions-967/Rebuilt-2026.git (push)
```

Three commands move commits between your computer and GitHub:

| Command | Direction | What it does |
|---|---|---|
| `git fetch` | GitHub → you | Downloads new commits and branches, but does not change your files |
| `git pull` | GitHub → you | Fetches, then merges the remote branch into your current branch |
| `git push` | you → GitHub | Uploads your commits on the current branch |

`git fetch` is always safe, and `git status` afterward tells you whether you are ahead of or behind GitHub.

### Pushing a new branch

A branch you create exists only on your computer until you push it:

```bash
git switch -c fix-passing-hood-offset
# ...edit, add, commit...
git push -u origin fix-passing-hood-offset
```

`-u` connects your branch to the one on GitHub, so later you can type just `git push` and `git pull`.

:::note Signing in to GitHub
GitHub no longer accepts account passwords for Git. On Windows, Git for Windows opens a browser window to sign you in the first time you push. The GitHub CLI (`gh auth login`) works on every platform. Never paste a password or access token into a chat, a document, or code.
:::

:::warning Pull before you push
If someone else pushed to the same branch first, GitHub rejects your push. Run `git pull`, resolve anything that needs it, and push again. Never "fix" a rejected push with `--force` on a shared branch.
:::

## Pull requests

A **pull request** (PR) is a GitHub page that says: "I would like to merge this branch into `main`. Please look first." It shows the changes, lets teammates comment on specific lines, and records the decision.

Rebuilt-2026's history includes one: `Merge pull request #4 from FRC-IronLions-967/Use-6328-field-constants`. The branch copied field constants from Team 6328's code, and GitHub recorded both the branch's commits and the merge.

### Opening a PR

:::steps
1. Push your branch to GitHub.
2. On the repository page, click **Compare & pull request**, or go to **Pull requests → New pull request** and choose your branch.
3. Set the base branch to `main` and your branch as the one to compare.
4. Write a title and description (see the template below), then click **Create pull request**.
5. Request a reviewer: a mentor or experienced student.
:::

### A description reviewers can act on

```text
## What
Adds hoodOffset to the three passing-map entries in redoPassingFunction.

## Why
After an alliance change, red passes used hood angles 0.291 lower than the
constructor's values. See Code Audit finding F4.

## How I tested
- Simulated both alliances and logged Turret/hoodSetAngle while passing
- Values now match between blue and red

## Risks
Passing shots on red may land farther than before; retune if needed.
```

A reviewer who reads that knows what to look at, why it matters, and how much to trust it.

## Reviewing and merging

- **Reviewers** read the **Files changed** tab, leave comments on specific lines, and choose *Comment*, *Approve*, or *Request changes*.
- **Authors** answer every comment. Push new commits to the same branch and the PR updates automatically.
- **Merging** happens on GitHub once the PR is approved. The **Merge pull request** button creates a merge commit like PR #4's. **Squash and merge** combines all the branch's commits into one tidy commit. Use whichever style the team agrees on.
- **Afterward,** click **Delete branch**, then update your computer:

```bash
git switch main
git pull
git branch -d fix-passing-hood-offset
```

[Code Review That Helps](course:15-quality/code-review) goes deeper into giving and receiving reviews.

## Issues: tracking work

GitHub **Issues** track bugs and ideas. The findings in the [Code Audit](course:reference/code-audit) would make good issues: one per finding, labeled by severity, each linked to the PR that fixes it. When a PR description says `Fixes #12`, GitHub closes issue 12 automatically on merge.

:::team A workflow worth adopting
Pull requests only protect `main` if people use them. A mentor with admin access can turn on **branch protection** for `main`, requiring a pull request and one approval before merging. The [967 Git Workflow](course:04-git/team-git-workflow) lesson proposes when to require it and when event-day speed matters more.
:::

:::quiz
? What is the difference between `git fetch` and `git pull`?
+ `fetch` downloads new commits without changing your files; `pull` fetches and then merges into your current branch
- `fetch` uploads and `pull` downloads
- They are identical
- `pull` only works on `main`
> Fetch is a safe "what's new?" Pull actually updates your branch.

? You created a branch, committed, and ran `git push`. Git says the branch has no upstream. What command fixes it?
+ `git push -u origin my-branch`
- `git pull --force`
- `git merge origin`
- `git init`
> `-u` pushes the branch and connects it to GitHub for later pushes and pulls.

?? Which belong in a good pull request description? (Select all that apply.)
+ What changed
+ Why it was needed
+ How it was tested
- Your GitHub password
+ Known risks
> Reviewers need the what, why, testing, and risks. Never put credentials anywhere.

? GitHub rejects your push because a teammate pushed to the same branch first. What do you do?
- `git push --force`
+ `git pull`, resolve any conflicts, then push again
- Delete the branch on GitHub
- Make a new repository
> Pulling brings in their commits so your push adds to history instead of erasing it.

?tf Writing `Fixes #12` in a pull request description closes issue 12 when the PR is merged into the default branch.
= true
> GitHub links the PR and closes the issue automatically on merge.
:::
