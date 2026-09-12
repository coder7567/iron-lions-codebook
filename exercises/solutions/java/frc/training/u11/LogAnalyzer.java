package frc.training.u11;

/** Reference solution for exercise u11-analyze. */
public final class LogAnalyzer {
  public record LoopStats(double meanSeconds, double maxSeconds, int overrunCount) {}

  private LogAnalyzer() {}

  public static LoopStats loopStats(double[] timestamps, double budgetSeconds) {
    if (timestamps.length < 2) {
      throw new IllegalArgumentException("need at least two timestamps");
    }

    double total = 0.0;
    double max = 0.0;
    int overruns = 0;
    for (int i = 1; i < timestamps.length; i++) {
      double duration = timestamps[i] - timestamps[i - 1];
      total += duration;
      max = Math.max(max, duration);
      if (duration > budgetSeconds) {
        overruns++;
      }
    }

    int gaps = timestamps.length - 1;
    return new LoopStats(total / gaps, max, overruns);
  }

  public static double firstTrue(double[] timestamps, boolean[] values) {
    if (timestamps.length != values.length) {
      throw new IllegalArgumentException("timestamps and values must be the same length");
    }
    for (int i = 0; i < values.length; i++) {
      if (values[i]) {
        return timestamps[i];
      }
    }
    return Double.NaN;
  }

  public static double worstTrackingError(double[] setpoints, double[] measurements) {
    if (setpoints.length != measurements.length) {
      throw new IllegalArgumentException("series must be the same length");
    }
    double worst = 0.0;
    for (int i = 0; i < setpoints.length; i++) {
      worst = Math.max(worst, Math.abs(setpoints[i] - measurements[i]));
    }
    return worst;
  }

  public static double fractionWithin(double[] setpoints, double[] measurements, double tolerance) {
    if (setpoints.length != measurements.length) {
      throw new IllegalArgumentException("series must be the same length");
    }
    if (setpoints.length == 0) {
      return 0.0;
    }
    int within = 0;
    for (int i = 0; i < setpoints.length; i++) {
      if (Math.abs(setpoints[i] - measurements[i]) <= tolerance) {
        within++;
      }
    }
    return (double) within / setpoints.length;
  }
}
