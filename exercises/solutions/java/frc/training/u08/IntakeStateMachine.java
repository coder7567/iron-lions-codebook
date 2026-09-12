package frc.training.u08;

/** Reference solution for exercise u08-intake. */
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

  public record Inputs(double intakeCurrentAmps, double intakeSpeedRpm, double armAngle, double armSetAngle) {}

  public static final double JAM_CURRENT_AMPS = 35;
  public static final double JAM_SPEED_RPM = 1000;
  public static final int JAM_MIN_COUNT = 50;
  public static final int UNJAM_MIN_COUNT = 10;
  public static final double ARM_OUT_TOLERANCE = 0.25;

  private int jamCount = 0;
  private int unjamCount = 0;
  private boolean jammed = false;

  public CurrentState update(WantedState wanted, Inputs inputs) {
    if (wanted != WantedState.INTAKING) {
      jamCount = 0;
      unjamCount = 0;
      jammed = false;
    }

    return switch (wanted) {
      case IDLE -> CurrentState.IDLE;
      case PAUSED -> CurrentState.PAUSED;
      case REVERSING -> CurrentState.REVERSING;
      case TESTING -> CurrentState.INTAKING;
      case INTAKING -> intaking(inputs);
    };
  }

  private CurrentState intaking(Inputs inputs) {
    boolean jamLike =
        inputs.intakeCurrentAmps() > JAM_CURRENT_AMPS && inputs.intakeSpeedRpm() < JAM_SPEED_RPM;
    if (jamLike && !jammed) {
      jamCount++;
    } else {
      jamCount = 0;
    }
    if (jamCount >= JAM_MIN_COUNT) {
      jammed = true;
    }

    boolean armOut = Math.abs(inputs.armAngle() - inputs.armSetAngle()) < ARM_OUT_TOLERANCE;
    if (jammed && armOut) {
      unjamCount++;
      if (unjamCount >= UNJAM_MIN_COUNT) {
        jammed = false;
        jamCount = 0;
        unjamCount = 0;
      }
      return CurrentState.REVERSING;
    }
    return CurrentState.INTAKING;
  }

  public boolean isJammed() {
    return jammed;
  }
}
