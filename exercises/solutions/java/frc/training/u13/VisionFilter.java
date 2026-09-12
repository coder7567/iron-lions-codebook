package frc.training.u13;

import java.util.ArrayList;
import java.util.List;

/** Reference solution for exercise u13-filter. */
public final class VisionFilter {
  public record PoseObservation(
      double ambiguity, double x, double y, double z, double timestamp, double avgTagDistance, int tagCount) {}

  public record RobotSpeeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  public static final double MAX_AMBIGUITY = 0.2;

  public static final double MAX_Z_ERROR = 0.75;
  public static final double MAX_SPEED = 2.0;
  public static final double MAX_ANGULAR_SPEED = 2.5;

  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  private VisionFilter() {}

  public static String reason(PoseObservation observation, RobotSpeeds speeds) {
    if (observation.ambiguity() > MAX_AMBIGUITY) {
      return "ambiguity";
    }
    if (observation.z() > MAX_Z_ERROR) {
      return "z";
    }
    if (observation.x() < 0.0
        || observation.y() < 0.0
        || observation.x() > FIELD_LENGTH
        || observation.y() > FIELD_WIDTH) {
      return "off field";
    }
    if (Math.hypot(speeds.vxMetersPerSec(), speeds.vyMetersPerSec()) > MAX_SPEED) {
      return "too fast";
    }
    if (Math.abs(speeds.omegaRadPerSec()) > MAX_ANGULAR_SPEED) {
      return "turning too fast";
    }
    return "accepted";
  }

  public static boolean reject(PoseObservation observation, RobotSpeeds speeds) {
    return !reason(observation, speeds).equals("accepted");
  }

  public static List<PoseObservation> accept(List<PoseObservation> observations, RobotSpeeds speeds) {
    List<PoseObservation> kept = new ArrayList<>();
    for (PoseObservation observation : observations) {
      if (!reject(observation, speeds)) {
        kept.add(observation);
      }
    }
    return kept;
  }
}
