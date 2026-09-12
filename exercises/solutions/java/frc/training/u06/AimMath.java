package frc.training.u06;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;

/** Reference solution for exercise u06-aim-wpi. */
public final class AimMath {
  private AimMath() {}

  public static Rotation2d fieldAngleToTarget(Pose2d robot, Translation2d target) {
    return target.minus(robot.getTranslation()).getAngle();
  }

  public static double robotRelativeAngleToTarget(Pose2d robot, Translation2d target) {
    return MathUtil.angleModulus(fieldAngleToTarget(robot, target).minus(robot.getRotation()).getRadians());
  }

  public static double distanceToTarget(Pose2d robot, Translation2d target) {
    return robot.getTranslation().getDistance(target);
  }

  public static boolean inFieldOfView(Pose2d robot, Translation2d target, double fovRadians) {
    return Math.abs(robotRelativeAngleToTarget(robot, target)) <= fovRadians / 2.0;
  }
}
