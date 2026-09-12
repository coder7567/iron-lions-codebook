package frc.training.u12;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u12.AutoLint.Auto;
import frc.training.u12.AutoLint.Named;
import frc.training.u12.AutoLint.Parallel;
import frc.training.u12.AutoLint.Path;
import frc.training.u12.AutoLint.Sequential;
import frc.training.u12.AutoLint.Step;
import frc.training.u12.AutoLint.Wait;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AutoLintTest {
  /** The commands RobotContainer registers today. */
  private static final Set<String> REGISTERED = Set.of("start", "reverse");

  /** Our "Just Preload" auto: reverse, wait, start, and no odometry reset. */
  private static Auto justPreload() {
    Step root = new Sequential(List.of(new Named("reverse"), new Wait(0.1), new Named("start")));
    return new Auto("Just Preload", root, false);
  }

  /** Our "Depot" auto: reverse, then the Depot path in parallel with a delayed start. */
  private static Auto depot() {
    Step root =
        new Sequential(
            List.of(
                new Named("reverse"),
                new Parallel(
                    List.of(
                        new Path("Depot"),
                        new Sequential(List.of(new Wait(0.1), new Named("start")))))));
    return new Auto("Depot", root, true);
  }

  @Test
  @DisplayName("named commands and paths are collected in order, through nesting")
  void collectSteps() {
    assertEquals(List.of("reverse", "start"), AutoLint.namedCommands(depot().root()));
    assertEquals(List.of("Depot"), AutoLint.paths(depot().root()));
    assertEquals(List.of(), AutoLint.paths(justPreload().root()));
  }

  @Test
  @DisplayName("an auto that follows a path without resetting odometry is flagged")
  void missingOdometryReset() {
    Auto auto = new Auto("Score 1", new Sequential(List.of(new Path("Score 1"))), false);

    assertEquals(List.of("Score 1: follows a path without resetting odometry"), AutoLint.check(auto, REGISTERED));
  }

  @Test
  @DisplayName("an auto with no paths is not flagged for odometry")
  void noPathsNoOdometryFinding() {
    List<String> findings = AutoLint.check(justPreload(), REGISTERED);

    assertTrue(findings.contains("Just Preload: ends with the robot still shooting"));
    assertEquals(1, findings.size());
  }

  @Test
  @DisplayName("unregistered commands are reported in the order they appear")
  void unregisteredCommands() {
    Step root =
        new Sequential(List.of(new Named("spinUp"), new Path("Depot"), new Named("shootAll")));
    Auto auto = new Auto("New Auto", root, true);

    assertEquals(
        List.of(
            "New Auto: command spinUp is not registered",
            "New Auto: command shootAll is not registered"),
        AutoLint.check(auto, REGISTERED));
  }

  @Test
  @DisplayName("two paths inside one parallel step are flagged, even when nested")
  void parallelPaths() {
    Step root =
        new Parallel(List.of(new Path("Depot"), new Sequential(List.of(new Path("Right NZ")))));
    Auto auto = new Auto("Both", root, true);

    List<String> findings = AutoLint.check(auto, REGISTERED);

    assertTrue(findings.contains("Both: two paths run in parallel"));
  }

  @Test
  @DisplayName("a path running in parallel with commands is fine")
  void parallelPathAndCommands() {
    List<String> findings = AutoLint.check(depot(), REGISTERED);

    assertEquals(List.of("Depot: ends with the robot still shooting"), findings);
  }

  @Test
  @DisplayName("an auto that ends with something other than shooting has no shooting finding")
  void endsIdle() {
    Step root =
        new Sequential(List.of(new Path("Depot"), new Named("start"), new Wait(1.0), new Named("reverse")));
    Auto auto = new Auto("Clean", root, true);

    assertEquals(List.of(), AutoLint.check(auto, REGISTERED));
  }

  @Test
  @DisplayName("findings come back in the documented order")
  void findingOrder() {
    Step root = new Sequential(List.of(new Named("spinUp"), new Path("Depot"), new Named("start")));
    Auto auto = new Auto("Messy", root, false);

    assertEquals(
        List.of(
            "Messy: command spinUp is not registered",
            "Messy: follows a path without resetting odometry",
            "Messy: ends with the robot still shooting"),
        AutoLint.check(auto, REGISTERED));
  }
}
