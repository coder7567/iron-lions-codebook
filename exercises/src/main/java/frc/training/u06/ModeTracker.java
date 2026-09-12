package frc.training.u06;

/**
 * Exercise u06-looptiming, part 2: decide which init method WPILib calls when the Driver Station changes the
 * robot's mode.
 */
public class ModeTracker {
  // TODO: remember the previous mode name

  /**
   * Returns the mode name for a Driver Station state: "disabled" when not enabled; otherwise "test" when test
   * is true; otherwise "autonomous" when autonomous is true; otherwise "teleop".
   */
  public static String modeName(boolean enabled, boolean autonomous, boolean test) {
    // TODO
    return "";
  }

  /**
   * Call once per loop. Returns "disabledInit", "autonomousInit", "teleopInit", or "testInit" when this call's
   * mode differs from the previous call's mode, or null when the mode has not changed. The very first call
   * always returns the init method for its mode.
   */
  public String update(boolean enabled, boolean autonomous, boolean test) {
    // TODO
    return "";
  }

  /** Returns the mode name from the most recent update, or "unknown" before the first update. */
  public String currentMode() {
    // TODO
    return "";
  }
}
