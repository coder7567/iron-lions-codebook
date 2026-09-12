package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u03.CanDevice;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StreamDrillsTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("drain copies the queue in order and empties it")
  void drain() {
    Queue<Double> queue = new ArrayDeque<>(List.of(1.0, 2.5, 3.0));
    assertArrayEquals(new double[] {1.0, 2.5, 3.0}, StreamDrills.drain(queue), EPSILON);
    assertTrue(queue.isEmpty());
    assertEquals(0, StreamDrills.drain(queue).length);
  }

  @Test
  @DisplayName("rotationsToWheelRadians converts every value and leaves the input alone")
  void convert() {
    double[] rotations = {1.0, 5.9};
    double[] radians = StreamDrills.rotationsToWheelRadians(rotations, 5.9);
    assertArrayEquals(new double[] {2 * Math.PI / 5.9, 2 * Math.PI}, radians, EPSILON);
    assertArrayEquals(new double[] {1.0, 5.9}, rotations, EPSILON);
  }

  @Test
  @DisplayName("namesOfController filters, maps to names, and sorts")
  void names() {
    List<CanDevice> devices = List.of(
        new CanDevice(12, "Turret", "SparkFlex"),
        new CanDevice(3, "Back-left drive", "SparkMax"),
        new CanDevice(9, "Flywheel leader", "SparkFlex"));
    assertEquals(List.of("Flywheel leader", "Turret"), StreamDrills.namesOfController(devices, "SparkFlex"));
    assertEquals(List.of(), StreamDrills.namesOfController(devices, "TalonFX"));
  }

  @Test
  @DisplayName("averageAbove averages only the high values, or returns 0")
  void averageAbove() {
    assertEquals(45.0, StreamDrills.averageAbove(new double[] {30, 40, 50, 10}, 35), EPSILON);
    assertEquals(0.0, StreamDrills.averageAbove(new double[] {1, 2, 3}, 35), EPSILON);
  }

  @Test
  @DisplayName("countMultiTag counts observations with two or more tags")
  void multiTag() {
    List<PoseObservation> observations = List.of(
        new PoseObservation(1, 1, 0, 1.0, 0.1, 1, 2.0),
        new PoseObservation(2, 2, 0, 2.0, 0.1, 2, 3.0),
        new PoseObservation(3, 3, 0, 3.0, 0.1, 3, 4.0));
    assertEquals(2L, StreamDrills.countMultiTag(observations));
  }
}
