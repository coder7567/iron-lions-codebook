package frc.training.u10;

/**
 * Exercise u10-odometry: turning wheel and gyro readings into a field position, plus the wheel-radius
 * measurement our characterization command performs.
 *
 * <p>This is the first-order version of odometry: each sample's movement is rotated by the heading at
 * the start of the sample. WPILib uses a twist (the exponential map), which also accounts for the arc
 * traveled while the robot rotates. At our 100 Hz sample rate the difference is tiny, which is one
 * reason odometry samples fast.
 */
public final class OdometryMath {
  /** A field pose: meters and radians, with the heading counterclockwise-positive. */
  public record Pose(double x, double y, double headingRad) {}

  /** A 2D vector in meters. */
  public record Vector(double x, double y) {}

  /** DriveConstants.wheelRadiusMeters: 2 inches. */
  public static final double WHEEL_RADIUS_METERS = 0.0508;
  /** DriveConstants.driveBaseRadius for our 20 inch square. */
  public static final double DRIVE_BASE_RADIUS_METERS = Math.hypot(0.254, 0.254);

  private OdometryMath() {}

  /** Converts a drive encoder reading in wheel radians into meters rolled. */
  public static double positionMeters(double wheelRadians, double wheelRadiusMeters) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * How far one module moved, as a robot-frame vector: the change in its rolled distance, pointed along
   * the angle the module was turned to.
   */
  public static Vector moduleDelta(double previousMeters, double currentMeters, double moduleAngleRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The chassis's robot-frame movement: the average of the module deltas. */
  public static Vector chassisDelta(Vector[] moduleDeltas) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Applies a robot-frame movement to a pose and sets the new heading. The movement is rotated into the
   * field frame by the pose's heading before it is added.
   */
  public static Pose advance(Pose pose, Vector robotFrameDelta, double newHeadingRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The wheel radius measured by spinning the robot in place, as wheelRadiusCharacterization computes
   * it: the arc each wheel should have traveled, divided by the wheel rotation it reported.
   */
  public static double wheelRadiusFromSpin(
      double gyroDeltaRad, double driveBaseRadiusMeters, double averageWheelDeltaRad) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
