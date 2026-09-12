package frc.training.u14;

/**
 * Exercise u14-aim: turning a target's field position into the number the turret's controller receives,
 * including the operator offset, the unreachable arc, and the soft limits.
 */
public final class TurretAim {
  /** TurretConstants.turretMinAngle and turretMaxAngle. */
  public static final double MIN_ANGLE = -4.261;

  public static final double MAX_ANGLE = 1.6;

  /** The far edge of the arc the turret cannot reach, from TurretIOSpark.setTurretAngle. */
  public static final double DEADZONE_END = 2.022;

  /** TurretConstants.turretStartingOffset. */
  public static final double STARTING_OFFSET = Math.PI;

  private TurretAim() {}

  /** Provided: wraps an angle into [-pi, pi). */
  public static double wrap(double angle) {
    double wrapped = (angle + Math.PI) % (2 * Math.PI);
    if (wrapped < 0) {
      wrapped += 2 * Math.PI;
    }
    return wrapped - Math.PI;
  }

  /** The angle from the robot to the target, in the field frame. */
  public static double fieldAngleTo(double robotX, double robotY, double targetX, double targetY) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * What Turret.calculationToTarget computes: the field angle to the target minus the robot's heading,
   * wrapped into [-pi, pi).
   */
  public static double robotRelativeAngle(
      double robotX, double robotY, double headingRad, double targetX, double targetY) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * True when the requested angle, after the offset is added and the result is wrapped, lands strictly
   * between MAX_ANGLE and DEADZONE_END: the arc the turret cannot reach from either direction.
   */
  public static boolean inDeadzone(double requestedAngle, double offset) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The setpoint the controller receives, exactly as TurretIOSpark.setTurretAngle builds it:
   *
   * <ol>
   *   <li>add the offset
   *   <li>wrap into [-pi, pi)
   *   <li>if the result is above DEADZONE_END, subtract 2*pi so the turret reaches it the other way
   *   <li>clamp into [MIN_ANGLE, MAX_ANGLE]
   * </ol>
   */
  public static double toTurretSetpoint(double requestedAngle, double offset) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
