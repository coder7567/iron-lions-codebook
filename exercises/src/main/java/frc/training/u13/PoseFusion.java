package frc.training.u13;

import java.util.List;

/**
 * Exercise u13-fusion: combining several camera observations into one pose, with the standard
 * deviations that tell the pose estimator how much to trust it.
 *
 * <p>You write the fusion twice: the way our code does it (weights of 1 / sigma) and the way the
 * statistics say it should be done (weights of 1 / sigma squared). The tests show what the difference
 * costs. That gap is finding F13.
 */
public final class PoseFusion {
  /** One accepted observation. */
  public record Observation(double x, double y, double yawRad, double timestamp, double avgTagDistance, int tagCount) {}

  /** A combined estimate and how much to trust it. */
  public record Fused(double x, double y, double yawRad, double timestamp, double linearStdDev, double angularStdDev) {}

  /** VisionConstants.camera1linearStdDevBaseline. */
  public static final double LINEAR_STD_DEV_BASELINE = 0.2;

  /** VisionConstants.camera1angularStdDevBaseline, about 30 degrees. */
  public static final double ANGULAR_STD_DEV_BASELINE = 0.524;

  /** What our code uses for a single-tag observation's angular standard deviation. */
  public static final double SINGLE_TAG_ANGULAR_STD_DEV = 99999;

  private PoseFusion() {}

  /**
   * Linear standard deviation for one observation: the baseline scaled by avgTagDistance / tagCount,
   * then doubled when only one tag was seen.
   */
  public static double linearStdDev(Observation observation) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Angular standard deviation: the baseline scaled the same way, except a single-tag observation gets
   * SINGLE_TAG_ANGULAR_STD_DEV, which is how our code says "don't trust this heading at all."
   */
  public static double angularStdDev(Observation observation) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * A weighted average of angles that handles the wrap, by averaging their sines and cosines and taking
   * atan2 of the result.
   *
   * @throws IllegalArgumentException if the arrays are different lengths
   */
  public static double circularMean(double[] anglesRad, double[] weights) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Fuses observations the way AprilTagVision does today: position and timestamp weighted by
   * 1 / linearStdDev, yaw weighted by 1 / angularStdDev through the circular mean, and a fused standard
   * deviation of 1 / (sum of the weights).
   *
   * @return null when the list is empty
   */
  public static Fused fuseWithInverseSigma(List<Observation> observations) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Fuses observations the statistically correct way: weights of 1 / sigma squared, and a fused
   * standard deviation of sqrt(1 / sum of those weights).
   *
   * @return null when the list is empty
   */
  public static Fused fuseWithInverseVariance(List<Observation> observations) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
