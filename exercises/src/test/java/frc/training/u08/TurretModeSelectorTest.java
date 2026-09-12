package frc.training.u08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u08.TurretModeSelector.CurrentState;
import frc.training.u08.TurretModeSelector.WantedState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TurretModeSelectorTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("zone edges are 5 m from each wall, mirrored for red")
  void zoneEdges() {
    assertEquals(5.0, TurretModeSelector.ourZoneEdgeX(false), EPSILON);
    assertEquals(11.541, TurretModeSelector.ourZoneEdgeX(true), EPSILON);
    assertEquals(11.541, TurretModeSelector.theirZoneEdgeX(false), EPSILON);
    assertEquals(5.0, TurretModeSelector.theirZoneEdgeX(true), EPSILON);
  }

  @Test
  @DisplayName("our side of a line depends on which wall is ours, and the line itself is not our side")
  void onOurSide() {
    assertTrue(TurretModeSelector.onOurSide(4.9, 5.0, false));
    assertFalse(TurretModeSelector.onOurSide(5.0, 5.0, false));
    assertFalse(TurretModeSelector.onOurSide(5.1, 5.0, false));

    double redEdge = TurretModeSelector.ourZoneEdgeX(true);
    assertTrue(TurretModeSelector.onOurSide(redEdge + 0.1, redEdge, true));
    assertFalse(TurretModeSelector.onOurSide(redEdge, redEdge, true));
    assertFalse(TurretModeSelector.onOurSide(redEdge - 0.1, redEdge, true));
  }

  @Test
  @DisplayName("SHOOTING shoots from our zone and passes from everywhere else, on blue")
  void shootingBlue() {
    assertEquals(CurrentState.SHOOTING, TurretModeSelector.select(WantedState.SHOOTING, 3.0, false));
    assertEquals(CurrentState.PASSING, TurretModeSelector.select(WantedState.SHOOTING, 8.0, false));
    assertEquals(CurrentState.PASSING, TurretModeSelector.select(WantedState.SHOOTING, 13.0, false));
  }

  @Test
  @DisplayName("SHOOTING shoots from our zone and passes from everywhere else, on red")
  void shootingRed() {
    assertEquals(CurrentState.SHOOTING, TurretModeSelector.select(WantedState.SHOOTING, 13.5, true));
    assertEquals(CurrentState.PASSING, TurretModeSelector.select(WantedState.SHOOTING, 8.0, true));
    assertEquals(CurrentState.PASSING, TurretModeSelector.select(WantedState.SHOOTING, 3.0, true));
  }

  @Test
  @DisplayName("PAUSED (F1 fixed) holds a shooting aim, a passing aim, or idles, by zone, on blue")
  void pausedBlue() {
    assertEquals(CurrentState.PAUSEDSHOOTING, TurretModeSelector.select(WantedState.PAUSED, 3.0, false));
    assertEquals(CurrentState.PAUSEDPASSING, TurretModeSelector.select(WantedState.PAUSED, 8.0, false));
    assertEquals(CurrentState.IDLE, TurretModeSelector.select(WantedState.PAUSED, 13.0, false));
  }

  @Test
  @DisplayName("PAUSED (F1 fixed) holds a shooting aim, a passing aim, or idles, by zone, on red")
  void pausedRed() {
    assertEquals(CurrentState.PAUSEDSHOOTING, TurretModeSelector.select(WantedState.PAUSED, 13.5, true));
    assertEquals(CurrentState.PAUSEDPASSING, TurretModeSelector.select(WantedState.PAUSED, 8.0, true));
    assertEquals(CurrentState.IDLE, TurretModeSelector.select(WantedState.PAUSED, 3.0, true));
  }

  @Test
  @DisplayName("IDLE and TESTING ignore the robot's position")
  void idleAndTestingIgnorePosition() {
    for (double x : new double[] {1.0, 8.0, 15.0}) {
      for (boolean isRed : new boolean[] {false, true}) {
        assertEquals(CurrentState.IDLE, TurretModeSelector.select(WantedState.IDLE, x, isRed));
        assertEquals(CurrentState.TESTING, TurretModeSelector.select(WantedState.TESTING, x, isRed));
      }
    }
  }
}
