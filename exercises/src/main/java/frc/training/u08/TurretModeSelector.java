package frc.training.u08;

/**
 * Exercise u08-turret: choose the turret's current state from its wanted state and the robot's field
 * position, like Turret.updateState, with finding F1 fixed.
 */
public final class TurretModeSelector {
  public enum WantedState {
    IDLE,
    PAUSED,
    SHOOTING,
    TESTING
  }

  public enum CurrentState {
    IDLE,
    PAUSEDSHOOTING,
    PAUSEDPASSING,
    SHOOTING,
    PASSING,
    TESTING
  }

  /** Field length in meters for the 2026 field. */
  public static final double FIELD_LENGTH = 16.541;
  /** TurretConstants.allianceZoneEnd: how far our alliance zone reaches from our wall, in meters. */
  public static final double ALLIANCE_ZONE_DEPTH = 5.0;

  private TurretModeSelector() {}

  /** Like TurretConstants.allianceZoneEnd(): the x of our zone's edge. 5.0 on blue, mirrored on red. */
  public static double ourZoneEdgeX(boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Like TurretConstants.oppositeAllianceEnd(): the x where the other alliance's zone starts. */
  public static double theirZoneEdgeX(boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Like Turret.isPastLine: true if robotX is strictly on our wall's side of lineX. Blue's wall is at
   * x = 0, so smaller x is our side. Red's wall is at x = FIELD_LENGTH, so larger x is our side.
   */
  public static boolean onOurSide(double robotX, double lineX, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * IDLE and TESTING ignore position. SHOOTING is SHOOTING in our zone and PASSING anywhere else. PAUSED
   * (with F1 fixed) is PAUSEDSHOOTING in our zone, PAUSEDPASSING between the two zones, and IDLE inside
   * the other alliance's zone.
   */
  public static CurrentState select(WantedState wanted, double robotX, boolean isRed) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
