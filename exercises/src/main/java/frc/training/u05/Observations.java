package frc.training.u05;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Exercise u05-records, part 2: working with lists of records. */
public final class Observations {
  private Observations() {}

  /**
   * Returns a new list, in the original order, of the observations that are on the field
   * (0 ≤ x ≤ fieldLength and 0 ≤ y ≤ fieldWidth) and whose ambiguity is at most {@code maxAmbiguity}.
   * The input list must not be changed.
   */
  public static List<PoseObservation> filter(
      List<PoseObservation> observations, double fieldLength, double fieldWidth, double maxAmbiguity) {
    // TODO
    return new ArrayList<>();
  }

  /** Returns the observation with the largest timestamp, or an empty Optional for an empty list. */
  public static Optional<PoseObservation> newest(List<PoseObservation> observations) {
    // TODO
    return Optional.empty();
  }
}
