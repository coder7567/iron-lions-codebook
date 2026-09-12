package frc.training.u05;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/** Reference solution for exercise u05-triggers. */
public final class Conditions {
  private Conditions() {}

  public static BooleanSupplier and(BooleanSupplier a, BooleanSupplier b) {
    return () -> a.getAsBoolean() && b.getAsBoolean();
  }

  public static BooleanSupplier or(BooleanSupplier a, BooleanSupplier b) {
    return () -> a.getAsBoolean() || b.getAsBoolean();
  }

  public static BooleanSupplier not(BooleanSupplier condition) {
    return () -> !condition.getAsBoolean();
  }

  public static BooleanSupplier above(DoubleSupplier value, double threshold) {
    return () -> value.getAsDouble() > threshold;
  }

  public static BooleanSupplier risingEdge(BooleanSupplier condition) {
    boolean[] previous = {false};
    return () -> {
      boolean now = condition.getAsBoolean();
      boolean rising = now && !previous[0];
      previous[0] = now;
      return rising;
    };
  }

  public static BooleanSupplier debounced(BooleanSupplier condition, int loops) {
    int required = Math.max(1, loops);
    int[] streak = {0};
    return () -> {
      if (condition.getAsBoolean()) {
        streak[0]++;
      } else {
        streak[0] = 0;
      }
      return streak[0] >= required;
    };
  }
}
