package frc.training.u06;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/**
 * Exercise u06-aim-wpi: the aiming math from Turret.calculationToTarget, using WPILib geometry classes.
 *
 * <p>This exercise uses WPILib, so it only builds inside the GradleRIO exercises project.
 */
public final class AimMath {
  private AimMath() {}

  /** Returns the field-relative direction from the robot's position to the target. */
  public static Rotation2d fieldAngleToTarget(Pose2d robot, Translation2d target) {
    // TODO
    return new Rotation2d();
  }

  /**
   * Returns the target's direction relative to the robot's heading, in radians wrapped into (-π, π], using
   * {@code MathUtil.angleModulus}.
   */
  public static double robotRelativeAngleToTarget(Pose2d robot, Translation2d target) {
    // TODO
    return Double.NaN;
  }

  /** Returns the straight-line distance from the robot to the target, in meters. */
  public static double distanceToTarget(Pose2d robot, Translation2d target) {
    // TODO
    return Double.NaN;
  }

  /**
   * Returns true when the target is within half of {@code fovRadians} on either side of straight ahead, like a
   * front-facing camera with that field of view.
   */
  public static boolean inFieldOfView(Pose2d robot, Translation2d target, double fovRadians) {
    // TODO
    return false;
  }
}
