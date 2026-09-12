package frc.training.u05;

/** Reference solution for exercise u05-io, part 1. */
public class FlywheelIOFake implements FlywheelIO {
  private double measuredRpm = 0.0;
  private double currentAmps = 0.0;
  private boolean connected = true;
  private double lastVolts = 0.0;
  private int voltageCommands = 0;

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    inputs.velocityRpm = measuredRpm;
    inputs.appliedVolts = lastVolts;
    inputs.currentAmps = currentAmps;
    inputs.connected = connected;
  }

  @Override
  public void setVoltage(double volts) {
    lastVolts = Math.max(-12.0, Math.min(12.0, volts));
    voltageCommands++;
  }

  public void setMeasuredRpm(double rpm) {
    measuredRpm = rpm;
  }

  public void setCurrentAmps(double amps) {
    currentAmps = amps;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  public double getLastVolts() {
    return lastVolts;
  }

  public int getVoltageCommandCount() {
    return voltageCommands;
  }
}
