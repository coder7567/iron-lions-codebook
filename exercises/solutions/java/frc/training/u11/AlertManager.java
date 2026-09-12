package frc.training.u11;

import java.util.ArrayList;
import java.util.List;

/** Reference solution for exercise u11-alert. */
public final class AlertManager {
  public enum Severity {
    ERROR,
    WARNING,
    INFO
  }

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

    public boolean update(boolean condition) {
      if (!condition) {
        loops = 0;
        active = false;
        return false;
      }
      loops++;
      active = loops >= loopsToTrigger;
      return active;
    }
  }

  private final List<Alert> alerts = new ArrayList<>();

  public Alert add(String text, Severity severity, int loopsToTrigger) {
    Alert alert = new Alert(text, severity, loopsToTrigger);
    alerts.add(alert);
    return alert;
  }

  public List<String> active(Severity severity) {
    List<String> texts = new ArrayList<>();
    for (Alert alert : alerts) {
      if (alert.isActive() && alert.severity() == severity) {
        texts.add(alert.text());
      }
    }
    return texts;
  }

  public String summary() {
    List<String> parts = new ArrayList<>();
    for (Severity severity : Severity.values()) {
      int count = active(severity).size();
      if (count > 0) {
        parts.add(count + " " + name(severity) + (count == 1 ? "" : "s"));
      }
    }
    return parts.isEmpty() ? "OK" : String.join(", ", parts);
  }

  private static String name(Severity severity) {
    return switch (severity) {
      case ERROR -> "error";
      case WARNING -> "warning";
      case INFO -> "note";
    };
  }
}
