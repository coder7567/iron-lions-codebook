package frc.training.u14;

import java.util.Map;
import java.util.TreeMap;

/** Reference solution for exercise u14-sotm. */
public final class ShootOnTheMove {
  public record Translation(double x, double y) {}

  public record FieldSpeeds(double vx, double vy) {}

  public static final int ITERATIONS = 3;

  public static final double TOF_MIN_DISTANCE = 0.0;

  public static final double TOF_MAX_DISTANCE = 10.0;

  public static final class ToFTable {
    private final TreeMap<Double, Double> entries = new TreeMap<>();

    public void put(double distanceMeters, double seconds) {
      entries.put(distanceMeters, seconds);
    }

    public double get(double distanceMeters) {
      if (entries.isEmpty()) {
        throw new IllegalStateException("the time of flight table is empty");
      }

      Map.Entry<Double, Double> below = entries.floorEntry(distanceMeters);
      Map.Entry<Double, Double> above = entries.ceilingEntry(distanceMeters);

      if (below == null) {
        return above.getValue();
      }
      if (above == null) {
        return below.getValue();
      }
      if (below.getKey().equals(above.getKey())) {
        return below.getValue();
      }

      double t = (distanceMeters - below.getKey()) / (above.getKey() - below.getKey());
      return below.getValue() + (above.getValue() - below.getValue()) * t;
    }
  }

  private ShootOnTheMove() {}

  public static ToFTable teamTimeOfFlight() {
    ToFTable table = new ToFTable();
    table.put(1.93, 1.22);
    table.put(3.92, 1.3);
    table.put(4.13, 1.3);
    table.put(5.45, 1.45);
    return table;
  }

  public static double distance(Translation a, Translation b) {
    return Math.hypot(a.x() - b.x(), a.y() - b.y());
  }

  public static double timeOfFlight(
      Translation target, Translation robot, ToFTable table, double realityConstant) {
    double clamped =
        Math.max(TOF_MIN_DISTANCE, Math.min(TOF_MAX_DISTANCE, distance(target, robot)));
    return table.get(clamped) * realityConstant;
  }

  public static Translation adjustTarget(
      Translation target, Translation robot, FieldSpeeds speeds, ToFTable table, double realityConstant) {
    double flightTime = 0.0;
    Translation adjusted = target;

    for (int i = 0; i < ITERATIONS; i++) {
      double previous = flightTime;
      flightTime = timeOfFlight(adjusted, robot, table, realityConstant);
      double step = flightTime - previous;
      adjusted = new Translation(adjusted.x() - speeds.vx() * step, adjusted.y() - speeds.vy() * step);
    }

    return adjusted;
  }
}
