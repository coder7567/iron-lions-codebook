package frc.training.u11;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import frc.training.u11.AlertManager.Alert;
import frc.training.u11.AlertManager.Severity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlertManagerTest {
  private AlertManager alerts;

  @BeforeEach
  void setUp() {
    alerts = new AlertManager();
  }

  @Test
  @DisplayName("an alert waits for its debounce before turning on")
  void debounce() {
    Alert gyro = alerts.add("Gyro disconnected", Severity.ERROR, 25);

    for (int loop = 1; loop < 25; loop++) {
      assertFalse(gyro.update(true), "loop " + loop);
    }
    assertTrue(gyro.update(true));
    assertTrue(gyro.isActive());
  }

  @Test
  @DisplayName("the condition going false clears the alert and its count immediately")
  void clearsImmediately() {
    Alert module = alerts.add("Module 2 drive disconnected", Severity.ERROR, 3);

    module.update(true);
    module.update(true);
    assertFalse(module.update(false));
    assertFalse(module.isActive());

    // The count restarted, so two more true loops are not enough.
    module.update(true);
    assertFalse(module.update(true));
    assertTrue(module.update(true));
  }

  @Test
  @DisplayName("an alert with a one-loop debounce reacts on the first loop")
  void immediateAlert() {
    Alert battery = alerts.add("Battery under 11 V", Severity.WARNING, 1);

    assertTrue(battery.update(true));
    assertFalse(battery.update(false));
  }

  @Test
  @DisplayName("active alerts are grouped by severity in the order they were added")
  void grouping() {
    Alert gyro = alerts.add("Gyro disconnected", Severity.ERROR, 1);
    Alert camera = alerts.add("Camera 2 disconnected", Severity.WARNING, 1);
    Alert module = alerts.add("Module 0 turn disconnected", Severity.ERROR, 1);
    Alert note = alerts.add("Tuning mode on", Severity.INFO, 1);

    gyro.update(true);
    camera.update(true);
    module.update(true);
    note.update(false);

    assertEquals(
        List.of("Gyro disconnected", "Module 0 turn disconnected"), alerts.active(Severity.ERROR));
    assertEquals(List.of("Camera 2 disconnected"), alerts.active(Severity.WARNING));
    assertEquals(List.of(), alerts.active(Severity.INFO));
  }

  @Test
  @DisplayName("the summary line reads like something a pit crew can scan")
  void summary() {
    Alert gyro = alerts.add("Gyro disconnected", Severity.ERROR, 1);
    Alert module = alerts.add("Module 0 turn disconnected", Severity.ERROR, 1);
    Alert camera = alerts.add("Camera 2 disconnected", Severity.WARNING, 1);

    assertEquals("OK", alerts.summary());

    gyro.update(true);
    assertEquals("1 error", alerts.summary());

    module.update(true);
    camera.update(true);
    assertEquals("2 errors, 1 warning", alerts.summary());

    gyro.update(false);
    module.update(false);
    assertEquals("1 warning", alerts.summary());
  }
}
