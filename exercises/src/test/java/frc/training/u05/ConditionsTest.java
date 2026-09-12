package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConditionsTest {
  /** A pretend controller whose values the tests change between calls. */
  static class Pad {
    double rightTriggerAxis = 0.0;
    boolean leftBumper = false;

    double getRightTriggerAxis() {
      return rightTriggerAxis;
    }

    boolean getLeftBumper() {
      return leftBumper;
    }
  }

  /** Feeds a scripted sequence of values, one per call. */
  static BooleanSupplier sequence(boolean... values) {
    int[] index = {0};
    return () -> values[Math.min(index[0]++, values.length - 1)];
  }

  static boolean[] run(BooleanSupplier supplier, int calls) {
    boolean[] results = new boolean[calls];
    for (int i = 0; i < calls; i++) {
      results[i] = supplier.getAsBoolean();
    }
    return results;
  }

  @Test
  @DisplayName("and/or/not combine conditions")
  void basicLogic() {
    assertTrue(Conditions.and(() -> true, () -> true).getAsBoolean());
    assertFalse(Conditions.and(() -> true, () -> false).getAsBoolean());
    assertTrue(Conditions.or(() -> false, () -> true).getAsBoolean());
    assertFalse(Conditions.or(() -> false, () -> false).getAsBoolean());
    assertTrue(Conditions.not(() -> false).getAsBoolean());
  }

  @Test
  @DisplayName("and and or short-circuit like && and ||")
  void shortCircuit() {
    int[] calls = {0};
    BooleanSupplier counted = () -> {
      calls[0]++;
      return true;
    };
    Conditions.and(() -> false, counted).getAsBoolean();
    Conditions.or(() -> true, counted).getAsBoolean();
    assertEquals(0, calls[0]);
  }

  @Test
  @DisplayName("conditions are re-checked every call, using method references to live values")
  void liveValues() {
    Pad pad = new Pad();
    BooleanSupplier shoot = Conditions.above(pad::getRightTriggerAxis, 0.5);
    BooleanSupplier eject = Conditions.and(pad::getLeftBumper, Conditions.not(shoot));
    assertFalse(shoot.getAsBoolean());
    pad.rightTriggerAxis = 0.9;
    assertTrue(shoot.getAsBoolean());
    pad.rightTriggerAxis = 0.5;
    assertFalse(shoot.getAsBoolean(), "exactly at the threshold is not above it");
    pad.leftBumper = true;
    assertTrue(eject.getAsBoolean());
  }

  @Test
  @DisplayName("risingEdge fires once per press")
  void risingEdge() {
    BooleanSupplier edge = Conditions.risingEdge(sequence(false, true, true, false, true));
    boolean[] expected = {false, true, false, false, true};
    boolean[] actual = run(edge, 5);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i], "call " + i);
    }
  }

  @Test
  @DisplayName("a condition that starts true counts as a press on the first call")
  void risingEdgeStartsFalse() {
    BooleanSupplier edge = Conditions.risingEdge(() -> true);
    assertTrue(edge.getAsBoolean());
    assertFalse(edge.getAsBoolean());
  }

  @Test
  @DisplayName("each risingEdge supplier keeps its own memory")
  void separateState() {
    Pad pad = new Pad();
    BooleanSupplier first = Conditions.risingEdge(pad::getLeftBumper);
    BooleanSupplier second = Conditions.risingEdge(pad::getLeftBumper);
    pad.leftBumper = true;
    assertTrue(first.getAsBoolean());
    assertTrue(second.getAsBoolean());
    assertFalse(first.getAsBoolean());
  }

  @Test
  @DisplayName("debounced waits for enough consecutive true calls")
  void debounced() {
    BooleanSupplier stable = Conditions.debounced(sequence(true, true, true, true, false, true), 3);
    boolean[] expected = {false, false, true, true, false, false};
    boolean[] actual = run(stable, 6);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i], "call " + i);
    }
    assertTrue(Conditions.debounced(() -> true, 0).getAsBoolean());
  }
}
