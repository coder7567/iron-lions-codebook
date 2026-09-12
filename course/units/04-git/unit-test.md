---
summary: Show that you can use Git and GitHub to work safely with the team, from commits and branches to pull requests, conflicts, and event-day recovery.
---

This test covers all of Unit 4. Several questions describe situations the team really faced in 2026.

:::exam Unit 4 test: Git and GitHub
? Which statement is correct?
+ Git works on your computer without internet; GitHub hosts repositories online for sharing and review
- GitHub is required to make any commit
- Git and GitHub are the same tool with two names
- Git only stores the newest version of each file
> Commits are local. Pushing shares them through GitHub.

?order Put these in the order a change normally travels.
1. Working tree
2. Staging area
3. Local commit history
4. GitHub
> `git add` stages, `git commit` records, and `git push` shares.

? `git status` shows a new file under "Untracked files." What happens if you commit without adding it?
- It is committed anyway
+ It is left out of the commit and exists only on your computer
- Git deletes it
- The commit fails
> The "Put the layout on git" commit fixed exactly this: a file that existed only on one laptop.

? Which command shows exactly what is about to be committed?
- `git diff`
+ `git diff --staged`
- `git log`
- `git status --all`
> Plain `git diff` shows unstaged changes. `--staged` shows what the next commit will contain.

? Which is the most useful commit message?
- "testing tuesday"
- "put values in idk what for"
+ "Tune turret kS to 0.275 so small aim corrections start moving"
- "stuff"
> It names the change, the value, and the reason.

? A bad commit is already on GitHub's `main`. What should you do?
+ `git revert <sha>` and push the new commit
- `git reset --hard` and `git push --force`
- Delete the commit on GitHub's website
- Nothing; Git cannot undo pushed commits
> Revert undoes a commit by adding a new one, without rewriting shared history.

? What happens when you commit on a branch?
- A copy of every file is placed in a new folder
+ The branch label moves forward to the new commit
- The commit goes to `main` as well
- The branch is merged automatically
> Branches are movable labels.

? Why does the command `git switch testing(no-superstructure)` fail in bash?
+ Parentheses are special characters in the shell, so the name must be quoted
- Branch names cannot contain hyphens
- The branch was deleted
- `switch` only works on `main`
> Names with only letters, numbers, hyphens, and slashes avoid quoting problems.

? A merge creates a merge commit instead of fast-forwarding. Why?
+ Both branches gained new commits after they split
- The branch was pushed to GitHub
- There was a conflict
- Merge commits happen on every merge
> If only one side moved, Git can fast-forward.

? Which command downloads new commits from GitHub without changing your files?
+ `git fetch`
- `git pull`
- `git push`
- `git merge`
> `pull` fetches and also merges. `fetch` alone only downloads.

? Your push is rejected because a teammate pushed first. What do you do?
- `git push --force`
+ `git pull`, resolve anything that needs it, and push again
- Make a new branch with the same name
- Delete your commits
> Force-pushing a shared branch can erase your teammate's work.

?? Which belong in a pull request description? (Select all that apply.)
+ What changed and why
+ How it was tested
+ Known risks
- Personal login credentials
> Reviewers need enough context to judge the change, and credentials never belong in a PR.

?code During a conflict, which branch's version appears first, right after the `<<<<<<< HEAD` line? Answer with the word Git uses for your current branch.
= HEAD
> `HEAD` is the branch you are on. The incoming branch's version follows the `=======` divider.

? Two students changed the same flywheel constant to different values. How should the conflict be resolved?
+ Choose the value supported by the newest reliable test data, after talking with both people
- Keep the larger value
- Accept Incoming Change every time
- Average them
> Git can show the disagreement, but only evidence settles a tuning conflict.

? A `.path` file from PathPlanner has a conflict in its control points. What is usually best?
- Hand-edit the JSON numbers until it compiles
+ Keep one side, then redo the other change in the PathPlanner app and commit again
- Delete the path
- Commit the conflict markers
> Tool-generated files are safer to regenerate than to merge by hand.

? In the proposed event workflow, what should happen immediately before every deploy?
+ Commit the change, with the match number in the message, and confirm `git status` is clean
- Delete old tags
- Pull request approval from the whole team
- Update every vendor library
> Committing first means every deployed version can be found and restored.

?tf Tagging a commit `known-good-q18` makes it quick to return to that exact code later with `git switch --detach known-good-q18`.
= true
> Tags never move, so they are reliable bookmarks for recovery.
:::
