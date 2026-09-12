package frc.training.u02;

/** Reference solution for exercise u02-deadband. */
public final class JoystickMath {
  private JoystickMath() {}

  public static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static double applyDeadband(double value, double deadband) {
    if (Math.abs(value) <= deadband) {
      return 0.0;
    }
    if (value > 0) {
      return (value - deadband) / (1.0 - deadband);
    }
    return (value + deadband) / (1.0 - deadband);
  }

  public static double squareKeepSign(double value) {
    return Math.copySign(value * value, value);
  }

  public static double shapeRotation(double stick, double deadband, double maxRadPerSec) {
    return squareKeepSign(applyDeadband(stick, deadband)) * maxRadPerSec;
  }

  public static double stickMagnitude(double x, double y) {
    return Math.min(1.0, Math.hypot(x, y));
  }
}
