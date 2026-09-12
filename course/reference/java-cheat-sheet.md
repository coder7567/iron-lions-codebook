---
summary: The Java this robot code uses, with robot examples: types, records, enums, switch expressions, lambdas, Optional, collections, and the standard library calls that come up every week.
---

Everything here appears somewhere in the team's code. For the language taught in order, start at
[Unit 2](course:02-java-basics/variables-and-types).

## Types and values

```java
double distanceMeters = 4.625;   // always double for physical quantities
int canId = 12;                  // int for counts and IDs
boolean jammed = false;
String cameraName = "April_Tag_1";
final double kWheelRadius = 0.0508;   // final: cannot be reassigned
var speeds = new ChassisSpeeds();     // var: the compiler infers the type
```

| Rule | Why |
|---|---|
| Use `double` for anything physical | Mixing `int` division into physics is a classic silent bug |
| Put units in the name | `maxSpeedMetersPerSec`, not `maxSpeed` |
| `final` by default for fields | It documents that nothing reassigns them |
| `var` only when the type is obvious from the right side | `var x = getThing()` hides what `x` is |

**Integer division truncates.** `1 / 2` is `0`. Write `1.0 / 2` when you mean a half.

## Classes, records, and enums

```java
// A class: state plus behavior.
public class Intake extends SubsystemBase {
  private final IntakeIO io;
  public Intake(IntakeIO io) { this.io = io; }
}

// A record: immutable data, with equals, hashCode, and toString for free.
public record PoseObservation(double ambiguity, Pose3d pose, double timestamp, int tagCount) {}

// An enum: a fixed set of named values.
public enum WantedState { IDLE, PAUSED, INTAKING, REVERSING, TESTING }
```

Records are the right choice for anything that is "just data": a setpoint, an observation, a pair of
speeds. Access a component with `observation.ambiguity()`, not a getter you wrote.

## Switch expressions

```java
// Arrow form: no fall-through, no break.
CurrentState next = switch (wantedState) {
  case IDLE -> CurrentState.IDLE;
  case SHOOTING -> shootingOrPassing();
  case PAUSED, TESTING -> CurrentState.TESTING;
};

// Colon form with yield, which our subsystems use.
return switch (wantedState) {
  case IDLE:
    yield CurrentState.IDLE;
  case SHOOTING:
    if (inZone) {
      yield CurrentState.SHOOTING;
    }
    yield CurrentState.PASSING;
};
```

A switch **expression** over an enum with no `default` must cover every constant, so adding a state
breaks the build until you handle it. A switch **statement** with a `default` does not. Prefer the
expression for decisions.

## Lambdas and functional interfaces

```java
Supplier<Pose2d> poseSupplier = drive::getPose;         // method reference
BooleanSupplier ready = () -> turret.shooterSpedUp();   // lambda
DoubleSupplier stickX = () -> -controller.getLeftY();
Runnable stop = drive::stop;
```

| Interface | Shape | Used for |
|---|---|---|
| `Supplier<T>` | `() -> T` | Reading a value later, like the pose |
| `BooleanSupplier` | `() -> boolean` | Trigger conditions and interlocks |
| `DoubleSupplier` | `() -> double` | Joystick axes |
| `Consumer<T>` | `(T) -> void` | Vision pushing measurements into the drive |
| `Runnable` | `() -> void` | Command actions |

**A lambda runs when it is called, not when it is written.** That is why `joystickDrive` reads the
alliance inside its lambda: at construction time nobody knows it yet.

## Optional

```java
Optional<Alliance> alliance = DriverStation.getAlliance();

if (alliance.isPresent() && alliance.get() == Alliance.Red) { ... }   // explicit
boolean isRed = alliance.orElse(Alliance.Blue) == Alliance.Red;       // with a default
```

`Optional` means "this may genuinely be absent." Our code uses `orElse` to make an unknown alliance
behave like blue, which is a deliberate default rather than a crash.

## Collections

```java
List<String> names = new ArrayList<>();
names.add("start");
for (String name : names) { ... }

Map<String, Double> totals = new LinkedHashMap<>();   // keeps insertion order
totals.merge("Drive", 0.004, Double::sum);            // add or accumulate

TreeMap<Double, Setpoint> map = new TreeMap<>();      // sorted keys
map.floorEntry(3.0);                                  // the entry at or below 3.0
map.ceilingEntry(3.0);                                // the entry at or above

Set<Subsystem> requirements = Set.of(drive);          // immutable
List<Integer> fixed = List.of(1, 2, 3);               // immutable
```

`TreeMap` with `floorEntry` and `ceilingEntry` is exactly how an interpolating table finds the two
entries around a distance.

## Streams, lightly

```java
double[] timestamps = queue.stream().mapToDouble((Double value) -> value).toArray();

long connected = Arrays.stream(modules).filter(Module::isConnected).count();
```

Streams are good for turning a collection into an array or a count, which is what our odometry code
uses them for. They are not worth reaching for inside a 20 ms loop when a plain `for` is clearer.

## Null

```java
if (tagPose.isPresent()) { ... }          // prefer Optional where an API offers it
if (autonomousCommand != null) { ... }    // and a plain check where it does not
```

Prefer never producing null. When an API hands you one, check it at the boundary, not everywhere
downstream.

## Math you will actually use

| Call | Does |
|---|---|
| `Math.hypot(x, y)` | The length of a vector, without overflow |
| `Math.atan2(y, x)` | The angle of a vector; note y comes first |
| `Math.abs`, `Math.signum` | Magnitude, and direction as −1, 0, or 1 |
| `Math.copySign(value, sign)` | Squaring without losing direction |
| `Math.max`, `Math.min` | Clamping, when combined |
| `MathUtil.clamp(v, min, max)` | WPILib's clamp |
| `MathUtil.inputModulus(v, min, max)` | Wrapping an angle into a range |
| `MathUtil.applyDeadband(v, band)` | A rescaled deadband |
| `Units.inchesToMeters`, `degreesToRadians` | Conversions, so the constant stays readable |

## Formatting and printing

```java
System.out.println("kS: " + formatter.format(kS));    // the characterization commands do this
Logger.recordOutput("Turret/State", currentState);    // what robot code should do instead
```

Prefer logging to printing. A printed line scrolls away; a logged value is in the match record.

## Things that bite

| Trap | Fix |
|---|---|
| `int` division in physics | Make one side a `double` |
| `==` on strings | Use `.equals` |
| Comparing doubles with `==` | Compare with a tolerance |
| Mutable state shared between loops | Reset it, or make it local |
| Arrays and lists starting at 0 | Modules are 0 through 3, not 1 through 4 |
| A lambda capturing a value that changes later | Capture the supplier, not the value |
