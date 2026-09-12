package frc.training.u05;

/**
 * Exercise u05-records, part 1: one vision measurement of the robot's pose, as an immutable record.
 *
 * <p>Our AprilTagIO declares a similar {@code PoseObservation} record. Records generate the constructor,
 * accessors like {@code x()} and {@code tagCount()}, and {@code equals}, {@code hashCode}, and
 * {@code toString} for you.
 */
public record PoseObservation(
    double x,
    double y,
    double headingRadians,
    double timestampSeconds,
    double ambiguity,
    int tagCount,
    double avgTagDistanceMeters) {

  /**
   * Compact constructor: runs before the fields are assigned.
   *
   * @throws IllegalArgumentException if {@code tagCount} is less than 1, or {@code ambiguity} is outside
   *     0 to 1
   */
  public PoseObservation {
    // TODO: validate tagCount and ambiguity
  }

  /** Returns true when the pose came from two or more tags. */
  public boolean isMultiTag() {
    // TODO
    return false;
  }

  /** Returns the straight-line distance from this pose to the point ({@code otherX}, {@code otherY}). */
  public double distanceTo(double otherX, double otherY) {
    // TODO
    return 0.0;
  }

  /** Returns a copy of this observation with a different timestamp. Records are immutable. */
  public PoseObservation withTimestamp(double newTimestampSeconds) {
    // TODO
    return this;
  }
}
