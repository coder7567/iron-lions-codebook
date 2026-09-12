package frc.training.u05;

import java.util.TreeMap;

/**
 * Exercise u05-interp, part 2: a generic lookup table that blends between the nearest keys, like the
 * InterpolatingTreeMap our turret uses for its shot map.
 *
 * @param <V> any type that can blend with values of its own type
 */
public class InterpolatingTable<V extends Blendable<V>> {
  // TODO: store entries in a TreeMap<Double, V> so the keys stay sorted

  /** Adds or replaces the value for {@code key}. */
  public void put(double key, V value) {
    // TODO
  }

  /**
   * Looks up {@code key}.
   *
   * <ul>
   *   <li>An exact key returns its value.
   *   <li>A key between two keys blends the lower key's value toward the upper key's value, by how far the key
   *       is between them.
   *   <li>A key below the smallest key returns the smallest key's value; above the largest returns the
   *       largest's.
   *   <li>An empty table returns null.
   * </ul>
   */
  public V get(double key) {
    // TODO: TreeMap's floorEntry and ceilingEntry do most of the work
    return null;
  }

  /** Returns how many keys the table holds. */
  public int size() {
    // TODO
    return 0;
  }

  /** Removes every entry. */
  public void clear() {
    // TODO
  }
}
