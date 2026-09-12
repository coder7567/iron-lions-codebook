package frc.training.u14;

/** Reference solution for exercise u14-passing. */
public final class PassingTargets {
  public record Translation(double x, double y) {}

  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  public static final Translation BLUE_LEFT = new Translation(1.0, 6.0);

  public static final Translation BLUE_RIGHT = new Translation(1.0, 2.0);

  public static final double BLUE_CENTER_Y = 4.0;

  public static final double ALLIANCE_ZONE_END = 5.0;

  public static final double HOOD_OFFSET = 0.291;

  public static final double HOOD_MIN = 0.239 + HOOD_OFFSET;

  public static final double HOOD_MAX = 0.616 + HOOD_OFFSET;

  private PassingTargets() {}

  public static double flipX(double x, boolean isRed) {
    return isRed ? FIELD_LENGTH - x : x;
  }

  public static double flipY(double y, boolean isRed) {
    return isRed ? FIELD_WIDTH - y : y;
  }

  public static Translation flip(Translation point, boolean isRed) {
    return new Translation(flipX(point.x(), isRed), flipY(point.y(), isRed));
  }

  public static Translation choosePassTarget(double robotY, boolean isRed) {
    double centerY = flipY(BLUE_CENTER_Y, isRed);
    Translation left = flip(BLUE_LEFT, isRed);
    Translation right = flip(BLUE_RIGHT, isRed);

    if (isRed) {
      return robotY > centerY ? right : left;
    }
    return robotY > centerY ? left : right;
  }

  public static double[] passingMapKeys(boolean isRed) {
    return new double[] {
      flipX(ALLIANCE_ZONE_END, isRed),
      FIELD_LENGTH / 2,
      flipX(FIELD_LENGTH - ALLIANCE_ZONE_END, isRed),
      flipX(FIELD_LENGTH, isRed)
    };
  }

  public static double clampHood(double hoodAngle) {
    return Math.max(HOOD_MIN, Math.min(HOOD_MAX, hoodAngle));
  }
}
