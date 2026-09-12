---
summary: Use branches to work on a feature without breaking main, merge them back, understand the merge commits in our history, and name branches so they are easy to type.
objectives:
  - Create, switch, and list branches, and keep a clean working tree while switching
  - Merge a branch and tell a fast-forward merge from a merge commit
  - Explain the merge commits and branch names in Rebuilt-2026
---

## Why branches

`main` is the branch everyone trusts: it should always build, and it should be close to what runs on the robot. New work, like rewriting the turret, is messy for a while. A **branch** gives that work its own line of commits so `main` stays stable.

In 2026 the team used branches for exactly this: `Turret-Dev`, `Intake-Dev`, `pose-estimation-dev-bot`, `Only-hood/shooter-testing`, `testing(no-superstructure)`, and `Use-6328-field-constants`.

## A branch is just a label

A branch is a lightweight label pointing at a commit. When you commit, the current branch's label moves to the new commit. Nothing is copied.

```text
                 C---D        Turret-Dev
                /
  A---B---E---F               main
```

`Turret-Dev` started from commit B. Two commits later (C, D) it has turret work, while `main` got E and F from other students.

## Working with branches

```bash
git branch                     # list local branches; * marks the current one
git switch -c turret-homing    # create a new branch here and switch to it
git switch main                # go back to main
git branch -a                  # include branches on GitHub (remotes/origin/...)
git switch Intake-Dev          # switch to a branch that exists on GitHub
```

(Older tutorials use `git checkout -b name` and `git checkout name`, which do the same thing.)

:::warning Commit or stash before switching
Switching branches changes the files in your folder. If you have uncommitted edits that conflict, Git refuses to switch. Either commit them, or set them aside temporarily:
```bash
git stash          # put uncommitted changes on a shelf
git switch main
git switch -       # back to the previous branch
git stash pop      # take the changes back off the shelf
```
:::

## Merging

When the branch's work is ready, **merge** it into `main`:

```bash
git switch main
git pull                 # make sure main is up to date first
git merge turret-homing
```

There are two kinds of merge result.

**Fast-forward.** If `main` has not moved since the branch started, Git simply slides the `main` label forward. No new commit is created.

**Merge commit.** If both branches have new commits, Git creates a **merge commit** with two parents that combines both lines of work:

```text
                 C---D
                /     \
  A---B---E---F---------M     main (M is the merge commit)
```

Our history is full of merge commits:

| Commit message | What happened |
|---|---|
| `Merge branch 'Intake-Dev'` | The intake branch was merged into `main` on February 14 |
| `Merge branch 'pose-estimation-dev-bot'` | Pose estimation work came back to `main` in January |
| `Merge pull request #4 from FRC-IronLions-967/Use-6328-field-constants` | A pull request was merged on GitHub (next lesson) |
| `Merge branch 'main' of https://github.com/FRC-IronLions-967/Rebuilt-2026` | Someone ran `git pull` while their `main` and GitHub's `main` had both moved |

That last kind appears when two students commit to `main` on different laptops. The second one to pull gets a merge commit. It is harmless, but frequent ones are a sign that people should work on branches.

## Keeping a branch current

A long-lived branch drifts away from `main`, and the eventual merge gets painful. Bring `main`'s changes into your branch regularly:

```bash
git switch turret-homing
git merge main
```

Small, frequent merges mean small, easy conflicts. The [next lesson but one](course:04-git/merge-conflicts) covers conflicts in detail.

## Naming branches

Good branch names are short, lowercase, and describe the work: `turret-homing`, `fix-passing-hood-offset`, `vision-per-camera-stddevs`.

:::tip Avoid characters the terminal treats specially
`testing(no-superstructure)` is a descriptive name, but parentheses mean something to the shell, so `git switch testing(no-superstructure)` fails in bash unless you quote it. Stick to letters, numbers, hyphens, and slashes.
:::

## Cleaning up

After a branch is merged, delete it so the branch list stays useful:

```bash
git branch -d turret-homing                 # delete locally (refuses if not merged)
git push origin --delete turret-homing      # delete on GitHub
```

Rebuilt-2026 still has branches from February on GitHub. None of them are harmful, but a year from now nobody will remember which ones hold unmerged work. Delete branches once they are merged, or write in the pull request why a branch should stay.

:::quiz
? What is a branch in Git?
- A full copy of the project's files
+ A movable label that points to a commit and moves forward when you commit
- A folder on GitHub
- A backup of `main`
> Branches are lightweight labels, which is why creating one is instant.

? You try `git switch main` and Git refuses because of local changes. What are two safe options?
+ Commit the changes, or `git stash` them and `git stash pop` later
- Delete the `.git` folder
- Use `git push --force`
- Close VS Code and try again
> Git protects uncommitted work. Save it in a commit or on the stash first.

? When does merging create a merge commit instead of a fast-forward?
+ When both the branch and the target branch have new commits since they split
- Always
- Only when there are conflicts
- Only on GitHub
> If the target has not moved, Git can just slide its label forward.

? What most likely produced `Merge branch 'main' of https://github.com/FRC-IronLions-967/Rebuilt-2026`?
- Someone opened a pull request
+ Someone ran `git pull` while both their local `main` and GitHub's `main` had new commits
- GitHub merged branches automatically overnight
- A tag was created
> Pulling divergent histories creates a merge commit with that default message.

?tf `git branch -d feature` deletes a branch even if its commits were never merged.
= false
> Lowercase `-d` refuses to delete unmerged work. Uppercase `-D` forces it, and should be used carefully.
:::
