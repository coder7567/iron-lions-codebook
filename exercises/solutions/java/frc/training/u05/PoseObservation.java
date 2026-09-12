package frc.training.u05;

/** Reference solution for exercise u05-records, part 1. */
public record PoseObservation(
    double x,
    double y,
    double headingRadians,
    double timestampSeconds,
    double ambiguity,
    int tagCount,
    double avgTagDistanceMeters) {

  public PoseObservation {
    if (tagCount < 1) {
      throw new IllegalArgumentException("tagCount must be at least 1, was " + tagCount);
    }
    if (ambiguity < 0.0 || ambiguity > 1.0) {
      throw new IllegalArgumentException("ambiguity must be between 0 and 1, was " + ambiguity);
    }
  }

  public boolean isMultiTag() {
    return tagCount >= 2;
  }

  public double distanceTo(double otherX, double otherY) {
    return Math.hypot(otherX - x, otherY - y);
  }

  public PoseObservation withTimestamp(double newTimestampSeconds) {
    return new PoseObservation(
        x, y, headingRadians, newTimestampSeconds, ambiguity, tagCount, avgTagDistanceMeters);
  }
}
