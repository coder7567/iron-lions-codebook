package frc.training.u14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u14.ShootOnTheMove.FieldSpeeds;
import frc.training.u14.ShootOnTheMove.ToFTable;
import frc.training.u14.ShootOnTheMove.Translation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShootOnTheMoveTest {
  private static final double EPSILON = 1e-9;

  /** A table where every distance takes the same time, so the arithmetic is exact. */
  private static ToFTable flatTable(double seconds) {
    ToFTable table = new ToFTable();
    table.put(0.0, seconds);
    table.put(10.0, seconds);
    return table;
  }

  @Test
  @DisplayName("the flight-time table interpolates and clamps at its ends")
  void table() {
    ToFTable table = ShootOnTheMove.teamTimeOfFlight();

    assertEquals(1.22, table.get(1.93), EPSILON);
    assertEquals(1.3, table.get(4.0), EPSILON); // between two entries that are both 1.3
    assertEquals(1.26, table.get(2.925), 1e-3); // halfway between 1.93 and 3.92
    assertEquals(1.22, table.get(0.5), EPSILON); // below the first key
    assertEquals(1.45, table.get(9.0), EPSILON); // above the last key
    assertThrows(IllegalStateException.class, () -> new ToFTable().get(1.0));
  }

  @Test
  @DisplayName("flight time is clamped into the table's distance range and scaled by the reality constant")
  void timeOfFlight() {
    ToFTable table = flatTable(1.5);
    Translation robot = new Translation(0.0, 0.0);

    assertEquals(1.5, ShootOnTheMove.timeOfFlight(new Translation(4.0, 0.0), robot, table, 1.0), EPSILON);
    assertEquals(0.9, ShootOnTheMove.timeOfFlight(new Translation(4.0, 0.0), robot, table, 0.6), 1e-9);
    // 40 m away is clamped to 10 m, which is still 1.5 s in a flat table.
    assertEquals(1.5, ShootOnTheMove.timeOfFlight(new Translation(40.0, 0.0), robot, table, 1.0), EPSILON);
  }

  @Test
  @DisplayName("a stationary robot aims at the real target")
  void stationary() {
    Translation target = new Translation(4.625, 4.0);
    Translation adjusted =
        ShootOnTheMove.adjustTarget(
            target, new Translation(2.0, 4.0), new FieldSpeeds(0.0, 0.0), ShootOnTheMove.teamTimeOfFlight(), 1.0);

    assertEquals(target, adjusted);
  }

  @Test
  @DisplayName("a moving robot aims opposite its own motion, by velocity times flight time")
  void leadsTheShot() {
    // With a flat table the three iterations collapse to one displacement of v * flightTime.
    Translation adjusted =
        ShootOnTheMove.adjustTarget(
            new Translation(6.0, 4.0),
            new Translation(2.0, 4.0),
            new FieldSpeeds(1.5, 0.0),
            flatTable(1.2),
            1.0);

    assertEquals(6.0 - 1.5 * 1.2, adjusted.x(), 1e-9);
    assertEquals(4.0, adjusted.y(), 1e-9);
  }

  @Test
  @DisplayName("sideways motion moves the aim point sideways, the other way")
  void sidewaysMotion() {
    Translation adjusted =
        ShootOnTheMove.adjustTarget(
            new Translation(4.625, 4.0),
            new Translation(4.625, 1.0),
            new FieldSpeeds(0.0, 2.0),
            flatTable(0.5),
            1.0);

    assertEquals(4.625, adjusted.x(), 1e-9);
    assertEquals(4.0 - 1.0, adjusted.y(), 1e-9);
  }

  @Test
  @DisplayName("the reality constant scales the whole correction")
  void realityConstant() {
    Translation full =
        ShootOnTheMove.adjustTarget(
            new Translation(6.0, 4.0), new Translation(2.0, 4.0), new FieldSpeeds(1.0, 0.0), flatTable(1.0), 1.0);
    Translation reduced =
        ShootOnTheMove.adjustTarget(
            new Translation(6.0, 4.0), new Translation(2.0, 4.0), new FieldSpeeds(1.0, 0.0), flatTable(1.0), 0.6);

    assertEquals(5.0, full.x(), 1e-9);
    assertEquals(5.4, reduced.x(), 1e-9);
  }

  @Test
  @DisplayName("with the real table, iterating moves the aim point toward a consistent answer")
  void iteratesTowardConsistency() {
    ToFTable table = ShootOnTheMove.teamTimeOfFlight();
    Translation robot = new Translation(0.0, 0.0);
    Translation target = new Translation(4.0, 0.0);

    Translation adjusted = ShootOnTheMove.adjustTarget(target, robot, new FieldSpeeds(1.0, 0.0), table, 1.0);

    // The aim point moved back along the robot's motion by roughly one flight time.
    assertTrue(adjusted.x() < target.x());
    assertTrue(adjusted.x() > target.x() - 1.5);
    // And the final position is consistent with the flight time computed there, within a few cm.
    double finalFlight = ShootOnTheMove.timeOfFlight(adjusted, robot, table, 1.0);
    assertEquals(target.x() - finalFlight, adjusted.x(), 0.05);
  }
}
