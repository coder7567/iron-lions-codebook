package frc.training.u03;

/**
 * Exercise u03-telemetry: turn robot data into clear text for dashboards, alerts, and logs.
 *
 * <p>Use {@code String.format(Locale.US, ...)} for numbers so the decimal point is always a period, no
 * matter what country the computer is set to. You can type the degree sign directly, because this project
 * compiles its Java files as UTF-8.
 */
public final class TelemetryText {
  private TelemetryText() {}

  /**
   * Formats a pose like {@code x=4.63 m, y=4.02 m, heading=90.0°}: meters with two decimals, and the heading
   * converted from radians to degrees with one decimal.
   */
  public static String formatPose(double xMeters, double yMeters, double headingRadians) {
    // TODO
    return "";
  }

  /** Formats a current with one decimal and a unit, like {@code 37.5 A}. */
  public static String formatCurrent(double amps) {
    // TODO
    return "";
  }

  /**
   * Returns the first 7 characters of a Git commit SHA in lowercase, the way GitHub abbreviates commits.
   * SHAs shorter than 7 characters are returned whole, in lowercase.
   */
  public static String shortSha(String sha) {
    // TODO
    return "";
  }

  /**
   * Summarizes a build for the log, like {@code fe04405 on main} or
   * {@code fe04405 on main (uncommitted changes)} when {@code dirty} is true.
   */
  public static String buildSummary(String sha, String branch, boolean dirty) {
    // TODO: reuse shortSha
    return "";
  }

  /**
   * Builds the alert text our Module class shows, like {@code Disconnected turn motor on module 3.}
   *
   * @param kind "drive" or "turn"
   */
  public static String disconnectedAlert(String kind, int moduleIndex) {
    // TODO
    return "";
  }

  /**
   * Returns the number at the end of a camera name like {@code April_Tag_2}. Returns -1 if the name is null,
   * does not start with exactly {@code April_Tag_}, or does not end in a whole number with only digits.
   */
  public static int cameraNumber(String cameraName) {
    // TODO: startsWith, substring, and Character.isDigit are useful here
    return 0;
  }
}
