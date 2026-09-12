package frc.training.u10;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.training.u10.OdometryMath.Pose;
import frc.training.u10.OdometryMath.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class OdometryMathTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("wheel radians become meters through the wheel radius")
  void positionMeters() {
    assertEquals(0.508, OdometryMath.positionMeters(10.0, OdometryMath.WHEEL_RADIUS_METERS), EPSILON);
    assertEquals(0.0, OdometryMath.positionMeters(0.0, OdometryMath.WHEEL_RADIUS_METERS), EPSILON);
    assertEquals(-0.254, OdometryMath.positionMeters(-5.0, OdometryMath.WHEEL_RADIUS_METERS), EPSILON);
  }

  @Test
  @DisplayName("a module delta points along the module's angle")
  void moduleDelta() {
    Vector forward = OdometryMath.moduleDelta(1.0, 1.5, 0.0);
    assertEquals(0.5, forward.x(), EPSILON);
    assertEquals(0.0, forward.y(), EPSILON);

    Vector left = OdometryMath.moduleDelta(1.0, 1.5, Math.PI / 2);
    assertEquals(0.0, left.x(), 1e-9);
    assertEquals(0.5, left.y(), EPSILON);

    Vector backward = OdometryMath.moduleDelta(2.0, 1.75, 0.0);
    assertEquals(-0.25, backward.x(), EPSILON);
  }

  @Test
  @DisplayName("the chassis delta averages the four modules")
  void chassisDelta() {
    Vector[] deltas = {
      new Vector(0.4, 0.0), new Vector(0.6, 0.0), new Vector(0.5, 0.2), new Vector(0.5, -0.2)
    };

    Vector chassis = OdometryMath.chassisDelta(deltas);

    assertEquals(0.5, chassis.x(), EPSILON);
    assertEquals(0.0, chassis.y(), EPSILON);
  }

  @Test
  @DisplayName("advancing a pose rotates the robot-frame movement into the field")
  void advance() {
    // Facing +Y (90 degrees), moving 0.5 m out its own front.
    Pose moved = OdometryMath.advance(new Pose(2.0, 3.0, Math.PI / 2), new Vector(0.5, 0.0), Math.PI / 2);
    assertEquals(2.0, moved.x(), 1e-9);
    assertEquals(3.5, moved.y(), 1e-9);
    assertEquals(Math.PI / 2, moved.headingRad(), EPSILON);

    // Facing +X, moving 0.5 m out its front and 0.25 m to its left.
    Pose diagonal = OdometryMath.advance(new Pose(0.0, 0.0, 0.0), new Vector(0.5, 0.25), 0.1);
    assertEquals(0.5, diagonal.x(), EPSILON);
    assertEquals(0.25, diagonal.y(), EPSILON);
    assertEquals(0.1, diagonal.headingRad(), EPSILON);
  }

  @Test
  @DisplayName("several samples add up to a path")
  void repeatedSamples() {
    Pose pose = new Pose(0.0, 0.0, 0.0);
    for (int i = 0; i < 4; i++) {
      pose = OdometryMath.advance(pose, new Vector(0.25, 0.0), 0.0);
    }

    assertEquals(1.0, pose.x(), EPSILON);
    assertEquals(0.0, pose.y(), EPSILON);
  }

  @Test
  @DisplayName("spinning in place measures the wheel radius")
  void wheelRadiusFromSpin() {
    double radius = OdometryMath.WHEEL_RADIUS_METERS;
    double wheelDelta = 2 * Math.PI * OdometryMath.DRIVE_BASE_RADIUS_METERS / radius;

    assertEquals(
        radius,
        OdometryMath.wheelRadiusFromSpin(2 * Math.PI, OdometryMath.DRIVE_BASE_RADIUS_METERS, wheelDelta),
        1e-12);

    // A worn wheel reports more rotation for the same spin, so the measured radius is smaller.
    double wornWheelDelta = wheelDelta * 1.05;
    assertEquals(
        radius / 1.05,
        OdometryMath.wheelRadiusFromSpin(
            2 * Math.PI, OdometryMath.DRIVE_BASE_RADIUS_METERS, wornWheelDelta),
        1e-12);
  }
}
