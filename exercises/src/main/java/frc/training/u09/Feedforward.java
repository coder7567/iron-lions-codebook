package frc.training.u09;

/**
 * Exercise u09-feedforward: the voltage model our drive uses, and the least-squares fit that
 * DriveCommands.feedforwardCharacterization runs to find its gains.
 *
 * <p>The model is volts = kS * sign(velocity) + kV * velocity (+ kA * acceleration).
 */
public final class Feedforward {
  /** A fitted pair of gains. */
  public record Gains(double kS, double kV) {}

  private Feedforward() {}

  /** The steady-state voltage for a velocity, exactly as ModuleIOSpark.setDriveVelocity computes it. */
  public static double calculate(double kS, double kV, double velocity) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Adds the acceleration term: kS * sign(velocity) + kV * velocity + kA * acceleration. */
  public static double calculate(double kS, double kV, double kA, double velocity, double acceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Fits kS and kV to samples of (velocity, volts) with least squares, the same formulas the drive's
   * characterization routine prints:
   *
   * <pre>
   * kS = (sumY * sumX2 - sumX * sumXY) / (n * sumX2 - sumX * sumX)
   * kV = (n * sumXY - sumX * sumY) / (n * sumX2 - sumX * sumX)
   * </pre>
   *
   * @throws IllegalArgumentException if the arrays differ in length or hold fewer than two samples
   */
  public static Gains fit(double[] velocities, double[] volts) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The highest velocity the model can hold at a given voltage: (volts - kS) / kV, or 0 when the
   * voltage is not even enough to overcome kS.
   */
  public static double maxVelocity(double kS, double kV, double volts) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
