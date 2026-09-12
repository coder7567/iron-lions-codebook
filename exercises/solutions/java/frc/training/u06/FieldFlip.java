package frc.training.u06;

/** Reference solution for exercise u06-flip. */
public final class FieldFlip {
  public static final double FIELD_LENGTH = 16.541;
  public static final double FIELD_WIDTH = 8.069;

  private FieldFlip() {}

  public record FieldPose(double x, double y, double headingRadians) {}

  public static double flipX(double x) {
    return FIELD_LENGTH - x;
  }

  public static double flipY(double y) {
    return FIELD_WIDTH - y;
  }

  public static double flipHeading(double headingRadians) {
    return wrap(headingRadians + Math.PI);
  }

  public static FieldPose flip(FieldPose pose) {
    return new FieldPose(flipX(pose.x()), flipY(pose.y()), flipHeading(pose.headingRadians()));
  }

  public static FieldPose forAlliance(FieldPose bluePose, boolean isRed) {
    return isRed ? flip(bluePose) : bluePose;
  }

  public static boolean inAllianceZone(double x, boolean isRed, double zoneDepth) {
    return isRed ? x >= FIELD_LENGTH - zoneDepth : x <= zoneDepth;
  }

  /** Wraps an angle into (-π, π]. */
  private static double wrap(double angle) {
    double twoPi = 2.0 * Math.PI;
    double wrapped = angle % twoPi;
    if (wrapped <= -Math.PI) {
      wrapped += twoPi;
    } else if (wrapped > Math.PI) {
      wrapped -= twoPi;
    }
    return wrapped;
  }
}
