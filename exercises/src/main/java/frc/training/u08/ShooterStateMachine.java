package frc.training.u08;

/**
 * Exercise u08-basics: a wanted-state / current-state machine for a flywheel and feeder, in the style of
 * our Turret and Intake subsystems. The feeder may run only once the flywheel is close to speed, the way
 * Intake waits for turret::shooterSpedUp.
 */
public final class ShooterStateMachine {
  /** What commands ask for. */
  public enum WantedState {
    IDLE,
    SHOOT
  }

  /** What the mechanism is actually doing this loop. */
  public enum CurrentState {
    IDLE,
    SPINNING_UP,
    READY
  }

  /** The outputs applyState sends to the hardware. */
  public record Outputs(double flywheelRpm, boolean feederOn) {}

  public static final double SHOT_RPM = 2500;
  /** Like TurretConstants.flywheelTolerance: close enough when less than this far below the setpoint. */
  public static final double TOLERANCE_RPM = 1000;

  private WantedState wantedState = WantedState.IDLE;
  private CurrentState currentState = CurrentState.IDLE;

  public void setWantedState(WantedState wantedState) {
    this.wantedState = wantedState;
  }

  public CurrentState getCurrentState() {
    return currentState;
  }

  /**
   * Decides the current state. IDLE stays IDLE. SHOOT is READY when measuredRpm - SHOT_RPM is greater
   * than -TOLERANCE_RPM (the same test as Turret.shooterSpedUp), and SPINNING_UP otherwise.
   */
  static CurrentState updateState(WantedState wanted, double measuredRpm) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** IDLE: flywheel 0 and feeder off. SPINNING_UP: flywheel at SHOT_RPM and feeder off. READY: both on. */
  static Outputs applyState(CurrentState current) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** One loop: update the current state from the measured speed, then return that state's outputs. */
  public Outputs periodic(double measuredRpm) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
