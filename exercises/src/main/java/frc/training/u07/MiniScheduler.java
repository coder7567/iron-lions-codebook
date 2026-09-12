package frc.training.u07;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Exercise u07-scheduler: a small version of WPILib's CommandScheduler.
 *
 * <p>These rules match WPILib's:
 *
 * <ul>
 *   <li>Subsystems run {@code periodic()} in the order they were registered.
 *   <li>At most one scheduled command may require each subsystem. Scheduling a command interrupts every
 *       scheduled command that shares a requirement with it.
 *   <li>At the end of {@link #run()}, each registered subsystem that no command requires gets its
 *       default command scheduled.
 * </ul>
 */
public final class MiniScheduler {
  /** Registered subsystems in registration order, each mapped to its default command (or null). */
  private final Map<MiniSubsystem, MiniCommand> subsystems = new LinkedHashMap<>();
  /** Scheduled commands, in the order they were scheduled. */
  private final Set<MiniCommand> scheduled = new LinkedHashSet<>();
  /** The scheduled command that currently requires each subsystem. */
  private final Map<MiniSubsystem, MiniCommand> requirements = new HashMap<>();

  /** Registers a subsystem so its periodic() runs every loop. Registering it again does nothing. */
  public void registerSubsystem(MiniSubsystem subsystem) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Sets a subsystem's default command, registering the subsystem if it is not registered yet.
   *
   * @throws IllegalArgumentException if the command does not require the subsystem
   */
  public void setDefaultCommand(MiniSubsystem subsystem, MiniCommand command) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Schedules a command. Does nothing if it is already scheduled. Otherwise, cancels every scheduled
   * command that shares a requirement with it, then calls initialize() and records its requirements.
   */
  public void schedule(MiniCommand command) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Stops a scheduled command with end(true) and frees its subsystems. Does nothing otherwise. */
  public void cancel(MiniCommand command) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Returns true if the command is currently scheduled. */
  public boolean isScheduled(MiniCommand command) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Returns the scheduled command that requires the subsystem, or null if no command does. */
  public MiniCommand requiring(MiniSubsystem subsystem) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Runs one loop:
   *
   * <ol>
   *   <li>Call periodic() on every registered subsystem, in registration order.
   *   <li>For each scheduled command, in the order scheduled, call execute(). If isFinished() then
   *       returns true, unschedule the command, free its subsystems, and call end(false).
   *   <li>Schedule the default command of every registered subsystem that no command requires.
   * </ol>
   */
  public void run() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
