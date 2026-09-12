package frc.training.u09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MiniPidTest {
  private static final double EPSILON = 1e-9;
  private static final double DT = 0.02;

  @Test
  @DisplayName("P alone scales the error")
  void proportional() {
    MiniPid pid = new MiniPid(2.0, 0.0, 0.0);
    pid.setSetpoint(10.0);

    assertEquals(12.0, pid.calculate(4.0, DT), EPSILON);
    assertEquals(0.0, pid.calculate(10.0, DT), EPSILON);
    assertEquals(-4.0, pid.calculate(12.0, DT), EPSILON);
  }

  @Test
  @DisplayName("D reacts to how fast the error is changing, and is 0 on the first loop")
  void derivative() {
    MiniPid pid = new MiniPid(0.0, 0.0, 0.5);
    pid.setSetpoint(10.0);

    assertEquals(0.0, pid.calculate(0.0, DT), EPSILON); // error 10, no previous error yet
    assertEquals(-25.0, pid.calculate(1.0, DT), EPSILON); // error 9: (9 - 10) / 0.02 * 0.5
  }

  @Test
  @DisplayName("I accumulates error over time and respects the integrator range")
  void integral() {
    MiniPid pid = new MiniPid(0.0, 1.0, 0.0);
    pid.setSetpoint(2.0);

    assertEquals(1.0, pid.calculate(0.0, 0.5), EPSILON);
    assertEquals(2.0, pid.calculate(0.0, 0.5), EPSILON);

    MiniPid clamped = new MiniPid(0.0, 1.0, 0.0);
    clamped.setSetpoint(2.0);
    clamped.setIntegratorRange(-1.0, 1.0);
    assertEquals(1.0, clamped.calculate(0.0, 0.5), EPSILON);
    assertEquals(1.0, clamped.calculate(0.0, 0.5), EPSILON);
  }

  @Test
  @DisplayName("continuous input takes the short way around, like the turn motors")
  void continuousInput() {
    MiniPid forward = new MiniPid(1.0, 0.0, 0.0);
    forward.enableContinuousInput(0.0, 2 * Math.PI);
    forward.setSetpoint(0.1);
    // 6.2 rad is just short of a full turn, so the short way to 0.1 is a small positive move.
    assertEquals(0.183185307, forward.calculate(6.2, DT), 1e-6);

    MiniPid backward = new MiniPid(1.0, 0.0, 0.0);
    backward.enableContinuousInput(0.0, 2 * Math.PI);
    backward.setSetpoint(6.2);
    assertEquals(-0.183185307, backward.calculate(0.1, DT), 1e-6);
  }

  @Test
  @DisplayName("atSetpoint uses the tolerance and is false before the first calculate")
  void atSetpoint() {
    MiniPid pid = new MiniPid(1.0, 0.0, 0.0);
    pid.setTolerance(0.05);
    pid.setSetpoint(1.0);

    assertFalse(pid.atSetpoint());
    pid.calculate(0.5, DT);
    assertFalse(pid.atSetpoint());
    pid.calculate(0.98, DT);
    assertTrue(pid.atSetpoint());
  }

  @Test
  @DisplayName("reset clears the integral and the remembered error")
  void reset() {
    MiniPid pid = new MiniPid(0.0, 1.0, 0.5);
    pid.setSetpoint(2.0);
    pid.calculate(0.0, 0.5);
    pid.calculate(0.0, 0.5);

    pid.reset();

    // Same first call as a brand new controller: integral restarted, no derivative kick.
    assertEquals(1.0, pid.calculate(0.0, 0.5), EPSILON);
    assertFalse(pid.atSetpoint());
  }
}
