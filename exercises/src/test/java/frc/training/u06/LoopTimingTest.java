package frc.training.u06;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoopTimingTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("cycleLengths subtracts neighboring timestamps")
  void cycleLengths() {
    assertArrayEquals(new double[] {0.02, 0.021, 0.019}, LoopStats.cycleLengths(new double[] {0, 0.02, 0.041, 0.06}), EPSILON);
    assertEquals(0, LoopStats.cycleLengths(new double[] {}).length);
    assertEquals(0, LoopStats.cycleLengths(new double[] {5.0}).length);
  }

  @Test
  @DisplayName("overrunCount counts cycles beyond the period plus tolerance")
  void overruns() {
    double[] starts = {0, 0.02, 0.045, 0.065, 0.1};
    assertEquals(2, LoopStats.overrunCount(starts, 0.02, 0.001));
    assertEquals(1, LoopStats.overrunCount(starts, 0.02, 0.01));
    assertEquals(0, LoopStats.overrunCount(new double[] {0}, 0.02, 0.001));
  }

  @Test
  @DisplayName("worstCycle and worstCycleIndex find the slowest loop")
  void worst() {
    double[] starts = {0, 0.02, 0.045, 0.065, 0.1};
    assertEquals(0.035, LoopStats.worstCycle(starts), EPSILON);
    assertEquals(3, LoopStats.worstCycleIndex(starts));
    assertEquals(0.0, LoopStats.worstCycle(new double[] {1.0}), EPSILON);
    assertEquals(-1, LoopStats.worstCycleIndex(new double[] {}));
  }

  @Test
  @DisplayName("a teleop start that blocks for 60 ms shows up as one big overrun")
  void teleopInitStall() {
    double[] starts = {10.00, 10.02, 10.04, 10.10, 10.12, 10.14};
    assertEquals(1, LoopStats.overrunCount(starts, LoopStats.NOMINAL_PERIOD_SECONDS, 0.002));
    assertEquals(2, LoopStats.worstCycleIndex(starts));
    assertEquals(0.06, LoopStats.worstCycle(starts), 1e-9);
  }

  @Test
  @DisplayName("modeName follows the Driver Station state")
  void modeNames() {
    assertEquals("disabled", ModeTracker.modeName(false, true, false));
    assertEquals("autonomous", ModeTracker.modeName(true, true, false));
    assertEquals("teleop", ModeTracker.modeName(true, false, false));
    assertEquals("test", ModeTracker.modeName(true, false, true));
  }

  @Test
  @DisplayName("update reports each init method once, following a real match")
  void matchSequence() {
    ModeTracker tracker = new ModeTracker();
    assertEquals("unknown", tracker.currentMode());
    assertEquals("disabledInit", tracker.update(false, false, false));
    assertNull(tracker.update(false, false, false));
    assertEquals("autonomousInit", tracker.update(true, true, false));
    assertNull(tracker.update(true, true, false));
    assertEquals("disabledInit", tracker.update(false, true, false));
    assertEquals("teleopInit", tracker.update(true, false, false));
    assertNull(tracker.update(true, false, false));
    assertEquals("teleop", tracker.currentMode());
    assertEquals("testInit", tracker.update(true, false, true));
    assertEquals("test", tracker.currentMode());
  }
}
