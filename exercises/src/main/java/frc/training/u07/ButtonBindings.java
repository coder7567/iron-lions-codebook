package frc.training.u07;

import java.util.function.BooleanSupplier;

/**
 * Exercise u07-bindings: how WPILib's Trigger turns a condition into scheduling. The scheduler polls
 * triggers once per loop. Each binding compares the condition with its value on the previous poll and
 * acts only on edges.
 */
public final class ButtonBindings {
  /** What a binding controls. In WPILib this is a Command, scheduled through the CommandScheduler. */
  public interface Target {
    void schedule();

    void cancel();

    boolean isScheduled();
  }

  private final BooleanSupplier condition;

  public ButtonBindings(BooleanSupplier condition) {
    this.condition = condition;
  }

  /** Schedules the target on the rising edge: the poll where the condition becomes true. */
  public ButtonBindings onTrue(Target target) {
    // TODO: add a binding, then return this so calls can be chained
    throw new UnsupportedOperationException("TODO");
  }

  /** Schedules the target on the falling edge: the poll where the condition becomes false. */
  public ButtonBindings onFalse(Target target) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** Schedules the target on the rising edge and cancels it on the falling edge. */
  public ButtonBindings whileTrue(Target target) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** On each rising edge, cancels the target if it is scheduled; otherwise schedules it. */
  public ButtonBindings toggleOnTrue(Target target) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Checks every binding once, in the order the bindings were added. Each binding reads the condition,
   * acts if an edge happened since its previous poll, and remembers the new value. A binding's first
   * "previous" value is the condition's value at the moment the binding was added.
   */
  public void poll() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
