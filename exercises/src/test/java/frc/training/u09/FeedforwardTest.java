package frc.training.u09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import frc.training.u09.Feedforward.Gains;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeedforwardTest {
  private static final double EPSILON = 1e-9;
  /** DriveConstants.driveKs and driveKv. */
  private static final double DRIVE_KS = 0.12349;
  private static final double DRIVE_KV = 0.12293;

  @Test
  @DisplayName("the voltage model adds a constant push in the direction of travel")
  void calculate() {
    assertEquals(1.35279, Feedforward.calculate(DRIVE_KS, DRIVE_KV, 10.0), 1e-9);
    assertEquals(-1.35279, Feedforward.calculate(DRIVE_KS, DRIVE_KV, -10.0), 1e-9);
    assertEquals(0.0, Feedforward.calculate(DRIVE_KS, DRIVE_KV, 0.0), EPSILON);
  }

  @Test
  @DisplayName("the acceleration term adds kA times acceleration")
  void calculateWithAcceleration() {
    assertEquals(
        1.35279 + 0.05, Feedforward.calculate(DRIVE_KS, DRIVE_KV, 0.01, 10.0, 5.0), 1e-9);
    assertEquals(-0.05, Feedforward.calculate(0.0, 0.0, 0.01, 0.0, -5.0), EPSILON);
  }

  @Test
  @DisplayName("least squares recovers the gains that generated the samples")
  void fitExactData() {
    double[] velocities = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    double[] volts = new double[velocities.length];
    for (int i = 0; i < velocities.length; i++) {
      volts[i] = 0.2 + 0.05 * velocities[i];
    }

    Gains gains = Feedforward.fit(velocities, volts);

    assertEquals(0.2, gains.kS(), 1e-9);
    assertEquals(0.05, gains.kV(), 1e-9);
  }

  @Test
  @DisplayName("least squares still lands close when the samples are noisy")
  void fitNoisyData() {
    double[] velocities = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    double[] noise = {0.01, -0.01, 0.02, -0.02, 0.0, 0.01, -0.01, 0.02, -0.02, 0.0};
    double[] volts = new double[velocities.length];
    for (int i = 0; i < velocities.length; i++) {
      volts[i] = 0.2 + 0.05 * velocities[i] + noise[i];
    }

    Gains gains = Feedforward.fit(velocities, volts);

    assertEquals(0.2, gains.kS(), 0.02);
    assertEquals(0.05, gains.kV(), 0.005);
  }

  @Test
  @DisplayName("fit rejects mismatched or too-short sample arrays")
  void fitRejectsBadInput() {
    assertThrows(IllegalArgumentException.class, () -> Feedforward.fit(new double[] {1, 2}, new double[] {1}));
    assertThrows(IllegalArgumentException.class, () -> Feedforward.fit(new double[] {1}, new double[] {1}));
  }

  @Test
  @DisplayName("the model predicts our drive's free speed at 12 volts")
  void maxVelocity() {
    // (12 - 0.12349) / 0.12293 rad/s at the wheel, times the 0.0508 m wheel radius.
    double wheelRadPerSec = Feedforward.maxVelocity(DRIVE_KS, DRIVE_KV, 12.0);
    assertEquals(96.61, wheelRadPerSec, 0.01);
    assertEquals(4.91, wheelRadPerSec * 0.0508, 0.01);

    assertEquals(0.0, Feedforward.maxVelocity(DRIVE_KS, DRIVE_KV, 0.1), EPSILON);
  }
}
