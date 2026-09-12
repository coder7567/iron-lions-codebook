package frc.training.u06;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u06.FieldFlip.FieldPose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FieldFlipTest {
  private static final double EPSILON = 1e-9;

  private static void assertPose(FieldPose expected, FieldPose actual) {
    assertEquals(expected.x(), actual.x(), EPSILON, "x");
    assertEquals(expected.y(), actual.y(), EPSILON, "y");
    assertEquals(expected.headingRadians(), actual.headingRadians(), EPSILON, "heading");
  }

  @Test
  @DisplayName("the blue HUB center flips to the red HUB center")
  void hubCenters() {
    assertEquals(11.915, FieldFlip.flipX(4.626), EPSILON);
    assertEquals(4.034, FieldFlip.flipY(4.035), EPSILON);
  }

  @Test
  @DisplayName("tag 13 on the red wall flips onto tag 29 on the blue wall, proving rotational symmetry")
  void tagSymmetry() {
    assertEquals(0.008, FieldFlip.flipX(16.533), EPSILON);
    assertEquals(0.666, FieldFlip.flipY(7.403), EPSILON);
  }

  @Test
  @DisplayName("headings turn around by π and stay in (-π, π]")
  void headings() {
    assertEquals(Math.PI, FieldFlip.flipHeading(0.0), EPSILON);
    assertEquals(-Math.PI / 2, FieldFlip.flipHeading(Math.PI / 2), EPSILON);
    assertEquals(Math.PI / 2, FieldFlip.flipHeading(-Math.PI / 2), EPSILON);
    assertEquals(0.0, FieldFlip.flipHeading(Math.PI), EPSILON);
    assertEquals(0.0, FieldFlip.flipHeading(-Math.PI), EPSILON);
  }

  @Test
  @DisplayName("flip converts a whole pose, and flipping twice returns the original")
  void flipPose() {
    FieldPose blue = new FieldPose(4.626, 4.035, 0.0);
    FieldPose red = FieldFlip.flip(blue);
    assertPose(new FieldPose(11.915, 4.034, Math.PI), red);
    assertPose(blue, FieldFlip.flip(red));
    FieldPose angled = new FieldPose(1.0, 6.0, Math.PI / 2);
    assertPose(angled, FieldFlip.flip(FieldFlip.flip(angled)));
  }

  @Test
  @DisplayName("forAlliance only flips for red")
  void forAlliance() {
    FieldPose blue = new FieldPose(1.0, 6.0, 0.25);
    assertPose(blue, FieldFlip.forAlliance(blue, false));
    assertPose(FieldFlip.flip(blue), FieldFlip.forAlliance(blue, true));
  }

  @Test
  @DisplayName("inAllianceZone checks the correct end of the field")
  void allianceZone() {
    assertTrue(FieldFlip.inAllianceZone(3.0, false, 4.03));
    assertFalse(FieldFlip.inAllianceZone(5.0, false, 4.03));
    assertTrue(FieldFlip.inAllianceZone(13.0, true, 4.03));
    assertFalse(FieldFlip.inAllianceZone(11.9, true, 4.03));
  }
}
