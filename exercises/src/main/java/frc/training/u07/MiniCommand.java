package frc.training.u07;

import java.util.Set;

/**
 * A stripped-down stand-in for WPILib's {@code Command}, used by the Unit 7 exercises. The scheduler
 * calls these methods; a command never calls them on itself. This file is provided; you do not need to
 * change it.
 */
public interface MiniCommand {
  /** Called once when the command is scheduled. */
  default void initialize() {}

  /** Called every scheduler run while the command is scheduled. */
  default void execute() {}

  /** Checked every run, right after {@link #execute()}. Return true when the command is done. */
  default boolean isFinished() {
    return false;
  }

  /**
   * Called once when the command stops running.
   *
   * @param interrupted false if {@link #isFinished()} returned true; true if the command was canceled
   *     or replaced by another command that needs the same subsystem
   */
  default void end(boolean interrupted) {}

  /** The subsystems this command needs exclusive use of while it runs. */
  default Set<MiniSubsystem> getRequirements() {
    return Set.of();
  }
}
