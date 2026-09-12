package frc.training.u05;

import java.util.Map;
import java.util.TreeMap;

/** Reference solution for exercise u05-interp, part 2. */
public class InterpolatingTable<V extends Blendable<V>> {
  private final TreeMap<Double, V> table = new TreeMap<>();

  public void put(double key, V value) {
    table.put(key, value);
  }

  public V get(double key) {
    if (table.isEmpty()) {
      return null;
    }
    Map.Entry<Double, V> lower = table.floorEntry(key);
    Map.Entry<Double, V> upper = table.ceilingEntry(key);
    if (lower == null) {
      return upper.getValue();
    }
    if (upper == null || lower.getKey().equals(upper.getKey())) {
      return lower.getValue();
    }
    double t = (key - lower.getKey()) / (upper.getKey() - lower.getKey());
    return lower.getValue().blend(upper.getValue(), t);
  }

  public int size() {
    return table.size();
  }

  public void clear() {
    table.clear();
  }
}
