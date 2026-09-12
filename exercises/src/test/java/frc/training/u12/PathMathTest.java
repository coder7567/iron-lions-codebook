package frc.training.u12;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u12.PathMath.Waypoint;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PathMathTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("length adds the straight-line segments between waypoints")
  void length() {
    List<Waypoint> path =
        List.of(new Waypoint(1.0, 1.0), new Waypoint(4.0, 5.0), new Waypoint(4.0, 8.0));

    assertEquals(8.0, PathMath.length(path), EPSILON); // 5 + 3
    assertEquals(0.0, PathMath.length(List.of(new Waypoint(1.0, 1.0))), EPSILON);
    assertEquals(0.0, PathMath.length(List.of()), EPSILON);
  }

  @Test
  @DisplayName("a long move reaches cruise speed; a short one is triangular")
  void driveTime() {
    // 10 m at 2 m/s and 1 m/s^2: 10/2 + 2/1 = 7 s.
    assertEquals(7.0, PathMath.driveTime(10.0, 2.0, 1.0), 1e-9);
    // 1 m is too short to reach 2 m/s, so it is 2 * sqrt(1 / 1) = 2 s.
    assertEquals(2.0, PathMath.driveTime(1.0, 2.0, 1.0), 1e-9);
    assertEquals(0.0, PathMath.driveTime(0.0, 2.0, 1.0), 1e-9);
  }

  @Test
  @DisplayName("auto time adds every segment plus the overhead between them")
  void autoTime() {
    double[] segments = {4.0, 3.0};

    // At 4 m/s and 3 m/s^2 the cruise distance is 16/3 = 5.33 m, so both legs are triangular:
    // 2 * sqrt(4 / 3) and 2 * sqrt(3 / 3), plus 1.5 s of overhead each.
    double expected = 2 * Math.sqrt(4.0 / 3.0) + 2.0 + 2 * 1.5;

    assertEquals(expected, PathMath.autoTime(segments, 4.0, 3.0, 1.5), 1e-9);
  }

  @Test
  @DisplayName("an auto has to fit in 20 seconds")
  void fitsInAuto() {
    assertTrue(PathMath.fitsInAuto(14.5));
    assertTrue(PathMath.fitsInAuto(20.0));
    assertFalse(PathMath.fitsInAuto(20.5));
  }

  @Test
  @DisplayName("flipping a waypoint mirrors it through the center of the field")
  void flip() {
    Waypoint blue = new Waypoint(2.0, 6.0);
    Waypoint red = PathMath.flip(blue);

    assertEquals(14.541, red.x(), 1e-9);
    assertEquals(2.069, red.y(), 1e-9);
    assertEquals(blue, PathMath.flip(red));
  }

  @Test
  @DisplayName("waypoints have to be on the field")
  void insideField() {
    assertTrue(PathMath.insideField(List.of(new Waypoint(0.0, 0.0), new Waypoint(16.541, 8.069))));
    assertFalse(PathMath.insideField(List.of(new Waypoint(1.0, 1.0), new Waypoint(17.0, 4.0))));
    assertFalse(PathMath.insideField(List.of(new Waypoint(1.0, -0.1))));
  }

  @Test
  @DisplayName("a two-leg auto fits comfortably; a five-leg one does not")
  void plannedAutos() {
    double[] modest = {3.0, 2.5};
    double[] greedy = {8.0, 8.0, 6.0, 6.0, 4.0};

    assertTrue(PathMath.fitsInAuto(PathMath.autoTime(modest, 4.0, 3.0, 1.5)));
    assertFalse(PathMath.fitsInAuto(PathMath.autoTime(greedy, 4.0, 3.0, 1.5)));
  }
}
