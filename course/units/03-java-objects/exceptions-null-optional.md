---
summary: Understand exceptions and stack traces, handle errors with try/catch/finally, avoid NullPointerExceptions, and use Optional for values that might be missing, all without crashing the robot mid-match.
objectives:
  - Read a stack trace and name common unchecked exceptions
  - Use try, catch, and finally, and throw exceptions with helpful messages
  - Handle possibly-missing values with null checks and Optional
files:
  - src/main/java/frc/robot/util/Elastic.java
  - src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/util/AllianceFlipUtil.java
---

## When things go wrong at runtime

An **exception** is Java's way of saying "something went wrong and I cannot continue this line of code." When one is thrown, Java stops the current method and unwinds until something **catches** it. If nothing does, the program ends.

On a robot, that is serious. An uncaught exception stops the robot program; the Driver Station console shows the error along with WPILib's famous line, *"Robots should not quit, but yours did!"*, and the code restarts. The robot is dead for several seconds, and in a match that can be decisive.

### Reading a stack trace

```text
Exception in thread "main" java.lang.NullPointerException:
    Cannot read the array length because "this.inputs[cameraIndex].targetInfo" is null
  at frc.robot.subsystems.vision.AprilTagVision.getTargetInfo(AprilTagVision.java:64)
  at frc.robot.RobotContainer.lambda$configureButtonBindings$3(RobotContainer.java:205)
  ...
```

Read from the top: **what** happened (a `NullPointerException`, with a helpful description) and **where** (file and line). The lines below show who called that method, back to the start. Find the first line in *our* code (`frc.robot...`) and start there.

## Common exceptions

| Exception | Typical cause |
|---|---|
| `NullPointerException` | Using a variable that holds `null` as an object |
| `ArrayIndexOutOfBoundsException` | `modules[4]` on a four-element array |
| `IllegalArgumentException` | A method got an argument it cannot accept |
| `IllegalStateException` | An object was used at the wrong time |
| `NumberFormatException` | `Integer.parseInt("twelve")` |
| `ArithmeticException` | Integer division by zero (double division gives Infinity or NaN instead) |

These are all **unchecked** exceptions: the compiler does not force you to handle them. **Checked** exceptions, like file and network errors, must be caught or declared. `Elastic.sendNotification` has to catch one when turning a notification into JSON:

::source file="src/main/java/frc/robot/util/Elastic.java" from="public static void sendNotification(Notification notification)" lines=7

If converting to JSON fails, the method prints the error and carries on. Losing one dashboard notification is much better than crashing the robot.

## try, catch, and finally

```java
try {
    int id = Integer.parseInt(text);   // might throw
    useId(id);
} catch (NumberFormatException e) {
    System.out.println("Bad CAN ID: " + text);  // runs only if that exception was thrown
} finally {
    cleanUp();                          // always runs, error or not
}
```

`finally` is how code guarantees cleanup. The odometry thread locks shared data, and must unlock it no matter what happens in between:

::source file="src/main/java/frc/robot/subsystems/drive/SparkOdometryThread.java" from="private void run()" to="Drive.odometryLock.unlock();" highlight="3-4,30-31"

If reading a motor threw an exception without that `finally`, the lock would stay locked forever and the main robot loop would freeze waiting for it.

## Throwing exceptions on purpose

Throw an exception when code receives something it truly cannot handle, and **say what was wrong**:

```java
if (id < 1 || id > 62) {
    throw new IllegalArgumentException("CAN ID out of range: " + id);
}
```

A clear message turns a mystery into a one-minute fix. "CAN ID out of range: 63" is far more useful than a crash three classes later.

:::tip Fail loudly at startup, degrade gently during a match
Checking configuration in constructors, and throwing if it is wrong, is good: the robot fails in the pit where someone can fix it. During a match, prefer to keep going. If a camera disconnects, drive without vision and raise an alert rather than throwing.
:::

## null: the billion-dollar mistake

`null` means "no object." Any variable of a class type can hold it, and calling a method on it throws a `NullPointerException`. Null sneaks in from:

- fields that were never assigned,
- `map.get(key)` when the key is missing,
- methods that return `null` to mean "not found."

Vision's `getTargetInfo` does that last one:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="public TargetInfo getTargetInfo(int cameraIndex, int aprilTagID)" lines=8

Every caller must remember to check for `null`. There is a second trap, too: `targetInfo` starts as `null` in the inputs class and is only filled in after a camera connects, so calling this method before that would crash inside the loop. This is part of finding **F14** in the [Code Audit](course:reference/code-audit).

## Optional: missing values you cannot ignore

`Optional<T>` is a box that holds **either** a value **or** nothing. Its type forces callers to decide what to do when it is empty. WPILib uses it for the alliance, which is unknown until the Driver Station reports it:

::source file="src/main/java/frc/robot/util/AllianceFlipUtil.java" from="public static boolean shouldFlip()" lines=4

| Method | Use |
|---|---|
| `isPresent()` / `isEmpty()` | Check whether a value exists |
| `get()` | Take the value, but it throws if empty, so check first |
| `orElse(fallback)` | The value, or a default. `Turret` uses `getAlliance().orElse(Alliance.Blue)` |
| `ifPresent(action)` | Run code only when a value exists |
| `map(function)` | Transform the value if present |

PhotonLib returns an `Optional` for tag positions, since a tag ID might not be on this field:

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagIOPhotonVision.java" from="var tagPose = VisionConstants.kTagLayout.getTagPose(target.fiducialId);" lines=3

:::warning orElse hides decisions
`getAlliance().orElse(Alliance.Blue)` quietly assumes blue whenever the alliance is unknown, which includes the moment the robot boots. Our `Turret` computes its passing targets in its constructor, before the alliance is known, which is why `Superstructure` calls `turret.redoPassingFunction()` once the alliance changes. When you write `orElse`, ask what the default costs if it is wrong.
:::

## Choosing a strategy

| Situation | Use |
|---|---|
| A value may legitimately be missing (no alliance yet, no target seen) | `Optional` |
| A caller passed something invalid, and that is a bug | Throw `IllegalArgumentException` |
| An outside system failed during a match (camera, JSON, CAN) | Catch it, keep running, log it, and raise an alert |
| An internal "not found" in a tiny private method | A sentinel such as `-1`, documented clearly |

:::exercise id="u03-gamedata"
Handle messy input the way robot code must.

- `inactiveFirst` reads game data into an `Optional`, returning empty for null, blank, or unknown text.
- `inactiveFirstOr` provides a fallback with `orElse`.
- `parseCanId` throws `IllegalArgumentException` with exact, helpful messages.
- `tryParseCanId` converts that exception into an empty `Optional`.
- `countValidCanIds` must skip bad entries, including `null`, without crashing.
---hint
Check `message == null` before calling any method on it, then `trim()` and check `isEmpty()` before `charAt(0)`.
---hint
Wrap only `Integer.parseInt(...)` in `try`, catch `NumberFormatException`, and throw a new `IllegalArgumentException("Not a number: " + trimmed, e)`. Passing `e` keeps the original cause for debugging.
---hint
`tryParseCanId` can call `parseCanId` inside `try`, return `Optional.of(id)` on success, and return `Optional.empty()` in the `catch`.
:::

:::quiz
? What happens on the robot when code throws an exception that nothing catches?
- The Driver Station ignores it
+ The robot program stops and restarts, leaving the robot disabled for several seconds
- Only the subsystem that threw it stops
- The roboRIO reboots its operating system
> An uncaught exception ends the robot program. WPILib reports it, and the program restarts.

? Why does `SparkOdometryThread.run()` unlock inside `finally`?
+ So the lock is released even if something inside `try` throws, preventing the main loop from freezing
- `finally` runs faster than normal code
- Locks can only be released in `finally`
- It makes the thread start earlier
> `finally` always runs, which guarantees the cleanup happens.

?code What does this print?
```java
Optional<String> alliance = Optional.empty();
System.out.println(alliance.orElse("Blue") + " " + alliance.isPresent());
```
= Blue false
> The Optional is empty, so `orElse` returns the fallback and `isPresent()` is false.

? Which is the best response when a teammate's code calls `parseCanId("63")`?
- Return 0 silently
+ Throw `IllegalArgumentException("CAN ID out of range: 63")`
- Crash with `NullPointerException`
- Round it down to 62
> Invalid input from code is a bug. A specific exception message tells the programmer exactly what to fix.

?tf `Integer.parseInt(" 12 ")` returns 12.
= false
> `parseInt` does not ignore spaces, so it throws `NumberFormatException`. Call `trim()` first.
:::
