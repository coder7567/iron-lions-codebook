package frc.training.u07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupsTest {
  private final List<String> log = new ArrayList<>();

  private static final class FakeSubsystem implements MiniSubsystem {
    private final String name;

    FakeSubsystem(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }
  }

  /** A command that logs every lifecycle call and finishes after a set number of executes (-1: never). */
  private final class LoggingCommand implements MiniCommand {
    private final String name;
    private final int executesUntilFinished;
    private final Set<MiniSubsystem> requirements;
    private int executes = 0;

    LoggingCommand(String name, int executesUntilFinished, MiniSubsystem... requirements) {
      this.name = name;
      this.executesUntilFinished = executesUntilFinished;
      this.requirements = Set.of(requirements);
    }

    @Override
    public void initialize() {
      executes = 0;
      log.add(name + ".initialize");
    }

    @Override
    public void execute() {
      executes++;
      log.add(name + ".execute");
    }

    @Override
    public boolean isFinished() {
      return executesUntilFinished >= 0 && executes >= executesUntilFinished;
    }

    @Override
    public void end(boolean interrupted) {
      log.add(name + ".end(" + interrupted + ")");
    }

    @Override
    public Set<MiniSubsystem> getRequirements() {
      return requirements;
    }
  }

  /**
   * Drives a command the way the scheduler does. Returns the loop it finished on, or -1 if it was
   * still running after maxLoops and had to be interrupted.
   */
  private static int runUntilDone(MiniCommand command, int maxLoops) {
    command.initialize();
    for (int loop = 1; loop <= maxLoops; loop++) {
      command.execute();
      if (command.isFinished()) {
        command.end(false);
        return loop;
      }
    }
    command.end(true);
    return -1;
  }

  private long count(String entry) {
    return log.stream().filter(entry::equals).count();
  }

  @BeforeEach
  void clearLog() {
    log.clear();
  }

  @Test
  @DisplayName("sequence runs commands in order and starts the next one in the same loop")
  void sequenceOrder() {
    MiniCommand group = Groups.sequence(new LoggingCommand("a", 2), new LoggingCommand("b", 1));

    assertEquals(3, runUntilDone(group, 10));
    assertEquals(
        List.of("a.initialize", "a.execute", "a.execute", "a.end(false)", "b.initialize", "b.execute", "b.end(false)"),
        log);
  }

  @Test
  @DisplayName("an interrupted sequence interrupts only its current command and never starts the rest")
  void sequenceInterrupted() {
    MiniCommand group =
        Groups.sequence(new LoggingCommand("a", 1), new LoggingCommand("b", -1), new LoggingCommand("c", 1));

    assertEquals(-1, runUntilDone(group, 3));
    assertEquals(
        List.of("a.initialize", "a.execute", "a.end(false)", "b.initialize", "b.execute", "b.execute", "b.end(true)"),
        log);
  }

  @Test
  @DisplayName("an empty sequence finishes on its first loop")
  void emptySequence() {
    assertEquals(1, runUntilDone(Groups.sequence(), 5));
  }

  @Test
  @DisplayName("parallel waits for every member, and a finished member stops executing")
  void parallelWaitsForAll() {
    MiniCommand group = Groups.parallel(new LoggingCommand("a", 1), new LoggingCommand("b", 3));

    assertEquals(3, runUntilDone(group, 10));
    assertEquals(
        List.of(
            "a.initialize", "b.initialize",
            "a.execute", "a.end(false)", "b.execute",
            "b.execute",
            "b.execute", "b.end(false)"),
        log);
  }

  @Test
  @DisplayName("an interrupted parallel group interrupts only the members still running")
  void parallelInterrupted() {
    MiniCommand group = Groups.parallel(new LoggingCommand("a", 1), new LoggingCommand("b", -1));

    assertEquals(-1, runUntilDone(group, 2));
    assertEquals(1, count("a.end(false)"));
    assertEquals(0, count("a.end(true)"));
    assertEquals("b.end(true)", log.get(log.size() - 1));
  }

  @Test
  @DisplayName("race finishes when any member finishes and interrupts the others")
  void race() {
    MiniCommand group = Groups.race(new LoggingCommand("a", 2), new LoggingCommand("b", -1));

    assertEquals(2, runUntilDone(group, 10));
    assertEquals(
        List.of(
            "a.initialize", "b.initialize",
            "a.execute", "b.execute",
            "a.execute", "b.execute",
            "a.end(false)", "b.end(true)"),
        log);
  }

  @Test
  @DisplayName("deadline finishes with its first command and interrupts members still running")
  void deadline() {
    MiniCommand group =
        Groups.deadline(new LoggingCommand("dl", 2), new LoggingCommand("short", 1), new LoggingCommand("forever", -1));

    assertEquals(2, runUntilDone(group, 10));
    assertEquals(
        List.of(
            "dl.initialize", "short.initialize", "forever.initialize",
            "dl.execute", "short.execute", "short.end(false)", "forever.execute",
            "dl.execute", "dl.end(false)", "forever.execute",
            "forever.end(true)"),
        log);
  }

  @Test
  @DisplayName("withTimeout interrupts a command that runs too long, and lets a quick one finish normally")
  void withTimeout() {
    assertEquals(3, runUntilDone(Groups.withTimeout(new LoggingCommand("spin", -1), 3), 10));
    assertEquals(3, count("spin.execute"));
    assertEquals("spin.end(true)", log.get(log.size() - 1));

    log.clear();
    assertEquals(2, runUntilDone(Groups.withTimeout(new LoggingCommand("quick", 2), 5), 10));
    assertEquals(1, count("quick.end(false)"));
    assertEquals(0, count("quick.end(true)"));
  }

  @Test
  @DisplayName("until interrupts the command once the condition is true")
  void until() {
    boolean[] flywheelAtSpeed = {false};
    MiniCommand group = Groups.until(new LoggingCommand("wait-to-feed", -1), () -> flywheelAtSpeed[0]);

    group.initialize();
    group.execute();
    assertFalse(group.isFinished());
    flywheelAtSpeed[0] = true;
    group.execute();
    assertTrue(group.isFinished());
    group.end(false);

    assertEquals(2, count("wait-to-feed.execute"));
    assertEquals("wait-to-feed.end(true)", log.get(log.size() - 1));
  }

  @Test
  @DisplayName("groups require every member's subsystems; sequences may repeat a subsystem")
  void requirementsUnion() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    MiniSubsystem turret = new FakeSubsystem("turret");

    assertEquals(
        Set.of(drive, turret),
        Groups.sequence(new LoggingCommand("a", 1, drive), new LoggingCommand("b", 1, turret), new LoggingCommand("c", 1, drive))
            .getRequirements());
    assertEquals(
        Set.of(drive, turret),
        Groups.parallel(new LoggingCommand("a", 1, drive), new LoggingCommand("b", 1, turret)).getRequirements());
    assertEquals(Set.of(drive), Groups.withTimeout(new LoggingCommand("a", 1, drive), 3).getRequirements());
    assertEquals(Set.of(turret), Groups.until(new LoggingCommand("a", 1, turret), () -> false).getRequirements());
  }

  @Test
  @DisplayName("parallel, race, and deadline reject members that need the same subsystem")
  void sharedRequirementsRejected() {
    MiniSubsystem intake = new FakeSubsystem("intake");

    assertThrows(
        IllegalArgumentException.class,
        () -> Groups.parallel(new LoggingCommand("a", 1, intake), new LoggingCommand("b", 1, intake)));
    assertThrows(
        IllegalArgumentException.class,
        () -> Groups.race(new LoggingCommand("a", 1, intake), new LoggingCommand("b", 1, intake)));
    assertThrows(
        IllegalArgumentException.class,
        () -> Groups.deadline(new LoggingCommand("a", 1, intake), new LoggingCommand("b", 1, intake)));
  }
}
