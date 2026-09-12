package frc.training.u08;

/**
 * Exercise u08-hub: REBUILT's HUB schedule from Superstructure.updateHubStatusAndPeriod, rewritten as a
 * pure function. It makes no DriverStation calls and keeps no state between calls.
 */
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

  /** Whether our HUB is active, and the seconds left in the current period. */
  public record Status(boolean hubActive, double secondsLeftInPeriod) {}

  private HubSchedule() {}

  /**
   * Returns true when the game data says red's HUB is inactive first, meaning its first character is 'R'.
   * Like our code, anything else returns false, including null and an empty string.
   */
  public static boolean redInactiveFirst(String gameData) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Returns the HUB status, following our code's rules in order:
   *
   * <ol>
   *   <li>Unknown alliance (null): inactive, 0 s left.
   *   <li>AUTONOMOUS: active, matchTime s left.
   *   <li>Not TELEOP: inactive, 0 s left.
   *   <li>TELEOP, by matchTime: above 130 is the transition (active, matchTime - 130 left); above 105 is
   *       shift 1 (matchTime - 105 left); above 80 is shift 2 (matchTime - 80); above 55 is shift 3
   *       (matchTime - 55); above 30 is shift 4 (matchTime - 30); otherwise the end game (active, matchTime
   *       left).
   *   <li>Our HUB is active in shift 1 unless our alliance is the one inactive first. Shifts 2, 3, and 4
   *       alternate from there.
   * </ol>
   */
  public static Status status(double matchTime, String gameData, Alliance alliance, Mode mode) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
