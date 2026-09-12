package frc.training.u05;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reference solution for exercise u05-records, part 2. */
public final class Observations {
  private Observations() {}

  public static List<PoseObservation> filter(
      List<PoseObservation> observations, double fieldLength, double fieldWidth, double maxAmbiguity) {
    List<PoseObservation> kept = new ArrayList<>();
    for (PoseObservation obs : observations) {
      boolean onField = obs.x() >= 0 && obs.x() <= fieldLength && obs.y() >= 0 && obs.y() <= fieldWidth;
      if (onField && obs.ambiguity() <= maxAmbiguity) {
        kept.add(obs);
      }
    }
    return kept;
  }

  public static Optional<PoseObservation> newest(List<PoseObservation> observations) {
    PoseObservation best = null;
    for (PoseObservation obs : observations) {
      if (best == null || obs.timestampSeconds() > best.timestampSeconds()) {
        best = obs;
      }
    }
    return Optional.ofNullable(best);
  }
}
