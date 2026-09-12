package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoseObservationTest {
  private static final double FIELD_LENGTH = 16.541;
  private static final double FIELD_WIDTH = 8.069;
  private static final double EPSILON = 1e-9;

  private static PoseObservation obs(double x, double y, double timestamp, double ambiguity, int tags) {
    return new PoseObservation(x, y, 0.0, timestamp, ambiguity, tags, 3.0);
  }

  @Test
  @DisplayName("records provide accessors and value equality")
  void recordBasics() {
    PoseObservation a = obs(4.6, 4.0, 10.0, 0.1, 2);
    PoseObservation b = obs(4.6, 4.0, 10.0, 0.1, 2);
    assertEquals(4.6, a.x(), EPSILON);
    assertEquals(2, a.tagCount());
    assertEquals(a, b);
    assertTrue(a.toString().startsWith("PoseObservation["));
  }

  @Test
  @DisplayName("isMultiTag is true for two or more tags")
  void multiTag() {
    assertTrue(obs(1, 1, 0, 0.0, 2).isMultiTag());
    assertFalse(obs(1, 1, 0, 0.0, 1).isMultiTag());
  }

  @Test
  @DisplayName("distanceTo measures straight-line distance")
  void distance() {
    assertEquals(5.0, obs(4, 5, 0, 0.0, 1).distanceTo(1, 1), EPSILON);
    assertEquals(0.0, obs(2, 2, 0, 0.0, 1).distanceTo(2, 2), EPSILON);
  }

  @Test
  @DisplayName("withTimestamp returns a changed copy and leaves the original alone")
  void withTimestamp() {
    PoseObservation original = obs(4.6, 4.0, 10.0, 0.1, 2);
    PoseObservation later = original.withTimestamp(10.5);
    assertNotSame(original, later);
    assertEquals(10.5, later.timestampSeconds(), EPSILON);
    assertEquals(10.0, original.timestampSeconds(), EPSILON);
    assertEquals(original.x(), later.x(), EPSILON);
    assertEquals(original.tagCount(), later.tagCount());
  }

  @Test
  @DisplayName("the compact constructor rejects impossible observations")
  void validation() {
    assertThrows(IllegalArgumentException.class, () -> obs(1, 1, 0, 0.1, 0));
    assertThrows(IllegalArgumentException.class, () -> obs(1, 1, 0, -0.1, 1));
    assertThrows(IllegalArgumentException.class, () -> obs(1, 1, 0, 1.5, 1));
    assertDoesNotThrow(() -> obs(1, 1, 0, 0.0, 1));
    assertDoesNotThrow(() -> obs(1, 1, 0, 1.0, 3));
  }

  @Test
  @DisplayName("filter keeps on-field, unambiguous observations in order without changing the input")
  void filter() {
    PoseObservation good1 = obs(4.6, 4.0, 1.0, 0.05, 2);
    PoseObservation offFieldX = obs(-0.2, 4.0, 2.0, 0.05, 2);
    PoseObservation offFieldY = obs(4.6, 8.2, 3.0, 0.05, 2);
    PoseObservation ambiguous = obs(4.6, 4.0, 4.0, 0.5, 1);
    PoseObservation good2 = obs(12.0, 1.0, 5.0, 0.2, 1);
    List<PoseObservation> all = new ArrayList<>(List.of(good1, offFieldX, offFieldY, ambiguous, good2));

    List<PoseObservation> kept = Observations.filter(all, FIELD_LENGTH, FIELD_WIDTH, 0.2);

    assertEquals(List.of(good1, good2), kept);
    assertEquals(5, all.size(), "the input list must not change");
  }

  @Test
  @DisplayName("newest finds the latest timestamp, or empty for no observations")
  void newest() {
    PoseObservation early = obs(1, 1, 3.0, 0.1, 1);
    PoseObservation late = obs(2, 2, 9.5, 0.1, 1);
    PoseObservation middle = obs(3, 3, 6.0, 0.1, 1);
    assertEquals(Optional.of(late), Observations.newest(List.of(early, late, middle)));
    assertEquals(Optional.empty(), Observations.newest(List.of()));
  }
}
