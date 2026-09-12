package frc.training.u08;

/** Reference solution for exercise u08-basics. */
public final class ShooterStateMachine {
  public enum WantedState {
    IDLE,
    SHOOT
  }

  public enum CurrentState {
    IDLE,
    SPINNING_UP,
    READY
  }

  public record Outputs(double flywheelRpm, boolean feederOn) {}

  public static final double SHOT_RPM = 2500;
  public static final double TOLERANCE_RPM = 1000;

  private WantedState wantedState = WantedState.IDLE;
  private CurrentState currentState = CurrentState.IDLE;

  public void setWantedState(WantedState wantedState) {
    this.wantedState = wantedState;
  }

  public CurrentState getCurrentState() {
    return currentState;
  }

  static CurrentState updateState(WantedState wanted, double measuredRpm) {
    return switch (wanted) {
      case IDLE -> CurrentState.IDLE;
      case SHOOT -> measuredRpm - SHOT_RPM > -TOLERANCE_RPM ? CurrentState.READY : CurrentState.SPINNING_UP;
    };
  }

  static Outputs applyState(CurrentState current) {
    return switch (current) {
      case IDLE -> new Outputs(0.0, false);
      case SPINNING_UP -> new Outputs(SHOT_RPM, false);
      case READY -> new Outputs(SHOT_RPM, true);
    };
  }

  public Outputs periodic(double measuredRpm) {
    currentState = updateState(wantedState, measuredRpm);
    return applyState(currentState);
  }
}
