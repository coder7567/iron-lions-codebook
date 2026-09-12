package frc.training.u10;

/** Reference solution for exercise u10-optimize. */
public final class ModuleOptimizer {
  public record ModuleState(double speedMetersPerSec, double angleRad) {}

  private ModuleOptimizer() {}

  public static double wrapRadians(double angle) {
    double wrapped = (angle + Math.PI) % (2 * Math.PI);
    if (wrapped < 0) {
      wrapped += 2 * Math.PI;
    }
    return wrapped - Math.PI;
  }

  public static double turnErrorRadians(double desiredAngleRad, double currentAngleRad) {
    return wrapRadians(desiredAngleRad - currentAngleRad);
  }

  public static ModuleState optimize(ModuleState desired, double currentAngleRad) {
    double error = turnErrorRadians(desired.angleRad(), currentAngleRad);
    if (Math.abs(error) > Math.PI / 2) {
      return new ModuleState(-desired.speedMetersPerSec(), wrapRadians(desired.angleRad() + Math.PI));
    }
    return desired;
  }

  public static ModuleState cosineScale(ModuleState state, double currentAngleRad) {
    double error = turnErrorRadians(state.angleRad(), currentAngleRad);
    return new ModuleState(state.speedMetersPerSec() * Math.cos(error), state.angleRad());
  }
}
