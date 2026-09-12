---
summary: Show that you can build and use classes, collections, enums, strings, and safe error handling in Java.
---

This test covers all of Unit 3. Trace the code carefully; several questions hinge on the difference between references and values.

:::exam Unit 3 test: objects and organizing code
? What does `new` do in `Module module = new Module(io, 0);`?
+ Creates a new `Module` object, runs its constructor, and gives back a reference to it
- Declares the `Module` class
- Copies an existing module
- Imports the `Module` class
> `new` allocates an object and runs the constructor. The variable stores a reference to that object.

?code What does this print?
```java
class Setpoint {
    double rpm;
    Setpoint(double rpm) { this.rpm = rpm; }
}
// ...
Setpoint a = new Setpoint(2000);
Setpoint b = a;
Setpoint c = new Setpoint(2000);
b.rpm = 2600;
System.out.println(a.rpm + " " + (a == c));
```
= 2600.0 false
> `a` and `b` share one object, so the change shows through `a`. `c` is a separate object, so `a == c` is false.

? Inside a constructor with a parameter named `io`, why write `this.io = io;`?
+ `this.io` names the field, while `io` alone names the parameter
- It makes the field static
- It is required only for interfaces
- It copies the IO object so changes do not affect it
> Without `this.`, `io = io` assigns the parameter to itself.

? Which choice best protects a turret's soft limits from being bypassed by other classes?
- Make the hardware field `public` so everyone can check it
+ Make the hardware field `private` and expose a method that clamps every requested angle
- Make the class `final`
- Make every method `static`
> Encapsulation means the only way in is a method that enforces the rules.

?code What does this print?
```java
class Counter {
    static int total = 0;
    int mine = 0;
    void tick() { total++; mine++; }
}
// ...
Counter a = new Counter();
Counter b = new Counter();
a.tick(); a.tick(); b.tick();
System.out.println(Counter.total + " " + a.mine + " " + b.mine);
```
= 3 2 1
> `total` is shared by the class; each object has its own `mine`.

?tf A field declared `private final double[] currents = new double[4];` can still have `currents[0] = 12.5;` assigned later in the class.
= true
> `final` locks which array the field refers to, not the array's contents.

? A class lives in `src/main/java/frc/robot/util/SparkUtil.java`. What must its first line be?
- `import frc.robot.util;`
+ `package frc.robot.util;`
- `package SparkUtil;`
- `namespace frc.robot.util;`
> The package declaration must match the folder path under `src/main/java`.

? Which import prefix tells you a class comes from AdvantageKit?
- `com.revrobotics`
- `edu.wpi.first`
+ `org.littletonrobotics.junction`
- `com.pathplanner.lib`
> AdvantageKit's classes, like `Logger` and `AutoLog`, live under `org.littletonrobotics.junction`.

?code What does this print?
```java
int[] ids = {1, 3, 5, 7};
int sum = 0;
for (int i = 1; i < ids.length; i += 2) {
    sum += ids[i];
}
System.out.println(sum);
```
= 10
> `i` is 1 and 3, so the loop adds `ids[1]` (3) and `ids[3]` (7).

? You need to look up a device by CAN ID and also list IDs in sorted order. Which collection fits best?
- `ArrayList<CanDevice>`
- `HashMap<Integer, CanDevice>`
+ `TreeMap<Integer, CanDevice>`
- `int[]`
> A `TreeMap` looks things up by key and keeps its keys sorted.

?code What does this print?
```java
Map<String, Integer> ids = new HashMap<>();
ids.put("Turret", 12);
System.out.println(ids.get("Hood") + " " + ids.containsKey("Turret"));
```
= null true
> A missing key returns `null`. Printing it shows the word `null`.

? What is `Superstructure.WantedState.SHOOTING.ordinal()`, given the order `IDLE, PAUSED, SHOOTING, EJECTING, TESTING`?
- 3
+ 2
- 1
- `"SHOOTING"`
> Ordinals count from 0 in declaration order.

? Which switch will fail to compile when a new constant is added to an enum and not handled?
+ A switch expression that returns a value for each enum case
- A switch statement with a `default` branch
- Any `if`/`else` chain
- None; Java never checks enum switches
> Switch expressions must be exhaustive, so the compiler flags the missing case.

?code What does this print?
```java
String a = "SHOOTING";
String b = new String("SHOOTING");
System.out.println(a.equals(b) + " " + (a == b));
```
= true false
> `equals` compares the characters; `==` compares references, and `new String` always creates a separate object.

?text What does `String.format(Locale.US, "%.2f m", 4.626)` return?
= 4.63 m
> `%.2f` rounds to two decimal places.

? A method receives `Optional<Alliance>` that might be empty. Which line safely produces an alliance, defaulting to blue?
- `alliance.get()`
+ `alliance.orElse(Alliance.Blue)`
- `(Alliance) alliance`
- `alliance == null ? Alliance.Blue : alliance`
> `get()` throws on an empty Optional. `orElse` supplies the default.

? The robot needs to parse a CAN ID typed into a dashboard, and the text might be "twelve". What is the most robust approach?
- Call `Integer.parseInt` and let any exception crash the program so the error is obvious
+ Catch `NumberFormatException` and handle the bad input, for example by returning an empty `Optional` and showing an alert
- Assume people never make typos
- Convert the text with `(int)`
> During operation, bad outside input should be handled without crashing the robot program.

?? Which of these can throw a `NullPointerException`? (Select all that apply.)
+ `String name = null; name.length();`
+ `Module[] modules = new Module[4]; modules[0].periodic();`
- `Optional.empty().isPresent();`
- `String s = "" + null;`
> Calling a method through `null` throws. A new object array starts full of `null`. Joining `null` into text just prints "null", and `isPresent()` on an empty Optional returns false.
:::
