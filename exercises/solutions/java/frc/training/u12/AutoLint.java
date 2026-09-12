package frc.training.u12;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Reference solution for exercise u12-autolint. */
public final class AutoLint {
  public sealed interface Step permits Named, Wait, Path, Sequential, Parallel {}

  public record Named(String name) implements Step {}

  public record Wait(double seconds) implements Step {}

  public record Path(String pathName) implements Step {}

  public record Sequential(List<Step> steps) implements Step {}

  public record Parallel(List<Step> steps) implements Step {}

  public record Auto(String name, Step root, boolean resetOdom) {}

  private AutoLint() {}

  public static List<String> namedCommands(Step root) {
    List<String> names = new ArrayList<>();
    collectNamed(root, names);
    return names;
  }

  public static List<String> paths(Step root) {
    List<String> names = new ArrayList<>();
    collectPaths(root, names);
    return names;
  }

  public static List<String> check(Auto auto, Set<String> registeredCommands) {
    List<String> findings = new ArrayList<>();

    for (String name : namedCommands(auto.root())) {
      if (!registeredCommands.contains(name)) {
        findings.add(auto.name() + ": command " + name + " is not registered");
      }
    }

    if (!paths(auto.root()).isEmpty() && !auto.resetOdom()) {
      findings.add(auto.name() + ": follows a path without resetting odometry");
    }

    if (hasParallelPaths(auto.root())) {
      findings.add(auto.name() + ": two paths run in parallel");
    }

    List<String> named = namedCommands(auto.root());
    if (!named.isEmpty() && named.get(named.size() - 1).equals("start")) {
      findings.add(auto.name() + ": ends with the robot still shooting");
    }

    return findings;
  }

  private static void collectNamed(Step step, List<String> into) {
    if (step instanceof Named named) {
      into.add(named.name());
    } else if (step instanceof Sequential sequential) {
      sequential.steps().forEach(child -> collectNamed(child, into));
    } else if (step instanceof Parallel parallel) {
      parallel.steps().forEach(child -> collectNamed(child, into));
    }
  }

  private static void collectPaths(Step step, List<String> into) {
    if (step instanceof Path path) {
      into.add(path.pathName());
    } else if (step instanceof Sequential sequential) {
      sequential.steps().forEach(child -> collectPaths(child, into));
    } else if (step instanceof Parallel parallel) {
      parallel.steps().forEach(child -> collectPaths(child, into));
    }
  }

  private static boolean hasParallelPaths(Step step) {
    if (step instanceof Parallel parallel) {
      if (paths(parallel).size() > 1) {
        return true;
      }
      for (Step child : parallel.steps()) {
        if (hasParallelPaths(child)) {
          return true;
        }
      }
      return false;
    }
    if (step instanceof Sequential sequential) {
      for (Step child : sequential.steps()) {
        if (hasParallelPaths(child)) {
          return true;
        }
      }
    }
    return false;
  }
}
