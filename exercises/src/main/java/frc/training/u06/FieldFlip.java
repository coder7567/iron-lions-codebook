package frc.training.u06;

/**
 * Exercise u06-flip: convert blue-alliance field positions to red-alliance positions, like our
 * AllianceFlipUtil. The 2026 REBUILT field is rotationally symmetric: the red side is the blue side turned
 * 180° around the field's center.
 */
public final class FieldFlip {
  /** 2026 field length in meters, from WPILib's k2026RebuiltWelded layout. */
  public static final double FIELD_LENGTH = 16.541;
  /** 2026 field width in meters. */
  public static final double FIELD_WIDTH = 8.069;

  private FieldFlip() {}

  /** A position on the field in meters, with a heading in radians (counterclockwise positive). */
  public record FieldPose(double x, double y, double headingRadians) {}

  /** Flips an X coordinate across the field's length. */
  public static double flipX(double x) {
    // TODO
    return 0.0;
  }

  /** Flips a Y coordinate across the field's width. */
  public static double flipY(double y) {
    // TODO
    return 0.0;
  }

  /** Turns a heading around by π and wraps the result into the range (-π, π]. */
  public static double flipHeading(double headingRadians) {
    // TODO
    return 0.0;
  }

  /** Flips a whole pose for a rotationally symmetric field. */
  public static FieldPose flip(FieldPose pose) {
    // TODO
    return pose;
  }

  /** Returns {@code bluePose} unchanged for the blue alliance, or flipped for the red alliance. */
  public static FieldPose forAlliance(FieldPose bluePose, boolean isRed) {
    // TODO
    return null;
  }

  /**
   * Returns true when {@code x} is inside the given alliance's zone: for blue, x at most {@code zoneDepth}; for
   * red, x at least {@code FIELD_LENGTH - zoneDepth}.
   */
  public static boolean inAllianceZone(double x, boolean isRed, double zoneDepth) {
    // TODO
    return false;
  }
}
