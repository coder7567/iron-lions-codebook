package frc.training.u06;

/** Reference solution for exercise u06-encoders. */
public final class EncoderMath {
  public static final int TURRET_COUNTS_PER_REV = 8192;
  public static final double TURRET_GEAR_RATIO = 180.0 / 44.0;

  private EncoderMath() {}

  public static double applyZeroOffset(double rawRotations, double zeroOffsetRotations) {
    double wrapped = (rawRotations - zeroOffsetRotations) % 1.0;
    if (wrapped < 0) {
      wrapped += 1.0;
    }
    return wrapped >= 1.0 ? wrapped - 1.0 : wrapped;
  }

  public static double rotationsToRadians(double rotations) {
    return rotations * 2.0 * Math.PI;
  }

  public static double mechanismRadiansFromCounts(long counts, int countsPerRev, double gearRatio) {
    double encoderRotations = (double) counts / countsPerRev;
    return rotationsToRadians(encoderRotations / gearRatio);
  }

  public static double turretRadiansFromCounts(long counts) {
    return mechanismRadiansFromCounts(counts, TURRET_COUNTS_PER_REV, TURRET_GEAR_RATIO);
  }

  public static boolean isJump(double previousRotations, double currentRotations, double maxChangeRotations) {
    double change = Math.abs(currentRotations - previousRotations) % 1.0;
    double shortWay = Math.min(change, 1.0 - change);
    return shortWay > maxChangeRotations;
  }
}
