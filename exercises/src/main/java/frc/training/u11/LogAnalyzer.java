package frc.training.u11;

/**
 * Exercise u11-analyze: the questions you ask a log after a practice match, as plain functions over
 * arrays of samples.
 */
public final class LogAnalyzer {
  /** Loop timing over a run. */
  public record LoopStats(double meanSeconds, double maxSeconds, int overrunCount) {}

  private LogAnalyzer() {}

  /**
   * Loop statistics from a series of sample timestamps. The durations are the gaps between
   * consecutive timestamps, and an overrun is any gap longer than the budget.
   *
   * @throws IllegalArgumentException when there are fewer than two timestamps
   */
  public static LoopStats loopStats(double[] timestamps, double budgetSeconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The timestamp of the first sample where the value is true, or Double.NaN if it never is.
   *
   * @throws IllegalArgumentException when the arrays are different lengths
   */
  public static double firstTrue(double[] timestamps, boolean[] values) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The largest absolute difference between a setpoint series and a measurement series. */
  public static double worstTrackingError(double[] setpoints, double[] measurements) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The fraction of samples (0 to 1) where the measurement is within tolerance of the setpoint, which
   * is how you answer "was the flywheel ready when we shot?"
   */
  public static double fractionWithin(double[] setpoints, double[] measurements, double tolerance) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
