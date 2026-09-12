package frc.training.u02;

/**
 * Exercise u02-loops: loop over sensor samples the way robot code does every cycle.
 *
 * <p>Each array holds readings taken one robot loop apart, like motor current in amps.
 */
public final class SampleStats {
  private SampleStats() {}

  /** Returns the sum of all values. An empty array sums to 0. */
  public static double sum(double[] values) {
    // TODO
    return 0.0;
  }

  /** Returns the average of the values, or 0 for an empty array. */
  public static double average(double[] values) {
    // TODO
    return 0.0;
  }

  /** Returns the largest value, or 0 for an empty array. Careful: every value might be negative. */
  public static double max(double[] values) {
    // TODO
    return 0.0;
  }

  /** Counts values strictly greater than {@code threshold}. */
  public static int countAbove(double[] values, double threshold) {
    // TODO
    return 0;
  }

  /** Returns the index of the first value strictly greater than {@code threshold}, or -1 if there is none. */
  public static int firstIndexAbove(double[] values, double threshold) {
    // TODO
    return 0;
  }

  /**
   * Returns the length of the longest run of consecutive values strictly greater than {@code threshold}.
   * Example: {40, 41, 10, 50, 50, 50} with threshold 35 returns 3.
   */
  public static int longestRunAbove(double[] values, double threshold) {
    // TODO
    return 0;
  }

  /**
   * Like our intake's jam detector: returns the index of the sample at which {@code count} consecutive
   * readings have been above {@code threshold}, or -1 if that never happens.
   *
   * <p>Example: {40, 40, 10, 40, 40, 40} with threshold 35 and count 3 returns 5, because samples 3, 4,
   * and 5 are the first three-in-a-row.
   */
  public static int jamDetectedAt(double[] currents, double threshold, int count) {
    // TODO
    return 0;
  }
}
