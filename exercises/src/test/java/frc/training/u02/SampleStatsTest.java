package frc.training.u02;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SampleStatsTest {
  private static final double EPSILON = 1e-9;
  private static final double[] EMPTY = {};

  @Test
  @DisplayName("sum adds every sample and an empty array sums to 0")
  void sum() {
    assertEquals(7.0, SampleStats.sum(new double[] {1.5, 2.5, 3}), EPSILON);
    assertEquals(0.0, SampleStats.sum(EMPTY), EPSILON);
  }

  @Test
  @DisplayName("average divides by the number of samples")
  void average() {
    assertEquals(25.0, SampleStats.average(new double[] {10, 20, 30, 40}), EPSILON);
    assertEquals(0.0, SampleStats.average(new double[] {-2, 2}), EPSILON);
    assertEquals(0.0, SampleStats.average(EMPTY), EPSILON);
  }

  @Test
  @DisplayName("max works even when every sample is negative")
  void max() {
    assertEquals(9.0, SampleStats.max(new double[] {3, 9, 4}), EPSILON);
    assertEquals(-2.0, SampleStats.max(new double[] {-5, -2, -8}), EPSILON);
    assertEquals(0.0, SampleStats.max(EMPTY), EPSILON);
  }

  @Test
  @DisplayName("countAbove counts only values strictly above the threshold")
  void countAbove() {
    assertEquals(3, SampleStats.countAbove(new double[] {30, 36, 35, 40, 50}, 35));
    assertEquals(0, SampleStats.countAbove(EMPTY, 35));
  }

  @Test
  @DisplayName("firstIndexAbove finds the first high sample or returns -1")
  void firstIndexAbove() {
    assertEquals(2, SampleStats.firstIndexAbove(new double[] {10, 20, 36, 40}, 35));
    assertEquals(-1, SampleStats.firstIndexAbove(new double[] {10, 20, 35}, 35));
  }

  @Test
  @DisplayName("longestRunAbove measures the longest streak")
  void longestRunAbove() {
    assertEquals(3, SampleStats.longestRunAbove(new double[] {40, 41, 10, 50, 50, 50, 5}, 35));
    assertEquals(2, SampleStats.longestRunAbove(new double[] {36, 36, 1, 36}, 35));
    assertEquals(0, SampleStats.longestRunAbove(new double[] {1, 2, 3}, 35));
    assertEquals(0, SampleStats.longestRunAbove(EMPTY, 35));
  }

  @Test
  @DisplayName("jamDetectedAt reports the sample that completes the first long-enough streak")
  void jamDetectedAt() {
    double[] currents = {40, 40, 10, 40, 40, 40, 40};
    assertEquals(5, SampleStats.jamDetectedAt(currents, 35, 3));
    assertEquals(1, SampleStats.jamDetectedAt(currents, 35, 2));
    assertEquals(-1, SampleStats.jamDetectedAt(currents, 35, 5));
    assertEquals(1, SampleStats.jamDetectedAt(new double[] {10, 36}, 35, 1));
  }
}
