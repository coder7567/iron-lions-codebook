package frc.training.u10;

/**
 * Exercise u10-kinematics: swerve kinematics for our 20 inch square drivetrain, in plain Java.
 *
 * <p>Inverse kinematics turns chassis speeds into four module states. Forward kinematics turns four
 * measured module states back into chassis speeds. Desaturation scales a set of module speeds down when
 * any of them exceeds what the drivetrain can do.
 */
public final class SwerveMath {
  /** A module's speed in meters per second and its angle in radians. */
  public record ModuleState(double speedMetersPerSec, double angleRad) {}

  /** Robot-relative chassis speeds: +x is forward, +y is left, +omega is counterclockwise. */
  public record ChassisSpeeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  /** Where a module sits relative to the robot's center, in meters. */
  public record ModuleLocation(double x, double y) {}

  /** Our modules, in the order Drive uses them: front left, front right, back left, back right. */
  public static final ModuleLocation[] MODULE_LOCATIONS = {
    new ModuleLocation(0.254, 0.254),
    new ModuleLocation(0.254, -0.254),
    new ModuleLocation(-0.254, 0.254),
    new ModuleLocation(-0.254, -0.254)
  };

  private SwerveMath() {}

  /**
   * Inverse kinematics. Each module's velocity is the chassis velocity plus the velocity from rotating
   * about the center: vx_i = vx - omega * y_i and vy_i = vy + omega * x_i. The module's speed is the
   * magnitude of that vector and its angle is its direction.
   */
  public static ModuleState[] toModuleStates(ChassisSpeeds speeds, ModuleLocation[] locations) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Forward kinematics. The chassis velocity is the average of the module velocity vectors, and the
   * rotation is the least-squares solution: sum(x_i * vy_i - y_i * vx_i) / sum(x_i^2 + y_i^2).
   */
  public static ChassisSpeeds toChassisSpeeds(ModuleState[] states, ModuleLocation[] locations) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Scales every module speed by the same factor when the fastest one exceeds maxSpeed, so the ratios
   * between modules (and therefore the robot's direction of travel) are preserved. Angles are unchanged.
   */
  public static ModuleState[] desaturate(ModuleState[] states, double maxSpeed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
