package frc.training.u12;

import java.util.List;

/** Reference solution for exercise u12-pathmath. */
public final class PathMath {
  public record Waypoint(double x, double y) {}

  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  public static final double AUTO_SECONDS = 20.0;

  private PathMath() {}

  public static double length(List<Waypoint> waypoints) {
    double total = 0.0;
    for (int i = 1; i < waypoints.size(); i++) {
      Waypoint previous = waypoints.get(i - 1);
      Waypoint current = waypoints.get(i);
      total += Math.hypot(current.x() - previous.x(), current.y() - previous.y());
    }
    return total;
  }

  public static double driveTime(double distance, double maxVelocity, double maxAcceleration) {
    if (distance <= 0.0) {
      return 0.0;
    }
    double rampDistance = maxVelocity * maxVelocity / maxAcceleration;
    if (distance < rampDistance) {
      return 2.0 * Math.sqrt(distance / maxAcceleration);
    }
    return distance / maxVelocity + maxVelocity / maxAcceleration;
  }

  public static double autoTime(
      double[] segmentDistances, double maxVelocity, double maxAcceleration, double overheadSeconds) {
    double total = 0.0;
    for (double distance : segmentDistances) {
      total += driveTime(distance, maxVelocity, maxAcceleration) + overheadSeconds;
    }
    return total;
  }

  public static boolean fitsInAuto(double totalSeconds) {
    return totalSeconds <= AUTO_SECONDS;
  }

  public static Waypoint flip(Waypoint waypoint) {
    return new Waypoint(FIELD_LENGTH - waypoint.x(), FIELD_WIDTH - waypoint.y());
  }

  public static boolean insideField(List<Waypoint> waypoints) {
    for (Waypoint waypoint : waypoints) {
      if (waypoint.x() < 0.0
          || waypoint.x() > FIELD_LENGTH
          || waypoint.y() < 0.0
          || waypoint.y() > FIELD_WIDTH) {
        return false;
      }
    }
    return true;
  }
}
