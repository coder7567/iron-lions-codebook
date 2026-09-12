package frc.training.u07;

/**
 * Exercise u07-joystick: the math inside DriveCommands.joystickDrive, in plain Java.
 *
 * <p>The x, y, and omega inputs are the values RobotContainer's suppliers return, already negated:
 * x = -leftY, y = -leftX, omega = -rightX. Angles are in radians, counterclockwise-positive.
 */
public final class DriverMath {
  /** DriveConstants.DEADBAND's default value. */
  public static final double DEADBAND = 0.1;
  /** DriveConstants.maxSpeedMetersPerSec. */
  public static final double MAX_SPEED_METERS_PER_SEC = 4.2;
  /** DriveConstants.driveBaseRadius: the distance from the robot's center to a module (20 in square). */
  public static final double DRIVE_BASE_RADIUS_METERS = Math.hypot(0.254, 0.254);

  /** Chassis speeds, like WPILib's ChassisSpeeds. */
  public record Speeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  private DriverMath() {}

  /** Provided: behaves like MathUtil.applyDeadband(value, deadband). */
  public static double applyDeadband(double value, double deadband) {
    if (Math.abs(value) <= deadband) {
      return 0.0;
    }
    return value > 0.0 ? (value - deadband) / (1.0 - deadband) : (value + deadband) / (1.0 - deadband);
  }

  /** Max rotation speed: max linear speed divided by the drive base radius, as in Drive. */
  public static double maxAngularSpeedRadPerSec() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Like getLinearVelocityFromJoysticks: apply the deadband to the stick's magnitude (not to each
   * axis), square the magnitude, and keep the direction. Returns {x, y} as fractions of max speed.
   */
  public static double[] linearVelocityFromJoysticks(double x, double y) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Applies the deadband to a rotation input, then squares it while keeping its sign. */
  public static double shapeRotation(double omega) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The field-relative speeds joystickDrive builds, scaled to max linear and angular speed. */
  public static Speeds fieldRelativeSpeeds(double x, double y, double omega) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Like ChassisSpeeds.fromFieldRelativeSpeeds: rotates the field-relative velocity vector by the
   * negative of the robot's heading. Omega is unchanged.
   */
  public static Speeds toRobotRelative(Speeds fieldRelative, double robotHeadingRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The robot-relative speeds joystickDrive sends to the drive. On the red alliance, π is added to the
   * heading before converting.
   */
  public static Speeds joystickDrive(double x, double y, double omega, double robotHeadingRad, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
