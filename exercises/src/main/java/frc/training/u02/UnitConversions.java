package frc.training.u02;

/**
 * Exercise u02-units: the unit conversions robot code needs constantly.
 *
 * <p>Our DriveConstants does these same conversions with WPILib's {@code Units} class. Here you write them
 * yourself so you understand exactly what they compute.
 */
public final class UnitConversions {
  private UnitConversions() {}

  /** One inch is exactly this many meters. */
  public static final double METERS_PER_INCH = 0.0254;

  /** Converts a length in inches to meters. Example: 20 in is 0.508 m. */
  public static double inchesToMeters(double inches) {
    // TODO
    return 0.0;
  }

  /** Converts a length in meters to inches. Example: 0.508 m is 20 in. */
  public static double metersToInches(double meters) {
    // TODO
    return 0.0;
  }

  /** Converts degrees to radians. Example: 180° is π radians. */
  public static double degreesToRadians(double degrees) {
    // TODO: use Math.PI
    return 0.0;
  }

  /** Converts rotations per minute (RPM) to radians per second. Example: 60 RPM is 2π rad/s. */
  public static double rpmToRadiansPerSecond(double rpm) {
    // TODO: one rotation is 2π radians, one minute is 60 seconds
    return 0.0;
  }

  /**
   * Returns how many wheel radians one motor rotation produces.
   *
   * <p>This is {@code DriveConstants.driveEncoderPositionFactor}. {@code reduction} is how many times the
   * motor turns for one turn of the wheel (5.9 on our drive).
   */
  public static double motorRotationsToWheelRadians(double reduction) {
    // TODO
    return 0.0;
  }

  /**
   * Returns the robot's linear speed in meters per second for a drive motor spinning at {@code motorRpm},
   * geared down by {@code reduction}, turning a wheel of radius {@code wheelRadiusMeters}.
   *
   * <p>Hint: wheel angular speed (rad/s) times radius (m) is linear speed (m/s).
   */
  public static double wheelSpeedMetersPerSecond(double motorRpm, double reduction, double wheelRadiusMeters) {
    // TODO: reuse your other methods
    return 0.0;
  }
}
