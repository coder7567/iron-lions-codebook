package frc.training.u06;

/** Exercise u06-encoders: turn raw encoder readings into mechanism angles, and catch readings to distrust. */
public final class EncoderMath {
  /** Counts per revolution of the turret's through-bore encoder. */
  public static final int TURRET_COUNTS_PER_REV = 8192;
  /** Encoder turns per turret turn, from TurretConstants.turretGearRatio (180 / 44). */
  public static final double TURRET_GEAR_RATIO = 180.0 / 44.0;

  private EncoderMath() {}

  /**
   * Subtracts a zero offset from an absolute encoder reading and wraps into [0, 1). Both values are in
   * rotations. Example: a raw reading of 0.10 with an offset of 0.31 becomes 0.79.
   */
  public static double applyZeroOffset(double rawRotations, double zeroOffsetRotations) {
    // TODO
    return Double.NaN;
  }

  /** Converts rotations to radians. */
  public static double rotationsToRadians(double rotations) {
    // TODO
    return Double.NaN;
  }

  /**
   * Converts external encoder counts to mechanism radians: counts divided by countsPerRev gives encoder
   * rotations, dividing by gearRatio (encoder turns per mechanism turn) gives mechanism rotations, and
   * multiplying by 2π gives radians.
   */
  public static double mechanismRadiansFromCounts(long counts, int countsPerRev, double gearRatio) {
    // TODO
    return Double.NaN;
  }

  /** Converts turret encoder counts to turret radians using the turret's constants. */
  public static double turretRadiansFromCounts(long counts) {
    // TODO: reuse mechanismRadiansFromCounts
    return Double.NaN;
  }

  /**
   * Returns true when an absolute reading (in rotations, from 0 to 1) changed by more than
   * {@code maxChangeRotations} since the previous reading, measured the short way around the wrap. For
   * example, 0.98 to 0.02 is a change of 0.04, not 0.96.
   */
  public static boolean isJump(double previousRotations, double currentRotations, double maxChangeRotations) {
    // TODO
    return false;
  }
}
