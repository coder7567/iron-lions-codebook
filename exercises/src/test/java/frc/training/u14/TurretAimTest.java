package frc.training.u14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TurretAimTest {
  private static final double EPSILON = 1e-9;
  /** The blue HUB, as TurretConstants stores it. */
  private static final double HUB_X = 4.625;

  private static final double HUB_Y = 4.0;

  @Test
  @DisplayName("the field angle points from the robot to the target")
  void fieldAngle() {
    assertEquals(0.0, TurretAim.fieldAngleTo(2.0, 4.0, HUB_X, HUB_Y), EPSILON);
    assertEquals(Math.PI / 2, TurretAim.fieldAngleTo(4.625, 2.0, HUB_X, HUB_Y), 1e-9);
    assertEquals(Math.PI, Math.abs(TurretAim.fieldAngleTo(8.0, 4.0, HUB_X, HUB_Y)), 1e-9);
  }

  @Test
  @DisplayName("the turret angle is the field angle minus the robot's heading")
  void robotRelative() {
    // Facing +Y with the HUB straight ahead in the field frame: the turret looks to its right.
    assertEquals(
        -Math.PI / 2, TurretAim.robotRelativeAngle(2.0, 4.0, Math.PI / 2, HUB_X, HUB_Y), 1e-9);
    assertEquals(0.0, TurretAim.robotRelativeAngle(2.0, 4.0, 0.0, HUB_X, HUB_Y), EPSILON);
    // Facing away from the HUB: the turret has to look behind the robot.
    assertEquals(
        Math.PI, Math.abs(TurretAim.robotRelativeAngle(2.0, 4.0, Math.PI, HUB_X, HUB_Y)), 1e-9);
  }

  @Test
  @DisplayName("the deadzone is the arc between the soft limit and 2.022 rad")
  void deadzone() {
    assertTrue(TurretAim.inDeadzone(1.8, 0.0));
    assertTrue(TurretAim.inDeadzone(2.0, 0.0));
    assertFalse(TurretAim.inDeadzone(1.6, 0.0));
    assertFalse(TurretAim.inDeadzone(2.022, 0.0));
    assertFalse(TurretAim.inDeadzone(1.5, 0.0));
    assertFalse(TurretAim.inDeadzone(2.5, 0.0));
  }

  @Test
  @DisplayName("the operator's trim can move a target into or out of the deadzone")
  void offsetMovesTheDeadzone() {
    assertTrue(TurretAim.inDeadzone(1.7, -0.05));
    assertFalse(TurretAim.inDeadzone(1.7, -0.15));
    assertTrue(TurretAim.inDeadzone(1.55, 0.1));
  }

  @Test
  @DisplayName("a reachable angle passes through unchanged")
  void reachable() {
    assertEquals(0.0, TurretAim.toTurretSetpoint(0.0, 0.0), EPSILON);
    assertEquals(-1.6, TurretAim.toTurretSetpoint(-1.6, 0.0), EPSILON);
    assertEquals(1.2, TurretAim.toTurretSetpoint(1.2, 0.0), 1e-9);
  }

  @Test
  @DisplayName("an angle past the deadzone is reached the long way around")
  void theLongWayAround() {
    // 2.5 rad is unreachable going forward, but 2.5 - 2*pi is inside the reverse travel.
    assertEquals(2.5 - 2 * Math.PI, TurretAim.toTurretSetpoint(2.5, 0.0), 1e-9);
    assertEquals(3.0 - 2 * Math.PI, TurretAim.toTurretSetpoint(3.0, 0.0), 1e-9);
  }

  @Test
  @DisplayName("a deadzone request is clamped to the soft limit, which is where the turret stops")
  void deadzoneClamps() {
    assertEquals(TurretAim.MAX_ANGLE, TurretAim.toTurretSetpoint(1.8, 0.0), 1e-9);
    assertTrue(TurretAim.inDeadzone(1.8, 0.0));
  }

  @Test
  @DisplayName("the starting offset of pi shifts every request by half a turn")
  void startingOffset() {
    // A request of 0 with the robot's real offset points the turret at -pi.
    assertEquals(-Math.PI, TurretAim.toTurretSetpoint(0.0, TurretAim.STARTING_OFFSET), 1e-9);
    // A request of -pi/2 becomes +pi/2 after the offset and the wrap.
    assertEquals(Math.PI / 2, TurretAim.toTurretSetpoint(-Math.PI / 2, TurretAim.STARTING_OFFSET), 1e-9);
  }

  @Test
  @DisplayName("requests outside one turn are wrapped before anything else happens")
  void wrapsFirst() {
    assertEquals(TurretAim.wrap(-5.0), TurretAim.toTurretSetpoint(-5.0, 0.0), 1e-9);
    assertEquals(0.5, TurretAim.toTurretSetpoint(0.5 + 2 * Math.PI, 0.0), 1e-9);
  }
}
