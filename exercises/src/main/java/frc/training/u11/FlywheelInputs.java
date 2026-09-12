package frc.training.u11;

import java.util.Map;

/**
 * Exercise u11-inputs: the record and replay contract behind AdvantageKit's {@code @AutoLog}.
 *
 * <p>An inputs object writes every field into a table when the robot runs, and reads every field back
 * out of the table when a log is replayed. Whatever is not written cannot be replayed.
 */
public final class FlywheelInputs {
  public double velocityRpm;
  public double appliedVolts;
  public double currentAmps;
  public boolean connected;

  /** Writes every field into the table, keyed as prefix + "/" + the field name. */
  public void toLog(String prefix, Map<String, Object> table) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Reads every field back out of the table using the same keys. A field whose entry is missing keeps
   * the value it already has, which is what makes a partially logged input silently wrong.
   */
  public void fromLog(String prefix, Map<String, Object> table) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
