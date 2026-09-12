package frc.training.u07;

import java.util.function.BooleanSupplier;

/**
 * Exercise u07-groups: command groups and decorators for {@link MiniCommand}, following WPILib's rules.
 *
 * <ul>
 *   <li>A group requires every subsystem that any member requires.
 *   <li>{@code parallel}, {@code race}, and {@code deadline} throw IllegalArgumentException when two
 *       members require the same subsystem. {@code sequence} allows it.
 *   <li>Timeouts count loops (calls to execute) instead of seconds.
 * </ul>
 */
public final class Groups {
  private Groups() {}

  /**
   * Runs commands one after another. When one finishes, it ends with end(false) and the next one is
   * initialized in the same loop. Finishes when the last command finishes, or right away if empty.
   * If interrupted, only the command currently running gets end(true).
   */
  public static MiniCommand sequence(MiniCommand... commands) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Runs commands at the same time. Each member that finishes ends with end(false) and stops executing.
   * Finishes when every member has finished. If interrupted, members still running get end(true).
   */
  public static MiniCommand parallel(MiniCommand... commands) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Runs commands at the same time and executes every member each loop. Finishes as soon as any member
   * reports finished. When the race ends, each member gets end(false) if it finished and end(true) if
   * it did not.
   */
  public static MiniCommand race(MiniCommand... commands) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Runs the deadline and the other commands at the same time. Each member that finishes ends with
   * end(false) and stops executing. Finishes when the deadline finishes; members still running then get
   * end(true).
   */
  public static MiniCommand deadline(MiniCommand deadline, MiniCommand... others) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Interrupts the command if it has not finished after {@code loops} calls to execute(). */
  public static MiniCommand withTimeout(MiniCommand command, int loops) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Interrupts the command when the condition becomes true, checking once per loop after execute(). */
  public static MiniCommand until(MiniCommand command, BooleanSupplier condition) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
