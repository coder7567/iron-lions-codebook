package frc.training.u13;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u13.VisionFilter.PoseObservation;
import frc.training.u13.VisionFilter.RobotSpeeds;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VisionFilterTest {
  private static final RobotSpeeds STILL = new RobotSpeeds(0.0, 0.0, 0.0);

  /** A clean two-tag observation near the middle of the field. */
  private static PoseObservation good() {
    return new PoseObservation(0.05, 8.0, 4.0, 0.1, 12.34, 3.0, 2);
  }

  @Test
  @DisplayName("a clean observation from a stationary robot is accepted")
  void accepted() {
    assertEquals("accepted", VisionFilter.reason(good(), STILL));
    assertFalse(VisionFilter.reject(good(), STILL));
  }

  @Test
  @DisplayName("an ambiguous single-tag solve is rejected")
  void ambiguity() {
    PoseObservation ambiguous = new PoseObservation(0.35, 8.0, 4.0, 0.1, 1.0, 4.0, 1);

    assertEquals("ambiguity", VisionFilter.reason(ambiguous, STILL));
    assertTrue(VisionFilter.reject(ambiguous, STILL));
  }

  @Test
  @DisplayName("a pose floating above the carpet is rejected")
  void height() {
    PoseObservation floating = new PoseObservation(0.05, 8.0, 4.0, 1.2, 1.0, 3.0, 2);

    assertEquals("z", VisionFilter.reason(floating, STILL));
  }

  @Test
  @DisplayName("a pose outside the field is rejected, on any side")
  void offField() {
    assertEquals("off field", VisionFilter.reason(new PoseObservation(0.05, -0.5, 4.0, 0.1, 1.0, 3.0, 2), STILL));
    assertEquals("off field", VisionFilter.reason(new PoseObservation(0.05, 8.0, -0.2, 0.1, 1.0, 3.0, 2), STILL));
    assertEquals("off field", VisionFilter.reason(new PoseObservation(0.05, 17.0, 4.0, 0.1, 1.0, 3.0, 2), STILL));
    assertEquals("off field", VisionFilter.reason(new PoseObservation(0.05, 8.0, 9.0, 0.1, 1.0, 3.0, 2), STILL));
  }

  @Test
  @DisplayName("observations taken while the robot moves or spins quickly are rejected")
  void motion() {
    assertEquals("too fast", VisionFilter.reason(good(), new RobotSpeeds(2.5, 0.0, 0.0)));
    assertEquals("too fast", VisionFilter.reason(good(), new RobotSpeeds(1.8, 1.8, 0.0)));
    assertEquals("turning too fast", VisionFilter.reason(good(), new RobotSpeeds(0.5, 0.0, 3.0)));
    assertEquals("turning too fast", VisionFilter.reason(good(), new RobotSpeeds(0.0, 0.0, -3.0)));
  }

  @Test
  @DisplayName("values exactly at a limit are accepted, because every comparison is strict")
  void boundaries() {
    assertEquals("accepted", VisionFilter.reason(new PoseObservation(0.2, 8.0, 4.0, 0.75, 1.0, 3.0, 2), STILL));
    assertEquals("accepted", VisionFilter.reason(good(), new RobotSpeeds(2.0, 0.0, 2.5)));
    assertEquals("accepted", VisionFilter.reason(new PoseObservation(0.05, 0.0, 0.0, 0.0, 1.0, 3.0, 2), STILL));
    assertEquals(
        "accepted", VisionFilter.reason(new PoseObservation(0.05, 16.541, 8.069, 0.0, 1.0, 3.0, 2), STILL));
  }

  @Test
  @DisplayName("filtering a list keeps the good observations in order")
  void filterList() {
    PoseObservation first = good();
    PoseObservation bad = new PoseObservation(0.9, 8.0, 4.0, 0.1, 2.0, 3.0, 1);
    PoseObservation last = new PoseObservation(0.02, 3.0, 2.0, 0.05, 3.0, 2.0, 3);

    List<PoseObservation> kept = VisionFilter.accept(List.of(first, bad, last), STILL);

    assertEquals(List.of(first, last), kept);
  }
}
