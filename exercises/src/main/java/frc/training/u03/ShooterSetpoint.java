package frc.training.u03;

/**
 * Exercise u03-setpoint: a flywheel speed and hood angle that belong together.
 *
 * <p>Our robot has a similar class at {@code frc.robot.subsystems.turret.ShooterSetpoint}. Yours is
 * <em>immutable</em>: once created, a setpoint never changes. Methods that "change" it return a new object.
 */
public class ShooterSetpoint {
  // TODO: add two private final double fields, one for rpm and one for hoodAngle.

  /** Creates a setpoint from a flywheel speed (RPM) and a hood angle (hood encoder rotations). */
  public ShooterSetpoint(double rpm, double hoodAngle) {
    // TODO: store the parameters in your fields
  }

  /** Returns the flywheel speed in RPM. */
  public double getRpm() {
    // TODO
    return 0.0;
  }

  /** Returns the hood angle in hood encoder rotations. */
  public double getHoodAngle() {
    // TODO
    return 0.0;
  }

  /**
   * Returns a NEW setpoint partway from this one to {@code end}.
   *
   * <p>{@code t = 0} gives this setpoint's values, {@code t = 1} gives {@code end}'s values, and
   * {@code t = 0.5} is halfway. Values of {@code t} below 0 or above 1 are clamped into that range.
   */
  public ShooterSetpoint interpolate(ShooterSetpoint end, double t) {
    // TODO
    return this;
  }

  /** Returns a NEW setpoint with a different RPM and the same hood angle. Never modify this object. */
  public ShooterSetpoint withRpm(double newRpm) {
    // TODO
    return this;
  }

  /** Returns text like {@code ShooterSetpoint[rpm=2000.0, hoodAngle=0.879]}. */
  @Override
  public String toString() {
    // TODO
    return "";
  }
}
