package frc.training.u13;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.util.Units;

/** Reference solution for exercise u13-transform-wpi. */
public final class CameraTransforms {
  private CameraTransforms() {}

  public static Transform3d cameraOneTransform() {
    return new Transform3d(
        Units.inchesToMeters(-9.733),
        Units.inchesToMeters(-9.733),
        Units.inchesToMeters(9.314),
        new Rotation3d(0.0, Units.degreesToRadians(-30.0), Units.degreesToRadians(-135.0)));
  }

  public static Pose3d robotPoseFromCamera(Transform3d fieldToCamera, Transform3d robotToCamera) {
    Transform3d fieldToRobot = fieldToCamera.plus(robotToCamera.inverse());
    return new Pose3d(fieldToRobot.getTranslation(), fieldToRobot.getRotation());
  }

  public static Transform3d fieldToCamera(Pose3d tagPose, Transform3d cameraToTarget) {
    Transform3d fieldToTarget = new Transform3d(tagPose.getTranslation(), tagPose.getRotation());
    return fieldToTarget.plus(cameraToTarget.inverse());
  }

  public static Pose3d robotPoseFromTag(
      Pose3d tagPose, Transform3d cameraToTarget, Transform3d robotToCamera) {
    return robotPoseFromCamera(fieldToCamera(tagPose, cameraToTarget), robotToCamera);
  }
}
