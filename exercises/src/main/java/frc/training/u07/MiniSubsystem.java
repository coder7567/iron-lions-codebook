package frc.training.u07;

/**
 * A stripped-down stand-in for WPILib's {@code Subsystem}, used by the Unit 7 exercises. This file is
 * provided; you do not need to change it.
 */
public interface MiniSubsystem {
  /** Called once per scheduler run, before any command runs. */
  default void periodic() {}

  /** A readable name for logs and test messages. */
  default String getName() {
    return getClass().getSimpleName();
  }
}
