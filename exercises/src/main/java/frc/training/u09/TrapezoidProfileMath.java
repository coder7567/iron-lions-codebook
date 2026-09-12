package frc.training.u09;

/**
 * Exercise u09-profile: a rest-to-rest trapezoid motion profile, the same shape WPILib's
 * TrapezoidProfile produces for the drive's heading controller.
 *
 * <p>The mechanism starts at position 0 with zero velocity, accelerates at maxAcceleration, may cruise
 * at maxVelocity, then decelerates to a stop at the goal distance. When the move is too short to reach
 * maxVelocity, the profile is a triangle with no cruise phase.
 */
public final class TrapezoidProfileMath {
  /** A point along the profile. */
  public record State(double position, double velocity) {}

  private TrapezoidProfileMath() {}

  /** True when the move is too short to reach maxVelocity. */
  public static boolean isTriangular(double distance, double maxVelocity, double maxAcceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The highest velocity the profile actually reaches. */
  public static double peakVelocity(double distance, double maxVelocity, double maxAcceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** How long the whole move takes, in seconds. */
  public static double totalTime(double distance, double maxVelocity, double maxAcceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The position and velocity at time t. Before the move starts, the state is (0, 0); at or after the
   * end, it is (distance, 0).
   */
  public static State sample(double t, double distance, double maxVelocity, double maxAcceleration) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
