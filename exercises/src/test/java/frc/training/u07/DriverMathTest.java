package frc.training.u07;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DriverMathTest {
  private static final double EPSILON = 1e-6;

  @Test
  @DisplayName("max angular speed is 4.2 m/s divided by the 0.359 m drive base radius")
  void maxAngularSpeed() {
    assertEquals(11.6923, DriverMath.maxAngularSpeedRadPerSec(), 1e-4);
  }

  @Test
  @DisplayName("the deadband is circular: small diagonals still move the robot slightly, tiny inputs don't")
  void circularDeadband() {
    double[] tiny = DriverMath.linearVelocityFromJoysticks(0.05, 0.05);
    assertEquals(0.0, tiny[0], EPSILON);
    assertEquals(0.0, tiny[1], EPSILON);

    // Each axis is below 0.1, but the magnitude (0.127) is above it.
    double[] diagonal = DriverMath.linearVelocityFromJoysticks(0.09, 0.09);
    assertEquals(0.00064962, diagonal[0], 1e-7);
    assertEquals(0.00064962, diagonal[1], 1e-7);
  }

  @Test
  @DisplayName("the magnitude is deadbanded and squared, and the direction is kept")
  void linearShaping() {
    double[] half = DriverMath.linearVelocityFromJoysticks(0.3, 0.4);
    assertEquals(0.1185185, half[0], EPSILON);
    assertEquals(0.1580247, half[1], EPSILON);

    double[] full = DriverMath.linearVelocityFromJoysticks(0.6, 0.8);
    assertEquals(0.6, full[0], EPSILON);
    assertEquals(0.8, full[1], EPSILON);
  }

  @Test
  @DisplayName("rotation is deadbanded and squared with its sign kept")
  void rotationShaping() {
    assertEquals(0.0, DriverMath.shapeRotation(0.08), EPSILON);
    assertEquals(-0.1975309, DriverMath.shapeRotation(-0.5), EPSILON);
    assertEquals(1.0, DriverMath.shapeRotation(1.0), EPSILON);
  }

  @Test
  @DisplayName("full stick gives max linear and angular speed")
  void fieldRelativeSpeeds() {
    DriverMath.Speeds speeds = DriverMath.fieldRelativeSpeeds(1.0, 0.0, -1.0);
    assertEquals(4.2, speeds.vxMetersPerSec(), EPSILON);
    assertEquals(0.0, speeds.vyMetersPerSec(), EPSILON);
    assertEquals(-11.6923, speeds.omegaRadPerSec(), 1e-4);
  }

  @Test
  @DisplayName("the Move Forward auto's inputs give about 0.78 m/s along field -Y, not 1 m/s forward")
  void moveForwardAuto() {
    DriverMath.Speeds speeds = DriverMath.fieldRelativeSpeeds(0.0, -Math.sqrt(1 / 4.2), 0.0);
    assertEquals(0.0, speeds.vxMetersPerSec(), EPSILON);
    assertEquals(-0.7804, speeds.vyMetersPerSec(), 1e-3);
    assertEquals(0.0, speeds.omegaRadPerSec(), EPSILON);
  }

  @Test
  @DisplayName("a robot facing +Y moves to its right (robot -Y) to drive along field +X")
  void toRobotRelative() {
    DriverMath.Speeds robot = DriverMath.toRobotRelative(new DriverMath.Speeds(1.0, 0.0, 0.5), Math.PI / 2);
    assertEquals(0.0, robot.vxMetersPerSec(), EPSILON);
    assertEquals(-1.0, robot.vyMetersPerSec(), EPSILON);
    assertEquals(0.5, robot.omegaRadPerSec(), EPSILON);
  }

  @Test
  @DisplayName("pushing forward drives away from the driver on both alliances, thanks to the red flip")
  void allianceFlip() {
    // Blue robot facing away from the blue wall (heading 0): full speed forward.
    assertEquals(4.2, DriverMath.joystickDrive(1.0, 0.0, 0.0, 0.0, false).vxMetersPerSec(), EPSILON);

    // Red robot facing away from the red wall (heading π): still full speed forward.
    DriverMath.Speeds red = DriverMath.joystickDrive(1.0, 0.0, 0.0, Math.PI, true);
    assertEquals(4.2, red.vxMetersPerSec(), EPSILON);
    assertEquals(0.0, red.vyMetersPerSec(), EPSILON);

    // Without the flip, the same red robot would drive backward, toward its driver.
    assertEquals(-4.2, DriverMath.joystickDrive(1.0, 0.0, 0.0, Math.PI, false).vxMetersPerSec(), EPSILON);
  }
}
