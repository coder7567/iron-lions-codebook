package frc.training.u06;

/** Reference solution for exercise u06-looptiming, part 1. */
public final class LoopStats {
  public static final double NOMINAL_PERIOD_SECONDS = 0.02;

  private LoopStats() {}

  public static double[] cycleLengths(double[] loopStartTimes) {
    if (loopStartTimes.length < 2) {
      return new double[0];
    }
    double[] lengths = new double[loopStartTimes.length - 1];
    for (int i = 0; i < lengths.length; i++) {
      lengths[i] = loopStartTimes[i + 1] - loopStartTimes[i];
    }
    return lengths;
  }

  public static int overrunCount(double[] loopStartTimes, double periodSeconds, double toleranceSeconds) {
    int count = 0;
    for (double length : cycleLengths(loopStartTimes)) {
      if (length > periodSeconds + toleranceSeconds) {
        count++;
      }
    }
    return count;
  }

  public static double worstCycle(double[] loopStartTimes) {
    int index = worstCycleIndex(loopStartTimes);
    return index < 0 ? 0.0 : cycleLengths(loopStartTimes)[index];
  }

  public static int worstCycleIndex(double[] loopStartTimes) {
    double[] lengths = cycleLengths(loopStartTimes);
    int worst = -1;
    for (int i = 0; i < lengths.length; i++) {
      if (worst < 0 || lengths[i] > lengths[worst]) {
        worst = i;
      }
    }
    return worst;
  }
}
