package frc.training.u07;

/** Reference solution for exercise u07-joystick. */
public final class DriverMath {
  public static final double DEADBAND = 0.1;
  public static final double MAX_SPEED_METERS_PER_SEC = 4.2;
  public static final double DRIVE_BASE_RADIUS_METERS = Math.hypot(0.254, 0.254);

  public record Speeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  private DriverMath() {}

  public static double applyDeadband(double value, double deadband) {
    if (Math.abs(value) <= deadband) {
      return 0.0;
    }
    return value > 0.0 ? (value - deadband) / (1.0 - deadband) : (value + deadband) / (1.0 - deadband);
  }

  public static double maxAngularSpeedRadPerSec() {
    return MAX_SPEED_METERS_PER_SEC / DRIVE_BASE_RADIUS_METERS;
  }

  public static double[] linearVelocityFromJoysticks(double x, double y) {
    double magnitude = applyDeadband(Math.hypot(x, y), DEADBAND);
    double direction = Math.atan2(y, x);
    magnitude = magnitude * magnitude;
    return new double[] {magnitude * Math.cos(direction), magnitude * Math.sin(direction)};
  }

  public static double shapeRotation(double omega) {
    double shaped = applyDeadband(omega, DEADBAND);
    return Math.copySign(shaped * shaped, shaped);
  }

  public static Speeds fieldRelativeSpeeds(double x, double y, double omega) {
    double[] linear = linearVelocityFromJoysticks(x, y);
    return new Speeds(
        linear[0] * MAX_SPEED_METERS_PER_SEC,
        linear[1] * MAX_SPEED_METERS_PER_SEC,
        shapeRotation(omega) * maxAngularSpeedRadPerSec());
  }

  public static Speeds toRobotRelative(Speeds fieldRelative, double robotHeadingRad) {
    double cos = Math.cos(-robotHeadingRad);
    double sin = Math.sin(-robotHeadingRad);
    return new Speeds(
        fieldRelative.vxMetersPerSec() * cos - fieldRelative.vyMetersPerSec() * sin,
        fieldRelative.vxMetersPerSec() * sin + fieldRelative.vyMetersPerSec() * cos,
        fieldRelative.omegaRadPerSec());
  }

  public static Speeds joystickDrive(double x, double y, double omega, double robotHeadingRad, boolean isRed) {
    double heading = isRed ? robotHeadingRad + Math.PI : robotHeadingRad;
    return toRobotRelative(fieldRelativeSpeeds(x, y, omega), heading);
  }
}
