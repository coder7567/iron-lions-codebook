package frc.training.u10;

/** Reference solution for exercise u10-kinematics. */
public final class SwerveMath {
  public record ModuleState(double speedMetersPerSec, double angleRad) {}

  public record ChassisSpeeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  public record ModuleLocation(double x, double y) {}

  public static final ModuleLocation[] MODULE_LOCATIONS = {
    new ModuleLocation(0.254, 0.254),
    new ModuleLocation(0.254, -0.254),
    new ModuleLocation(-0.254, 0.254),
    new ModuleLocation(-0.254, -0.254)
  };

  private SwerveMath() {}

  public static ModuleState[] toModuleStates(ChassisSpeeds speeds, ModuleLocation[] locations) {
    ModuleState[] states = new ModuleState[locations.length];
    for (int i = 0; i < locations.length; i++) {
      double vx = speeds.vxMetersPerSec() - speeds.omegaRadPerSec() * locations[i].y();
      double vy = speeds.vyMetersPerSec() + speeds.omegaRadPerSec() * locations[i].x();
      states[i] = new ModuleState(Math.hypot(vx, vy), Math.atan2(vy, vx));
    }
    return states;
  }

  public static ChassisSpeeds toChassisSpeeds(ModuleState[] states, ModuleLocation[] locations) {
    double sumVx = 0.0;
    double sumVy = 0.0;
    double sumMoment = 0.0;
    double sumRadiusSquared = 0.0;

    for (int i = 0; i < states.length; i++) {
      double vx = states[i].speedMetersPerSec() * Math.cos(states[i].angleRad());
      double vy = states[i].speedMetersPerSec() * Math.sin(states[i].angleRad());
      sumVx += vx;
      sumVy += vy;
      sumMoment += locations[i].x() * vy - locations[i].y() * vx;
      sumRadiusSquared += locations[i].x() * locations[i].x() + locations[i].y() * locations[i].y();
    }

    return new ChassisSpeeds(sumVx / states.length, sumVy / states.length, sumMoment / sumRadiusSquared);
  }

  public static ModuleState[] desaturate(ModuleState[] states, double maxSpeed) {
    double fastest = 0.0;
    for (ModuleState state : states) {
      fastest = Math.max(fastest, Math.abs(state.speedMetersPerSec()));
    }
    if (fastest <= maxSpeed) {
      return states.clone();
    }

    double scale = maxSpeed / fastest;
    ModuleState[] limited = new ModuleState[states.length];
    for (int i = 0; i < states.length; i++) {
      limited[i] = new ModuleState(states[i].speedMetersPerSec() * scale, states[i].angleRad());
    }
    return limited;
  }
}
