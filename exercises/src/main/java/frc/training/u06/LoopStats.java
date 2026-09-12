package frc.training.u06;

/**
 * Exercise u06-looptiming, part 1: statistics about robot loop timing.
 *
 * <p>Each array holds the time (in seconds) at which a robot loop started. Cycle {@code i} is the time between
 * timestamps {@code i} and {@code i + 1}.
 */
public final class LoopStats {
  /** The TimedRobot default period. */
  public static final double NOMINAL_PERIOD_SECONDS = 0.02;

  private LoopStats() {}

  /**
   * Returns the length of every cycle. With n timestamps there are n - 1 cycles; with fewer than two
   * timestamps, returns an empty array.
   */
  public static double[] cycleLengths(double[] loopStartTimes) {
    // TODO
    return new double[] {-1};
  }

  /** Counts cycles longer than {@code periodSeconds + toleranceSeconds}. */
  public static int overrunCount(double[] loopStartTimes, double periodSeconds, double toleranceSeconds) {
    // TODO
    return -1;
  }

  /** Returns the longest cycle length, or 0 when there are no cycles. */
  public static double worstCycle(double[] loopStartTimes) {
    // TODO
    return -1.0;
  }

  /** Returns the index of the longest cycle (the first one if tied), or -1 when there are no cycles. */
  public static int worstCycleIndex(double[] loopStartTimes) {
    // TODO
    return -2;
  }
}
