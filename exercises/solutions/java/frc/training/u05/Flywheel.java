package frc.training.u05;

/** Reference solution for exercise u05-io, part 2. */
public class Flywheel {
  public static final double KS_VOLTS = 0.2;
  public static final double KV_VOLTS_PER_RPM = 1.93e-3;
  public static final double KP_VOLTS_PER_RPM = 1.0e-3;

  private final FlywheelIO io;
  private final FlywheelIO.FlywheelIOInputs inputs = new FlywheelIO.FlywheelIOInputs();
  private double targetRpm = 0.0;

  public Flywheel(FlywheelIO io) {
    this.io = io;
  }

  public void setTargetRpm(double rpm) {
    targetRpm = rpm;
  }

  public void stop() {
    targetRpm = 0.0;
  }

  public void periodic() {
    io.updateInputs(inputs);
    if (targetRpm == 0.0 || !inputs.connected) {
      io.stop();
      return;
    }
    double error = targetRpm - inputs.velocityRpm;
    double volts = KS_VOLTS * Math.signum(targetRpm) + KV_VOLTS_PER_RPM * targetRpm + KP_VOLTS_PER_RPM * error;
    io.setVoltage(Math.max(-12.0, Math.min(12.0, volts)));
  }

  public boolean isAtSpeed(double toleranceRpm) {
    return targetRpm != 0.0 && Math.abs(targetRpm - inputs.velocityRpm) <= toleranceRpm;
  }

  public FlywheelIO.FlywheelIOInputs getInputs() {
    return inputs;
  }
}
