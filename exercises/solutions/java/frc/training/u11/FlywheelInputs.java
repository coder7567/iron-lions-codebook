package frc.training.u11;

import java.util.Map;

/** Reference solution for exercise u11-inputs. */
public final class FlywheelInputs {
  public double velocityRpm;
  public double appliedVolts;
  public double currentAmps;
  public boolean connected;

  public void toLog(String prefix, Map<String, Object> table) {
    table.put(prefix + "/velocityRpm", velocityRpm);
    table.put(prefix + "/appliedVolts", appliedVolts);
    table.put(prefix + "/currentAmps", currentAmps);
    table.put(prefix + "/connected", connected);
  }

  public void fromLog(String prefix, Map<String, Object> table) {
    velocityRpm = readDouble(table, prefix + "/velocityRpm", velocityRpm);
    appliedVolts = readDouble(table, prefix + "/appliedVolts", appliedVolts);
    currentAmps = readDouble(table, prefix + "/currentAmps", currentAmps);
    connected = readBoolean(table, prefix + "/connected", connected);
  }

  private static double readDouble(Map<String, Object> table, String key, double fallback) {
    Object value = table.get(key);
    return value instanceof Double d ? d : fallback;
  }

  private static boolean readBoolean(Map<String, Object> table, String key, boolean fallback) {
    Object value = table.get(key);
    return value instanceof Boolean b ? b : fallback;
  }
}
