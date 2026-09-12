package frc.training.u05;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;

/**
 * Exercise u05-triggers: build new conditions from old ones with lambdas.
 *
 * <p>WPILib's {@code Trigger} works the same way: a trigger wraps a {@code BooleanSupplier}, and methods
 * like {@code and()}, {@code negate()}, and {@code onTrue()} combine and watch them. Every method here
 * returns a new {@link BooleanSupplier} that checks its inputs each time it is called.
 */
public final class Conditions {
  private Conditions() {}

  /** True when both are true. Must not call {@code b} when {@code a} is false. */
  public static BooleanSupplier and(BooleanSupplier a, BooleanSupplier b) {
    // TODO
    return () -> false;
  }

  /** True when either is true. Must not call {@code b} when {@code a} is true. */
  public static BooleanSupplier or(BooleanSupplier a, BooleanSupplier b) {
    // TODO
    return () -> false;
  }

  /** True when {@code condition} is false. */
  public static BooleanSupplier not(BooleanSupplier condition) {
    // TODO
    return () -> false;
  }

  /**
   * True when {@code value} is strictly greater than {@code threshold}. This is how
   * {@code controller.rightTrigger()} turns an analog trigger axis into a button.
   */
  public static BooleanSupplier above(DoubleSupplier value, double threshold) {
    // TODO
    return () -> false;
  }

  /**
   * Returns a supplier that is true only on calls where {@code condition} has just changed from false to
   * true, like {@code Trigger.onTrue}. Treat the condition as false before the very first call. Each
   * supplier returned by this method remembers its own previous value.
   */
  public static BooleanSupplier risingEdge(BooleanSupplier condition) {
    // TODO: a lambda can only capture effectively final variables, so store changing state in an array
    return () -> false;
  }

  /**
   * Returns a supplier that is true only after {@code condition} has been true for {@code loops}
   * consecutive calls. A false call resets the count. Values of {@code loops} below 1 act like 1.
   */
  public static BooleanSupplier debounced(BooleanSupplier condition, int loops) {
    // TODO
    return () -> false;
  }
}
