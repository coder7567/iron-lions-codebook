package frc.training.u06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AimMathTest {
  private static final double EPSILON = 1e-9;
  private static final Translation2d BLUE_HUB = new Translation2d(4.625, 4.0);

  @Test
  @DisplayName("the lesson's worked example: robot at (2, 4) facing 90°")
  void workedExample() {
    Pose2d robot = new Pose2d(2.0, 4.0, Rotation2d.fromDegrees(90));
    assertEquals(0.0, AimMath.fieldAngleToTarget(robot, BLUE_HUB).getRadians(), EPSILON);
    assertEquals(-Math.PI / 2, AimMath.robotRelativeAngleToTarget(robot, BLUE_HUB), EPSILON);
    assertEquals(2.625, AimMath.distanceToTarget(robot, BLUE_HUB), EPSILON);
  }

  @Test
  @DisplayName("facing the HUB means a robot-relative angle of 0")
  void facingTarget() {
    Pose2d robot = new Pose2d(8.0, 4.0, Rotation2d.fromDegrees(180));
    assertEquals(0.0, AimMath.robotRelativeAngleToTarget(robot, BLUE_HUB), EPSILON);
    assertTrue(AimMath.inFieldOfView(robot, BLUE_HUB, Math.toRadians(70)));
  }

  @Test
  @DisplayName("a target directly behind is π away and out of view")
  void behind() {
    Pose2d robot = new Pose2d(2.0, 4.0, Rotation2d.fromDegrees(180));
    assertEquals(Math.PI, Math.abs(AimMath.robotRelativeAngleToTarget(robot, BLUE_HUB)), EPSILON);
    assertFalse(AimMath.inFieldOfView(robot, BLUE_HUB, Math.toRadians(70)));
  }

  @Test
  @DisplayName("angles wrap: heading -170° to a target at 170° is -20°")
  void wraps() {
    Pose2d robot = new Pose2d(0.0, 0.0, Rotation2d.fromDegrees(-170));
    Translation2d target = new Translation2d(Math.cos(Math.toRadians(170)), Math.sin(Math.toRadians(170)));
    assertEquals(Math.toRadians(-20), AimMath.robotRelativeAngleToTarget(robot, target), EPSILON);
  }

  @Test
  @DisplayName("field of view edges: 30° is inside ±35°, 40° is outside")
  void fieldOfView() {
    Pose2d robot = new Pose2d(0.0, 0.0, new Rotation2d());
    Translation2d at30 = new Translation2d(Math.cos(Math.toRadians(30)), Math.sin(Math.toRadians(30)));
    Translation2d at40 = new Translation2d(Math.cos(Math.toRadians(40)), Math.sin(Math.toRadians(40)));
    assertTrue(AimMath.inFieldOfView(robot, at30, Math.toRadians(70)));
    assertFalse(AimMath.inFieldOfView(robot, at40, Math.toRadians(70)));
  }
}
