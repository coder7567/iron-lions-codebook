---
summary: Get the course's exercises project, run its JUnit tests from the terminal and VS Code, read a failing test, and make your first tests pass.
objectives:
  - Open the exercises project and run all or some of its tests
  - Read a JUnit failure message and use it to fix your code
  - Complete the first exercise, Hello, Iron Lions
---

## What the exercises project is

Every coding exercise in this course lives in one **GradleRIO 2026 project**, set up just like the robot code. Each exercise has three parts:

| Folder | What is inside | Who edits it |
|---|---|---|
| `src/main/java/frc/training/` | **Starter code** with `TODO` comments | You |
| `src/test/java/frc/training/` | **JUnit tests** that check your work | Nobody (read them) |
| `solutions/java/frc/training/` | **Reference solutions** | Nobody (look only after trying) |

Folders are grouped by unit, like `frc/training/u01` and `frc/training/u02`.

## Get it

- **From a mentor:** the team may share the `iron-lions-exercises` folder directly.
- **From this site:** open [Your progress](#/progress) and choose **Download exercises (.zip)**. Unzip it somewhere like `Documents`.

Then, in WPILib VS Code, choose **File → Open Folder** and open the `iron-lions-exercises` folder, the one that contains `build.gradle`.

:::tip Track your own work with Git
After [Unit 4](course:04-git/commits-and-history), run `git init` in the exercises folder and commit each time an exercise passes. You will get practice with Git and a history of your progress.
:::

## Run the tests

From the VS Code terminal:

```powershell
.\gradlew test
```

On macOS or Linux, use `./gradlew test`. The first run downloads JUnit and compiles everything. You will see **many failing tests and BUILD FAILED**. That is correct: nothing is solved yet.

To run just one unit or one exercise:

```powershell
.\gradlew test --tests "frc.training.u01.*"
.\gradlew test --tests frc.training.u01.HelloIronLionsTest
```

You can also click the **Testing** panel (the flask icon) in VS Code, expand the tree, and press the play button next to a single test class or a single test.

:::note Why "BUILD FAILED" when my code compiled?
Gradle treats any failing test as a failed build. Look at the test report lines. `FAILED` next to a test name is a test failing. `error:` with a file and line number is a compile error, which you must fix first, because no test can run until everything compiles.
:::

## Read a failure like a detective

Here is a failure from the first exercise before it is solved:

```text
HelloIronLionsTest > teamNumber() returns 967 FAILED
    org.opentest4j.AssertionFailedError: expected: <967> but was: <0>
        at frc.training.u01.HelloIronLionsTest.teamNumberIs967(HelloIronLionsTest.java:13)
```

Every part tells you something:

- **`teamNumber() returns 967`** is the test's display name, which describes what should happen.
- **`expected: <967> but was: <0>`**: the test wanted 967, and your method returned 0.
- **`HelloIronLionsTest.java:13`** is the exact line in the test that checked it.

Now open the test file. Tests are the specification. They tell you exactly what "correct" means:

```java
@Test
@DisplayName("teamNumber() returns 967")
void teamNumberIs967() {
  assertEquals(967, HelloIronLions.teamNumber());
}
```

`assertEquals(expected, actual)` passes when the two are equal. The test calls `HelloIronLions.teamNumber()` and expects `967`. You will learn to write tests like this yourself in [Unit 15](course:15-quality/unit-testing-robot-code).

## The workflow for every exercise

:::steps
1. **Read the task** in the lesson and the Javadoc comments in the starter file.
2. **Read the tests.** Figure out what inputs they use and what outputs they expect.
3. **Run the tests once** and watch them fail. That proves the tests are really checking something.
4. **Write the smallest code** that could make one test pass, then run the tests again.
5. **Repeat** until everything is green, then tick "My tests pass" on the lesson page.
6. **Compare with the solution.** Different code can be equally correct. Ask a mentor if you are unsure whether yours is.
:::

:::exercise id="u01-hello"
Make both methods in `HelloIronLions` pass their tests.

- `teamNumber()` must return our team number.
- `greeting(name)` must return exactly `Welcome to 967, ` followed by the name and an exclamation point. For `"Ada"`, that is `Welcome to 967, Ada!`

Watch out for the space after the comma and the `!` at the end. Tests compare text exactly.
---hint
In Java you can join text with `+`, like `"Hello, " + name`.
---hint
`return "Welcome to 967, " + name + "!";`
:::

:::warning Solutions are for learning, not for passing
The `solutions` folder exists so you can compare approaches after you finish, and so mentors can check that every test works with `./gradlew test -Psolutions`. Copying a solution without understanding it passes one exercise and fails you on the unit test.
:::

:::quiz
? You run `.\gradlew test` for the first time and see BUILD FAILED with many failing tests. What does that mean?
+ Everything is set up correctly; the exercises simply are not solved yet
- The exercises project is broken and needs to be downloaded again
- You must install a newer JDK
- JUnit is not installed
> Starters are meant to fail their tests. Solving each exercise turns its tests green.

? A test reports `expected: <Welcome to 967, Ada!> but was: <Welcome to 967,Ada!>`. What is wrong?
- The test is wrong
+ Your greeting is missing the space after the comma
- `Ada` should be lowercase
- The method must print the text instead of returning it
> Tests compare text exactly, including spaces and punctuation. Read expected and actual character by character.

?text What Gradle command runs only the tests in the `frc.training.u01` package? (Windows form)
= .\gradlew test --tests "frc.training.u01.*" | /gradlew\s+test\s+--tests\s+"?frc\.training\.u01\.\*"?/
> `--tests` filters by class or package name, and `*` matches every class in the package.

?tf You should read an exercise's tests before you start writing code.
= true
> Tests are the specification. They show the exact inputs and outputs that count as correct.
:::
