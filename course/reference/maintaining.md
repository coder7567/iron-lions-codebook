---
summary: How to keep this course alive: rebuilding it, adding lessons and exercises, updating it when the robot code changes, and handing it to the next maintainer.
---

This course is a small static site generated from Markdown, plus a Gradle project of exercises. Both
live next to the robot code and read from it. Nothing here is committed to the robot repository.

## Rebuilding

```bash
node tools/build.mjs
```

Node 18 or newer, no dependencies. It reads `course/`, pulls source excerpts from `../Rebuilt-2026`,
and writes `site/`. Add `--strict` to fail on missing pages instead of warning.

```bash
python tools/serve.py 8967 site
```

A no-cache server for previewing at <http://localhost:8967>.

## Adding a lesson

:::steps
1. Add the lesson to its unit's `lessons` array in `course/course.json`, with a slug, title, and
   estimated minutes.
2. Create `course/units/<unit>/<slug>.md` with front matter: `summary`, `objectives`, and the `files`
   it refers to.
3. Write it. Use `::source` for code rather than pasting, so the excerpt cannot go stale silently.
4. End with a `:::quiz`, and an `:::exercise` if the unit has one for it.
5. Rebuild and read it in a browser.
:::

The order of `lessons` in `course.json` is the order in the sidebar and the previous and next links.

## Adding an exercise

:::steps
1. Write the starter under `exercises/src/main/java/frc/training/<unit>/`, with TODO bodies that throw
   `UnsupportedOperationException`.
2. Write the tests under `exercises/src/test/java/frc/training/<unit>/`.
3. Write the reference solution under `exercises/solutions/java/frc/training/<unit>/`, at the same path
   as the starter.
4. Add an entry to `course/exercises.json`: `id`, `unit`, `title`, `level` 1 to 3, `wpilib`, and the
   three file lists.
5. Verify: every starter must fail its tests, and every solution must pass.
6. Reference it from a lesson with `:::exercise id="..."`.
:::

Rules that keep the exercises usable: pure Java unless the id ends in `-wpi`, JUnit 5 only, no mocking
frameworks, and tests that name the behavior in a `@DisplayName`.

```bash
cd exercises && ./gradlew test            # the starters, which should fail
cd exercises && ./gradlew test -Psolutions # the solutions, which should pass
```

## Directives

| Directive | Purpose |
|---|---|
| `::source file="..." from="..." lines=N` | A live excerpt. Also takes `to`, `occurrence`, `highlight`, and `title` |
| `::diagram name="..." caption="..."` | Inlines `course/diagrams/<name>.svg` |
| `:::note`, `tip`, `info`, `warning`, `danger`, `team`, `try` | Callouts; `team` renders as "In our code" |
| `:::details Summary` | A collapsed block |
| `:::steps` | A numbered procedure |
| `:::exercise id="..."` | An exercise card, with `---hint` blocks after the task |
| `:::quiz` and `:::exam` | Questions; `exam` is the unit test format |

Links use `course:<unit>/<lesson>`, `course:reference/<page>`, `repo:<path>#L10-L20`, and
`ex:<exercise-id>`.

Quiz syntax: `?` single choice, `??` multiple, `?tf` true or false, `?text` short answer, `?num` a
number with a tolerance, `?order` an ordering, and `?code` a predicted output. Options are `+` for
correct and `-` for incorrect, and `>` starts the explanation.

## When the robot code changes

This is the part that matters most, because a course that quotes stale code is worse than no course.

| Change | What to do |
|---|---|
| A method or block moved | The build fails on the `::source` anchor. Update the anchor |
| A constant changed | Search the course for the number and update the prose and quizzes |
| A finding was fixed | Update `course/reference/code-audit.md`, and the lessons that cite it, and say it was fixed rather than deleting the entry |
| A subsystem was added | Decide which unit it belongs to before writing anything |
| The season changed | Units 1 through 11 and 15 mostly survive; Units 12 through 14 and 16 are game-specific |

Update the pinned commit in `course/course.json` (`repo.sha`) when you re-point the course at newer
robot code, and rebuild. Every "view on GitHub" link uses that SHA, so the links keep matching the
excerpts.

## Conventions worth keeping

- **Every claim about the robot comes from the code**, with a file to check it against.
- **Findings keep their numbers.** Add new ones at the end; never renumber, because lessons cite them.
- **Quizzes test understanding**, not recall of a number that might change.
- **Diagrams use the theme's CSS variables** (`d-ink`, `d-accent`, and the rest) so they work in both
  light and dark.
- **Prose stays plain.** The audience includes someone reading their first Java, at 9 p.m., before an
  event.

## Publishing

The `site/` folder is self-contained and can be opened from disk, served locally, or published as a
web page. Rebuild before publishing, and check three things in a browser: the home page, one lesson
with an exercise card, and one unit test.

## Handing it over

The next maintainer needs four things: this page, `PLAN.md` for the internal conventions and the source
facts, the knowledge that the build fails loudly when the robot code moves, and permission to delete
anything that has stopped being true.

A course is a living document or it is a historical one. This one is worth keeping alive for as long as
the team writes robot code, because the parts that are not about REBUILT are about how this team works.
