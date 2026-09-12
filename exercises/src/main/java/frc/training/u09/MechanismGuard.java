package frc.training.u09;

/**
 * Exercise u09-guard: the guards that keep a mechanism from hurting itself, in the same shapes our code
 * uses: clamped setpoints, limited output range, a stop at the soft limits, and a rate limit.
 */
public final class MechanismGuard {
  private MechanismGuard() {}

  /** Clamps a setpoint into [min, max], like MathUtil.clamp in setTurretAngle and setIntakeArmAngle. */
  public static double clampSetpoint(double setpoint, double min, double max) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Clamps an output into [-maxOutput, maxOutput], like the SPARK's closed-loop output range. */
  public static double limitOutput(double output, double maxOutput) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Returns 0 when the mechanism is at or past a limit and the output would push it further past that
   * limit. Otherwise returns the output unchanged. This is what a soft limit does in the controller.
   */
  public static double stopAtLimits(double output, double position, double min, double max) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Limits how fast a commanded value may change, like WPILib's SlewRateLimiter. */
  public static final class RateLimiter {
    private final double maxChangePerSecond;
    private double value;

    public RateLimiter(double maxChangePerSecond, double initialValue) {
      this.maxChangePerSecond = maxChangePerSecond;
      this.value = initialValue;
    }

    /** Moves the stored value toward the input by at most maxChangePerSecond * dtSeconds. */
    public double calculate(double input, double dtSeconds) {
      // TODO
      throw new UnsupportedOperationException("TODO");
    }

    /** Jumps straight to a value, like SlewRateLimiter.reset. */
    public void reset(double value) {
      this.value = value;
    }

    public double get() {
      return value;
    }
  }
}
