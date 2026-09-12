package frc.training.u10;

/** Reference solution for exercise u10-odometry. */
public final class OdometryMath {
  public record Pose(double x, double y, double headingRad) {}

  public record Vector(double x, double y) {}

  public static final double WHEEL_RADIUS_METERS = 0.0508;
  public static final double DRIVE_BASE_RADIUS_METERS = Math.hypot(0.254, 0.254);

  private OdometryMath() {}

  public static double positionMeters(double wheelRadians, double wheelRadiusMeters) {
    return wheelRadians * wheelRadiusMeters;
  }

  public static Vector moduleDelta(double previousMeters, double currentMeters, double moduleAngleRad) {
    double distance = currentMeters - previousMeters;
    return new Vector(distance * Math.cos(moduleAngleRad), distance * Math.sin(moduleAngleRad));
  }

  public static Vector chassisDelta(Vector[] moduleDeltas) {
    double sumX = 0.0;
    double sumY = 0.0;
    for (Vector delta : moduleDeltas) {
      sumX += delta.x();
      sumY += delta.y();
    }
    return new Vector(sumX / moduleDeltas.length, sumY / moduleDeltas.length);
  }

  public static Pose advance(Pose pose, Vector robotFrameDelta, double newHeadingRad) {
    double cos = Math.cos(pose.headingRad());
    double sin = Math.sin(pose.headingRad());
    double fieldX = robotFrameDelta.x() * cos - robotFrameDelta.y() * sin;
    double fieldY = robotFrameDelta.x() * sin + robotFrameDelta.y() * cos;
    return new Pose(pose.x() + fieldX, pose.y() + fieldY, newHeadingRad);
  }

  public static double wheelRadiusFromSpin(
      double gyroDeltaRad, double driveBaseRadiusMeters, double averageWheelDeltaRad) {
    return gyroDeltaRad * driveBaseRadiusMeters / averageWheelDeltaRad;
  }
}
