package frc.training.u10;

/**
 * Exercise u10-optimize: the two adjustments Module.runSetpoint makes before commanding a module,
 * matching WPILib's SwerveModuleState.optimize and cosineScale.
 */
public final class ModuleOptimizer {
  /** A module's speed in meters per second and its angle in radians. */
  public record ModuleState(double speedMetersPerSec, double angleRad) {}

  private ModuleOptimizer() {}

  /** Provided: wraps an angle into [-pi, pi). */
  public static double wrapRadians(double angle) {
    double wrapped = (angle + Math.PI) % (2 * Math.PI);
    if (wrapped < 0) {
      wrapped += 2 * Math.PI;
    }
    return wrapped - Math.PI;
  }

  /** The shortest turn from the current angle to the desired one, wrapped into [-pi, pi). */
  public static double turnErrorRadians(double desiredAngleRad, double currentAngleRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * If the module would have to turn more than 90 degrees, turn the other way instead and drive
   * backward: the angle moves by pi (wrapped) and the speed is negated. A turn of exactly 90 degrees is
   * left alone, matching WPILib.
   */
  public static ModuleState optimize(ModuleState desired, double currentAngleRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Scales the speed by the cosine of the remaining turn error, so a module that is still turning
   * contributes less drive speed. The angle is unchanged.
   */
  public static ModuleState cosineScale(ModuleState state, double currentAngleRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
