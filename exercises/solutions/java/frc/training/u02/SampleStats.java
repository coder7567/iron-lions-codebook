package frc.training.u02;

/** Reference solution for exercise u02-loops. */
public final class SampleStats {
  private SampleStats() {}

  public static double sum(double[] values) {
    double total = 0.0;
    for (double value : values) {
      total += value;
    }
    return total;
  }

  public static double average(double[] values) {
    if (values.length == 0) {
      return 0.0;
    }
    return sum(values) / values.length;
  }

  public static double max(double[] values) {
    if (values.length == 0) {
      return 0.0;
    }
    double largest = values[0];
    for (int i = 1; i < values.length; i++) {
      if (values[i] > largest) {
        largest = values[i];
      }
    }
    return largest;
  }

  public static int countAbove(double[] values, double threshold) {
    int count = 0;
    for (double value : values) {
      if (value > threshold) {
        count++;
      }
    }
    return count;
  }

  public static int firstIndexAbove(double[] values, double threshold) {
    for (int i = 0; i < values.length; i++) {
      if (values[i] > threshold) {
        return i;
      }
    }
    return -1;
  }

  public static int longestRunAbove(double[] values, double threshold) {
    int longest = 0;
    int current = 0;
    for (double value : values) {
      if (value > threshold) {
        current++;
        longest = Math.max(longest, current);
      } else {
        current = 0;
      }
    }
    return longest;
  }

  public static int jamDetectedAt(double[] currents, double threshold, int count) {
    int streak = 0;
    for (int i = 0; i < currents.length; i++) {
      if (currents[i] > threshold) {
        streak++;
      } else {
        streak = 0;
      }
      if (streak >= count) {
        return i;
      }
    }
    return -1;
  }
}
