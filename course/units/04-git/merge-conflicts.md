---
summary: Resolve merge conflicts calmly, including reading conflict markers, choosing the right result, verifying it builds, and preventing the conflicts that robot teams hit most.
objectives:
  - Explain when Git reports a merge conflict and read conflict markers
  - Resolve a conflict in a text editor or VS Code's merge editor, or abort safely
  - Handle robot-specific conflicts in tuning constants, PathPlanner files, and generated files
---

## What a conflict is

Git merges most changes automatically, even when two people edit the same file, as long as they changed **different lines**. A **conflict** happens only when both sides changed the **same lines** differently. Git will not guess which version is right. It stops and asks you.

A conflict is not an error or a sign that something broke. It is Git being careful with your robot's code.

## Anatomy of a conflict

Suppose one student made the operator's turret trim finer on a branch, while another made it coarser on `main`. Merging produces:

```text
$ git merge operator-trim
Auto-merging TurretConstants.java
CONFLICT (content): Merge conflict in TurretConstants.java
Automatic merge failed; fix conflicts and then commit the result.
```

Open the file and Git has marked the disputed section:

```java
public class TurretConstants {
<<<<<<< HEAD
    public static final double turretOffsetChange = 0.10; // radians per operator bumper press
=======
    public static final double turretOffsetChange = 0.02; // radians per operator bumper press
>>>>>>> operator-trim
    public static final double flywheelTolerance = 1000;  // RPM below setpoint that still counts as ready
}
```

| Marker | Meaning |
|---|---|
| `<<<<<<< HEAD` | Start of **your** side, the branch you are on (`main`) |
| `=======` | Divider |
| `>>>>>>> operator-trim` | End of **their** side, the branch being merged |

Lines outside the markers merged cleanly. `flywheelTolerance` is untouched.

## Resolving it

:::steps
1. **Check the state:** `git status` lists files under "both modified."
2. **Decide what the code should be.** Keep your side, keep theirs, or write something new that combines both ideas. This is a team decision, not a coin flip. Ask the people who made each change.
3. **Edit the file** to the final result and **delete all three marker lines.**
4. **Build and test.** A resolution that does not compile is not a resolution. Run `.\gradlew build`.
5. **Mark it resolved:** `git add TurretConstants.java`.
6. **Finish the merge:** `git commit`. Git fills in a message such as `Merge branch 'operator-trim'`. Add a line saying what you decided and why.
:::

If you get lost, you can always back out and start over:

```bash
git merge --abort    # returns everything to how it was before the merge
```

### Using VS Code

VS Code highlights conflicts with clickable options: **Accept Current Change**, **Accept Incoming Change**, **Accept Both Changes**, and **Compare Changes**. For bigger conflicts, click **Resolve in Merge Editor** to see *Incoming*, *Current*, and *Result* side by side. The buttons are shortcuts, not decisions. You still need to know which value is right, and you still need to build.

## Robot conflicts need robot answers

**Tuning constants** are the most common conflict. Two students tune the same flywheel kV on different days. The right answer comes from **data**: which value came from the most recent, most reliable testing? Check the logs and the commit messages, and talk to both people. This is exactly why commit messages like "Retune flywheel kV to 1.93e-3 after Iowa shot data" matter.

**PathPlanner files** (`.path`, `.auto`, `settings.json`) are JSON written by the PathPlanner app. Hand-merging control point coordinates is error-prone. Usually it is better to keep one side, then redo the other person's change in the PathPlanner app and commit again.

**Generated files** should not be merged by hand at all. Files like `BuildConstants.java` are rewritten by the build. Pick either side, rebuild, and let the build regenerate the file.

**Dashboard layouts** such as `elastic-layout.json` are also tool-generated. Keep one side and redo the other's layout change in Elastic.

## Preventing conflicts

- **Pull often.** The longer branches drift apart, the bigger the conflicts get.
- **Keep commits small and focused.** A commit that changes one mechanism rarely collides with another.
- **Say what you are touching.** A quick "I'm editing `TurretConstants` today" in the team chat prevents the classic tuning collision.
- **Do not reformat whole files** in the same commit as real changes. Reformatting touches every line and conflicts with everyone.
- **Split big files.** Separate files per mechanism, like our `IntakeConstants` and `TurretConstants`, mean fewer people edit the same file.

## Practice

The exercises project includes a script that builds a small repository with this exact conflict.

- [ ] From the exercises folder, run `bash git-practice/make-conflict.sh` in Git Bash, macOS, or Linux, or `powershell -ExecutionPolicy Bypass -File .\git-practice\make-conflict.ps1` in PowerShell.
- [ ] `cd git-conflict-practice` and run `git merge operator-trim`.
- [ ] Run `git status` and open `TurretConstants.java` to read the markers.
- [ ] Resolve it by choosing a value and writing the reason in a comment, and remove every marker.
- [ ] `git add TurretConstants.java`, then `git commit`.
- [ ] Run `git log --oneline --graph` and find your merge commit.
- [ ] Bonus: delete the folder, recreate it, and this time practice `git merge --abort`.

:::quiz
? When does Git report a merge conflict?
- Whenever two branches change the same file
+ When both branches changed the same lines differently
- Whenever a branch is more than a week old
- When a file is added on one branch
> Git merges different-line edits in the same file automatically. Only same-line disagreements need a person.

? In a conflict, what does the section between `<<<<<<< HEAD` and `=======` contain?
+ The version from the branch you are currently on
- The version from the branch being merged in
- The original version from before either change
- Git's recommended resolution
> HEAD is your current branch. The incoming branch's version comes after the divider.

?order Put the steps for finishing a merge conflict in order.
1. Edit the file to the correct result and remove the markers
2. Build and test the project
3. `git add` the resolved file
4. `git commit` to complete the merge
> Resolve, verify, stage, then commit.

? Two students changed `flywheelkV` to different values and the merge conflicts. What is the best way to choose?
- Keep whichever change is shorter
+ Use the value backed by the newest reliable test data, confirmed with the people who tuned it
- Average the two values
- Always accept the incoming change
> Tuning conflicts are decided by evidence from testing, not by Git or by guessing.

?tf `git merge --abort` returns your files to how they were before you started the merge.
= true
> It is the safe exit whenever a merge gets confusing.
:::
