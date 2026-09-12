package frc.training.u10;

import static org.junit.jupiter.api.Assertions.assertEquals;

import frc.training.u10.SwerveMath.ChassisSpeeds;
import frc.training.u10.SwerveMath.ModuleState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SwerveMathTest {
  private static final double EPSILON = 1e-9;
  /** Distance from the center to a module: hypot(0.254, 0.254). */
  private static final double R = Math.hypot(0.254, 0.254);

  private static ModuleState[] states(ChassisSpeeds speeds) {
    return SwerveMath.toModuleStates(speeds, SwerveMath.MODULE_LOCATIONS);
  }

  @Test
  @DisplayName("driving straight forward points every module ahead at the same speed")
  void pureTranslation() {
    ModuleState[] result = states(new ChassisSpeeds(2.0, 0.0, 0.0));

    for (ModuleState state : result) {
      assertEquals(2.0, state.speedMetersPerSec(), EPSILON);
      assertEquals(0.0, state.angleRad(), EPSILON);
    }
  }

  @Test
  @DisplayName("strafing left points every module 90 degrees left")
  void pureStrafe() {
    ModuleState[] result = states(new ChassisSpeeds(0.0, 1.0, 0.0));

    for (ModuleState state : result) {
      assertEquals(1.0, state.speedMetersPerSec(), EPSILON);
      assertEquals(Math.PI / 2, state.angleRad(), EPSILON);
    }
  }

  @Test
  @DisplayName("spinning in place sends every module tangentially at omega times the base radius")
  void pureRotation() {
    ModuleState[] result = states(new ChassisSpeeds(0.0, 0.0, 1.0));

    for (ModuleState state : result) {
      assertEquals(R, state.speedMetersPerSec(), EPSILON);
    }
    // Front left sits at (+0.254, +0.254); rotating counterclockwise sends it up and to the left.
    assertEquals(3 * Math.PI / 4, result[0].angleRad(), EPSILON);
    assertEquals(Math.PI / 4, result[1].angleRad(), EPSILON);
    assertEquals(-3 * Math.PI / 4, result[2].angleRad(), EPSILON);
    assertEquals(-Math.PI / 4, result[3].angleRad(), EPSILON);
  }

  @Test
  @DisplayName("driving and turning at once gives every module a different state")
  void combined() {
    ModuleState[] result = states(new ChassisSpeeds(1.0, 0.0, 1.0));

    // Front left: (1 - 0.254, 0 + 0.254)
    assertEquals(Math.hypot(0.746, 0.254), result[0].speedMetersPerSec(), EPSILON);
    assertEquals(Math.atan2(0.254, 0.746), result[0].angleRad(), EPSILON);
    // Front right: (1 + 0.254, 0 + 0.254)
    assertEquals(Math.hypot(1.254, 0.254), result[1].speedMetersPerSec(), EPSILON);
  }

  @Test
  @DisplayName("forward kinematics recovers the chassis speeds that produced the module states")
  void roundTrip() {
    ChassisSpeeds[] cases = {
      new ChassisSpeeds(2.0, 0.0, 0.0),
      new ChassisSpeeds(0.0, -1.5, 0.0),
      new ChassisSpeeds(0.0, 0.0, 2.5),
      new ChassisSpeeds(1.2, -0.8, 1.7)
    };

    for (ChassisSpeeds speeds : cases) {
      ChassisSpeeds measured =
          SwerveMath.toChassisSpeeds(states(speeds), SwerveMath.MODULE_LOCATIONS);
      assertEquals(speeds.vxMetersPerSec(), measured.vxMetersPerSec(), 1e-9);
      assertEquals(speeds.vyMetersPerSec(), measured.vyMetersPerSec(), 1e-9);
      assertEquals(speeds.omegaRadPerSec(), measured.omegaRadPerSec(), 1e-9);
    }
  }

  @Test
  @DisplayName("desaturation scales every speed by the same factor and leaves angles alone")
  void desaturate() {
    ModuleState[] requested = {
      new ModuleState(5.0, 0.1),
      new ModuleState(3.0, 0.2),
      new ModuleState(-2.0, 0.3),
      new ModuleState(1.0, 0.4)
    };

    ModuleState[] limited = SwerveMath.desaturate(requested, 4.2);

    double scale = 4.2 / 5.0;
    assertEquals(4.2, limited[0].speedMetersPerSec(), EPSILON);
    assertEquals(3.0 * scale, limited[1].speedMetersPerSec(), EPSILON);
    assertEquals(-2.0 * scale, limited[2].speedMetersPerSec(), EPSILON);
    assertEquals(1.0 * scale, limited[3].speedMetersPerSec(), EPSILON);
    assertEquals(0.3, limited[2].angleRad(), EPSILON);
  }

  @Test
  @DisplayName("speeds already within the limit are left alone")
  void desaturateNoop() {
    ModuleState[] requested = {
      new ModuleState(1.0, 0.0), new ModuleState(2.0, 0.0), new ModuleState(3.0, 0.0), new ModuleState(-4.0, 0.0)
    };

    ModuleState[] limited = SwerveMath.desaturate(requested, 4.2);

    assertEquals(1.0, limited[0].speedMetersPerSec(), EPSILON);
    assertEquals(-4.0, limited[3].speedMetersPerSec(), EPSILON);
  }
}
