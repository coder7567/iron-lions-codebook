package frc.training.u11;

import java.util.ArrayList;
import java.util.List;

/**
 * Exercise u11-alert: debounced alerts, like the disconnect alerts our drivetrain raises.
 *
 * <p>An alert turns on only after its condition has held for a set number of loops, and turns off
 * immediately when the condition clears. A summary line gives the pit one thing to read.
 */
public final class AlertManager {
  public enum Severity {
    ERROR,
    WARNING,
    INFO
  }

  /** One alert and its debounce state. */
  public static final class Alert {
    private final String text;
    private final Severity severity;
    private final int loopsToTrigger;
    private int loops = 0;
    private boolean active = false;

    Alert(String text, Severity severity, int loopsToTrigger) {
      this.text = text;
      this.severity = severity;
      this.loopsToTrigger = loopsToTrigger;
    }

    public String text() {
      return text;
    }

    public Severity severity() {
      return severity;
    }

    public boolean isActive() {
      return active;
    }

    /**
     * Feeds one loop's condition. While the condition is true, count loops; once the count reaches
     * loopsToTrigger, the alert is active. A false condition clears the count and the alert at once.
     *
     * @return whether the alert is active after this loop
     */
    public boolean update(boolean condition) {
      // TODO
      throw new UnsupportedOperationException("TODO");
    }
  }

  private final List<Alert> alerts = new ArrayList<>();

  /** Creates an alert, registers it, and returns it. */
  public Alert add(String text, Severity severity, int loopsToTrigger) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The text of every active alert of one severity, in the order the alerts were added. */
  public List<String> active(Severity severity) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * One line for the dashboard: "OK" when nothing is active, otherwise the non-zero counts in severity
   * order, pluralized, joined with ", ". For example "2 errors, 1 warning".
   */
  public String summary() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
