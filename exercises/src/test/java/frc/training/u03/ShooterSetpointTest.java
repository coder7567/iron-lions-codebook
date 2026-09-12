package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShooterSetpointTest {
  private static final double EPSILON = 1e-9;
  // Two neighboring rows of the team's shot map (2.0 m and 2.38 m), with the 0.291 hood offset applied.
  private final ShooterSetpoint near = new ShooterSetpoint(2000, 0.879);
  private final ShooterSetpoint far = new ShooterSetpoint(2050, 0.841);

  @Test
  @DisplayName("the constructor stores both values")
  void storesValues() {
    assertEquals(2000.0, near.getRpm(), EPSILON);
    assertEquals(0.879, near.getHoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("interpolate at t = 0.5 lands halfway")
  void halfway() {
    ShooterSetpoint middle = near.interpolate(far, 0.5);
    assertEquals(2025.0, middle.getRpm(), EPSILON);
    assertEquals(0.860, middle.getHoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("interpolate at t = 0 and t = 1 returns the endpoints' values")
  void endpoints() {
    assertEquals(2000.0, near.interpolate(far, 0.0).getRpm(), EPSILON);
    assertEquals(2050.0, near.interpolate(far, 1.0).getRpm(), EPSILON);
    assertEquals(0.841, near.interpolate(far, 1.0).getHoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("t outside 0..1 is clamped")
  void clamps() {
    assertEquals(2050.0, near.interpolate(far, 1.5).getRpm(), EPSILON);
    assertEquals(2000.0, near.interpolate(far, -0.2).getRpm(), EPSILON);
  }

  @Test
  @DisplayName("interpolate returns a new object and changes neither input")
  void interpolateIsImmutable() {
    ShooterSetpoint middle = near.interpolate(far, 0.25);
    assertNotSame(near, middle);
    assertEquals(2000.0, near.getRpm(), EPSILON);
    assertEquals(2050.0, far.getRpm(), EPSILON);
  }

  @Test
  @DisplayName("withRpm returns a changed copy and leaves the original alone")
  void withRpm() {
    ShooterSetpoint faster = near.withRpm(2500);
    assertNotSame(near, faster);
    assertEquals(2500.0, faster.getRpm(), EPSILON);
    assertEquals(0.879, faster.getHoodAngle(), EPSILON);
    assertEquals(2000.0, near.getRpm(), EPSILON);
  }

  @Test
  @DisplayName("toString uses the exact format")
  void readableText() {
    assertEquals("ShooterSetpoint[rpm=2000.0, hoodAngle=0.879]", near.toString());
  }
}
