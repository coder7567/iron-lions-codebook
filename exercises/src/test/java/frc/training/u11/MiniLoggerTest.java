package frc.training.u11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MiniLoggerTest {
  private static final double EPSILON = 1e-9;

  /** The "subsystem": whatever the code above the IO layer computes from its inputs. */
  private static double feedforwardVolts(FlywheelInputs inputs) {
    return 0.2 + 1.93e-3 * inputs.velocityRpm;
  }

  @Test
  @DisplayName("inputs round-trip through a table")
  void inputsRoundTrip() {
    FlywheelInputs written = new FlywheelInputs();
    written.velocityRpm = 2500.0;
    written.appliedVolts = 5.1;
    written.currentAmps = 22.5;
    written.connected = true;

    Map<String, Object> table = MiniLogger.newTable();
    written.toLog("Flywheel", table);

    FlywheelInputs read = new FlywheelInputs();
    read.fromLog("Flywheel", table);

    assertEquals(2500.0, read.velocityRpm, EPSILON);
    assertEquals(5.1, read.appliedVolts, EPSILON);
    assertEquals(22.5, read.currentAmps, EPSILON);
    assertTrue(read.connected);
  }

  @Test
  @DisplayName("a field missing from the table keeps its current value")
  void missingEntriesAreLeftAlone() {
    Map<String, Object> table = MiniLogger.newTable();
    FlywheelInputs partial = new FlywheelInputs();
    partial.velocityRpm = 1000.0;
    partial.toLog("Flywheel", table);
    table.remove("Flywheel/currentAmps");

    FlywheelInputs read = new FlywheelInputs();
    read.currentAmps = 99.0;
    read.fromLog("Flywheel", table);

    assertEquals(1000.0, read.velocityRpm, EPSILON);
    assertEquals(99.0, read.currentAmps, EPSILON);
  }

  @Test
  @DisplayName("a real run records one frame per loop, with inputs and outputs")
  void realRunRecordsFrames() {
    MiniLogger logger = new MiniLogger();
    FlywheelInputs inputs = new FlywheelInputs();

    for (int loop = 0; loop < 3; loop++) {
      logger.beginLoop();
      inputs.velocityRpm = 1000.0 * loop; // "hardware"
      inputs.connected = true;
      logger.processInputs("Flywheel", inputs);
      logger.recordOutput("Flywheel/ffVolts", feedforwardVolts(inputs));
    }

    List<Map<String, Object>> frames = logger.frames();
    assertEquals(3, frames.size());
    assertEquals(2000.0, (Double) frames.get(2).get("Flywheel/velocityRpm"), EPSILON);
    assertEquals(0.2 + 1.93e-3 * 1000.0, (Double) frames.get(1).get("Flywheel/ffVolts"), 1e-9);
  }

  @Test
  @DisplayName("replaying a log reproduces the outputs even with broken hardware readings")
  void replayReproducesOutputs() {
    MiniLogger real = new MiniLogger();
    FlywheelInputs inputs = new FlywheelInputs();
    List<Double> liveOutputs = new ArrayList<>();

    double[] measuredRpm = {0.0, 900.0, 1800.0, 2400.0, 2500.0};
    for (double rpm : measuredRpm) {
      real.beginLoop();
      inputs.velocityRpm = rpm;
      inputs.connected = true;
      real.processInputs("Flywheel", inputs);
      double volts = feedforwardVolts(inputs);
      liveOutputs.add(volts);
      real.recordOutput("Flywheel/ffVolts", volts);
    }

    // Replay with an IO layer that reports nonsense: the logged inputs must win.
    MiniLogger replay = new MiniLogger(real.frames());
    FlywheelInputs replayInputs = new FlywheelInputs();
    for (int loop = 0; loop < measuredRpm.length; loop++) {
      replay.beginLoop();
      replayInputs.velocityRpm = -12345.0;
      replayInputs.connected = false;
      replay.processInputs("Flywheel", replayInputs);
      replay.recordOutput("Flywheel/ffVolts", feedforwardVolts(replayInputs));
    }

    for (int loop = 0; loop < measuredRpm.length; loop++) {
      assertEquals(
          liveOutputs.get(loop),
          (Double) replay.frames().get(loop).get("Flywheel/ffVolts"),
          1e-9,
          "loop " + loop);
    }
  }

  @Test
  @DisplayName("replay runs out of frames instead of inventing data")
  void replayRunsOut() {
    MiniLogger real = new MiniLogger();
    real.beginLoop();
    real.recordOutput("x", 1.0);

    MiniLogger replay = new MiniLogger(real.frames());
    replay.beginLoop();

    assertThrows(IllegalStateException.class, replay::beginLoop);
  }

  @Test
  @DisplayName("the logger reports which mode it is in")
  void modes() {
    assertEquals(MiniLogger.Mode.REAL, new MiniLogger().getMode());
    assertEquals(MiniLogger.Mode.REPLAY, new MiniLogger(List.of(MiniLogger.newTable())).getMode());
  }
}
