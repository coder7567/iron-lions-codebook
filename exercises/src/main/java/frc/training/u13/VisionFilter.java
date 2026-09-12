package frc.training.u13;

import java.util.List;

/**
 * Exercise u13-filter: the checks AprilTagVision runs on every pose observation before it is allowed
 * anywhere near the pose estimator.
 */
public final class VisionFilter {
  /** One observation from a camera, matching AprilTagIO.PoseObservation. */
  public record PoseObservation(
      double ambiguity, double x, double y, double z, double timestamp, double avgTagDistance, int tagCount) {}

  /** How fast the robot was moving when the picture was taken. */
  public record RobotSpeeds(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {}

  /** Tuning values from VisionConstants. */
  public static final double MAX_AMBIGUITY = 0.2;

  public static final double MAX_Z_ERROR = 0.75;
  public static final double MAX_SPEED = 2.0;
  public static final double MAX_ANGULAR_SPEED = 2.5;

  /** The 2026 field. */
  public static final double FIELD_LENGTH = 16.541;

  public static final double FIELD_WIDTH = 8.069;

  private VisionFilter() {}

  /**
   * The first reason this observation should be thrown away, or "accepted" when it survives. Check in
   * the same order our code does, and use exactly these words:
   *
   * <ul>
   *   <li>"ambiguity" when the ambiguity is above MAX_AMBIGUITY
   *   <li>"z" when the reported height is above MAX_Z_ERROR
   *   <li>"off field" when x or y is outside the field
   *   <li>"too fast" when the robot's translation speed is above MAX_SPEED
   *   <li>"turning too fast" when the robot's rotation speed is above MAX_ANGULAR_SPEED
   * </ul>
   *
   * Every comparison is strict, so a value exactly at a limit is accepted.
   */
  public static String reason(PoseObservation observation, RobotSpeeds speeds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** True when the observation should be thrown away. */
  public static boolean reject(PoseObservation observation, RobotSpeeds speeds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Keeps the accepted observations, in their original order. */
  public static List<PoseObservation> accept(List<PoseObservation> observations, RobotSpeeds speeds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
