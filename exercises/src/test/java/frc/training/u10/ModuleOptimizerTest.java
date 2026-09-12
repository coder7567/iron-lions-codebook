package frc.training.u10;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.training.u10.ModuleOptimizer.ModuleState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ModuleOptimizerTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("turn error takes the short way around the wrap")
  void turnError() {
    assertEquals(0.2, ModuleOptimizer.turnErrorRadians(0.7, 0.5), 1e-9);
    assertEquals(-0.2, ModuleOptimizer.turnErrorRadians(0.5, 0.7), 1e-9);
    // From just below +pi to just above -pi is a small positive turn, not a full lap.
    assertEquals(0.2, ModuleOptimizer.turnErrorRadians(-Math.PI + 0.1, Math.PI - 0.1), 1e-9);
  }

  @Test
  @DisplayName("a small turn is left alone")
  void smallTurnUnchanged() {
    ModuleState result = ModuleOptimizer.optimize(new ModuleState(2.0, 0.4), 0.1);

    assertEquals(2.0, result.speedMetersPerSec(), EPSILON);
    assertEquals(0.4, result.angleRad(), EPSILON);
  }

  @Test
  @DisplayName("a turn past 90 degrees flips the angle and drives backward instead")
  void largeTurnFlips() {
    // Asking for 170 degrees from 0: turning 10 degrees the other way and reversing is faster.
    ModuleState result = ModuleOptimizer.optimize(new ModuleState(2.0, Math.toRadians(170)), 0.0);

    assertEquals(-2.0, result.speedMetersPerSec(), EPSILON);
    assertEquals(Math.toRadians(-10), result.angleRad(), 1e-9);
    assertEquals(Math.toRadians(-10), ModuleOptimizer.turnErrorRadians(result.angleRad(), 0.0), 1e-9);
  }

  @Test
  @DisplayName("exactly 90 degrees is not flipped")
  void ninetyDegreesUnchanged() {
    ModuleState result = ModuleOptimizer.optimize(new ModuleState(1.5, Math.PI / 2), 0.0);

    assertEquals(1.5, result.speedMetersPerSec(), EPSILON);
    assertEquals(Math.PI / 2, result.angleRad(), EPSILON);
  }

  @Test
  @DisplayName("a reverse request from a reversed module is also optimized")
  void flipsAcrossTheWrap() {
    ModuleState result =
        ModuleOptimizer.optimize(new ModuleState(1.0, Math.toRadians(-175)), Math.toRadians(5));

    assertEquals(-1.0, result.speedMetersPerSec(), EPSILON);
    assertEquals(Math.toRadians(5), result.angleRad(), 1e-9);
  }

  @Test
  @DisplayName("cosine scaling reduces speed while a module is still turning")
  void cosineScaling() {
    assertEquals(
        1.0, ModuleOptimizer.cosineScale(new ModuleState(1.0, 0.5), 0.5).speedMetersPerSec(), EPSILON);
    assertEquals(
        0.5,
        ModuleOptimizer.cosineScale(new ModuleState(1.0, Math.toRadians(60)), 0.0).speedMetersPerSec(),
        1e-9);
    assertEquals(
        0.0,
        ModuleOptimizer.cosineScale(new ModuleState(1.0, Math.PI / 2), 0.0).speedMetersPerSec(),
        1e-9);
    assertEquals(
        0.3, ModuleOptimizer.cosineScale(new ModuleState(2.0, 0.3), 0.1).angleRad(), EPSILON);
  }

  @Test
  @DisplayName("optimize then cosine scale is what Module.runSetpoint does each loop")
  void optimizeThenScale() {
    ModuleState desired = new ModuleState(2.0, Math.toRadians(170));
    double current = 0.0;

    ModuleState optimized = ModuleOptimizer.optimize(desired, current);
    ModuleState commanded = ModuleOptimizer.cosineScale(optimized, current);

    // After the flip the module only has 10 degrees to turn, so it keeps almost all of its speed.
    assertEquals(-2.0 * Math.cos(Math.toRadians(10)), commanded.speedMetersPerSec(), 1e-9);
  }
}
