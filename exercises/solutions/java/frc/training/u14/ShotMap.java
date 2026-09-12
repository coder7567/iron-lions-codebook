package frc.training.u14;

import java.util.Map;
import java.util.TreeMap;

/** Reference solution for exercise u14-shotmap. */
public final class ShotMap {
  public record Setpoint(double rpm, double hoodAngle) {}

  public static final double MIN_DISTANCE = 0.0;

  public static final double MAX_DISTANCE = 10.0;

  public static final double HOOD_OFFSET = 0.291;

  public static final double HOOD_MIN = 0.239 + HOOD_OFFSET;
  public static final double HOOD_MAX = 0.616 + HOOD_OFFSET;
  public static final double FLYWHEEL_MAX_RPM = 6758;

  private final TreeMap<Double, Setpoint> entries = new TreeMap<>();

  public void put(double distanceMeters, Setpoint setpoint) {
    entries.put(distanceMeters, setpoint);
  }

  public Setpoint get(double distanceMeters) {
    if (entries.isEmpty()) {
      throw new IllegalStateException("the shot map is empty");
    }

    Map.Entry<Double, Setpoint> below = entries.floorEntry(distanceMeters);
    Map.Entry<Double, Setpoint> above = entries.ceilingEntry(distanceMeters);

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
    return new Setpoint(
        blend(below.getValue().rpm(), above.getValue().rpm(), t),
        blend(below.getValue().hoodAngle(), above.getValue().hoodAngle(), t));
  }

  public Setpoint lookupClamped(double distanceMeters) {
    Setpoint setpoint = get(clamp(distanceMeters, MIN_DISTANCE, MAX_DISTANCE));
    return new Setpoint(
        clamp(setpoint.rpm(), 0.0, FLYWHEEL_MAX_RPM), clamp(setpoint.hoodAngle(), HOOD_MIN, HOOD_MAX));
  }

  private static double blend(double start, double end, double t) {
    return start + (end - start) * t;
  }

  private static double clamp(double value, double min, double max) {
    return Math.max(min, Math.min(max, value));
  }

  public static ShotMap teamTable() {
    ShotMap map = new ShotMap();
    map.put(1.03, new Setpoint(1850, 0.588 + HOOD_OFFSET));
    map.put(1.77, new Setpoint(1950, 0.588 + HOOD_OFFSET));
    map.put(2.0, new Setpoint(2000, 0.588 + HOOD_OFFSET));
    map.put(2.38, new Setpoint(2050, 0.55 + HOOD_OFFSET));
    map.put(2.86, new Setpoint(2100, 0.525 + HOOD_OFFSET));
    map.put(3.377, new Setpoint(2250, 0.5 + HOOD_OFFSET));
    map.put(3.84, new Setpoint(2300, 0.475 + HOOD_OFFSET));
    map.put(4.06, new Setpoint(2400, 0.45 + HOOD_OFFSET));
    map.put(4.66, new Setpoint(2500, 0.425 + HOOD_OFFSET));
    map.put(5.56, new Setpoint(2600, 0.4 + HOOD_OFFSET));
    map.put(6.45, new Setpoint(2700, 0.35 + HOOD_OFFSET));
    return map;
  }
}
