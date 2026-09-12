package frc.training.u02;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UnitConversionsTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("20 inches (our module spacing) is 0.508 m")
  void moduleSpacing() {
    assertEquals(0.508, UnitConversions.inchesToMeters(20), EPSILON);
  }

  @Test
  @DisplayName("our 2-inch wheel radius is 0.0508 m")
  void wheelRadius() {
    assertEquals(0.0508, UnitConversions.inchesToMeters(2), EPSILON);
  }

  @Test
  @DisplayName("meters convert back to inches")
  void metersToInches() {
    assertEquals(20.0, UnitConversions.metersToInches(0.508), EPSILON);
    assertEquals(31.732283, UnitConversions.metersToInches(0.806), 1e-6);
  }

  @Test
  @DisplayName("degrees convert to radians, including negative camera pitch")
  void degreesToRadians() {
    assertEquals(Math.PI, UnitConversions.degreesToRadians(180), EPSILON);
    assertEquals(-0.5235987756, UnitConversions.degreesToRadians(-30), 1e-9);
    assertEquals(0.0, UnitConversions.degreesToRadians(0), EPSILON);
  }

  @Test
  @DisplayName("60 RPM is one rotation per second, 2π rad/s")
  void rpmToRadiansPerSecond() {
    assertEquals(2 * Math.PI, UnitConversions.rpmToRadiansPerSecond(60), EPSILON);
    assertEquals(314.159265, UnitConversions.rpmToRadiansPerSecond(3000), 1e-6);
  }

  @Test
  @DisplayName("5.9:1 reduction gives DriveConstants' position factor (≈1.0649 rad per motor turn)")
  void positionFactor() {
    assertEquals(1.064947, UnitConversions.motorRotationsToWheelRadians(5.9), 1e-6);
    assertEquals(2 * Math.PI, UnitConversions.motorRotationsToWheelRadians(1.0), EPSILON);
  }

  @Test
  @DisplayName("a NEO at 5676 RPM through 5.9:1 on a 2-inch wheel moves about 5.118 m/s")
  void wheelSpeed() {
    assertEquals(5.117793, UnitConversions.wheelSpeedMetersPerSecond(5676, 5.9, 0.0508), 1e-5);
    assertEquals(0.0, UnitConversions.wheelSpeedMetersPerSecond(0, 5.9, 0.0508), EPSILON);
  }
}
