---
summary: Show that you can read and write basic Java: types, math, decisions, loops, and methods.
---

This test covers every Unit 2 lesson. Several questions ask you to predict output, so trace the code by hand instead of guessing. Explanations appear after you submit.

:::exam Unit 2 test: Java basics
? Which type should hold the turret's current angle in radians?
- `int`
+ `double`
- `boolean`
- `char`
> Angles are measured values with decimals.

?code What does this print?
```java
int loops = 50;
int seconds = 3;
System.out.println(loops * seconds / 4);
```
= 37
> 50 × 3 = 150, and 150 / 4 is 37 in integer division.

?code What does this print?
```java
System.out.println(1.0 / 4 + 1 / 4);
```
= 0.25
> `1.0 / 4` is 0.25, but `1 / 4` is integer division, which is 0. The sum is 0.25.

?code What does this print?
```java
System.out.println(370 % 360);
```
= 10
> 370 divided by 360 leaves a remainder of 10.

? Which call returns the angle, in radians, of a joystick pushed to (x, y)?
- `Math.atan2(x, y)`
+ `Math.atan2(y, x)`
- `Math.hypot(y, x)`
- `Math.signum(y / x)`
> `atan2` takes y first. `hypot` gives the length of the push, not its direction.

? What is the safe way to check whether two doubles are "equal"?
- `a == b`
- `a.equals(b)`
+ `Math.abs(a - b) < tolerance`
- `(int) a == (int) b`
> Floating-point math is approximate, so compare within a small tolerance.

?tf `gameData.isEmpty() || gameData == null` is just as safe as `gameData == null || gameData.isEmpty()`.
= false
> If `gameData` is null, the first version calls `isEmpty()` on null and throws a `NullPointerException` before `||` can help. Put the null check first.

?code What does this print?
```java
double matchTime = 100;
String period;
if (matchTime > 105) {
    period = "SHIFT 1";
} else if (matchTime > 80) {
    period = "SHIFT 2";
} else {
    period = "OTHER";
}
System.out.println(period);
```
= SHIFT 2
> 100 is not greater than 105, but it is greater than 80.

?code What does this print?
```java
boolean jammed = true;
System.out.println(jammed ? "REVERSING" : "INTAKING");
```
= REVERSING
> The ternary picks the first value when the condition is true.

?code What does this print?
```java
int dirty = 0;
switch (dirty) {
  case 0:
    System.out.print("clean ");
  case 1:
    System.out.print("dirty ");
    break;
  default:
    System.out.print("unknown ");
}
```
= clean dirty
> Case 0 has no `break`, so execution falls through into case 1.

?num How many times does "tick" print?
```java
for (int i = 1; i <= 9; i += 2) {
    System.out.println("tick");
}
```
= 5 ± 0
> `i` takes the values 1, 3, 5, 7, and 9.

?code What does this print?
```java
int sum = 0;
for (int i = 0; i < 6; i++) {
    if (i == 2) continue;
    if (i == 4) break;
    sum += i;
}
System.out.println(sum);
```
= 4
> The loop adds 0, 1, and 3. It skips 2, and stops at 4 before adding it.

? A teammate writes `while (!turretAtTarget()) { }` inside `Turret.periodic()`. What happens on the robot?
- The turret moves to the target faster
+ The robot cycle freezes: no sensors update and no new motor commands are sent, so the loop may never end
- Java rejects it at compile time
- It works in teleop but not in autonomous
> Robot code must return quickly every 20 ms. Check the condition once per cycle instead.

?code What does this print?
```java
static int addOne(int count) {
    count = count + 1;
    return count;
}
// ...
int jamCount = 5;
addOne(jamCount);
System.out.println(jamCount);
```
= 5
> The method changes its own copy and the return value is ignored, so `jamCount` is still 5.

? Which pair of methods is **not** a legal overload in the same class?
- `double clamp(double v)` and `double clamp(double v, double max)`
- `void log(int value)` and `void log(double value)`
+ `double speed(double rpm)` and `int speed(double rpm)`
- `void ifOk(int a)` and `void ifOk(int a, int b)`
> Overloads must differ in their parameter lists. Changing only the return type is a compile error.

?num With a deadband of 0.2, what does WPILib-style `applyDeadband(-0.6, 0.2)` return?
= -0.5 ± 0.0001
> (−0.6 + 0.2) / (1 − 0.2) = −0.4 / 0.8 = −0.5.

?? Which of these names follow Java conventions? (Select all that apply.)
+ `wheelRadiusMeters` for a local variable
+ `DriveConstants` for a class
- `Get_Pose` for a method
+ `METERS_PER_INCH` for a constant
- `turretangle` for a variable
> Variables and methods use camelCase, classes use PascalCase, and constants use UPPER_SNAKE_CASE.
:::
