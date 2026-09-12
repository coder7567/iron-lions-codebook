package frc.training.u14;

/** Reference solution for exercise u14-aim. */
public final class TurretAim {
  public static final double MIN_ANGLE = -4.261;

  public static final double MAX_ANGLE = 1.6;

  public static final double DEADZONE_END = 2.022;

  public static final double STARTING_OFFSET = Math.PI;

  private TurretAim() {}

  public static double wrap(double angle) {
    double wrapped = (angle + Math.PI) % (2 * Math.PI);
    if (wrapped < 0) {
      wrapped += 2 * Math.PI;
    }
    return wrapped - Math.PI;
  }

  public static double fieldAngleTo(double robotX, double robotY, double targetX, double targetY) {
    return Math.atan2(targetY - robotY, targetX - robotX);
  }

  public static double robotRelativeAngle(
      double robotX, double robotY, double headingRad, double targetX, double targetY) {
    return wrap(fieldAngleTo(robotX, robotY, targetX, targetY) - headingRad);
  }

  public static boolean inDeadzone(double requestedAngle, double offset) {
    double angle = wrap(requestedAngle + offset);
    return angle < DEADZONE_END && angle > MAX_ANGLE;
  }

  public static double toTurretSetpoint(double requestedAngle, double offset) {
    double angle = wrap(requestedAngle + offset);
    if (angle > DEADZONE_END && angle < Math.PI) {
      angle -= 2 * Math.PI;
    }
    return Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, angle));
  }
}
