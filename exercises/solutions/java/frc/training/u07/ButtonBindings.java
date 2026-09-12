package frc.training.u07;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

/** Reference solution for exercise u07-bindings. */
public final class ButtonBindings {
  public interface Target {
    void schedule();

    void cancel();

    boolean isScheduled();
  }

  /** What a binding does, given the condition's previous and current values. */
  private interface EdgeAction {
    void apply(boolean previous, boolean current);
  }

  private final BooleanSupplier condition;
  private final List<Runnable> bindings = new ArrayList<>();

  public ButtonBindings(BooleanSupplier condition) {
    this.condition = condition;
  }

  public ButtonBindings onTrue(Target target) {
    return addBinding(
        (previous, current) -> {
          if (!previous && current) {
            target.schedule();
          }
        });
  }

  public ButtonBindings onFalse(Target target) {
    return addBinding(
        (previous, current) -> {
          if (previous && !current) {
            target.schedule();
          }
        });
  }

  public ButtonBindings whileTrue(Target target) {
    return addBinding(
        (previous, current) -> {
          if (!previous && current) {
            target.schedule();
          } else if (previous && !current) {
            target.cancel();
          }
        });
  }

  public ButtonBindings toggleOnTrue(Target target) {
    return addBinding(
        (previous, current) -> {
          if (!previous && current) {
            if (target.isScheduled()) {
              target.cancel();
            } else {
              target.schedule();
            }
          }
        });
  }

  public void poll() {
    for (Runnable binding : bindings) {
      binding.run();
    }
  }

  private ButtonBindings addBinding(EdgeAction action) {
    bindings.add(
        new Runnable() {
          // Like WPILib, read the condition when the binding is created.
          private boolean previous = condition.getAsBoolean();

          @Override
          public void run() {
            boolean current = condition.getAsBoolean();
            action.apply(previous, current);
            previous = current;
          }
        });
    return this;
  }
}
