package frc.training.u07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MiniSchedulerTest {
  private final List<String> log = new ArrayList<>();
  private MiniScheduler scheduler;

  /** A subsystem that logs each periodic() call. */
  private final class FakeSubsystem implements MiniSubsystem {
    private final String name;

    FakeSubsystem(String name) {
      this.name = name;
    }

    @Override
    public void periodic() {
      log.add(name + ".periodic");
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

  @BeforeEach
  void setUp() {
    scheduler = new MiniScheduler();
    log.clear();
  }

  @Test
  @DisplayName("subsystems run periodic() once each, in registration order, like RobotContainer's construction order")
  void periodicOrder() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    MiniSubsystem turret = new FakeSubsystem("turret");
    MiniSubsystem superstructure = new FakeSubsystem("superstructure");
    scheduler.registerSubsystem(drive);
    scheduler.registerSubsystem(turret);
    scheduler.registerSubsystem(superstructure);
    scheduler.registerSubsystem(drive);

    scheduler.run();

    assertEquals(List.of("drive.periodic", "turret.periodic", "superstructure.periodic"), log);
  }

  @Test
  @DisplayName("a command runs initialize, then execute each loop until finished, then end(false)")
  void lifecycle() {
    MiniSubsystem intake = new FakeSubsystem("intake");
    scheduler.registerSubsystem(intake);
    LoggingCommand eject = new LoggingCommand("eject", 2, intake);

    scheduler.schedule(eject);
    assertTrue(scheduler.isScheduled(eject));
    assertSame(eject, scheduler.requiring(intake));
    scheduler.run();
    scheduler.run();

    assertEquals(
        List.of(
            "eject.initialize",
            "intake.periodic",
            "eject.execute",
            "intake.periodic",
            "eject.execute",
            "eject.end(false)"),
        log);
    assertFalse(scheduler.isScheduled(eject));
    assertNull(scheduler.requiring(intake));
  }

  @Test
  @DisplayName("subsystem periodic() methods run before any command executes")
  void periodicBeforeCommands() {
    MiniSubsystem turret = new FakeSubsystem("turret");
    scheduler.registerSubsystem(turret);
    scheduler.schedule(new LoggingCommand("aim", -1, turret));
    log.clear();

    scheduler.run();

    assertEquals(List.of("turret.periodic", "aim.execute"), log);
  }

  @Test
  @DisplayName("scheduling a command that is already scheduled does nothing")
  void scheduleTwice() {
    MiniSubsystem turret = new FakeSubsystem("turret");
    LoggingCommand aim = new LoggingCommand("aim", -1, turret);

    scheduler.schedule(aim);
    scheduler.schedule(aim);

    assertEquals(List.of("aim.initialize"), log);
  }

  @Test
  @DisplayName("a new command interrupts the command that requires the same subsystem")
  void interruption() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    LoggingCommand joystick = new LoggingCommand("joystick", -1, drive);
    LoggingCommand path = new LoggingCommand("path", -1, drive);

    scheduler.schedule(joystick);
    scheduler.schedule(path);

    assertEquals(List.of("joystick.initialize", "joystick.end(true)", "path.initialize"), log);
    assertFalse(scheduler.isScheduled(joystick));
    assertSame(path, scheduler.requiring(drive));
  }

  @Test
  @DisplayName("commands with different requirements run side by side; a shared requirement interrupts only that command")
  void sideBySide() {
    MiniSubsystem turret = new FakeSubsystem("turret");
    MiniSubsystem intake = new FakeSubsystem("intake");
    LoggingCommand aim = new LoggingCommand("aim", -1, turret);
    LoggingCommand collect = new LoggingCommand("collect", -1, intake);
    LoggingCommand eject = new LoggingCommand("eject", -1, intake);

    scheduler.schedule(aim);
    scheduler.schedule(collect);
    assertTrue(scheduler.isScheduled(aim));
    assertTrue(scheduler.isScheduled(collect));

    scheduler.schedule(eject);
    assertTrue(scheduler.isScheduled(aim));
    assertFalse(scheduler.isScheduled(collect));
    assertTrue(scheduler.isScheduled(eject));
  }

  @Test
  @DisplayName("a command with no requirements never interrupts anything, like our setWantedStateCommand")
  void noRequirements() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    LoggingCommand joystick = new LoggingCommand("joystick", -1, drive);
    LoggingCommand setWanted = new LoggingCommand("setWanted", 0);

    scheduler.schedule(joystick);
    scheduler.schedule(setWanted);
    scheduler.run();

    assertTrue(scheduler.isScheduled(joystick));
    assertFalse(scheduler.isScheduled(setWanted));
    assertEquals(
        List.of(
            "joystick.initialize",
            "setWanted.initialize",
            "joystick.execute",
            "setWanted.execute",
            "setWanted.end(false)"),
        log);
  }

  @Test
  @DisplayName("cancel ends a command with end(true) and frees its subsystem; canceling again does nothing")
  void cancel() {
    MiniSubsystem hood = new FakeSubsystem("hood");
    LoggingCommand hold = new LoggingCommand("hold", -1, hood);

    scheduler.schedule(hold);
    scheduler.cancel(hold);
    scheduler.cancel(hold);

    assertEquals(List.of("hold.initialize", "hold.end(true)"), log);
    assertFalse(scheduler.isScheduled(hold));
    assertNull(scheduler.requiring(hood));
  }

  @Test
  @DisplayName("a default command starts at the end of run(), yields to another command, and comes back")
  void defaultCommands() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    LoggingCommand joystick = new LoggingCommand("joystick", -1, drive);
    LoggingCommand path = new LoggingCommand("path", 1, drive);
    scheduler.setDefaultCommand(drive, joystick);

    scheduler.run();
    assertEquals(List.of("drive.periodic", "joystick.initialize"), log);

    log.clear();
    scheduler.schedule(path);
    scheduler.run();
    assertEquals(
        List.of(
            "joystick.end(true)",
            "path.initialize",
            "drive.periodic",
            "path.execute",
            "path.end(false)",
            "joystick.initialize"),
        log);
    assertSame(joystick, scheduler.requiring(drive));
  }

  @Test
  @DisplayName("a default command must require its subsystem")
  void defaultMustRequire() {
    MiniSubsystem drive = new FakeSubsystem("drive");
    LoggingCommand wrong = new LoggingCommand("wrong", -1);

    assertThrows(IllegalArgumentException.class, () -> scheduler.setDefaultCommand(drive, wrong));
  }
}
