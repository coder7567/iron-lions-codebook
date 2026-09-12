package frc.training.u15;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CurrentAveragerTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("an empty averager reports zero rather than dividing by zero")
  void empty() {
    CurrentAverager averager = new CurrentAverager(1.0);

    assertEquals(0, averager.sampleCount());
    assertEquals(0.0, averager.average(), EPSILON);
    assertEquals(0.0, averager.peak(), EPSILON);
  }

  @Test
  @DisplayName("samples inside the window are averaged")
  void averages() {
    CurrentAverager averager = new CurrentAverager(1.0);

    averager.addSample(0.0, 10.0);
    averager.addSample(0.2, 20.0);
    averager.addSample(0.4, 30.0);

    assertEquals(3, averager.sampleCount());
    assertEquals(20.0, averager.average(), EPSILON);
    assertEquals(30.0, averager.peak(), EPSILON);
  }

  @Test
  @DisplayName("samples older than the window are dropped")
  void evicts() {
    CurrentAverager averager = new CurrentAverager(1.0);

    averager.addSample(0.0, 10.0);
    averager.addSample(0.5, 20.0);
    averager.addSample(1.2, 30.0);

    assertEquals(2, averager.sampleCount());
    assertEquals(25.0, averager.average(), EPSILON);
    assertEquals(30.0, averager.peak(), EPSILON);
  }

  @Test
  @DisplayName("a sample exactly one window old is still inside the window")
  void windowEdge() {
    CurrentAverager averager = new CurrentAverager(1.0);

    averager.addSample(0.0, 10.0);
    averager.addSample(1.0, 20.0);

    assertEquals(2, averager.sampleCount());
    assertEquals(15.0, averager.average(), EPSILON);
  }

  @Test
  @DisplayName("a long quiet stretch leaves only the newest sample")
  void longGap() {
    CurrentAverager averager = new CurrentAverager(2.0);

    averager.addSample(0.0, 40.0);
    averager.addSample(0.5, 45.0);
    averager.addSample(30.0, 5.0);

    assertEquals(1, averager.sampleCount());
    assertEquals(5.0, averager.average(), EPSILON);
    assertEquals(5.0, averager.peak(), EPSILON);
  }

  @Test
  @DisplayName("peak works even when every sample is negative")
  void negativeSamples() {
    CurrentAverager averager = new CurrentAverager(1.0);

    averager.addSample(0.0, -10.0);
    averager.addSample(0.1, -5.0);

    assertEquals(-7.5, averager.average(), EPSILON);
    assertEquals(-5.0, averager.peak(), EPSILON);
  }

  @Test
  @DisplayName("reset forgets everything")
  void reset() {
    CurrentAverager averager = new CurrentAverager(1.0);
    averager.addSample(0.0, 10.0);
    averager.addSample(0.1, 20.0);

    averager.reset();

    assertEquals(0, averager.sampleCount());
    assertEquals(0.0, averager.average(), EPSILON);
  }

  @Test
  @DisplayName("a 20 ms loop over a one second window holds about fifty samples")
  void realisticLoop() {
    CurrentAverager averager = new CurrentAverager(1.0);

    for (int loop = 0; loop <= 200; loop++) {
      averager.addSample(loop * 0.02, 30.0);
    }

    assertEquals(51, averager.sampleCount());
    assertEquals(30.0, averager.average(), EPSILON);
  }
}
