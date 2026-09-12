package frc.training.u08;

/** Reference solution for exercise u08-hub. */
public final class HubSchedule {
  public enum Alliance {
    RED,
    BLUE
  }

  public enum Mode {
    DISABLED,
    AUTONOMOUS,
    TELEOP
  }

  public record Status(boolean hubActive, double secondsLeftInPeriod) {}

  private HubSchedule() {}

  public static boolean redInactiveFirst(String gameData) {
    return gameData != null && !gameData.isEmpty() && gameData.charAt(0) == 'R';
  }

  public static Status status(double matchTime, String gameData, Alliance alliance, Mode mode) {
    if (alliance == null) {
      return new Status(false, 0.0);
    }
    if (mode == Mode.AUTONOMOUS) {
      return new Status(true, matchTime);
    }
    if (mode != Mode.TELEOP) {
      return new Status(false, 0.0);
    }

    boolean redInactiveFirst = redInactiveFirst(gameData);
    boolean shift1Active = alliance == Alliance.RED ? !redInactiveFirst : redInactiveFirst;

    if (matchTime > 130) {
      return new Status(true, matchTime - 130); // transition
    } else if (matchTime > 105) {
      return new Status(shift1Active, matchTime - 105); // shift 1
    } else if (matchTime > 80) {
      return new Status(!shift1Active, matchTime - 80); // shift 2
    } else if (matchTime > 55) {
      return new Status(shift1Active, matchTime - 55); // shift 3
    } else if (matchTime > 30) {
      return new Status(!shift1Active, matchTime - 30); // shift 4
    }
    return new Status(true, matchTime); // end game
  }
}
