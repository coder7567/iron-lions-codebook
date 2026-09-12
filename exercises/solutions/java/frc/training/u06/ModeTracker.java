package frc.training.u06;

/** Reference solution for exercise u06-looptiming, part 2. */
public class ModeTracker {
  private String previousMode = null;

  public static String modeName(boolean enabled, boolean autonomous, boolean test) {
    if (!enabled) {
      return "disabled";
    } else if (test) {
      return "test";
    } else if (autonomous) {
      return "autonomous";
    } else {
      return "teleop";
    }
  }

  public String update(boolean enabled, boolean autonomous, boolean test) {
    String mode = modeName(enabled, autonomous, test);
    if (mode.equals(previousMode)) {
      return null;
    }
    previousMode = mode;
    return mode + "Init";
  }

  public String currentMode() {
    return previousMode == null ? "unknown" : previousMode;
  }
}
