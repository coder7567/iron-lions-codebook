package frc.training.u03;

/** Reference solution for exercise u03-setpoint. */
public class ShooterSetpoint {
  private final double rpm;
  private final double hoodAngle;

  public ShooterSetpoint(double rpm, double hoodAngle) {
    this.rpm = rpm;
    this.hoodAngle = hoodAngle;
  }

  public double getRpm() {
    return rpm;
  }

  public double getHoodAngle() {
    return hoodAngle;
  }

  public ShooterSetpoint interpolate(ShooterSetpoint end, double t) {
    double clamped = Math.max(0.0, Math.min(1.0, t));
    return new ShooterSetpoint(
        rpm + (end.rpm - rpm) * clamped,
        hoodAngle + (end.hoodAngle - hoodAngle) * clamped);
  }

  public ShooterSetpoint withRpm(double newRpm) {
    return new ShooterSetpoint(newRpm, hoodAngle);
  }

  @Override
  public String toString() {
    return "ShooterSetpoint[rpm=" + rpm + ", hoodAngle=" + hoodAngle + "]";
  }
}
