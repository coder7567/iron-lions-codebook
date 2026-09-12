package frc.training.u08;

/** Exercise u08-intake: the jam detection and recovery from Intake.updateState, as a pure class. */
public final class IntakeStateMachine {
  public enum WantedState {
    IDLE,
    PAUSED,
    INTAKING,
    REVERSING,
    TESTING
  }

  public enum CurrentState {
    IDLE,
    PAUSED,
    INTAKING,
    REVERSING
  }

  /** One loop of sensor readings. Current in amps, speed in RPM, arm positions in encoder rotations. */
  public record Inputs(double intakeCurrentAmps, double intakeSpeedRpm, double armAngle, double armSetAngle) {}

  /** Values from IntakeConstants. */
  public static final double JAM_CURRENT_AMPS = 35;
  public static final double JAM_SPEED_RPM = 1000;
  public static final int JAM_MIN_COUNT = 50;
  public static final int UNJAM_MIN_COUNT = 10;
  /** Intake.armOut(): the arm counts as out when it is within this distance of its setpoint. */
  public static final double ARM_OUT_TOLERANCE = 0.25;

  private int jamCount = 0;
  private int unjamCount = 0;
  private boolean jammed = false;

  /**
   * Decides this loop's state, exactly like Intake.updateState:
   *
   * <ol>
   *   <li>If wanted is not INTAKING, reset jamCount and unjamCount to 0 and clear jammed.
   *   <li>IDLE, PAUSED, and REVERSING map to the matching current state. TESTING runs INTAKING with no
   *       jam detection.
   *   <li>INTAKING: if the current is above JAM_CURRENT_AMPS, the speed is below JAM_SPEED_RPM, and the
   *       intake is not already jammed, add 1 to jamCount. Otherwise set jamCount to 0. When jamCount
   *       reaches JAM_MIN_COUNT, become jammed.
   *   <li>Then, if jammed and the arm is out, add 1 to unjamCount and return REVERSING. If unjamCount has
   *       reached UNJAM_MIN_COUNT, first clear jammed and both counts (this loop still reverses).
   *       Otherwise return INTAKING.
   * </ol>
   */
  public CurrentState update(WantedState wanted, Inputs inputs) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** True while a jam is detected. Intake.getRumble() returns this value. */
  public boolean isJammed() {
    return jammed;
  }
}
