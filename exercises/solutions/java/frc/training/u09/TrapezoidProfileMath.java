package frc.training.u09;

/** Reference solution for exercise u09-profile. */
public final class TrapezoidProfileMath {
  public record State(double position, double velocity) {}

  private TrapezoidProfileMath() {}

  public static boolean isTriangular(double distance, double maxVelocity, double maxAcceleration) {
    double rampDistance = maxVelocity * maxVelocity / (2.0 * maxAcceleration);
    return 2.0 * rampDistance > distance;
  }

  public static double peakVelocity(double distance, double maxVelocity, double maxAcceleration) {
    return isTriangular(distance, maxVelocity, maxAcceleration)
        ? Math.sqrt(maxAcceleration * distance)
        : maxVelocity;
  }

  public static double totalTime(double distance, double maxVelocity, double maxAcceleration) {
    double peak = peakVelocity(distance, maxVelocity, maxAcceleration);
    double rampTime = peak / maxAcceleration;
    double cruiseDistance = distance - peak * peak / maxAcceleration;
    double cruiseTime = cruiseDistance <= 0.0 ? 0.0 : cruiseDistance / peak;
    return 2.0 * rampTime + cruiseTime;
  }

  public static State sample(double t, double distance, double maxVelocity, double maxAcceleration) {
    if (t <= 0.0) {
      return new State(0.0, 0.0);
    }
    double peak = peakVelocity(distance, maxVelocity, maxAcceleration);
    double rampTime = peak / maxAcceleration;
    double rampDistance = peak * peak / (2.0 * maxAcceleration);
    double cruiseDistance = distance - 2.0 * rampDistance;
    double cruiseTime = cruiseDistance <= 0.0 ? 0.0 : cruiseDistance / peak;

    if (t >= 2.0 * rampTime + cruiseTime) {
      return new State(distance, 0.0);
    }
    if (t < rampTime) {
      return new State(0.5 * maxAcceleration * t * t, maxAcceleration * t);
    }
    if (t < rampTime + cruiseTime) {
      return new State(rampDistance + peak * (t - rampTime), peak);
    }

    double decelTime = t - rampTime - cruiseTime;
    double position =
        rampDistance + cruiseDistance + peak * decelTime - 0.5 * maxAcceleration * decelTime * decelTime;
    return new State(position, peak - maxAcceleration * decelTime);
  }
}
