package frc.training.u05;

/**
 * Exercise u05-io, part 2: a flywheel "subsystem" that depends only on the {@link FlywheelIO} interface.
 *
 * <p>The gains match our real flywheel's TurretConstants (kS 0.2, kV 1.93e-3, P 1.0e-3). On the robot the
 * SPARK Flex applies them internally; here you compute the voltage yourself.
 */
public class Flywheel {
  public static final double KS_VOLTS = 0.2;
  public static final double KV_VOLTS_PER_RPM = 1.93e-3;
  public static final double KP_VOLTS_PER_RPM = 1.0e-3;

  // TODO: a private final FlywheelIO, a private final FlywheelIO.FlywheelIOInputs, and the target RPM

  /** Creates a flywheel that talks to hardware only through {@code io}. */
  public Flywheel(FlywheelIO io) {
    // TODO
  }

  /** Sets the speed to reach, in RPM. Zero means stopped. */
  public void setTargetRpm(double rpm) {
    // TODO
  }

  /** Sets the target to zero. */
  public void stop() {
    // TODO
  }

  /**
   * Call once per robot loop. First read inputs from the IO. Then, if the target is 0 or the motor is
   * disconnected, call {@code io.stop()}. Otherwise command
   * {@code kS * signum(target) + kV * target + kP * (target - measured)}, clamped to -12..12 volts.
   */
  public void periodic() {
    // TODO
  }

  /**
   * Returns true when the target is not 0 and the measured speed (from the last periodic) is within
   * {@code toleranceRpm} of the target, above or below.
   */
  public boolean isAtSpeed(double toleranceRpm) {
    // TODO
    return false;
  }

  /** Returns the inputs read during the most recent periodic call. */
  public FlywheelIO.FlywheelIOInputs getInputs() {
    // TODO
    return new FlywheelIO.FlywheelIOInputs();
  }
}
