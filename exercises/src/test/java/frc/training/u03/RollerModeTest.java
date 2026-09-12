package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RollerModeTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("the enum declares exactly four modes, in cycle order")
  void declaredModes() {
    assertArrayEquals(
        new RollerMode[] {RollerMode.STOPPED, RollerMode.INTAKING, RollerMode.REVERSING, RollerMode.PAUSED},
        RollerMode.values());
  }

  @Test
  @DisplayName("each mode carries the intake's real speeds")
  void speeds() {
    assertEquals(5000.0, RollerMode.INTAKING.getIntakeRpm(), EPSILON);
    assertEquals(4500.0, RollerMode.INTAKING.getFeederRpm(), EPSILON);
    assertEquals(-5000.0, RollerMode.REVERSING.getIntakeRpm(), EPSILON);
    assertEquals(-4500.0, RollerMode.REVERSING.getFeederRpm(), EPSILON);
    assertEquals(0.0, RollerMode.STOPPED.getIntakeRpm(), EPSILON);
    assertEquals(0.0, RollerMode.PAUSED.getFeederRpm(), EPSILON);
  }

  @Test
  @DisplayName("isMoving is true only when a roller spins")
  void moving() {
    assertTrue(RollerMode.INTAKING.isMoving());
    assertTrue(RollerMode.REVERSING.isMoving());
    assertFalse(RollerMode.STOPPED.isMoving());
    assertFalse(RollerMode.PAUSED.isMoving());
  }

  @Test
  @DisplayName("the arm is deployed in every mode except STOPPED")
  void arm() {
    assertFalse(RollerMode.STOPPED.armDeployed());
    assertTrue(RollerMode.INTAKING.armDeployed());
    assertTrue(RollerMode.REVERSING.armDeployed());
    assertTrue(RollerMode.PAUSED.armDeployed());
  }

  @Test
  @DisplayName("fromText ignores case and spaces, and falls back to STOPPED")
  void fromText() {
    assertSame(RollerMode.INTAKING, RollerMode.fromText(" intaking "));
    assertSame(RollerMode.REVERSING, RollerMode.fromText("REVERSING"));
    assertSame(RollerMode.PAUSED, RollerMode.fromText("Paused"));
    assertSame(RollerMode.STOPPED, RollerMode.fromText("jammed"));
    assertSame(RollerMode.STOPPED, RollerMode.fromText(null));
  }

  @Test
  @DisplayName("next cycles through the modes and wraps around")
  void next() {
    assertSame(RollerMode.INTAKING, RollerMode.STOPPED.next());
    assertSame(RollerMode.REVERSING, RollerMode.INTAKING.next());
    assertSame(RollerMode.PAUSED, RollerMode.REVERSING.next());
    assertSame(RollerMode.STOPPED, RollerMode.PAUSED.next());
  }
}
