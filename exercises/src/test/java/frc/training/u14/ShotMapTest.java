package frc.training.u14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import frc.training.u14.ShotMap.Setpoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShotMapTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("an exact key returns its own setpoint")
  void exactKeys() {
    ShotMap map = ShotMap.teamTable();

    assertEquals(2000.0, map.get(2.0).rpm(), EPSILON);
    assertEquals(0.588 + ShotMap.HOOD_OFFSET, map.get(2.0).hoodAngle(), EPSILON);
    assertEquals(2700.0, map.get(6.45).rpm(), EPSILON);
  }

  @Test
  @DisplayName("a distance between entries interpolates both values")
  void interpolates() {
    ShotMap map = ShotMap.teamTable();

    // Halfway between 2.0 and 2.38: rpm 2000 to 2050, hood 0.588 to 0.55 (plus the offset).
    Setpoint middle = map.get(2.19);

    assertEquals(2025.0, middle.rpm(), 1e-9);
    assertEquals(0.569 + ShotMap.HOOD_OFFSET, middle.hoodAngle(), 1e-9);
  }

  @Test
  @DisplayName("outside the table, the nearest entry is used")
  void clampsToEnds() {
    ShotMap map = ShotMap.teamTable();

    assertEquals(1850.0, map.get(0.5).rpm(), EPSILON);
    assertEquals(2700.0, map.get(9.0).rpm(), EPSILON);
  }

  @Test
  @DisplayName("a table with one entry answers every distance with it")
  void singleEntry() {
    ShotMap map = new ShotMap();
    map.put(3.0, new Setpoint(2222, 0.5));

    assertEquals(2222.0, map.get(1.0).rpm(), EPSILON);
    assertEquals(2222.0, map.get(3.0).rpm(), EPSILON);
    assertEquals(2222.0, map.get(8.0).rpm(), EPSILON);
  }

  @Test
  @DisplayName("an empty table has nothing to answer with")
  void emptyTable() {
    assertThrows(IllegalStateException.class, () -> new ShotMap().get(2.0));
  }

  @Test
  @DisplayName("lookupClamped keeps the distance and the outputs inside their limits")
  void lookupClamped() {
    ShotMap map = ShotMap.teamTable();

    // A negative distance clamps to the table's minimum, which is below its first key.
    assertEquals(1850.0, map.lookupClamped(-2.0).rpm(), EPSILON);
    // Beyond 10 m clamps to 10 m, which is past the last key.
    assertEquals(2700.0, map.lookupClamped(25.0).rpm(), EPSILON);

    ShotMap extreme = new ShotMap();
    extreme.put(2.0, new Setpoint(9000, 1.5));
    extreme.put(4.0, new Setpoint(-500, 0.0));

    assertEquals(ShotMap.FLYWHEEL_MAX_RPM, extreme.lookupClamped(2.0).rpm(), EPSILON);
    assertEquals(ShotMap.HOOD_MAX, extreme.lookupClamped(2.0).hoodAngle(), EPSILON);
    assertEquals(0.0, extreme.lookupClamped(4.0).rpm(), EPSILON);
    assertEquals(ShotMap.HOOD_MIN, extreme.lookupClamped(4.0).hoodAngle(), EPSILON);
  }

  @Test
  @DisplayName("every tuned hood angle in the team's table is inside the hood's travel")
  void teamTableIsInRange() {
    ShotMap map = ShotMap.teamTable();

    for (double distance = 1.03; distance <= 6.45; distance += 0.1) {
      Setpoint raw = map.get(distance);
      Setpoint clamped = map.lookupClamped(distance);
      assertEquals(raw.hoodAngle(), clamped.hoodAngle(), 1e-9, "distance " + distance);
      assertEquals(raw.rpm(), clamped.rpm(), 1e-9, "distance " + distance);
    }
  }
}
