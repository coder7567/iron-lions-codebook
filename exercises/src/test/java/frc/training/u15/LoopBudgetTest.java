package frc.training.u15;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u15.LoopBudget.Section;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoopBudgetTest {
  private static final double EPSILON = 1e-9;
  private LoopBudget budget;

  @BeforeEach
  void setUp() {
    budget = new LoopBudget(0.02);
  }

  @Test
  @DisplayName("sections add up within the loop in progress")
  void currentLoop() {
    budget.record("Drive.periodic", 0.004);
    budget.record("Vision.periodic", 0.003);

    assertEquals(0.007, budget.currentLoopSeconds(), EPSILON);
    assertEquals(0, budget.loopCount());
  }

  @Test
  @DisplayName("a loop under budget is counted but is not an overrun")
  void underBudget() {
    budget.record("Drive.periodic", 0.004);
    budget.record("Vision.periodic", 0.003);

    assertFalse(budget.endLoop());
    assertEquals(1, budget.loopCount());
    assertEquals(0, budget.overrunCount());
    assertEquals(0.0, budget.currentLoopSeconds(), EPSILON);
  }

  @Test
  @DisplayName("a loop over budget is an overrun; exactly at budget is not")
  void overBudget() {
    budget.record("Vision.periodic", 0.025);
    assertTrue(budget.endLoop());
    assertEquals(1, budget.overrunCount());

    budget.record("Vision.periodic", 0.02);
    assertFalse(budget.endLoop());
    assertEquals(1, budget.overrunCount());
    assertEquals(2, budget.loopCount());
  }

  @Test
  @DisplayName("recording the same section twice in one loop adds the times together")
  void sameSectionTwice() {
    budget.record("Drive.periodic", 0.002);
    budget.record("Drive.periodic", 0.002);

    assertEquals(0.004, budget.currentLoopSeconds(), EPSILON);
    budget.endLoop();
    assertEquals(new Section("Drive.periodic", 0.004), budget.worstSection());
  }

  @Test
  @DisplayName("the worst section is the biggest total across every loop, not the biggest single loop")
  void worstAcrossLoops() {
    budget.record("Drive.periodic", 0.004);
    budget.record("Vision.periodic", 0.010);
    budget.endLoop();

    budget.record("Drive.periodic", 0.004);
    budget.endLoop();

    budget.record("Drive.periodic", 0.004);
    budget.endLoop();

    Section worst = budget.worstSection();
    assertEquals("Drive.periodic", worst.name());
    assertEquals(0.012, worst.seconds(), 1e-9);
  }

  @Test
  @DisplayName("the average is over completed loops")
  void average() {
    budget.record("a", 0.010);
    budget.endLoop();
    budget.record("a", 0.020);
    budget.endLoop();
    budget.record("a", 0.030);
    budget.endLoop();

    assertEquals(0.020, budget.averageLoopSeconds(), 1e-9);
  }

  @Test
  @DisplayName("nothing recorded means nothing to report")
  void nothingRecorded() {
    assertNull(budget.worstSection());
    assertEquals(0.0, budget.averageLoopSeconds(), EPSILON);

    assertFalse(budget.endLoop());
    assertEquals(1, budget.loopCount());
    assertEquals(0.0, budget.averageLoopSeconds(), EPSILON);
    assertNull(budget.worstSection());
  }
}
