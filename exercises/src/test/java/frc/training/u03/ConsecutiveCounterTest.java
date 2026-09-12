package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsecutiveCounterTest {
  @Test
  @DisplayName("the default constructor requires 50 loops (one second)")
  void defaultLoops() {
    assertEquals(50, new ConsecutiveCounter().getRequiredLoops());
  }

  @Test
  @DisplayName("required loops below 1 become 1")
  void minimumOneLoop() {
    assertEquals(1, new ConsecutiveCounter(0).getRequiredLoops());
    assertEquals(1, new ConsecutiveCounter(-5).getRequiredLoops());
  }

  @Test
  @DisplayName("triggers only after enough consecutive true loops, and a false loop resets")
  void streaks() {
    ConsecutiveCounter counter = new ConsecutiveCounter(3);
    assertFalse(counter.update(true));
    assertFalse(counter.update(true));
    assertTrue(counter.update(true));
    assertTrue(counter.update(true));
    assertEquals(4, counter.getCount());
    assertFalse(counter.update(false));
    assertEquals(0, counter.getCount());
    assertFalse(counter.update(true));
    assertFalse(counter.isTriggered());
  }

  @Test
  @DisplayName("reset clears the streak")
  void reset() {
    ConsecutiveCounter counter = new ConsecutiveCounter(2);
    counter.update(true);
    counter.update(true);
    assertTrue(counter.isTriggered());
    counter.reset();
    assertFalse(counter.isTriggered());
    assertEquals(0, counter.getCount());
  }

  @Test
  @DisplayName("each counter object keeps its own streak")
  void separateObjects() {
    ConsecutiveCounter intake = new ConsecutiveCounter(2);
    ConsecutiveCounter feeder = new ConsecutiveCounter(2);
    intake.update(true);
    intake.update(true);
    assertTrue(intake.isTriggered());
    assertFalse(feeder.isTriggered());
  }

  @Test
  @DisplayName("the static created count is shared by the class")
  void createdCount() {
    int before = ConsecutiveCounter.getCreatedCount();
    new ConsecutiveCounter();
    new ConsecutiveCounter(10);
    new ConsecutiveCounter(3);
    assertEquals(before + 3, ConsecutiveCounter.getCreatedCount());
  }

  @Test
  @DisplayName("loopsForSeconds rounds up to whole 20 ms loops")
  void loopsForSeconds() {
    assertEquals(50, ConsecutiveCounter.loopsForSeconds(1.0));
    assertEquals(25, ConsecutiveCounter.loopsForSeconds(0.5));
    assertEquals(3, ConsecutiveCounter.loopsForSeconds(0.05));
    assertEquals(1, ConsecutiveCounter.loopsForSeconds(0.02));
    assertEquals(0, ConsecutiveCounter.loopsForSeconds(0.0));
  }

  @Test
  @DisplayName("fields are encapsulated: every non-constant field is private")
  void fieldsArePrivate() {
    int instanceFields = 0;
    for (Field field : ConsecutiveCounter.class.getDeclaredFields()) {
      int modifiers = field.getModifiers();
      if (Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
        continue;
      }
      assertTrue(Modifier.isPrivate(modifiers), field.getName() + " should be private");
      if (!Modifier.isStatic(modifiers)) {
        instanceFields++;
      }
    }
    assertTrue(instanceFields >= 2, "expected private fields for the required loops and the current count");
  }
}
