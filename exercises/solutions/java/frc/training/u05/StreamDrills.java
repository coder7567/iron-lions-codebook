package frc.training.u05;

import frc.training.u03.CanDevice;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

/** Reference solution for exercise u05-streams. */
public final class StreamDrills {
  private StreamDrills() {}

  public static double[] drain(Queue<Double> queue) {
    double[] values = queue.stream().mapToDouble(Double::doubleValue).toArray();
    queue.clear();
    return values;
  }

  public static double[] rotationsToWheelRadians(double[] motorRotations, double reduction) {
    return Arrays.stream(motorRotations).map(rotations -> rotations * 2.0 * Math.PI / reduction).toArray();
  }

  public static List<String> namesOfController(List<CanDevice> devices, String controller) {
    return devices.stream()
        .filter(device -> device.getController().equals(controller))
        .map(CanDevice::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  public static double averageAbove(double[] values, double threshold) {
    return Arrays.stream(values).filter(value -> value > threshold).average().orElse(0.0);
  }

  public static long countMultiTag(List<PoseObservation> observations) {
    return observations.stream().filter(observation -> observation.tagCount() >= 2).count();
  }
}
