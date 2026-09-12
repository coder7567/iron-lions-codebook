package frc.training.u14;

/**
 * Exercise u14-passing: choosing where to throw FUEL when the robot is too far to score, mirroring the
 * field for the alliance, and measuring what finding F4 actually costs.
 */
public final class PassingTargets {
  /** A point on the field, in meters, in the blue-origin frame. */
  public record Translation(double x, double y) {}

  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  /** TurretConstants.left and right, as stored for the blue alliance. */
  public static final Translation BLUE_LEFT = new Translation(1.0, 6.0);

  public static final Translation BLUE_RIGHT = new Translation(1.0, 2.0);

  /** TurretConstants.center. */
  public static final double BLUE_CENTER_Y = 4.0;

  /** TurretConstants.allianceZoneEnd. */
  public static final double ALLIANCE_ZONE_END = 5.0;

  /** TurretConstants.hoodOffset. */
  public static final double HOOD_OFFSET = 0.291;

  /** The hood's travel, from TurretConstants. */
  public static final double HOOD_MIN = 0.239 + HOOD_OFFSET;

  public static final double HOOD_MAX = 0.616 + HOOD_OFFSET;

  private PassingTargets() {}

  /** AllianceFlipUtil.applyX: mirror an x coordinate for the red alliance. */
  public static double flipX(double x, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** AllianceFlipUtil.applyY: mirror a y coordinate for the red alliance. */
  public static double flipY(double y, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** AllianceFlipUtil.apply: mirror a point for the red alliance. */
  public static Translation flip(Translation point, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Turret.chooseTargetBasedOnY: compare the robot's y against the alliance's center line. On blue,
   * being above the center picks the left target; on red, being above it picks the right one. The
   * returned point is already mirrored for the alliance.
   */
  public static Translation choosePassTarget(double robotY, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The four x keys the passing map is built on, in the order our code inserts them: the end of our
   * alliance zone, midfield, the start of the other alliance's zone, and the far wall. Each is mirrored
   * for the alliance.
   */
  public static double[] passingMapKeys(boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** TurretIOSpark.setHoodAngle clamps every hood setpoint into the hood's travel. */
  public static double clampHood(double hoodAngle) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
