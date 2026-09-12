package frc.training.u11;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Reference solution for exercise u11-inputs. */
public final class MiniLogger {
  public enum Mode {
    REAL,
    REPLAY
  }

  private final Mode mode;
  private final List<Map<String, Object>> recorded = new ArrayList<>();
  private final List<Map<String, Object>> source;
  private int frameIndex = -1;
  private Map<String, Object> frame;

  public MiniLogger() {
    this.mode = Mode.REAL;
    this.source = List.of();
  }

  public MiniLogger(List<Map<String, Object>> source) {
    this.mode = Mode.REPLAY;
    this.source = source;
  }

  public Mode getMode() {
    return mode;
  }

  public List<Map<String, Object>> frames() {
    return recorded;
  }

  public void beginLoop() {
    frameIndex++;
    if (mode == Mode.REPLAY && frameIndex >= source.size()) {
      throw new IllegalStateException("replay source ran out of frames at loop " + frameIndex);
    }
    frame = newTable();
    recorded.add(frame);
  }

  public void processInputs(String prefix, FlywheelInputs inputs) {
    if (mode == Mode.REPLAY) {
      // The logged values replace whatever the IO layer produced.
      inputs.fromLog(prefix, source.get(frameIndex));
    }
    inputs.toLog(prefix, frame);
  }

  public void recordOutput(String key, Object value) {
    frame.put(key, value);
  }

  static Map<String, Object> newTable() {
    return new LinkedHashMap<>();
  }
}
