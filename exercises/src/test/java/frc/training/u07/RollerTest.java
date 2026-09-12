package frc.training.u07;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.wpilibj.simulation.DriverStationSim;
import edu.wpi.first.wpilibj.simulation.SimHooks;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RollerTest {
  private static final double EPSILON = 1e-9;
  private final CommandScheduler scheduler = CommandScheduler.getInstance();
  private Roller roller;

  @BeforeEach
  void setUp() {
    assertTrue(HAL.initialize(500, 0));
    SimHooks.pauseTiming();
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    resetScheduler();
    roller = new Roller();
  }

  @AfterEach
  void tearDown() {
    resetScheduler();
    SimHooks.resumeTiming();
  }

  private void resetScheduler() {
    scheduler.cancelAll();
    scheduler.unregisterAllSubsystems();
    scheduler.getDefaultButtonLoop().clear();
  }

  /** Runs the scheduler for a number of 20 ms loops, stepping simulated time after each. */
  private void runLoops(int loops) {
    for (int i = 0; i < loops; i++) {
      scheduler.run();
      SimHooks.stepTiming(0.02);
    }
  }

  @Test
  @DisplayName("runAtVolts applies its voltage until canceled, then stops the roller")
  void runAtVolts() {
    Command intake = roller.runAtVolts(6.0);
    scheduler.schedule(intake);
    runLoops(3);
    assertEquals(6.0, roller.getAppliedVolts(), EPSILON);
    assertTrue(intake.isScheduled());

    scheduler.cancel(intake);
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
  }

  @Test
  @DisplayName("every factory command requires the roller")
  void requirements() {
    assertTrue(roller.runAtVolts(6.0).hasRequirement(roller));
    assertTrue(roller.ejectFor(1.0).hasRequirement(roller));
    assertTrue(roller.stopCommand().hasRequirement(roller));
  }

  @Test
  @DisplayName("ejectFor runs at -6 V for its time, then stops and finishes")
  void ejectFor() {
    Command eject = roller.ejectFor(0.5);
    scheduler.schedule(eject);

    runLoops(20); // 0.4 s of simulated time
    assertEquals(-6.0, roller.getAppliedVolts(), EPSILON);
    assertTrue(eject.isScheduled());

    runLoops(10); // past 0.5 s
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
    assertFalse(eject.isScheduled());
  }

  @Test
  @DisplayName("a new roller command interrupts the running one")
  void interruption() {
    Command intake = roller.runAtVolts(6.0);
    scheduler.schedule(intake);
    runLoops(1);

    Command eject = roller.ejectFor(1.0);
    scheduler.schedule(eject);
    runLoops(1);

    assertFalse(intake.isScheduled());
    assertTrue(eject.isScheduled());
    assertEquals(-6.0, roller.getAppliedVolts(), EPSILON);
  }

  @Test
  @DisplayName("the default command holds 0 V whenever nothing else uses the roller")
  void defaultCommand() {
    roller.setDefaultCommand(roller.stopCommand());
    roller.setVolts(3.0);
    runLoops(2);
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);

    Command intake = roller.runAtVolts(6.0);
    scheduler.schedule(intake);
    runLoops(1);
    assertEquals(6.0, roller.getAppliedVolts(), EPSILON);

    scheduler.cancel(intake);
    runLoops(2);
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
  }

  @Test
  @DisplayName("a whileTrue binding runs the roller only while the button is held")
  void triggerBinding() {
    boolean[] held = {false};
    new Trigger(() -> held[0]).whileTrue(roller.runAtVolts(6.0));

    runLoops(1);
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
    held[0] = true;
    runLoops(1);
    assertEquals(6.0, roller.getAppliedVolts(), EPSILON);
    held[0] = false;
    runLoops(1);
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
  }

  @Test
  @DisplayName("commands don't start while the robot is disabled")
  void disabled() {
    DriverStationSim.setEnabled(false);
    DriverStationSim.notifyNewData();

    Command intake = roller.runAtVolts(6.0);
    scheduler.schedule(intake);
    runLoops(1);

    assertFalse(intake.isScheduled());
    assertEquals(0.0, roller.getAppliedVolts(), EPSILON);
  }
}
