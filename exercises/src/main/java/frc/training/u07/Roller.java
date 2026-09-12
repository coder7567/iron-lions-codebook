package frc.training.u07;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/**
 * Exercise u07-commands-wpi: a subsystem with command factories, tested with WPILib's real
 * CommandScheduler. Run with: ./gradlew test --tests "frc.training.u07.RollerTest"
 */
public class Roller extends SubsystemBase {
  private double appliedVolts = 0.0;

  /** The voltage the roller was last told to apply. A real subsystem would send it to an IO object. */
  public double getAppliedVolts() {
    return appliedVolts;
  }

  /** Sets the roller voltage. Commands call this. */
  public void setVolts(double volts) {
    appliedVolts = volts;
  }

  /** Runs the roller at the given voltage until the command ends, then sets 0 V. Requires this roller. */
  public Command runAtVolts(double volts) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Runs the roller at -6 V for the given number of seconds, then sets 0 V. Requires this roller. */
  public Command ejectFor(double seconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** A command for use as the default command: holds the roller at 0 V until interrupted. */
  public Command stopCommand() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
