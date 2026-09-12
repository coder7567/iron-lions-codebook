package frc.training.u02;

/**
 * Exercise u02-matchclock: turn the Driver Station's match time into REBUILT periods.
 *
 * <p>In TELEOP, {@code DriverStation.getMatchTime()} counts down from about 140 seconds. Use the same
 * boundaries our Superstructure uses: more than 130 s is the TRANSITION, more than 105 s is SHIFT 1, more
 * than 80 s is SHIFT 2, more than 55 s is SHIFT 3, more than 30 s is SHIFT 4, and anything else is the
 * END GAME. In simulation or practice without a field, match time can be negative.
 */
public final class MatchClock {
  private MatchClock() {}

  /**
   * Names the current period.
   *
   * @param autonomous true while the robot is in autonomous
   * @param matchTime seconds remaining, as reported by the Driver Station
   * @return "AUTO" in autonomous; otherwise "TRANSITION", "SHIFT 1" through "SHIFT 4", or "END GAME"
   */
  public static String periodName(boolean autonomous, double matchTime) {
    // TODO
    return "";
  }

  /**
   * Returns which TELEOP shift is running: 1, 2, 3, or 4. Returns 0 during the TRANSITION and END GAME.
   */
  public static int shiftNumber(double matchTime) {
    // TODO
    return 0;
  }

  /**
   * Returns the seconds left before the current TELEOP period ends. For example, at 120 s the robot is in
   * SHIFT 1, which ends at 105 s, so this returns 15. In the END GAME it returns the time left in the match.
   * Never returns a negative number.
   */
  public static double secondsLeftInPeriod(double matchTime) {
    // TODO
    return 0.0;
  }

  /**
   * Returns true when a HUB change is 3 seconds away or less: during the TRANSITION or any SHIFT, with 3 or
   * fewer seconds left in that period. Always false in the END GAME.
   */
  public static boolean warnHubChange(double matchTime) {
    // TODO: reuse secondsLeftInPeriod
    return false;
  }
}
