package frc.training.u12;

import java.util.List;

/**
 * Exercise u12-pathmath: the arithmetic behind planning an autonomous routine, using our field and
 * drivetrain numbers.
 */
public final class PathMath {
  /** A point on the field, in meters, in the blue-origin frame. */
  public record Waypoint(double x, double y) {}

  /** The 2026 field. */
  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  /** The autonomous period. */
  public static final double AUTO_SECONDS = 20.0;

  private PathMath() {}

  /** The straight-line distance through the waypoints, in meters. Fewer than two points is 0. */
  public static double length(List<Waypoint> waypoints) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * How long a rest-to-rest move of the given distance takes under a trapezoid profile. A move long
   * enough to reach the cruise velocity takes {@code distance / maxVelocity + maxVelocity /
   * maxAcceleration}; a shorter one is triangular and takes {@code 2 * sqrt(distance /
   * maxAcceleration)}.
   */
  public static double driveTime(double distance, double maxVelocity, double maxAcceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The total time for an auto: every segment driven rest to rest, plus a fixed overhead for the
   * commands between them.
   */
  public static double autoTime(
      double[] segmentDistances, double maxVelocity, double maxAcceleration, double overheadSeconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Whether a routine of this length fits inside the autonomous period. */
  public static boolean fitsInAuto(double totalSeconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Mirrors a waypoint for the red alliance: the field is rotationally symmetric. */
  public static Waypoint flip(Waypoint waypoint) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** True when every waypoint is inside the field's boundaries, edges included. */
  public static boolean insideField(List<Waypoint> waypoints) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
