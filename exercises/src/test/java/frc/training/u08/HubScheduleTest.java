package frc.training.u08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u08.HubSchedule.Alliance;
import frc.training.u08.HubSchedule.Mode;
import frc.training.u08.HubSchedule.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HubScheduleTest {
  private static Status teleop(double matchTime, String gameData, Alliance alliance) {
    return HubSchedule.status(matchTime, gameData, alliance, Mode.TELEOP);
  }

  @Test
  @DisplayName("game data 'R' means red is inactive first; anything else falls back to false")
  void gameData() {
    assertTrue(HubSchedule.redInactiveFirst("R"));
    assertFalse(HubSchedule.redInactiveFirst("B"));
    assertFalse(HubSchedule.redInactiveFirst(""));
    assertFalse(HubSchedule.redInactiveFirst(null));
  }

  @Test
  @DisplayName("an unknown alliance is treated as inactive")
  void unknownAlliance() {
    assertEquals(new Status(false, 0.0), teleop(100.0, "R", null));
  }

  @Test
  @DisplayName("both HUBs are active in autonomous; disabled counts as inactive")
  void autonomousAndDisabled() {
    assertEquals(new Status(true, 15.0), HubSchedule.status(15.0, "", Alliance.BLUE, Mode.AUTONOMOUS));
    assertEquals(new Status(false, 0.0), HubSchedule.status(100.0, "R", Alliance.RED, Mode.DISABLED));
  }

  @Test
  @DisplayName("the transition period is active for both alliances")
  void transition() {
    assertEquals(new Status(true, 5.0), teleop(135.0, "R", Alliance.RED));
    assertEquals(new Status(true, 5.0), teleop(135.0, "R", Alliance.BLUE));
  }

  @Test
  @DisplayName("with 'B', blue is inactive in shifts 1 and 3 and red is inactive in shifts 2 and 4")
  void blueInactiveFirst() {
    assertEquals(new Status(false, 15.0), teleop(120.0, "B", Alliance.BLUE));
    assertEquals(new Status(true, 15.0), teleop(95.0, "B", Alliance.BLUE));
    assertEquals(new Status(false, 15.0), teleop(70.0, "B", Alliance.BLUE));
    assertEquals(new Status(true, 10.0), teleop(40.0, "B", Alliance.BLUE));
    assertEquals(new Status(true, 20.0), teleop(20.0, "B", Alliance.BLUE));

    assertEquals(new Status(true, 15.0), teleop(120.0, "B", Alliance.RED));
    assertEquals(new Status(false, 15.0), teleop(95.0, "B", Alliance.RED));
    assertEquals(new Status(true, 15.0), teleop(70.0, "B", Alliance.RED));
    assertEquals(new Status(false, 10.0), teleop(40.0, "B", Alliance.RED));
  }

  @Test
  @DisplayName("with 'R', red is inactive in shift 1 and blue is active")
  void redInactiveFirst() {
    assertEquals(new Status(false, 15.0), teleop(120.0, "R", Alliance.RED));
    assertEquals(new Status(true, 15.0), teleop(120.0, "R", Alliance.BLUE));
    assertEquals(new Status(true, 15.0), teleop(95.0, "R", Alliance.RED));
  }

  @Test
  @DisplayName("each boundary time belongs to the later period")
  void boundaries() {
    assertEquals(new Status(false, 25.0), teleop(130.0, "B", Alliance.BLUE)); // shift 1 begins
    assertEquals(new Status(true, 25.0), teleop(105.0, "B", Alliance.BLUE)); // shift 2 begins
    assertEquals(new Status(true, 30.0), teleop(30.0, "B", Alliance.BLUE)); // end game begins
  }

  @Test
  @DisplayName("missing game data falls back exactly like 'B'")
  void missingGameData() {
    assertEquals(teleop(120.0, "B", Alliance.BLUE), teleop(120.0, "", Alliance.BLUE));
    assertEquals(teleop(95.0, "B", Alliance.RED), teleop(95.0, null, Alliance.RED));
  }
}
