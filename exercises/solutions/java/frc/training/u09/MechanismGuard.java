package frc.training.u09;

/** Reference solution for exercise u09-guard. */
public final class MechanismGuard {
  private MechanismGuard() {}

  public static double clampSetpoint(double setpoint, double min, double max) {
    return Math.max(min, Math.min(max, setpoint));
  }

  public static double limitOutput(double output, double maxOutput) {
    return Math.max(-maxOutput, Math.min(maxOutput, output));
  }

  public static double stopAtLimits(double output, double position, double min, double max) {
    if (position >= max && output > 0.0) {
      return 0.0;
    }
    if (position <= min && output < 0.0) {
      return 0.0;
    }
    return output;
  }

  public static final class RateLimiter {
    private final double maxChangePerSecond;
    private double value;

    public RateLimiter(double maxChangePerSecond, double initialValue) {
      this.maxChangePerSecond = maxChangePerSecond;
      this.value = initialValue;
    }

    public double calculate(double input, double dtSeconds) {
      double maxChange = maxChangePerSecond * dtSeconds;
      double change = Math.max(-maxChange, Math.min(maxChange, input - value));
      value += change;
      return value;
    }

    public void reset(double value) {
      this.value = value;
    }

    public double get() {
      return value;
    }
  }
}
