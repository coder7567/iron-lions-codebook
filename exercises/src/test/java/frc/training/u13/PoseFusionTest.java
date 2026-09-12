package frc.training.u13;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u13.PoseFusion.Fused;
import frc.training.u13.PoseFusion.Observation;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PoseFusionTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("standard deviations grow with distance and shrink with more tags")
  void standardDeviations() {
    // Two tags averaging 3 m away: 0.2 * 3 / 2.
    Observation twoTags = new Observation(8.0, 4.0, 0.0, 1.0, 3.0, 2);
    assertEquals(0.3, PoseFusion.linearStdDev(twoTags), EPSILON);
    assertEquals(0.524 * 1.5, PoseFusion.angularStdDev(twoTags), EPSILON);

    // One tag 3 m away: 0.2 * 3 / 1, doubled, and the heading is not trusted at all.
    Observation oneTag = new Observation(8.0, 4.0, 0.0, 1.0, 3.0, 1);
    assertEquals(1.2, PoseFusion.linearStdDev(oneTag), EPSILON);
    assertEquals(99999.0, PoseFusion.angularStdDev(oneTag), EPSILON);
  }

  @Test
  @DisplayName("the circular mean handles angles that straddle the wrap")
  void circularMean() {
    double[] straddling = {Math.PI - 0.1, -Math.PI + 0.1};
    double[] equal = {1.0, 1.0};

    // A plain average would give 0, pointing the opposite way.
    assertEquals(Math.PI, Math.abs(PoseFusion.circularMean(straddling, equal)), 1e-9);

    assertEquals(0.25, PoseFusion.circularMean(new double[] {0.0, 0.5}, equal), 1e-9);
    assertEquals(0.1, PoseFusion.circularMean(new double[] {0.0, 0.5}, new double[] {4.0, 1.0}), 1e-2);
    assertThrows(
        IllegalArgumentException.class,
        () -> PoseFusion.circularMean(new double[] {0.0}, new double[] {1.0, 1.0}));
  }

  @Test
  @DisplayName("two identical observations fuse to the same pose, both ways")
  void identicalObservations() {
    Observation a = new Observation(8.0, 4.0, 0.3, 10.0, 3.0, 2);
    Observation b = new Observation(8.0, 4.0, 0.3, 10.0, 3.0, 2);

    Fused ours = PoseFusion.fuseWithInverseSigma(List.of(a, b));
    Fused correct = PoseFusion.fuseWithInverseVariance(List.of(a, b));

    assertEquals(8.0, ours.x(), EPSILON);
    assertEquals(8.0, correct.x(), EPSILON);
    assertEquals(0.3, ours.yawRad(), 1e-9);
    assertEquals(10.0, ours.timestamp(), EPSILON);
  }

  @Test
  @DisplayName("finding F13: weighting by 1/sigma makes the fused estimate look better than it is")
  void overconfidence() {
    // Two cameras with identical quality: sigma = 0.2 * 3 / 2 = 0.3 each.
    Observation a = new Observation(8.0, 4.0, 0.0, 10.0, 3.0, 2);
    Observation b = new Observation(8.2, 4.0, 0.0, 10.0, 3.0, 2);

    double single = PoseFusion.linearStdDev(a);
    Fused ours = PoseFusion.fuseWithInverseSigma(List.of(a, b));
    Fused correct = PoseFusion.fuseWithInverseVariance(List.of(a, b));

    // Ours reports sigma / 2; the statistics say sigma / sqrt(2).
    assertEquals(single / 2.0, ours.linearStdDev(), 1e-9);
    assertEquals(single / Math.sqrt(2.0), correct.linearStdDev(), 1e-9);
    assertTrue(ours.linearStdDev() < correct.linearStdDev());
  }

  @Test
  @DisplayName("a near camera outvotes a far one, more strongly under inverse variance")
  void weighting() {
    Observation near = new Observation(8.0, 4.0, 0.0, 10.0, 1.0, 2); // sigma 0.1
    Observation far = new Observation(9.0, 4.0, 0.0, 10.0, 4.0, 2); // sigma 0.4

    Fused ours = PoseFusion.fuseWithInverseSigma(List.of(near, far));
    Fused correct = PoseFusion.fuseWithInverseVariance(List.of(near, far));

    // Weights 10 and 2.5 give 8.2; weights 100 and 6.25 give about 8.059.
    assertEquals(8.2, ours.x(), 1e-9);
    assertEquals(8.0 + 6.25 / 106.25, correct.x(), 1e-9);
    assertTrue(correct.x() < ours.x());
  }

  @Test
  @DisplayName("an empty list fuses to nothing")
  void empty() {
    assertNull(PoseFusion.fuseWithInverseSigma(List.of()));
    assertNull(PoseFusion.fuseWithInverseVariance(List.of()));
  }
}
