---
summary: Package logic into methods with parameters and return values, understand scope and overloading, document with Javadoc, and design small functions that are easy to test.
objectives:
  - Declare and call methods with parameters and return values
  - Explain local scope, pass-by-value, and method overloading
  - Write small pure functions with Javadoc, like the joystick math in DriveCommands
files:
  - src/main/java/frc/robot/commands/DriveCommands.java
  - src/main/java/frc/robot/util/SparkUtil.java
  - src/main/java/frc/robot/subsystems/Superstructure.java
---

## A method is a named, reusable job

A **method** is a block of code with a name. You define it once and **call** it wherever you need that job done.

```java
/** Returns the distance a wheel travels for a number of wheel radians. */
public static double wheelRadiansToMeters(double radians, double wheelRadiusMeters) {
    return radians * wheelRadiusMeters;
}

double meters = wheelRadiansToMeters(10.0, 0.0508);   // call it: 0.508
```

Every method declaration has the same parts:

| Part | In the example | Meaning |
|---|---|---|
| Modifiers | `public static` | Who can call it and how (Unit 3 explains) |
| Return type | `double` | The type of value it hands back, or `void` for nothing |
| Name | `wheelRadiansToMeters` | A verb or question, in camelCase |
| Parameters | `(double radians, double wheelRadiusMeters)` | Inputs, each with a type and name |
| Body | `{ return ...; }` | The statements that do the job |

The values you pass when calling, `10.0` and `0.0508`, are **arguments**. Inside the method they become the parameters `radians` and `wheelRadiusMeters`.

## Returning values, or not

`return` sends a value back and **immediately exits** the method. A method with a non-`void` return type must return a value on every path, and the compiler checks this.

A `void` method does a job without handing anything back. Our Superstructure has both kinds, each with Javadoc:

::source file="src/main/java/frc/robot/subsystems/Superstructure.java" start=182 end=198

`getCurrentState()` returns a value. `setWantedState(...)` changes a field and returns nothing.

## Scope: variables live inside their braces

A variable declared inside a method, or inside any `{ }` block, is **local**. It exists only from its declaration to the closing brace:

```java
double shape(double stick) {
    double squared = stick * stick;   // squared exists from here...
    if (squared > 0.5) {
        double extra = 0.1;           // extra exists only inside this if
        squared += extra;
    }
    return squared;                   // ...to here. extra is already gone.
}
```

Two methods can use the same local name without conflict. Their variables are completely separate.

## Parameters are copies

For primitive types like `double` and `int`, Java passes a **copy** of the value. Changing the parameter inside the method does not change the caller's variable:

```java
static void tryToChange(double speed) {
    speed = 0.0;   // changes only the copy
}

double mySpeed = 4.2;
tryToChange(mySpeed);
System.out.println(mySpeed);   // still 4.2
```

To get a new value out of a method, **return it**. Objects behave differently, which [Unit 3](course:03-java-objects/classes-and-objects) covers.

## Overloading: same name, different parameters

Two methods can share a name if their parameter lists differ. `SparkUtil` has two `ifOk` methods, one for a single value and one for several:

::source file="src/main/java/frc/robot/util/SparkUtil.java" from="/** Processes a value from a Spark only if the value is valid. */" lines=23

When you call `ifOk(...)`, Java picks the version whose parameter types match your arguments. (The parameters with types like `DoubleSupplier` pass *functions* as arguments. [Unit 5](course:05-java-advanced/lambdas-and-method-references) makes that clear.)

## Small, pure methods

A **pure** method's result depends only on its parameters. It reads no sensors, changes no fields, and prints nothing. Pure methods are the easiest code in the world to test, because the same input always gives the same output.

`DriveCommands` shapes the rotation stick with a few pure steps:

::source file="src/main/java/frc/robot/commands/DriveCommands.java" from="// Apply rotation deadband" lines=6

1. **Deadband:** ignore tiny stick values, because controller sticks never rest at exactly 0.
2. **Square, keeping the sign:** small pushes become gentle, and full push stays full.

Pulling logic like this into named methods, such as `applyDeadband` and `squareKeepSign`, gives each step a name you can read and a unit you can test.

## Designing good methods

- **One job per method.** If the name needs "and," it is probably two methods.
- **Name it by what it does:** `getTotalCurrent()`, `isPastLine(x, lineX)`, `applyState()`.
- **Keep it short.** If it does not fit on your screen, look for a smaller method hiding inside.
- **Document it with Javadoc** when the name alone is not enough: what it returns, parameter units, and edge cases.

```java
/**
 * Applies a joystick deadband.
 *
 * @param value raw stick value, from -1 to 1
 * @param deadband values with magnitude at or below this become 0
 * @return the rescaled value, still from -1 to 1
 */
```

Javadoc shows up when anyone hovers over your method in VS Code.

:::exercise id="u02-deadband"
Rebuild the joystick shaping from `DriveCommands` as small pure methods.

- `clamp(value, min, max)` keeps a value inside a range.
- `applyDeadband(value, deadband)` matches WPILib's formula exactly (read the Javadoc in the starter).
- `squareKeepSign(value)` squares while keeping direction.
- `shapeRotation(...)` chains the three steps, reusing your methods.
- `stickMagnitude(x, y)` measures a diagonal push and never returns more than 1.

The last test checks something surprising about the team's code: the "Move Forward" auto option was written to drive at 1 m/s, but after the deadband is applied it really drives about **0.78 m/s**. It is finding **F17** in the [Code Audit](course:reference/code-audit).
---hint
`clamp` can be written in one line with `Math.max` and `Math.min`.
---hint
In `applyDeadband`, handle `Math.abs(value) <= deadband` first and return 0. Then pick the positive or negative formula.
---hint
`squareKeepSign` is exactly what `Math.copySign(value * value, value)` does.
:::

:::quiz
?code What does this print?
```java
static double twice(double x) {
    x = x * 2;
    return x;
}
// ...
double speed = 1.5;
double result = twice(speed);
System.out.println(speed + " " + result);
```
= 1.5 3.0
> The method doubles its own copy and returns it. The caller's `speed` is unchanged.

? Which method signature correctly declares a method that takes a distance in meters and returns nothing?
- `double logDistance(meters)`
+ `void logDistance(double meters)`
- `logDistance(double meters) void`
- `void logDistance(meters double)`
> The return type comes before the name, and each parameter needs a type before its name.

?tf Two methods in the same class can have the same name if their parameter lists are different.
= true
> That is overloading, like the two `ifOk` methods in `SparkUtil`.

?num With a deadband of 0.1, what does `applyDeadband(0.55, 0.1)` return?
= 0.5 ± 0.0001
> (0.55 − 0.1) / (1 − 0.1) = 0.45 / 0.9 = 0.5.

? Why are pure methods easy to test?
+ The same inputs always produce the same output, with no hidden sensors, fields, or timing involved
- They run faster than other methods
- JUnit can only test static methods
- They cannot throw exceptions
> Tests just call the method with known inputs and check the result. No robot, simulator, or setup is needed.
:::
