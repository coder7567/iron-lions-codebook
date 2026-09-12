package frc.training.u09;

/** Reference solution for exercise u09-pid. */
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

  public void enableContinuousInput(double minInput, double maxInput) {
    this.continuous = true;
    this.minInput = minInput;
    this.maxInput = maxInput;
  }

  public void setIntegratorRange(double integralMin, double integralMax) {
    this.integralMin = integralMin;
    this.integralMax = integralMax;
  }

  public void setTolerance(double tolerance) {
    this.tolerance = tolerance;
  }

  static double inputModulus(double value, double min, double max) {
    double modulus = max - min;
    double result = (value - min) % modulus;
    if (result < 0) {
      result += modulus;
    }
    return result + min;
  }

  public double calculate(double measurement, double dtSeconds) {
    double error = setpoint - measurement;
    if (continuous) {
      double range = maxInput - minInput;
      error = inputModulus(error, -range / 2.0, range / 2.0);
    }

    integral = Math.max(integralMin, Math.min(integralMax, integral + error * dtSeconds));
    double derivative = hasLastError ? (error - lastError) / dtSeconds : 0.0;

    lastError = error;
    hasLastError = true;

    return kp * error + ki * integral + kd * derivative;
  }

  public boolean atSetpoint() {
    return hasLastError && Math.abs(lastError) <= tolerance;
  }

  public void reset() {
    integral = 0.0;
    lastError = 0.0;
    hasLastError = false;
  }
}
