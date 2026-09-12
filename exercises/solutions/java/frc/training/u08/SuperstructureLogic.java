package frc.training.u08;

/** Reference solution for exercise u08-superstructure. */
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

  public record Requests(TurretWanted turret, IntakeWanted intake) {}

  private SuperstructureLogic() {}

  public static Requests requestsFor(SuperState state, boolean turretIntakeSafe) {
    return switch (state) {
      case IDLE -> new Requests(TurretWanted.IDLE, turretIntakeSafe ? IntakeWanted.IDLE : IntakeWanted.PAUSED);
      case PAUSED -> new Requests(TurretWanted.PAUSED, IntakeWanted.PAUSED);
      case SHOOTING -> new Requests(TurretWanted.SHOOTING, IntakeWanted.INTAKING);
      case EJECTING -> new Requests(TurretWanted.SHOOTING, IntakeWanted.REVERSING);
      case TESTING -> new Requests(TurretWanted.TESTING, IntakeWanted.TESTING);
    };
  }

  public static boolean rumble(
      TurretCurrent turret, boolean hubActive, boolean intakeJammed, boolean targetInDeadzone) {
    return (turret == TurretCurrent.SHOOTING && !hubActive) || intakeJammed || targetInDeadzone;
  }

  public static final class AllianceWatcher {
    private Alliance previous = Alliance.BLUE;

    public boolean update(Alliance current) {
      // Like DriverStation.getAlliance().orElse(previousAlliance): unknown means "no change".
      Alliance known = current == null ? previous : current;
      boolean changed = known != previous;
      previous = known;
      return changed;
    }
  }
}
