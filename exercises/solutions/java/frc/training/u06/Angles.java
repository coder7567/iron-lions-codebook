package frc.training.u06;

/** Reference solution for exercise u06-angles. */
public final class Angles {
  private static final double TWO_PI = 2.0 * Math.PI;

  private Angles() {}

  public static double wrapRadians(double radians) {
    double wrapped = radians % TWO_PI;
    if (wrapped <= -Math.PI) {
      wrapped += TWO_PI;
    } else if (wrapped > Math.PI) {
      wrapped -= TWO_PI;
    }
    return wrapped;
  }

  public static double wrapPositive(double radians) {
    double wrapped = radians % TWO_PI;
    if (wrapped < 0) {
      wrapped += TWO_PI;
    }
    if (wrapped >= TWO_PI) {
      wrapped -= TWO_PI;
    }
    return wrapped;
  }

  public static double shortestDelta(double fromRadians, double toRadians) {
    return wrapRadians(toRadians - fromRadians);
  }

  public static boolean withinTolerance(double aRadians, double bRadians, double toleranceRadians) {
    return Math.abs(shortestDelta(aRadians, bRadians)) <= toleranceRadians;
  }

  public static double toSparkTurnSetpoint(double desiredRadians, double zeroOffsetRadians) {
    return wrapPositive(desiredRadians + zeroOffsetRadians);
  }
}
