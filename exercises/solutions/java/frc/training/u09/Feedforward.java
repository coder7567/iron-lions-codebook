package frc.training.u09;

/** Reference solution for exercise u09-feedforward. */
public final class Feedforward {
  public record Gains(double kS, double kV) {}

  private Feedforward() {}

  public static double calculate(double kS, double kV, double velocity) {
    return kS * Math.signum(velocity) + kV * velocity;
  }

  public static double calculate(double kS, double kV, double kA, double velocity, double acceleration) {
    return calculate(kS, kV, velocity) + kA * acceleration;
  }

  public static Gains fit(double[] velocities, double[] volts) {
    if (velocities.length != volts.length) {
      throw new IllegalArgumentException("velocities and volts must be the same length");
    }
    if (velocities.length < 2) {
      throw new IllegalArgumentException("need at least two samples to fit a line");
    }

    int n = velocities.length;
    double sumX = 0.0;
    double sumY = 0.0;
    double sumXY = 0.0;
    double sumX2 = 0.0;
    for (int i = 0; i < n; i++) {
      sumX += velocities[i];
      sumY += volts[i];
      sumXY += velocities[i] * volts[i];
      sumX2 += velocities[i] * velocities[i];
    }

    double denominator = n * sumX2 - sumX * sumX;
    double kS = (sumY * sumX2 - sumX * sumXY) / denominator;
    double kV = (n * sumXY - sumX * sumY) / denominator;
    return new Gains(kS, kV);
  }

  public static double maxVelocity(double kS, double kV, double volts) {
    double available = volts - kS;
    return available <= 0.0 ? 0.0 : available / kV;
  }
}
