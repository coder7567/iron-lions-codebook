package frc.training.u13;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.util.Units;

/**
 * Exercise u13-transform-wpi: the transform chain AprilTagIOPhotonVision uses to turn a tag sighting
 * into a robot pose.
 *
 * <p>This one uses WPILib geometry, so run it with Gradle: ./gradlew test --tests
 * "frc.training.u13.CameraTransformsTest"
 */
public final class CameraTransforms {
  private CameraTransforms() {}

  /**
   * VisionConstants.AprilTagCamera1Transform: 9.733 inches back, 9.733 inches right, 9.314 inches up,
   * pitched 30 degrees up and yawed -135 degrees.
   */
  public static Transform3d cameraOneTransform() {
    return new Transform3d(
        Units.inchesToMeters(-9.733),
        Units.inchesToMeters(-9.733),
        Units.inchesToMeters(9.314),
        new Rotation3d(0.0, Units.degreesToRadians(-30.0), Units.degreesToRadians(-135.0)));
  }

  /**
   * Given where the camera is on the field and where the camera sits on the robot, returns the robot's
   * pose. This is the multi-tag path: fieldToCamera plus the inverse of robotToCamera.
   */
  public static Pose3d robotPoseFromCamera(Transform3d fieldToCamera, Transform3d robotToCamera) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Given a tag's known field pose and the camera's measurement of where that tag is relative to itself,
   * returns where the camera is on the field.
   */
  public static Transform3d fieldToCamera(Pose3d tagPose, Transform3d cameraToTarget) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The whole single-tag solve: tag pose, camera-to-tag measurement, and camera mounting. */
  public static Pose3d robotPoseFromTag(
      Pose3d tagPose, Transform3d cameraToTarget, Transform3d robotToCamera) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
