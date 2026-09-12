package frc.training.u12;

import java.util.List;
import java.util.Set;

/**
 * Exercise u12-autolint: check an autonomous routine for the mistakes that cost matches, using the same
 * structure PathPlanner stores in a .auto file.
 */
public final class AutoLint {
  /** One step of an auto. */
  public sealed interface Step permits Named, Wait, Path, Sequential, Parallel {}

  /** A named command, registered with NamedCommands in RobotContainer. */
  public record Named(String name) implements Step {}

  /** A pause. */
  public record Wait(double seconds) implements Step {}

  /** Following a path from the paths folder. */
  public record Path(String pathName) implements Step {}

  /** Steps that run one after another. */
  public record Sequential(List<Step> steps) implements Step {}

  /** Steps that run at the same time. */
  public record Parallel(List<Step> steps) implements Step {}

  /** A whole auto file: its name, its root step, and whether it resets odometry when it starts. */
  public record Auto(String name, Step root, boolean resetOdom) {}

  private AutoLint() {}

  /** Every named command the auto uses, in the order they appear. */
  public static List<String> namedCommands(Step root) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Every path the auto follows, in the order they appear. */
  public static List<String> paths(Step root) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Returns findings about the auto, in this order:
   *
   * <ol>
   *   <li>For each named command that is not registered, in order of appearance:
   *       {@code "<auto>: command <name> is not registered"}
   *   <li>When the auto follows at least one path but does not reset odometry:
   *       {@code "<auto>: follows a path without resetting odometry"}
   *   <li>When any parallel step contains more than one path anywhere inside it:
   *       {@code "<auto>: two paths run in parallel"}
   *   <li>When the last named command is "start":
   *       {@code "<auto>: ends with the robot still shooting"}
   * </ol>
   *
   * An auto with no findings returns an empty list.
   */
  public static List<String> check(Auto auto, Set<String> registeredCommands) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
