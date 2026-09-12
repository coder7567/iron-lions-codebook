package frc.training.u14;

import java.util.TreeMap;

/**
 * Exercise u14-sotm: the aim-point adjustment in Turret.considerChassisSpeeds.
 *
 * <p>FUEL leaves a moving robot carrying the robot's velocity, so the turret aims at a point offset
 * from the real target. How far to offset depends on the flight time, and the flight time depends on
 * the distance to the offset point, so the code iterates three times.
 */
public final class ShootOnTheMove {
  /** A point on the field, in meters. */
  public record Translation(double x, double y) {}

  /** The robot's field-relative velocity. */
  public record FieldSpeeds(double vx, double vy) {}

  /** Turret's loop runs this many times. */
  public static final int ITERATIONS = 3;

  /** Turret.TOFMinDistance and TOFMaxDistance. */
  public static final double TOF_MIN_DISTANCE = 0.0;

  public static final double TOF_MAX_DISTANCE = 10.0;

  /** An interpolating table of distance to flight time, clamped at its ends. */
  public static final class ToFTable {
    private final TreeMap<Double, Double> entries = new TreeMap<>();

    public void put(double distanceMeters, double seconds) {
      entries.put(distanceMeters, seconds);
    }

    /**
     * The flight time at a distance: interpolated between neighbors, or the nearest entry's value
     * outside the table.
     *
     * @throws IllegalStateException when the table is empty
     */
    public double get(double distanceMeters) {
      // TODO
      throw new UnsupportedOperationException("TODO");
    }
  }

  private ShootOnTheMove() {}

  /** The team's measured flight times, from the Turret constructor. */
  public static ToFTable teamTimeOfFlight() {
    ToFTable table = new ToFTable();
    table.put(1.93, 1.22);
    table.put(3.92, 1.3);
    table.put(4.13, 1.3);
    table.put(5.45, 1.45);
    return table;
  }

  /** The distance between two points. */
  public static double distance(Translation a, Translation b) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The flight time from the robot to a target: look the distance up in the table, clamped into
   * [TOF_MIN_DISTANCE, TOF_MAX_DISTANCE], then scale by the reality constant.
   */
  public static double timeOfFlight(
      Translation target, Translation robot, ToFTable table, double realityConstant) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The adjusted aim point, exactly as considerChassisSpeeds computes it:
   *
   * <pre>
   * flightTime = 0; previous = 0;
   * repeat ITERATIONS times:
   *   previous = flightTime
   *   flightTime = timeOfFlight(target, robot, table, realityConstant)
   *   target = target - speeds * (flightTime - previous)
   * </pre>
   *
   * The robot's position does not change during the iteration; only the aim point moves.
   */
  public static Translation adjustTarget(
      Translation target, Translation robot, FieldSpeeds speeds, ToFTable table, double realityConstant) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
