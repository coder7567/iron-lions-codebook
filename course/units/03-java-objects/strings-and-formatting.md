---
summary: Work with text the way robot code does, from building alert messages and comparing strings correctly to formatting numbers and keeping log keys consistent.
objectives:
  - Use common String methods such as charAt, substring, trim, startsWith, and split
  - Compare strings with equals instead of ==
  - Format numbers with String.format and DecimalFormat, and choose consistent log key names
files:
  - src/main/java/frc/robot/subsystems/drive/Module.java
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## Strings are objects that hold text

A `String` is a sequence of characters. Strings are **immutable**: methods that seem to change a string actually return a new one.

```java
String camera = "April_Tag_2";
String upper = camera.toUpperCase();   // "APRIL_TAG_2"; camera itself is unchanged
```

## Building text

The `+` operator joins strings, converting other values to text along the way. `Module` builds a different alert message for each of its four copies:

::source file="src/main/java/frc/robot/subsystems/drive/Module.java" from="driveDisconnectedAlert =" lines=7

`Integer.toString(index)` turns the int into text. `"module " + index` would do the same thing automatically, because joining a String with anything converts that thing to text.

:::warning Order matters with + and numbers
Java evaluates `+` left to right: `"Team " + 9 + 67` is `"Team 967"`, but `9 + 67 + " points"` is `"76 points"`, because the numbers add first. Use parentheses when mixing math and text.
:::

## String methods you will use

| Method | Example | Result |
|---|---|---|
| `length()` | `"April_Tag_2".length()` | `11` |
| `charAt(i)` | `"R".charAt(0)` | `'R'` |
| `substring(start)` | `"April_Tag_2".substring(10)` | `"2"` |
| `substring(start, end)` | `"fe04405398".substring(0, 7)` | `"fe04405"` |
| `indexOf(text)` | `"Drive/Module2".indexOf("/")` | `5` (or `-1` if absent) |
| `startsWith(text)` | `"Tuning/Drive/Kp".startsWith("Tuning/")` | `true` |
| `contains(text)` | `"4646 Race right".contains("Race")` | `true` |
| `trim()` | `"  12 ".trim()` | `"12"` |
| `isEmpty()` | `"".isEmpty()` | `true` |
| `split(regex)` | `"Drive/Module2".split("/")` | `["Drive", "Module2"]` |
| `replace(a, b)` | `"SlowNZ 2".replace(" ", "_")` | `"SlowNZ_2"` |
| `toLowerCase()` | `"FE0440".toLowerCase()` | `"fe0440"` |

Indexes start at 0, and `substring(start, end)` includes `start` but stops **before** `end`.

## Compare text with equals

```java
String mode = readModeFromDashboard();   // "SHOOTING"
if (mode == "SHOOTING") { ... }        // WRONG: compares references
if (mode.equals("SHOOTING")) { ... }   // right: compares the characters
if ("SHOOTING".equals(mode)) { ... }   // also right, and safe even if mode is null
```

`==` asks whether two variables point to the **same object**. Two strings with identical text can be different objects, especially text that arrived from a file, the network, or a dashboard. Sometimes `==` happens to work, which makes the bug worse, because it passes on your laptop and fails on the robot. **Always use `equals` for Strings**, or `equalsIgnoreCase` when capitalization does not matter.

## Characters

A `char` is a single character in single quotes. The HUB logic switches on the first character of the game data:

```java
switch (gameData.charAt(0)) {
    case 'B' -> autoWinColor = new Color(0, 0, 255);
    case 'R' -> autoWinColor = new Color(255, 0, 0);
    default -> autoWinColor = new Color();
}
```

The `Character` class has helpers like `Character.isDigit('7')`, `Character.isLetter('R')`, and `Character.toUpperCase('r')`.

## Formatting numbers

Printing a raw `double` gives ugly output like `4.625999999999999`. Two ways to control it:

**`String.format`** uses a pattern with placeholders:

```java
String.format("x=%.2f m", 4.626)          // "x=4.63 m"
String.format("%d loops", 50)             // "50 loops"
String.format("%s is %.1f A", "Turret", 21.47)  // "Turret is 21.5 A"
String.format("%6.1f|", 3.14159)          // "   3.1|", padded to width 6
```

| Placeholder | Formats |
|---|---|
| `%d` | whole numbers |
| `%.2f` | decimals, rounded to 2 places |
| `%s` | anything, using its `toString()` |
| `%%` | a literal percent sign |

**`DecimalFormat`** is what our characterization command uses to print its results:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="#0.00000" lines=4

`"#0.00000"` means at least one digit before the decimal point and exactly five after it.

:::tip Formatting and locales
`String.format` follows the computer's regional settings, and some countries write decimals with a comma: `4,63`. For text a program will read back, or a test compares exactly, pass a locale: `String.format(Locale.US, "%.2f", x)`.
:::

## Strings as log keys

AdvantageKit and NetworkTables identify every value by a **string key**. A key is just text, so every spelling creates a separate entry:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" from="Wanted State" lines=3

`"Wanted State"` has a space, `"CurrentState"` does not, and `"FieldBasedTurret"` does not say which subsystem it belongs to. In AdvantageScope these appear as three unrelated entries. Keys organized like folders, such as `Superstructure/WantedState` and `Superstructure/CurrentState`, group together and are easy to find. A typo in a key does not cause an error. It silently creates a new, empty entry.

## StringBuilder for building text in loops

Each `+` creates a new String. That is fine for one message, but wasteful in a loop. `StringBuilder` builds text in place:

```java
StringBuilder report = new StringBuilder();
for (int i = 0; i < modules.length; i++) {
    report.append("Module ").append(i).append(": ").append(currents[i]).append(" A\n");
}
String text = report.toString();
```

:::exercise id="u03-telemetry"
Write text helpers that a dashboard, alert, or log could use.

- `formatPose` and `formatCurrent` use `String.format(Locale.US, ...)`.
- `shortSha` and `buildSummary` produce the short commit text GitHub shows, like `fe04405 on main`.
- `disconnectedAlert` recreates the exact text from `Module`.
- `cameraNumber` pulls the number out of names like `April_Tag_2` and rejects anything malformed.
---hint
You can type the degree sign `°` directly inside the format string, because the exercises project compiles Java files as UTF-8. If a build ever shows it garbled, the Unicode escape `\u00B0` means the same character.
---hint
For `cameraNumber`, check `startsWith("April_Tag_")` first, then look at `substring("April_Tag_".length())`. Loop over its characters with `Character.isDigit` before calling `Integer.parseInt`.
:::

:::quiz
?code What does this print?
```java
String sha = "fe04405398eec6f8";
System.out.println(sha.substring(0, 7) + " " + sha.length());
```
= fe04405 16
> `substring(0, 7)` takes characters 0 through 6. The whole string has 16 characters.

?code What does this print?
```java
System.out.println(9 + 67 + " points, team " + 9 + 67);
```
= 76 points, team 967
> The first `+` adds numbers. After a String joins in, every later `+` joins text.

? Why should robot code compare Strings with `equals` instead of `==`?
+ `==` checks whether both variables point to the same object, and equal text can live in different objects
- `==` does not compile for Strings
- `equals` is faster
- `==` ignores capital letters
> Identical text from files, dashboards, or the network is often in different objects, so `==` can fail unexpectedly.

?text What does `String.format(Locale.US, "%.1f A", 21.47)` return?
= 21.5 A
> `%.1f` rounds to one decimal place, and ` A` is copied as written.

?tf Logging one value under `"Wanted State"` and another under `"WantedState"` puts both on the same AdvantageScope entry.
= false
> Keys are compared exactly, so the space makes them two different entries.
:::
