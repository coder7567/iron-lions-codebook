package frc.training.u13;

import static org.junit.jupiter.api.Assertions.assertEquals;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CameraTransformsTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("the camera one transform matches VisionConstants")
  void cameraOne() {
    Transform3d transform = CameraTransforms.cameraOneTransform();

    assertEquals(Units.inchesToMeters(-9.733), transform.getX(), EPSILON);
    assertEquals(Units.inchesToMeters(-9.733), transform.getY(), EPSILON);
    assertEquals(Units.inchesToMeters(9.314), transform.getZ(), EPSILON);
    assertEquals(Units.degreesToRadians(-135.0), transform.getRotation().getZ(), 1e-9);
  }

  @Test
  @DisplayName("a camera one meter ahead of the robot puts the robot one meter behind it")
  void robotBehindCamera() {
    Transform3d robotToCamera = new Transform3d(new Translation3d(1.0, 0.0, 0.5), new Rotation3d());
    Transform3d fieldToCamera = new Transform3d(new Translation3d(5.0, 2.0, 0.5), new Rotation3d());

    Pose3d robot = CameraTransforms.robotPoseFromCamera(fieldToCamera, robotToCamera);

    assertEquals(4.0, robot.getX(), 1e-9);
    assertEquals(2.0, robot.getY(), 1e-9);
    assertEquals(0.0, robot.getZ(), 1e-9);
  }

  @Test
  @DisplayName("a tag two meters in front of the camera puts the camera two meters from the tag")
  void cameraFromTag() {
    Pose3d tagPose = new Pose3d(new Translation3d(8.0, 4.0, 1.0), new Rotation3d(0.0, 0.0, Math.PI));
    Transform3d cameraToTarget = new Transform3d(new Translation3d(2.0, 0.0, 0.0), new Rotation3d());

    Transform3d fieldToCamera = CameraTransforms.fieldToCamera(tagPose, cameraToTarget);

    assertEquals(10.0, fieldToCamera.getX(), 1e-9);
    assertEquals(4.0, fieldToCamera.getY(), 1e-9);
    assertEquals(1.0, fieldToCamera.getZ(), 1e-9);
  }

  @Test
  @DisplayName("the full single-tag solve finds the robot")
  void fullSolve() {
    // A tag on the far wall, facing back down the field.
    Pose3d tagPose = new Pose3d(new Translation3d(8.0, 4.0, 1.0), new Rotation3d(0.0, 0.0, Math.PI));
    // The camera sees it 2 m straight ahead.
    Transform3d cameraToTarget = new Transform3d(new Translation3d(2.0, 0.0, 0.0), new Rotation3d());
    // The camera is 0.25 m ahead of the robot's center, at the same height as the tag.
    Transform3d robotToCamera = new Transform3d(new Translation3d(0.25, 0.0, 1.0), new Rotation3d());

    Pose3d robot = CameraTransforms.robotPoseFromTag(tagPose, cameraToTarget, robotToCamera);

    assertEquals(9.75, robot.getX(), 1e-9);
    assertEquals(4.0, robot.getY(), 1e-9);
    assertEquals(0.0, robot.getZ(), 1e-9);
  }
}
