package frc.training.u02;

/**
 * Exercise u02-deadband: the joystick shaping math inside {@code DriveCommands.joystickDrive}.
 *
 * <p>Write each method as a small, pure function: its result depends only on its parameters.
 */
public final class JoystickMath {
  private JoystickMath() {}

  /** Returns {@code value} limited to the range [{@code min}, {@code max}]. */
  public static double clamp(double value, double min, double max) {
    // TODO
    return 0.0;
  }

  /**
   * Applies a deadband the way WPILib's {@code MathUtil.applyDeadband(value, deadband)} does.
   *
   * <p>If {@code |value| <= deadband}, return 0. Otherwise rescale so the output starts at 0 just outside the
   * deadband and still reaches ±1 at full stick: {@code (value - deadband) / (1 - deadband)} for positive
   * values and {@code (value + deadband) / (1 - deadband)} for negative values.
   */
  public static double applyDeadband(double value, double deadband) {
    // TODO
    return 0.0;
  }

  /** Squares {@code value} but keeps its sign, so -0.5 becomes -0.25. */
  public static double squareKeepSign(double value) {
    // TODO
    return 0.0;
  }

  /**
   * Turns a rotation stick value into an angular velocity: apply the deadband, square while keeping the
   * sign, then scale by {@code maxRadPerSec}. Reuse your other methods.
   */
  public static double shapeRotation(double stick, double deadband, double maxRadPerSec) {
    // TODO
    return 0.0;
  }

  /** Returns how far a stick is pushed in any direction, sqrt(x² + y²), clamped to at most 1. */
  public static double stickMagnitude(double x, double y) {
    // TODO
    return 0.0;
  }
}
