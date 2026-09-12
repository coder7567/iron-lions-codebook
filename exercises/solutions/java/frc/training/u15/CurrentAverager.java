package frc.training.u15;

import java.util.ArrayDeque;
import java.util.Deque;

/** Reference solution for exercise u15-averager. */
public final class CurrentAverager {
  private record Sample(double timestamp, double amps) {}

  private final double windowSeconds;
  private final Deque<Sample> samples = new ArrayDeque<>();

  public CurrentAverager(double windowSeconds) {
    this.windowSeconds = windowSeconds;
  }

  public double windowSeconds() {
    return windowSeconds;
  }

  public void addSample(double timestampSeconds, double amps) {
    samples.addLast(new Sample(timestampSeconds, amps));
    while (!samples.isEmpty() && timestampSeconds - samples.peekFirst().timestamp() > windowSeconds) {
      samples.removeFirst();
    }
  }

  public int sampleCount() {
    return samples.size();
  }

  public double average() {
    if (samples.isEmpty()) {
      return 0.0;
    }
    double total = 0.0;
    for (Sample sample : samples) {
      total += sample.amps();
    }
    return total / samples.size();
  }

  public double peak() {
    double max = 0.0;
    boolean any = false;
    for (Sample sample : samples) {
      max = any ? Math.max(max, sample.amps()) : sample.amps();
      any = true;
    }
    return any ? max : 0.0;
  }

  public void reset() {
    samples.clear();
  }
}
