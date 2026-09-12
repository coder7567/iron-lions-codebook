package frc.training.u07;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

/** Reference solution for exercise u07-commands-wpi. */
public class Roller extends SubsystemBase {
  private double appliedVolts = 0.0;

  public double getAppliedVolts() {
    return appliedVolts;
  }

  public void setVolts(double volts) {
    appliedVolts = volts;
  }

  public Command runAtVolts(double volts) {
    return startEnd(() -> setVolts(volts), () -> setVolts(0.0)).withName("Roller " + volts + " V");
  }

  public Command ejectFor(double seconds) {
    return runAtVolts(-6.0).withTimeout(seconds).withName("Roller eject");
  }

  public Command stopCommand() {
    return run(() -> setVolts(0.0)).withName("Roller stop");
  }
}
