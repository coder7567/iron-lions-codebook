package frc.training.u06;

/** Exercise u06-angles: angle helpers for plain double radians. */
public final class Angles {
  private Angles() {}

  /** Wraps an angle into the range (-π, π]. Example: 3π/2 becomes -π/2, and -π becomes π. */
  public static double wrapRadians(double radians) {
    // TODO
    return Double.NaN;
  }

  /** Wraps an angle into the range [0, 2π). Example: -π/2 becomes 3π/2, and 2π becomes 0. */
  public static double wrapPositive(double radians) {
    // TODO
    return Double.NaN;
  }

  /**
   * Returns the shortest signed turn from {@code fromRadians} to {@code toRadians}, in (-π, π]. Turning from
   * 3.1 to -3.1 is a small positive turn across the ±π seam, not a turn of -6.2.
   */
  public static double shortestDelta(double fromRadians, double toRadians) {
    // TODO
    return Double.NaN;
  }

  /** Returns true when two angles are within {@code toleranceRadians} of each other, measured the short way. */
  public static boolean withinTolerance(double aRadians, double bRadians, double toleranceRadians) {
    // TODO
    return false;
  }

  /**
   * Mimics ModuleIOSpark.setTurnPosition: adds the module's zero offset to the desired angle and wraps the
   * result into [0, 2π), the range the SPARK's absolute encoder uses.
   */
  public static double toSparkTurnSetpoint(double desiredRadians, double zeroOffsetRadians) {
    // TODO
    return Double.NaN;
  }
}
