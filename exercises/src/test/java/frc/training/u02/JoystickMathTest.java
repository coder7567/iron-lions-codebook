package frc.training.u02;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class JoystickMathTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("clamp keeps values inside the range")
  void clamp() {
    assertEquals(1.0, JoystickMath.clamp(5, -1, 1), EPSILON);
    assertEquals(-1.0, JoystickMath.clamp(-3, -1, 1), EPSILON);
    assertEquals(0.3, JoystickMath.clamp(0.3, -1, 1), EPSILON);
  }

  @ParameterizedTest(name = "applyDeadband({0}, 0.1) = {1}")
  @CsvSource({
    "0.05, 0.0",
    "-0.1, 0.0",
    "0.1, 0.0",
    "0.55, 0.5",
    "-0.55, -0.5",
    "1.0, 1.0",
    "-1.0, -1.0",
    "0.0, 0.0"
  })
  void deadband(double input, double expected) {
    assertEquals(expected, JoystickMath.applyDeadband(input, 0.1), EPSILON);
  }

  @Test
  @DisplayName("squareKeepSign squares but keeps direction")
  void squareKeepSign() {
    assertEquals(0.25, JoystickMath.squareKeepSign(0.5), EPSILON);
    assertEquals(-0.25, JoystickMath.squareKeepSign(-0.5), EPSILON);
    assertEquals(0.0, JoystickMath.squareKeepSign(0.0), EPSILON);
  }

  @Test
  @DisplayName("shapeRotation = deadband, then signed square, then scale")
  void shapeRotation() {
    assertEquals(2.9225, JoystickMath.shapeRotation(0.55, 0.1, 11.69), 1e-9);
    assertEquals(-11.69, JoystickMath.shapeRotation(-1.0, 0.1, 11.69), 1e-9);
    assertEquals(0.0, JoystickMath.shapeRotation(0.05, 0.1, 11.69), EPSILON);
  }

  @Test
  @DisplayName("stickMagnitude measures a diagonal push and never exceeds 1")
  void stickMagnitude() {
    assertEquals(1.0, JoystickMath.stickMagnitude(0.6, 0.8), EPSILON);
    assertEquals(0.5, JoystickMath.stickMagnitude(0.3, -0.4), EPSILON);
    assertEquals(1.0, JoystickMath.stickMagnitude(1.0, 1.0), EPSILON);
  }

  @Test
  @DisplayName("the 'Move Forward' auto's stick value really commands about 0.78 m/s, not 1.0")
  void moveForwardAutoSpeed() {
    double stick = Math.sqrt(1 / 4.2);
    double metersPerSecond = JoystickMath.squareKeepSign(JoystickMath.applyDeadband(stick, 0.1)) * 4.2;
    assertEquals(0.7804, metersPerSecond, 1e-3);
  }
}
