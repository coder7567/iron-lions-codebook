package frc.training.u05;

/**
 * Exercise u05-io, part 1: a fake flywheel for tests.
 *
 * <p>Tests set what the "sensor" reads with the setters, and check what the subsystem commanded with the
 * getters. A fake starts connected, reading 0 RPM and 0 A.
 */
public class FlywheelIOFake implements FlywheelIO {
  // TODO: private fields for measured RPM, current, connected (starts true), last volts, and a command count

  @Override
  public void updateInputs(FlywheelIOInputs inputs) {
    // TODO: copy measured RPM, last commanded volts, current, and connected into inputs
  }

  /** Records the command, clamped to -12..12 volts, and counts it. */
  @Override
  public void setVoltage(double volts) {
    // TODO
  }

  public void setMeasuredRpm(double rpm) {
    // TODO
  }

  public void setCurrentAmps(double amps) {
    // TODO
  }

  public void setConnected(boolean connected) {
    // TODO
  }

  /** Returns the most recent voltage command after clamping. */
  public double getLastVolts() {
    // TODO
    return Double.NaN;
  }

  /** Returns how many times setVoltage has been called (stop() counts, because it calls setVoltage). */
  public int getVoltageCommandCount() {
    // TODO
    return -1;
  }
}
