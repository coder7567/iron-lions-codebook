package frc.training.u03;

/**
 * Exercise u03-rollermode: an enum that carries data and behavior.
 *
 * <p>Our Intake spreads its roller speeds across IntakeConstants and several methods. Here each mode
 * carries its own speeds, so a mode and its speeds can never get out of sync.
 */
public enum RollerMode {
  /** Everything stopped and the arm stowed. */
  STOPPED(0, 0),
  /** TODO: the intake runs at 5000 RPM and the feeder at 4500 RPM (IntakeConstants values). */
  INTAKING(0, 0),
  /** TODO: both run backward: intake -5000 RPM, feeder -4500 RPM. */
  REVERSING(0, 0),
  /** Rollers stopped, but the arm stays deployed (like Intake.pause()). */
  PAUSED(0, 0);

  private final double intakeRpm;
  private final double feederRpm;

  RollerMode(double intakeRpm, double feederRpm) {
    this.intakeRpm = intakeRpm;
    this.feederRpm = feederRpm;
  }

  public double getIntakeRpm() {
    return intakeRpm;
  }

  public double getFeederRpm() {
    return feederRpm;
  }

  /** Returns true when either roller should spin in this mode. */
  public boolean isMoving() {
    // TODO
    return false;
  }

  /** Returns true when the intake arm should be deployed: every mode except STOPPED. */
  public boolean armDeployed() {
    // TODO
    return false;
  }

  /**
   * Returns the mode whose name matches {@code text}, ignoring capitalization and surrounding spaces.
   * Returns STOPPED when {@code text} is null or matches nothing. Example: {@code " intaking "} gives
   * INTAKING.
   */
  public static RollerMode fromText(String text) {
    // TODO: loop over values() and compare with name()
    return STOPPED;
  }

  /**
   * Returns the next mode in declaration order, wrapping from the last back to the first:
   * STOPPED → INTAKING → REVERSING → PAUSED → STOPPED. Use {@code values()} and {@code ordinal()}.
   */
  public RollerMode next() {
    // TODO
    return this;
  }
}
