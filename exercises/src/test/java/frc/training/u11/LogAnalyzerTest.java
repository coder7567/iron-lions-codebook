package frc.training.u11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u11.LogAnalyzer.LoopStats;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LogAnalyzerTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("loop stats find the mean, the worst loop, and the overruns")
  void loopStats() {
    // Four gaps: 0.015, 0.015, 0.035, 0.015. Only the third is over the 0.02 budget.
    double[] timestamps = {0.0, 0.015, 0.03, 0.065, 0.08};

    LoopStats stats = LogAnalyzer.loopStats(timestamps, 0.02);

    assertEquals(0.02, stats.meanSeconds(), 1e-9);
    assertEquals(0.035, stats.maxSeconds(), 1e-9);
    assertEquals(1, stats.overrunCount());
  }

  @Test
  @DisplayName("a clean run has no overruns")
  void noOverruns() {
    double[] timestamps = {0.0, 0.015, 0.03, 0.045};

    LoopStats stats = LogAnalyzer.loopStats(timestamps, 0.02);

    assertEquals(0, stats.overrunCount());
    assertEquals(0.015, stats.maxSeconds(), 1e-9);
  }

  @Test
  @DisplayName("loop stats need at least two timestamps")
  void loopStatsNeedsSamples() {
    assertThrows(IllegalArgumentException.class, () -> LogAnalyzer.loopStats(new double[] {0.0}, 0.02));
  }

  @Test
  @DisplayName("firstTrue finds when a condition started, or reports that it never did")
  void firstTrue() {
    double[] timestamps = {0.0, 0.02, 0.04, 0.06};
    boolean[] jammed = {false, false, true, true};

    assertEquals(0.04, LogAnalyzer.firstTrue(timestamps, jammed), EPSILON);
    assertTrue(Double.isNaN(LogAnalyzer.firstTrue(timestamps, new boolean[] {false, false, false, false})));
    assertThrows(
        IllegalArgumentException.class,
        () -> LogAnalyzer.firstTrue(timestamps, new boolean[] {true, false}));
  }

  @Test
  @DisplayName("the worst tracking error is the biggest gap between command and measurement")
  void worstTrackingError() {
    double[] setpoints = {2500, 2500, 2500, 2500};
    double[] measured = {0, 1800, 2450, 2510};

    assertEquals(2500.0, LogAnalyzer.worstTrackingError(setpoints, measured), EPSILON);
    assertEquals(
        60.0,
        LogAnalyzer.worstTrackingError(new double[] {2500, 2500}, new double[] {2450, 2560}),
        EPSILON);
  }

  @Test
  @DisplayName("fractionWithin answers how often the mechanism was actually ready")
  void fractionWithin() {
    double[] setpoints = {2500, 2500, 2500, 2500};
    double[] measured = {0, 1800, 2450, 2510};

    assertEquals(0.5, LogAnalyzer.fractionWithin(setpoints, measured, 100.0), EPSILON);
    assertEquals(1.0, LogAnalyzer.fractionWithin(setpoints, measured, 3000.0), EPSILON);
    assertEquals(0.0, LogAnalyzer.fractionWithin(setpoints, measured, 1.0), EPSILON);
  }
}
