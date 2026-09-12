package frc.training.u15;

import java.util.LinkedHashMap;
import java.util.Map;

/** Reference solution for exercise u15-budget. */
public final class LoopBudget {
  public record Section(String name, double seconds) {}

  private final double budgetSeconds;
  private final Map<String, Double> currentLoop = new LinkedHashMap<>();
  private final Map<String, Double> totals = new LinkedHashMap<>();

  private int loops = 0;
  private int overruns = 0;
  private double totalSeconds = 0.0;

  public LoopBudget(double budgetSeconds) {
    this.budgetSeconds = budgetSeconds;
  }

  public double budgetSeconds() {
    return budgetSeconds;
  }

  public void record(String name, double seconds) {
    currentLoop.merge(name, seconds, Double::sum);
  }

  public double currentLoopSeconds() {
    double total = 0.0;
    for (double seconds : currentLoop.values()) {
      total += seconds;
    }
    return total;
  }

  public boolean endLoop() {
    double loopSeconds = currentLoopSeconds();
    for (Map.Entry<String, Double> entry : currentLoop.entrySet()) {
      totals.merge(entry.getKey(), entry.getValue(), Double::sum);
    }
    currentLoop.clear();

    loops++;
    totalSeconds += loopSeconds;
    boolean overran = loopSeconds > budgetSeconds;
    if (overran) {
      overruns++;
    }
    return overran;
  }

  public int loopCount() {
    return loops;
  }

  public int overrunCount() {
    return overruns;
  }

  public double averageLoopSeconds() {
    return loops == 0 ? 0.0 : totalSeconds / loops;
  }

  public Section worstSection() {
    Section worst = null;
    for (Map.Entry<String, Double> entry : totals.entrySet()) {
      if (worst == null || entry.getValue() > worst.seconds()) {
        worst = new Section(entry.getKey(), entry.getValue());
      }
    }
    return worst;
  }
}
