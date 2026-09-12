package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CanMapTest {
  private final CanDevice turret = new CanDevice(12, "Turret", "SparkFlex");

  @Test
  @DisplayName("add stores a device that get can find")
  void addAndGet() {
    CanMap map = new CanMap();
    assertTrue(map.add(turret));
    assertEquals(1, map.size());
    assertSame(turret, map.get(12));
    assertTrue(map.isUsed(12));
    assertFalse(map.isUsed(11));
    assertNull(map.get(99));
  }

  @Test
  @DisplayName("duplicate IDs are refused and the original stays")
  void refusesDuplicates() {
    CanMap map = new CanMap();
    map.add(turret);
    assertFalse(map.add(new CanDevice(12, "Hood", "SparkFlex")));
    assertEquals(1, map.size());
    assertEquals("Turret", map.get(12).getName());
  }

  @Test
  @DisplayName("IDs outside 1..62 are refused")
  void refusesOutOfRange() {
    CanMap map = new CanMap();
    assertFalse(map.add(new CanDevice(0, "Bad", "SparkMax")));
    assertFalse(map.add(new CanDevice(63, "Bad", "SparkMax")));
    assertFalse(map.add(new CanDevice(-1, "Bad", "SparkMax")));
    assertTrue(map.add(new CanDevice(62, "Edge", "SparkMax")));
    assertEquals(1, map.size());
  }

  @Test
  @DisplayName("ids and byController come back sorted even when added out of order")
  void sorted() {
    CanMap map = new CanMap();
    map.add(new CanDevice(12, "Turret", "SparkFlex"));
    map.add(new CanDevice(3, "Back-left drive", "SparkMax"));
    map.add(new CanDevice(9, "Flywheel leader", "SparkFlex"));
    assertArrayEquals(new int[] {3, 9, 12}, map.ids());
    List<CanDevice> flexes = map.byController("SparkFlex");
    assertEquals(2, flexes.size());
    assertEquals(9, flexes.get(0).getId());
    assertEquals(12, flexes.get(1).getId());
  }

  @Test
  @DisplayName("byController returns an empty list, not null, when nothing matches")
  void emptyMatches() {
    List<CanDevice> none = CanMap.robot2026().byController("TalonFX");
    assertNotNull(none);
    assertTrue(none.isEmpty());
  }

  @Test
  @DisplayName("lowestFreeId finds gaps and reports a full bus")
  void lowestFreeId() {
    CanMap map = new CanMap();
    assertEquals(1, map.lowestFreeId());
    map.add(new CanDevice(1, "a", "SparkMax"));
    map.add(new CanDevice(2, "b", "SparkMax"));
    map.add(new CanDevice(4, "c", "SparkMax"));
    assertEquals(3, map.lowestFreeId());
    CanMap full = new CanMap();
    for (int id = 1; id <= 62; id++) {
      full.add(new CanDevice(id, "device " + id, "SparkMax"));
    }
    assertEquals(-1, full.lowestFreeId());
  }

  @Test
  @DisplayName("robot2026 matches the real robot: 18 devices, 10 SPARK MAX and 8 SPARK Flex")
  void robot2026() {
    CanMap robot = CanMap.robot2026();
    assertEquals(18, robot.size());
    assertEquals("Turret", robot.get(12).getName());
    assertEquals(10, robot.byController("SparkMax").size());
    assertArrayEquals(new int[] {9, 10, 11, 12, 13, 14, 15, 18},
        robot.byController("SparkFlex").stream().mapToInt(CanDevice::getId).toArray());
    assertEquals(19, robot.lowestFreeId());
  }
}
