package frc.training.u02;

/** Reference solution for exercise u02-units. */
public final class UnitConversions {
  private UnitConversions() {}

  /** One inch is exactly this many meters. */
  public static final double METERS_PER_INCH = 0.0254;

  public static double inchesToMeters(double inches) {
    return inches * METERS_PER_INCH;
  }

  public static double metersToInches(double meters) {
    return meters / METERS_PER_INCH;
  }

  public static double degreesToRadians(double degrees) {
    return degrees * Math.PI / 180.0;
  }

  public static double rpmToRadiansPerSecond(double rpm) {
    return rpm * 2.0 * Math.PI / 60.0;
  }

  public static double motorRotationsToWheelRadians(double reduction) {
    return 2.0 * Math.PI / reduction;
  }

  public static double wheelSpeedMetersPerSecond(double motorRpm, double reduction, double wheelRadiusMeters) {
    double wheelRadiansPerSecond = rpmToRadiansPerSecond(motorRpm) / reduction;
    return wheelRadiansPerSecond * wheelRadiusMeters;
  }
}
