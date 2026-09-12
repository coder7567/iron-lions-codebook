package frc.training.u09;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u09.TrapezoidProfileMath.State;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TrapezoidProfileMathTest {
  private static final double EPSILON = 1e-9;
  // A 10 m move at 2 m/s and 1 m/s^2: 2 s up, 3 s cruising, 2 s down.
  private static final double DISTANCE = 10.0;
  private static final double MAX_V = 2.0;
  private static final double MAX_A = 1.0;

  @Test
  @DisplayName("a long move reaches its cruise velocity; a short one doesn't")
  void shape() {
    assertFalse(TrapezoidProfileMath.isTriangular(DISTANCE, MAX_V, MAX_A));
    assertEquals(2.0, TrapezoidProfileMath.peakVelocity(DISTANCE, MAX_V, MAX_A), EPSILON);

    assertTrue(TrapezoidProfileMath.isTriangular(1.0, MAX_V, MAX_A));
    assertEquals(1.0, TrapezoidProfileMath.peakVelocity(1.0, MAX_V, MAX_A), EPSILON);
  }

  @Test
  @DisplayName("total time adds the ramps and the cruise")
  void totalTime() {
    assertEquals(7.0, TrapezoidProfileMath.totalTime(DISTANCE, MAX_V, MAX_A), EPSILON);
    assertEquals(2.0, TrapezoidProfileMath.totalTime(1.0, MAX_V, MAX_A), EPSILON);
  }

  @Test
  @DisplayName("the three phases of a trapezoid have the right position and velocity")
  void samplePhases() {
    State accelerating = TrapezoidProfileMath.sample(1.0, DISTANCE, MAX_V, MAX_A);
    assertEquals(1.0, accelerating.velocity(), EPSILON);
    assertEquals(0.5, accelerating.position(), EPSILON);

    State cruising = TrapezoidProfileMath.sample(3.0, DISTANCE, MAX_V, MAX_A);
    assertEquals(2.0, cruising.velocity(), EPSILON);
    assertEquals(4.0, cruising.position(), EPSILON);

    State decelerating = TrapezoidProfileMath.sample(6.0, DISTANCE, MAX_V, MAX_A);
    assertEquals(1.0, decelerating.velocity(), EPSILON);
    assertEquals(9.5, decelerating.position(), EPSILON);
  }

  @Test
  @DisplayName("the profile ends stopped at the goal and stays there")
  void endsAtGoal() {
    assertEquals(new State(10.0, 0.0), TrapezoidProfileMath.sample(7.0, DISTANCE, MAX_V, MAX_A));
    assertEquals(new State(10.0, 0.0), TrapezoidProfileMath.sample(100.0, DISTANCE, MAX_V, MAX_A));
    assertEquals(new State(0.0, 0.0), TrapezoidProfileMath.sample(0.0, DISTANCE, MAX_V, MAX_A));
  }

  @Test
  @DisplayName("a triangular move peaks halfway and lands on its goal")
  void triangularProfile() {
    State peak = TrapezoidProfileMath.sample(1.0, 1.0, MAX_V, MAX_A);
    assertEquals(1.0, peak.velocity(), EPSILON);
    assertEquals(0.5, peak.position(), EPSILON);

    assertEquals(new State(1.0, 0.0), TrapezoidProfileMath.sample(2.0, 1.0, MAX_V, MAX_A));
  }

  @Test
  @DisplayName("a 90 degree heading change at the drive's constraints takes about 0.56 s")
  void headingTurn() {
    // DriveConstants.ANGLE_MAX_VELOCITY 8 rad/s, ANGLE_MAX_ACCELERATION 20 rad/s^2.
    assertEquals(0.5605, TrapezoidProfileMath.totalTime(Math.PI / 2, 8.0, 20.0), 1e-3);
    assertTrue(TrapezoidProfileMath.isTriangular(Math.PI / 2, 8.0, 20.0));
  }
}
