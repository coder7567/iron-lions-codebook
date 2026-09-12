package frc.training.u14;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import frc.training.u14.PassingTargets.Translation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PassingTargetsTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("flipping mirrors both coordinates for the red alliance")
  void flipping() {
    assertEquals(5.0, PassingTargets.flipX(5.0, false), EPSILON);
    assertEquals(11.541, PassingTargets.flipX(5.0, true), 1e-9);
    assertEquals(6.0, PassingTargets.flipY(6.0, false), EPSILON);
    assertEquals(2.069, PassingTargets.flipY(6.0, true), 1e-9);

    assertEquals(new Translation(1.0, 6.0), PassingTargets.flip(new Translation(1.0, 6.0), false));
    Translation red = PassingTargets.flip(new Translation(1.0, 6.0), true);
    assertEquals(15.541, red.x(), 1e-9);
    assertEquals(2.069, red.y(), 1e-9);
  }

  @Test
  @DisplayName("on blue, a robot above the center line passes to the left target")
  void chooseBlue() {
    assertEquals(PassingTargets.BLUE_LEFT, PassingTargets.choosePassTarget(6.5, false));
    assertEquals(PassingTargets.BLUE_RIGHT, PassingTargets.choosePassTarget(1.5, false));
    // Exactly on the center line is not "above" it.
    assertEquals(PassingTargets.BLUE_RIGHT, PassingTargets.choosePassTarget(4.0, false));
  }

  @Test
  @DisplayName("on red, the choice flips so the target stays on the same side of the field")
  void chooseRed() {
    Translation high = PassingTargets.choosePassTarget(6.5, true);
    Translation low = PassingTargets.choosePassTarget(1.5, true);

    // Red mirrors both the comparison and the target, so a robot high on the field still throws
    // toward the near corner on its own side.
    assertEquals(PassingTargets.flip(PassingTargets.BLUE_RIGHT, true), high);
    assertEquals(PassingTargets.flip(PassingTargets.BLUE_LEFT, true), low);
    assertNotEquals(high, low);
  }

  @Test
  @DisplayName("the passing map's keys mirror with the alliance")
  void mapKeys() {
    assertArrayEquals(
        new double[] {5.0, 16.541 / 2, 11.541, 16.541}, PassingTargets.passingMapKeys(false), 1e-9);
    assertArrayEquals(
        new double[] {11.541, 16.541 / 2, 5.0, 0.0}, PassingTargets.passingMapKeys(true), 1e-9);
  }

  @Test
  @DisplayName("the hood clamp keeps setpoints inside the mechanism's travel")
  void hoodClamp() {
    assertEquals(0.6, PassingTargets.clampHood(0.6), EPSILON);
    assertEquals(PassingTargets.HOOD_MIN, PassingTargets.clampHood(0.1), EPSILON);
    assertEquals(PassingTargets.HOOD_MAX, PassingTargets.clampHood(1.5), EPSILON);
  }

  @Test
  @DisplayName("finding F4: dropping the hood offset costs about 0.011 rotations after clamping")
  void hoodOffsetBug() {
    double withOffset = PassingTargets.clampHood(0.25 + PassingTargets.HOOD_OFFSET);
    double withoutOffset = PassingTargets.clampHood(0.25);

    // The intended value survives the clamp; the rebuilt one is clamped up to the hood's minimum.
    assertEquals(0.541, withOffset, 1e-9);
    assertEquals(PassingTargets.HOOD_MIN, withoutOffset, EPSILON);
    assertEquals(0.011, withOffset - withoutOffset, 1e-9);
  }
}
