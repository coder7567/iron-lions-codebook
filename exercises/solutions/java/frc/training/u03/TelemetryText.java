package frc.training.u03;

import java.util.Locale;

/** Reference solution for exercise u03-telemetry. */
public final class TelemetryText {
  private TelemetryText() {}

  public static String formatPose(double xMeters, double yMeters, double headingRadians) {
    return String.format(
        Locale.US, "x=%.2f m, y=%.2f m, heading=%.1f°", xMeters, yMeters, Math.toDegrees(headingRadians));
  }

  public static String formatCurrent(double amps) {
    return String.format(Locale.US, "%.1f A", amps);
  }

  public static String shortSha(String sha) {
    String lower = sha.toLowerCase(Locale.ROOT);
    return lower.length() <= 7 ? lower : lower.substring(0, 7);
  }

  public static String buildSummary(String sha, String branch, boolean dirty) {
    String summary = shortSha(sha) + " on " + branch;
    return dirty ? summary + " (uncommitted changes)" : summary;
  }

  public static String disconnectedAlert(String kind, int moduleIndex) {
    return "Disconnected " + kind + " motor on module " + moduleIndex + ".";
  }

  public static int cameraNumber(String cameraName) {
    String prefix = "April_Tag_";
    if (cameraName == null || !cameraName.startsWith(prefix) || cameraName.length() == prefix.length()) {
      return -1;
    }
    String digits = cameraName.substring(prefix.length());
    for (int i = 0; i < digits.length(); i++) {
      if (!Character.isDigit(digits.charAt(i))) {
        return -1;
      }
    }
    return Integer.parseInt(digits);
  }
}
