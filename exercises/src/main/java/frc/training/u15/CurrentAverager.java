package frc.training.u15;

/**
 * Exercise u15-averager: a current average that actually averages, which is what finding F2 was trying
 * to be.
 *
 * <p>Time arrives as a parameter instead of being read from a clock inside the class. That one choice
 * is what makes this testable: a test can hand it any timeline it likes.
 */
public final class CurrentAverager {
  private final double windowSeconds;

  public CurrentAverager(double windowSeconds) {
    this.windowSeconds = windowSeconds;
  }

  public double windowSeconds() {
    return windowSeconds;
  }

  /**
   * Records one sample and drops every sample older than the window, measured back from this sample's
   * timestamp. Samples arrive in time order.
   */
  public void addSample(double timestampSeconds, double amps) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** How many samples are inside the window right now. */
  public int sampleCount() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The mean of the samples in the window, or 0.0 when there are none. */
  public double average() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The largest sample in the window, or 0.0 when there are none. */
  public double peak() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Forgets every sample, as if the averager had just been created. */
  public void reset() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
