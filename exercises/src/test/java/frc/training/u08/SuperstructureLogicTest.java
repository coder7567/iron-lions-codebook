package frc.training.u08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u08.SuperstructureLogic.Alliance;
import frc.training.u08.SuperstructureLogic.AllianceWatcher;
import frc.training.u08.SuperstructureLogic.IntakeWanted;
import frc.training.u08.SuperstructureLogic.Requests;
import frc.training.u08.SuperstructureLogic.SuperState;
import frc.training.u08.SuperstructureLogic.TurretCurrent;
import frc.training.u08.SuperstructureLogic.TurretWanted;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SuperstructureLogicTest {
  @Test
  @DisplayName("SHOOTING runs the intake; EJECTING keeps the turret shooting while the intake reverses")
  void shootingAndEjecting() {
    assertEquals(
        new Requests(TurretWanted.SHOOTING, IntakeWanted.INTAKING),
        SuperstructureLogic.requestsFor(SuperState.SHOOTING, true));
    assertEquals(
        new Requests(TurretWanted.SHOOTING, IntakeWanted.REVERSING),
        SuperstructureLogic.requestsFor(SuperState.EJECTING, false));
  }

  @Test
  @DisplayName("PAUSED and TESTING pass straight through to both mechanisms")
  void pausedAndTesting() {
    assertEquals(
        new Requests(TurretWanted.PAUSED, IntakeWanted.PAUSED),
        SuperstructureLogic.requestsFor(SuperState.PAUSED, true));
    assertEquals(
        new Requests(TurretWanted.TESTING, IntakeWanted.TESTING),
        SuperstructureLogic.requestsFor(SuperState.TESTING, false));
  }

  @Test
  @DisplayName("in IDLE, the intake arm only returns to rest once the turret is intake-safe")
  void idleWaitsForTurretSafety() {
    assertEquals(
        new Requests(TurretWanted.IDLE, IntakeWanted.IDLE),
        SuperstructureLogic.requestsFor(SuperState.IDLE, true));
    assertEquals(
        new Requests(TurretWanted.IDLE, IntakeWanted.PAUSED),
        SuperstructureLogic.requestsFor(SuperState.IDLE, false));
  }

  @Test
  @DisplayName("every state produces a request for both mechanisms")
  void everyStateHandled() {
    for (SuperState state : SuperState.values()) {
      for (boolean safe : new boolean[] {true, false}) {
        Requests requests = SuperstructureLogic.requestsFor(state, safe);
        assertNotNull(requests, state.name());
        assertNotNull(requests.turret(), state.name());
        assertNotNull(requests.intake(), state.name());
      }
    }
  }

  @Test
  @DisplayName("rumble for shooting into an inactive HUB, a jam, or a deadzone target")
  void rumble() {
    assertTrue(SuperstructureLogic.rumble(TurretCurrent.SHOOTING, false, false, false));
    assertFalse(SuperstructureLogic.rumble(TurretCurrent.SHOOTING, true, false, false));
    assertFalse(SuperstructureLogic.rumble(TurretCurrent.PASSING, false, false, false));
    assertTrue(SuperstructureLogic.rumble(TurretCurrent.IDLE, true, true, false));
    assertTrue(SuperstructureLogic.rumble(TurretCurrent.PASSING, true, false, true));
    assertFalse(SuperstructureLogic.rumble(TurretCurrent.IDLE, false, false, false));
  }

  @Test
  @DisplayName("the alliance watcher reports only real changes and ignores unknown alliances")
  void allianceWatcher() {
    AllianceWatcher watcher = new AllianceWatcher();

    assertFalse(watcher.update(null));
    assertFalse(watcher.update(Alliance.BLUE));
    assertTrue(watcher.update(Alliance.RED));
    assertFalse(watcher.update(Alliance.RED));
    assertFalse(watcher.update(null));
    assertTrue(watcher.update(Alliance.BLUE));
  }
}
