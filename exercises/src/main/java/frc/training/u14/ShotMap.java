package frc.training.u14;

/**
 * Exercise u14-shotmap: the interpolating table that turns a distance into a flywheel speed and hood
 * angle, with the clamping our turret applies before the setpoints reach the hardware.
 */
public final class ShotMap {
  /** A flywheel speed in RPM and a hood angle in rotations, like TurretConstants' ShooterSetpoint. */
  public record Setpoint(double rpm, double hoodAngle) {}

  /** Turret.shooterSetpointMinDistance and shooterSetpointMaxDistance. */
  public static final double MIN_DISTANCE = 0.0;

  public static final double MAX_DISTANCE = 10.0;

  /** TurretConstants.hoodOffset, added to every tuned hood angle. */
  public static final double HOOD_OFFSET = 0.291;

  public static final double HOOD_MIN = 0.239 + HOOD_OFFSET;
  public static final double HOOD_MAX = 0.616 + HOOD_OFFSET;
  public static final double FLYWHEEL_MAX_RPM = 6758;

  /** Adds an entry to the table. */
  public void put(double distanceMeters, Setpoint setpoint) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Looks up a distance. Between two entries, interpolate both values linearly. Outside the table,
   * return the nearest entry's values unchanged.
   *
   * @throws IllegalStateException when the table is empty
   */
  public Setpoint get(double distanceMeters) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * What Turret.calculationToTarget actually does: clamp the distance into the table's range, look it
   * up, then clamp the hood angle into its limits and the flywheel speed into 0 to FLYWHEEL_MAX_RPM.
   */
  public Setpoint lookupClamped(double distanceMeters) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The team's tuned table from the Turret constructor, with the hood offset already applied. */
  public static ShotMap teamTable() {
    ShotMap map = new ShotMap();
    map.put(1.03, new Setpoint(1850, 0.588 + HOOD_OFFSET));
    map.put(1.77, new Setpoint(1950, 0.588 + HOOD_OFFSET));
    map.put(2.0, new Setpoint(2000, 0.588 + HOOD_OFFSET));
    map.put(2.38, new Setpoint(2050, 0.55 + HOOD_OFFSET));
    map.put(2.86, new Setpoint(2100, 0.525 + HOOD_OFFSET));
    map.put(3.377, new Setpoint(2250, 0.5 + HOOD_OFFSET));
    map.put(3.84, new Setpoint(2300, 0.475 + HOOD_OFFSET));
    map.put(4.06, new Setpoint(2400, 0.45 + HOOD_OFFSET));
    map.put(4.66, new Setpoint(2500, 0.425 + HOOD_OFFSET));
    map.put(5.56, new Setpoint(2600, 0.4 + HOOD_OFFSET));
    map.put(6.45, new Setpoint(2700, 0.35 + HOOD_OFFSET));
    return map;
  }
}
