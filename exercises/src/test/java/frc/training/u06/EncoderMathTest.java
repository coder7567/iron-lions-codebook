package frc.training.u06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EncoderMathTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("applyZeroOffset subtracts and wraps into [0, 1), like the intake arm's 0.31 offset")
  void zeroOffset() {
    assertEquals(0.79, EncoderMath.applyZeroOffset(0.10, 0.31), EPSILON);
    assertEquals(0.19, EncoderMath.applyZeroOffset(0.50, 0.31), EPSILON);
    assertEquals(0.0, EncoderMath.applyZeroOffset(0.31, 0.31), EPSILON);
    assertEquals(0.01, EncoderMath.applyZeroOffset(0.99, -0.02), EPSILON);
  }

  @Test
  @DisplayName("rotationsToRadians multiplies by 2π")
  void rotations() {
    assertEquals(Math.PI / 2, EncoderMath.rotationsToRadians(0.25), EPSILON);
    assertEquals(2 * Math.PI, EncoderMath.rotationsToRadians(1.0), EPSILON);
  }

  @Test
  @DisplayName("mechanismRadiansFromCounts accounts for resolution and gearing")
  void countsToRadians() {
    assertEquals(2 * Math.PI, EncoderMath.mechanismRadiansFromCounts(8192, 8192, 1.0), EPSILON);
    assertEquals(Math.PI / 2, EncoderMath.mechanismRadiansFromCounts(4096, 8192, 2.0), EPSILON);
    assertEquals(0.0, EncoderMath.mechanismRadiansFromCounts(0, 8192, 4.0), EPSILON);
  }

  @Test
  @DisplayName("one encoder turn moves the turret 2π × 44 / 180 ≈ 1.5359 rad, matching TurretIOSpark's conversion")
  void turret() {
    assertEquals(1.5358897417550, EncoderMath.turretRadiansFromCounts(8192), 1e-9);
    assertEquals(-0.7679448708775, EncoderMath.turretRadiansFromCounts(-4096), 1e-9);
    assertEquals(2 * Math.PI / (180.0 / 44.0), EncoderMath.turretRadiansFromCounts(EncoderMath.TURRET_COUNTS_PER_REV), EPSILON);
  }

  @Test
  @DisplayName("isJump measures change the short way around the 0/1 wrap")
  void jumps() {
    assertFalse(EncoderMath.isJump(0.98, 0.02, 0.1));
    assertFalse(EncoderMath.isJump(0.02, 0.98, 0.05));
    assertTrue(EncoderMath.isJump(0.2, 0.7, 0.1));
    assertTrue(EncoderMath.isJump(0.5, 0.52, 0.01));
  }
}
