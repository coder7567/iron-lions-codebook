package frc.training.u09;

/**
 * Exercise u09-pid: a PID controller with the same behavior as WPILib's PIDController, including
 * continuous input for angles and an integrator range.
 */
public final class MiniPid {
  private final double kp;
  private final double ki;
  private final double kd;

  private double setpoint = 0.0;
  private double integral = 0.0;
  private double lastError = 0.0;
  private boolean hasLastError = false;

  private boolean continuous = false;
  private double minInput = 0.0;
  private double maxInput = 0.0;

  private double integralMin = Double.NEGATIVE_INFINITY;
  private double integralMax = Double.POSITIVE_INFINITY;
  private double tolerance = 0.05;

  public MiniPid(double kp, double ki, double kd) {
    this.kp = kp;
    this.ki = ki;
    this.kd = kd;
  }

  public void setSetpoint(double setpoint) {
    this.setpoint = setpoint;
  }

  public double getSetpoint() {
    return setpoint;
  }

  /** Treats the input as circular, so the error always takes the short way around. */
  public void enableContinuousInput(double minInput, double maxInput) {
    this.continuous = true;
    this.minInput = minInput;
    this.maxInput = maxInput;
  }

  /** Clamps the accumulated integral, which is how WPILib prevents integral windup. */
  public void setIntegratorRange(double integralMin, double integralMax) {
    this.integralMin = integralMin;
    this.integralMax = integralMax;
  }

  public void setTolerance(double tolerance) {
    this.tolerance = tolerance;
  }

  /** Provided: wraps a value into [min, max), like MathUtil.inputModulus. */
  static double inputModulus(double value, double min, double max) {
    double modulus = max - min;
    double result = (value - min) % modulus;
    if (result < 0) {
      result += modulus;
    }
    return result + min;
  }

  /**
   * One loop of PID. Computes the error (taking the short way around when continuous input is on),
   * accumulates the integral with clamping, computes the derivative from the previous error (0 on the
   * first call after construction or reset), and returns kp*error + ki*integral + kd*derivative.
   */
  public double calculate(double measurement, double dtSeconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** True when the last computed error was within the tolerance. False before the first calculate. */
  public boolean atSetpoint() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Clears the integral and the remembered error, so the next calculate starts fresh. */
  public void reset() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
