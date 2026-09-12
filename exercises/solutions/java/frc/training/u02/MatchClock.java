package frc.training.u02;

/** Reference solution for exercise u02-matchclock. */
public final class MatchClock {
  private MatchClock() {}

  public static String periodName(boolean autonomous, double matchTime) {
    if (autonomous) {
      return "AUTO";
    } else if (matchTime > 130) {
      return "TRANSITION";
    } else if (matchTime > 105) {
      return "SHIFT 1";
    } else if (matchTime > 80) {
      return "SHIFT 2";
    } else if (matchTime > 55) {
      return "SHIFT 3";
    } else if (matchTime > 30) {
      return "SHIFT 4";
    } else {
      return "END GAME";
    }
  }

  public static int shiftNumber(double matchTime) {
    if (matchTime > 130 || matchTime <= 30) {
      return 0;
    } else if (matchTime > 105) {
      return 1;
    } else if (matchTime > 80) {
      return 2;
    } else if (matchTime > 55) {
      return 3;
    } else {
      return 4;
    }
  }

  public static double secondsLeftInPeriod(double matchTime) {
    if (matchTime <= 0) {
      return 0.0;
    } else if (matchTime > 130) {
      return matchTime - 130;
    } else if (matchTime > 105) {
      return matchTime - 105;
    } else if (matchTime > 80) {
      return matchTime - 80;
    } else if (matchTime > 55) {
      return matchTime - 55;
    } else if (matchTime > 30) {
      return matchTime - 30;
    } else {
      return matchTime;
    }
  }

  public static boolean warnHubChange(double matchTime) {
    return matchTime > 30 && secondsLeftInPeriod(matchTime) <= 3.0;
  }
}
