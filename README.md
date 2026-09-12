# Iron Lions Codebook

A ground-up programming course for **FRC Team 967, Linn-Mar Robotics**, built on the team's own
2026 REBUILT robot code. Every lesson, quiz, and exercise refers to the real repository at
[FRC-IronLions-967/Rebuilt-2026](https://github.com/FRC-IronLions/Rebuilt-2026), pinned to commit
`fe04405`.

It teaches a new programmer from "what is Java" to "I can find and fix a bug in our turret," and it
gives returning members a reference and a set of findings worth fixing.

## Open the site

The built site is plain static files. Two ways to read it:

```bash
python tools/serve.py 8967 site
```

Then open <http://localhost:8967>. The local server sends no-cache headers, so a rebuild shows up on
refresh.

You can also open `site/index.html` directly from the file system; the site is built to work from
`file://` as well.

## Run the exercises

The exercises are a normal GradleRIO project. From the `exercises/` folder:

```bash
./gradlew test
```

```bash
./gradlew test --tests "frc.training.u08.*"
```

Starters live in `src/main/java`, tests in `src/test/java`, and reference solutions in
`solutions/java`. Mentors can grade the solutions instead of the starters:

```bash
./gradlew test -Psolutions
```

Most exercises are plain Java and need nothing but a JDK. The few that use WPILib (their ids end in
`-wpi`) need the Gradle run above, because they load WPILib's native libraries.

## Rebuild the site

```bash
node tools/build.mjs
```

The build needs Node 18 or newer and no dependencies. It reads `course/`, pulls source excerpts from
`../Rebuilt-2026` at the pinned commit, and writes `site/`. Add `--strict` to turn missing pages into
errors.

## Layout

| Path | What it holds |
|---|---|
| `course/course.json` | The curriculum: units, lessons, order, minutes |
| `course/units/<unit>/<lesson>.md` | Lesson sources (Markdown plus directives) |
| `course/units/<unit>/unit-test.md` | The unit's exam |
| `course/reference/*.md` | Reference pages: robot reference, code audit, cheat sheets, glossary |
| `course/diagrams/*.svg` | Inline diagrams, themed with CSS variables |
| `course/exercises.json` | The exercise catalog: ids, levels, and file paths |
| `exercises/` | The GradleRIO practice project |
| `tools/build.mjs` | The static site builder |
| `tools/web/` | The site's CSS and JavaScript |
| `site/` | Build output; regenerate rather than editing |
| `PLAN.md` | Internal notes: source facts, audit findings, conventions |

## Writing a lesson

A lesson is Markdown with a front matter block and a few directives:

```markdown
---
summary: One sentence for lists and search.
objectives:
  - What the reader can do afterward
files:
  - src/main/java/frc/robot/subsystems/turret/Turret.java
---

## A heading

::source file="src/main/java/frc/robot/Robot.java" from="public void robotPeriodic()" lines=15

::diagram name="robot-loop" caption="Optional caption."

:::note
Callouts: note, tip, info, warning, danger, team, try, details, steps.
:::

:::exercise id="u08-intake"
The task, then hints separated by three dashes and the word hint.
:::

:::quiz
? A question
+ The right answer
- A wrong answer
> Why.
:::
```

`::source` pulls live code from the team repository, so an excerpt cannot go stale silently: if an
anchor stops matching, the build fails. Links use `course:<unit>/<lesson>`, `repo:<path>#L10-L20`,
and `ex:<exercise-id>`.

## How the content was verified

- **Source excerpts** are pulled from the team repository at build time; a moved anchor breaks the
  build rather than producing a wrong quote.
- **Pure-Java exercises** are checked offline: every starter must compile and fail, and every
  reference solution must pass. That check runs against the same JUnit 5 the Gradle project uses.
- **WPILib exercises** (ids ending in `-wpi`) are written against documented APIs and are meant to be
  run with `./gradlew test` on a machine with the WPILib toolchain.
- **Findings F1 through F24** in `PLAN.md` and the code audit page were each read out of the current
  source, with file and line references.

## A note on the team repository

Nothing in this project is committed to the robot code repository. The course lives here, beside it,
and reads it. Fixes suggested by the course are for the team to make deliberately, on their own
branches, with their own review.
