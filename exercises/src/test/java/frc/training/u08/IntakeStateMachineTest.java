package frc.training.u08;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u08.IntakeStateMachine.CurrentState;
import frc.training.u08.IntakeStateMachine.Inputs;
import frc.training.u08.IntakeStateMachine.WantedState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class IntakeStateMachineTest {
  /** High current, low speed, arm at its setpoint. */
  private static final Inputs JAM_ARM_OUT = new Inputs(40, 200, 0.77, 0.77);
  /** Normal intaking, arm at its setpoint. */
  private static final Inputs NORMAL_ARM_OUT = new Inputs(15, 5000, 0.77, 0.77);
  /** High current, low speed, arm still moving out. */
  private static final Inputs JAM_ARM_MOVING = new Inputs(40, 200, 0.30, 0.77);

  private IntakeStateMachine intake;

  @BeforeEach
  void setUp() {
    intake = new IntakeStateMachine();
  }

  private CurrentState runLoops(WantedState wanted, Inputs inputs, int loops) {
    CurrentState last = null;
    for (int i = 0; i < loops; i++) {
      last = intake.update(wanted, inputs);
    }
    return last;
  }

  @Test
  @DisplayName("IDLE, PAUSED, and REVERSING pass through; TESTING runs INTAKING")
  void passThrough() {
    assertEquals(CurrentState.IDLE, intake.update(WantedState.IDLE, NORMAL_ARM_OUT));
    assertEquals(CurrentState.PAUSED, intake.update(WantedState.PAUSED, NORMAL_ARM_OUT));
    assertEquals(CurrentState.REVERSING, intake.update(WantedState.REVERSING, NORMAL_ARM_OUT));
    assertEquals(CurrentState.INTAKING, intake.update(WantedState.TESTING, NORMAL_ARM_OUT));
  }

  @Test
  @DisplayName("a current spike shorter than 50 loops is not a jam")
  void briefSpikeIsNotAJam() {
    assertEquals(CurrentState.INTAKING, runLoops(WantedState.INTAKING, JAM_ARM_OUT, 49));
    assertEquals(CurrentState.INTAKING, intake.update(WantedState.INTAKING, NORMAL_ARM_OUT));
    assertEquals(CurrentState.INTAKING, runLoops(WantedState.INTAKING, JAM_ARM_OUT, 49));
    assertFalse(intake.isJammed());
  }

  @Test
  @DisplayName("50 jam-like loops in a row detect a jam and start reversing on that loop")
  void sustainedJamReverses() {
    assertEquals(CurrentState.INTAKING, runLoops(WantedState.INTAKING, JAM_ARM_OUT, 49));
    assertFalse(intake.isJammed());

    assertEquals(CurrentState.REVERSING, intake.update(WantedState.INTAKING, JAM_ARM_OUT));
    assertTrue(intake.isJammed());
  }

  @Test
  @DisplayName("recovery reverses for 10 loops, then goes back to intaking")
  void reversesForTenLoopsThenResumes() {
    assertEquals(CurrentState.REVERSING, runLoops(WantedState.INTAKING, JAM_ARM_OUT, 50)); // reverse loop 1

    for (int loop = 2; loop <= 9; loop++) {
      assertEquals(CurrentState.REVERSING, intake.update(WantedState.INTAKING, NORMAL_ARM_OUT), "loop " + loop);
      assertTrue(intake.isJammed(), "loop " + loop);
    }
    assertEquals(CurrentState.REVERSING, intake.update(WantedState.INTAKING, NORMAL_ARM_OUT)); // loop 10
    assertFalse(intake.isJammed());

    assertEquals(CurrentState.INTAKING, intake.update(WantedState.INTAKING, NORMAL_ARM_OUT));
  }

  @Test
  @DisplayName("a detected jam waits to reverse until the arm is out")
  void jamWaitsForTheArm() {
    assertEquals(CurrentState.INTAKING, runLoops(WantedState.INTAKING, JAM_ARM_MOVING, 60));
    assertTrue(intake.isJammed());

    assertEquals(CurrentState.REVERSING, intake.update(WantedState.INTAKING, JAM_ARM_OUT));
  }

  @Test
  @DisplayName("leaving INTAKING clears the jam and restarts the count")
  void leavingIntakingClearsTheJam() {
    runLoops(WantedState.INTAKING, JAM_ARM_MOVING, 50);
    assertTrue(intake.isJammed());

    assertEquals(CurrentState.PAUSED, intake.update(WantedState.PAUSED, NORMAL_ARM_OUT));
    assertFalse(intake.isJammed());

    assertEquals(CurrentState.INTAKING, runLoops(WantedState.INTAKING, JAM_ARM_OUT, 49));
    assertFalse(intake.isJammed());
  }

  @Test
  @DisplayName("TESTING never detects a jam")
  void testingNeverDetectsJams() {
    assertEquals(CurrentState.INTAKING, runLoops(WantedState.TESTING, JAM_ARM_OUT, 100));
    assertFalse(intake.isJammed());
  }
}
