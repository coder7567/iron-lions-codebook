package frc.training.u11;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Exercise u11-inputs: a tiny version of AdvantageKit's Logger, with the two modes that matter.
 *
 * <p>In REAL mode, {@code processInputs} saves the inputs object's values into this loop's frame. In
 * REPLAY mode, it overwrites the inputs object from the recorded frame, so the code above the IO layer
 * runs on exactly the values the robot saw.
 */
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

  /** A logger that records what the robot does. */
  public MiniLogger() {
    this.mode = Mode.REAL;
    this.source = List.of();
  }

  /** A logger that replays recorded frames instead of reading hardware. */
  public MiniLogger(List<Map<String, Object>> source) {
    this.mode = Mode.REPLAY;
    this.source = source;
  }

  public Mode getMode() {
    return mode;
  }

  /** The frames recorded so far, one per loop. */
  public List<Map<String, Object>> frames() {
    return recorded;
  }

  /**
   * Starts a loop. In REAL mode, begin a new empty frame and add it to the recorded list. In REPLAY
   * mode, take the next frame from the source as the values for this loop, and also start a new
   * recorded frame so the replay's outputs can be compared with the original ones.
   *
   * @throws IllegalStateException in REPLAY mode when the source has no more frames
   */
  public void beginLoop() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * In REAL mode, writes the inputs into this loop's recorded frame. In REPLAY mode, overwrites the
   * inputs object from the source frame, then writes them into the recorded frame as well.
   */
  public void processInputs(String prefix, FlywheelInputs inputs) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Records an output value for this loop, in both modes. */
  public void recordOutput(String key, Object value) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Provided: a fresh table, keeping insertion order so frames are easy to read in tests. */
  static Map<String, Object> newTable() {
    return new LinkedHashMap<>();
  }
}
