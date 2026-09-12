package frc.training.u03;

/** Reference solution for exercise u03-rollermode. */
public enum RollerMode {
  STOPPED(0, 0),
  INTAKING(5000, 4500),
  REVERSING(-5000, -4500),
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

  public boolean isMoving() {
    return intakeRpm != 0 || feederRpm != 0;
  }

  public boolean armDeployed() {
    return this != STOPPED;
  }

  public static RollerMode fromText(String text) {
    if (text == null) {
      return STOPPED;
    }
    String cleaned = text.trim();
    for (RollerMode mode : values()) {
      if (mode.name().equalsIgnoreCase(cleaned)) {
        return mode;
      }
    }
    return STOPPED;
  }

  public RollerMode next() {
    RollerMode[] all = values();
    return all[(ordinal() + 1) % all.length];
  }
}
