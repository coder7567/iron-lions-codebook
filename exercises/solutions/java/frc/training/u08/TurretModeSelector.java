package frc.training.u08;

/** Reference solution for exercise u08-turret. */
public final class TurretModeSelector {
  public enum WantedState {
    IDLE,
    PAUSED,
    SHOOTING,
    TESTING
  }

  public enum CurrentState {
    IDLE,
    PAUSEDSHOOTING,
    PAUSEDPASSING,
    SHOOTING,
    PASSING,
    TESTING
  }

  public static final double FIELD_LENGTH = 16.541;
  public static final double ALLIANCE_ZONE_DEPTH = 5.0;

  private TurretModeSelector() {}

  public static double ourZoneEdgeX(boolean isRed) {
    return isRed ? FIELD_LENGTH - ALLIANCE_ZONE_DEPTH : ALLIANCE_ZONE_DEPTH;
  }

  public static double theirZoneEdgeX(boolean isRed) {
    return ourZoneEdgeX(!isRed);
  }

  public static boolean onOurSide(double robotX, double lineX, boolean isRed) {
    return isRed ? robotX > lineX : robotX < lineX;
  }

  public static CurrentState select(WantedState wanted, double robotX, boolean isRed) {
    return switch (wanted) {
      case IDLE -> CurrentState.IDLE;
      case TESTING -> CurrentState.TESTING;
      case SHOOTING ->
          onOurSide(robotX, ourZoneEdgeX(isRed), isRed) ? CurrentState.SHOOTING : CurrentState.PASSING;
      case PAUSED -> {
        if (onOurSide(robotX, ourZoneEdgeX(isRed), isRed)) {
          yield CurrentState.PAUSEDSHOOTING;
        } else if (onOurSide(robotX, theirZoneEdgeX(isRed), isRed)) {
          yield CurrentState.PAUSEDPASSING; // the original code yields PAUSEDSHOOTING here (F1)
        }
        yield CurrentState.IDLE;
      }
    };
  }
}
