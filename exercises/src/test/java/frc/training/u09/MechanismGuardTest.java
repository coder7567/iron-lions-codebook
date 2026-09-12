package frc.training.u09;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.training.u09.MechanismGuard.RateLimiter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MechanismGuardTest {
  private static final double EPSILON = 1e-9;
  /** TurretConstants.turretMinAngle and turretMaxAngle. */
  private static final double TURRET_MIN = -4.261;
  private static final double TURRET_MAX = 1.6;

  @Test
  @DisplayName("setpoints are clamped into the turret's soft limits")
  void clampSetpoint() {
    assertEquals(0.5, MechanismGuard.clampSetpoint(0.5, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(TURRET_MAX, MechanismGuard.clampSetpoint(2.0, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(TURRET_MIN, MechanismGuard.clampSetpoint(-5.0, TURRET_MIN, TURRET_MAX), EPSILON);
  }

  @Test
  @DisplayName("output is limited symmetrically, like the turret's 0.5 output range")
  void limitOutput() {
    assertEquals(0.5, MechanismGuard.limitOutput(0.9, 0.5), EPSILON);
    assertEquals(-0.5, MechanismGuard.limitOutput(-0.9, 0.5), EPSILON);
    assertEquals(0.3, MechanismGuard.limitOutput(0.3, 0.5), EPSILON);
    assertEquals(0.25, MechanismGuard.limitOutput(1.0, 0.25), EPSILON);
  }

  @Test
  @DisplayName("a soft limit blocks output that pushes further past the limit, but allows escape")
  void stopAtLimits() {
    assertEquals(0.0, MechanismGuard.stopAtLimits(0.4, TURRET_MAX, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(-0.4, MechanismGuard.stopAtLimits(-0.4, TURRET_MAX, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(0.0, MechanismGuard.stopAtLimits(-0.4, TURRET_MIN, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(0.4, MechanismGuard.stopAtLimits(0.4, TURRET_MIN, TURRET_MIN, TURRET_MAX), EPSILON);
    assertEquals(0.4, MechanismGuard.stopAtLimits(0.4, 0.0, TURRET_MIN, TURRET_MAX), EPSILON);
  }

  @Test
  @DisplayName("a rate limiter walks toward its input at a fixed rate per loop")
  void rateLimiter() {
    RateLimiter limiter = new RateLimiter(2.0, 0.0); // 2 units per second, 0.04 per 20 ms loop

    assertEquals(0.04, limiter.calculate(1.0, 0.02), EPSILON);
    assertEquals(0.08, limiter.calculate(1.0, 0.02), EPSILON);
    assertEquals(0.08 + 0.04, limiter.calculate(5.0, 0.02), EPSILON);

    // Small steps are followed exactly, in both directions.
    limiter.reset(0.5);
    assertEquals(0.52, limiter.calculate(0.52, 0.02), EPSILON);
    assertEquals(0.5, limiter.calculate(0.5, 0.02), EPSILON);
    assertEquals(0.46, limiter.calculate(-1.0, 0.02), EPSILON);
  }

  @Test
  @DisplayName("a rate limiter reaches its target after enough loops")
  void rateLimiterConverges() {
    RateLimiter limiter = new RateLimiter(2.0, 0.0);
    for (int loop = 0; loop < 25; loop++) {
      limiter.calculate(1.0, 0.02);
    }
    assertEquals(1.0, limiter.get(), 1e-9);
    assertEquals(1.0, limiter.calculate(1.0, 0.02), 1e-9);
  }
}
