package frc.training.u08;

/** Exercise u08-superstructure: the Superstructure's decisions, with no WPILib and no hardware. */
public final class SuperstructureLogic {
  public enum SuperState {
    IDLE,
    PAUSED,
    SHOOTING,
    EJECTING,
    TESTING
  }

  public enum TurretWanted {
    IDLE,
    PAUSED,
    SHOOTING,
    TESTING
  }

  public enum TurretCurrent {
    IDLE,
    PAUSEDSHOOTING,
    PAUSEDPASSING,
    SHOOTING,
    PASSING,
    TESTING
  }

  public enum IntakeWanted {
    IDLE,
    PAUSED,
    INTAKING,
    REVERSING,
    TESTING
  }

  public enum Alliance {
    RED,
    BLUE
  }

  /** What the Superstructure asks of the turret and the intake. */
  public record Requests(TurretWanted turret, IntakeWanted intake) {}

  private SuperstructureLogic() {}

  /**
   * Like Superstructure.applyState. IDLE: turret IDLE; the intake goes IDLE (arm to rest) only when the
   * turret is in its intake-safe position, and is PAUSED otherwise. PAUSED: both PAUSED. SHOOTING: turret
   * SHOOTING, intake INTAKING. EJECTING: turret SHOOTING, intake REVERSING. TESTING: both TESTING.
   */
  public static Requests requestsFor(SuperState state, boolean turretIntakeSafe) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Like Superstructure.getRumble: rumble when the turret is SHOOTING (not PASSING) while our HUB is
   * inactive, when the intake is jammed, or when the turret target is in its deadzone.
   */
  public static boolean rumble(
      TurretCurrent turret, boolean hubActive, boolean intakeJammed, boolean targetInDeadzone) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Like the alliance check in Superstructure.periodic: remembers the last known alliance (BLUE at
   * startup) and reports when it changes, so the turret can rebuild its passing map.
   */
  public static final class AllianceWatcher {
    private Alliance previous = Alliance.BLUE;

    /**
     * @param current the alliance from the Driver Station, or null if it isn't known yet
     * @return true only when a known alliance differs from the previous known alliance
     */
    public boolean update(Alliance current) {
      // TODO
      throw new UnsupportedOperationException("TODO");
    }
  }
}
