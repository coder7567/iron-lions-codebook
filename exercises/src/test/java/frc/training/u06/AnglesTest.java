package frc.training.u06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AnglesTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("wrapRadians lands in (-π, π]")
  void wrapRadians() {
    assertEquals(0.0, Angles.wrapRadians(0.0), EPSILON);
    assertEquals(Math.PI, Angles.wrapRadians(Math.PI), EPSILON);
    assertEquals(Math.PI, Angles.wrapRadians(-Math.PI), EPSILON);
    assertEquals(-Math.PI / 2, Angles.wrapRadians(3 * Math.PI / 2), EPSILON);
    assertEquals(Math.PI / 2, Angles.wrapRadians(-3 * Math.PI / 2), EPSILON);
    assertEquals(Math.PI, Angles.wrapRadians(7 * Math.PI), EPSILON);
    assertEquals(0.0, Angles.wrapRadians(4 * Math.PI), EPSILON);
  }

  @Test
  @DisplayName("wrapPositive lands in [0, 2π)")
  void wrapPositive() {
    assertEquals(3 * Math.PI / 2, Angles.wrapPositive(-Math.PI / 2), EPSILON);
    assertEquals(0.0, Angles.wrapPositive(2 * Math.PI), EPSILON);
    assertEquals(0.0, Angles.wrapPositive(0.0), EPSILON);
    assertEquals(Math.PI / 2, Angles.wrapPositive(5 * Math.PI / 2), EPSILON);
  }

  @Test
  @DisplayName("shortestDelta crosses the ±π seam the short way")
  void shortestDelta() {
    assertEquals(2 * Math.PI - 6.2, Angles.shortestDelta(3.1, -3.1), EPSILON);
    assertEquals(-(2 * Math.PI - 6.2), Angles.shortestDelta(-3.1, 3.1), EPSILON);
    assertEquals(Math.PI / 2, Angles.shortestDelta(0.0, Math.PI / 2), EPSILON);
  }

  @Test
  @DisplayName("withinTolerance compares angles across the seam")
  void withinTolerance() {
    assertTrue(Angles.withinTolerance(3.1, -3.1, 0.1));
    assertFalse(Angles.withinTolerance(0.0, 0.2, 0.1));
    assertTrue(Angles.withinTolerance(-Math.PI, Math.PI, 1e-9));
  }

  @Test
  @DisplayName("toSparkTurnSetpoint adds the zero offset like ModuleIOSpark")
  void sparkSetpoint() {
    // Front-left module zero offset from DriveConstants: -1.671 rad.
    assertEquals(2 * Math.PI - 1.671, Angles.toSparkTurnSetpoint(0.0, -1.671), EPSILON);
    // Back-right module zero offset: 1.549 rad.
    assertEquals(Math.PI + 1.549, Angles.toSparkTurnSetpoint(Math.PI, 1.549), EPSILON);
    assertEquals(0.5, Angles.toSparkTurnSetpoint(2 * Math.PI + 0.25, 0.25), EPSILON);
  }
}
