package frc.training.u05;

/**
 * The hardware contract for a flywheel, in the same style as our ModuleIO and TurretIO. This interface is
 * provided; you do not need to change it.
 *
 * <p>The subsystem only ever talks to hardware through these methods, so a real motor, a physics
 * simulation, or a test fake can be swapped in without changing the subsystem.
 */
public interface FlywheelIO {
  /** Everything the flywheel subsystem reads from hardware each loop. */
  class FlywheelIOInputs {
    public double velocityRpm = 0.0;
    public double appliedVolts = 0.0;
    public double currentAmps = 0.0;
    public boolean connected = false;
  }

  /** Copies the latest sensor readings into {@code inputs}. */
  default void updateInputs(FlywheelIOInputs inputs) {}

  /** Commands the motor with a voltage from -12 to 12. */
  default void setVoltage(double volts) {}

  /** Stops the motor. */
  default void stop() {
    setVoltage(0.0);
  }
}
