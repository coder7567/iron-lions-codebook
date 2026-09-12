package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InterpolatingTableTest {
  private static final double EPSILON = 1e-6;

  /** A completely different Blendable type, to prove the table is generic. */
  record Gain(double kp) implements Blendable<Gain> {
    @Override
    public Gain blend(Gain end, double t) {
      return new Gain(kp + (end.kp - kp) * t);
    }
  }

  /** The 2026 robot's shooting map from Turret.java: distance (m) to RPM and hood angle (0.291 offset applied). */
  private static InterpolatingTable<Shot> teamShotMap() {
    InterpolatingTable<Shot> map = new InterpolatingTable<>();
    map.put(1.03, new Shot(1850, 0.879));
    map.put(1.77, new Shot(1950, 0.879));
    map.put(2.0, new Shot(2000, 0.879));
    map.put(2.38, new Shot(2050, 0.841));
    map.put(2.86, new Shot(2100, 0.816));
    map.put(3.377, new Shot(2250, 0.791));
    map.put(3.84, new Shot(2300, 0.766));
    map.put(4.06, new Shot(2400, 0.741));
    map.put(4.66, new Shot(2500, 0.716));
    map.put(5.56, new Shot(2600, 0.691));
    map.put(6.45, new Shot(2700, 0.641));
    return map;
  }

  @Test
  @DisplayName("Shot.blend blends both RPM and hood angle")
  void shotBlend() {
    Shot near = new Shot(2000, 0.879);
    Shot far = new Shot(2050, 0.841);
    Shot middle = near.blend(far, 0.5);
    assertEquals(2025.0, middle.rpm(), EPSILON);
    assertEquals(0.860, middle.hoodAngle(), EPSILON);
    assertEquals(near, near.blend(far, 0.0));
    assertEquals(2050.0, near.blend(far, 1.0).rpm(), EPSILON);
  }

  @Test
  @DisplayName("an empty table returns null")
  void emptyTable() {
    InterpolatingTable<Shot> table = new InterpolatingTable<>();
    assertNull(table.get(2.0));
    assertEquals(0, table.size());
  }

  @Test
  @DisplayName("an exact distance returns that row")
  void exactKey() {
    Shot shot = teamShotMap().get(2.0);
    assertEquals(2000.0, shot.rpm(), EPSILON);
    assertEquals(0.879, shot.hoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("2.19 m is halfway between the 2.0 m and 2.38 m rows")
  void halfwayBetweenRows() {
    Shot shot = teamShotMap().get(2.19);
    assertEquals(2025.0, shot.rpm(), EPSILON);
    assertEquals(0.860, shot.hoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("3.0 m blends the 2.86 m and 3.377 m rows by how far between they are")
  void unevenlyBetweenRows() {
    Shot shot = teamShotMap().get(3.0);
    assertEquals(2140.619, shot.rpm(), 1e-3);
    assertEquals(0.80923, shot.hoodAngle(), 1e-5);
  }

  @Test
  @DisplayName("distances outside the map use the nearest end")
  void clampsToEnds() {
    InterpolatingTable<Shot> map = teamShotMap();
    assertEquals(1850.0, map.get(0.5).rpm(), EPSILON);
    assertEquals(2700.0, map.get(7.0).rpm(), EPSILON);
    assertEquals(0.641, map.get(7.0).hoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("keys inserted out of order still interpolate correctly")
  void outOfOrder() {
    InterpolatingTable<Gain> table = new InterpolatingTable<>();
    table.put(5.0, new Gain(50));
    table.put(1.0, new Gain(10));
    table.put(3.0, new Gain(30));
    assertEquals(20.0, table.get(2.0).kp(), EPSILON);
    assertEquals(3, table.size());
  }

  @Test
  @DisplayName("putting an existing key replaces it, and clear empties the table")
  void replaceAndClear() {
    InterpolatingTable<Gain> table = new InterpolatingTable<>();
    table.put(2.0, new Gain(1));
    table.put(2.0, new Gain(4));
    assertEquals(1, table.size());
    assertEquals(4.0, table.get(2.0).kp(), EPSILON);
    table.clear();
    assertEquals(0, table.size());
    assertNull(table.get(2.0));
  }

  @Test
  @DisplayName("the same generic table works for gains, and a one-row table returns that row everywhere")
  void genericReuse() {
    InterpolatingTable<Gain> table = new InterpolatingTable<>();
    table.put(0.0, new Gain(1.0));
    assertEquals(1.0, table.get(-3.0).kp(), EPSILON);
    assertEquals(1.0, table.get(8.0).kp(), EPSILON);
    table.put(10.0, new Gain(3.0));
    assertEquals(2.0, table.get(5.0).kp(), EPSILON);
  }
}
