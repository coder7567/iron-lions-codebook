package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelemetryTextTest {
  private static final String FULL_SHA = "FE04405398eec6f862f07de3f670d015f8e9f7e5";

  @Test
  @DisplayName("formatPose rounds meters to 2 places and shows heading in degrees")
  void formatPose() {
    assertEquals("x=4.63 m, y=4.02 m, heading=90.0°", TelemetryText.formatPose(4.626, 4.02, Math.PI / 2));
    assertEquals("x=0.00 m, y=0.00 m, heading=-45.0°", TelemetryText.formatPose(0, 0, -Math.PI / 4));
  }

  @Test
  @DisplayName("formatCurrent shows one decimal and the unit")
  void formatCurrent() {
    assertEquals("37.5 A", TelemetryText.formatCurrent(37.46));
    assertEquals("0.0 A", TelemetryText.formatCurrent(0));
  }

  @Test
  @DisplayName("shortSha keeps 7 lowercase characters")
  void shortSha() {
    assertEquals("fe04405", TelemetryText.shortSha(FULL_SHA));
    assertEquals("abc", TelemetryText.shortSha("ABC"));
  }

  @Test
  @DisplayName("buildSummary mentions uncommitted changes only when dirty")
  void buildSummary() {
    assertEquals("fe04405 on main", TelemetryText.buildSummary(FULL_SHA, "main", false));
    assertEquals("fe04405 on Turret-Dev (uncommitted changes)", TelemetryText.buildSummary(FULL_SHA, "Turret-Dev", true));
  }

  @Test
  @DisplayName("disconnectedAlert matches the Module alert text")
  void disconnectedAlert() {
    assertEquals("Disconnected drive motor on module 2.", TelemetryText.disconnectedAlert("drive", 2));
    assertEquals("Disconnected turn motor on module 0.", TelemetryText.disconnectedAlert("turn", 0));
  }

  @Test
  @DisplayName("cameraNumber reads only well-formed camera names")
  void cameraNumber() {
    assertEquals(2, TelemetryText.cameraNumber("April_Tag_2"));
    assertEquals(10, TelemetryText.cameraNumber("April_Tag_10"));
    assertEquals(-1, TelemetryText.cameraNumber("april_tag_2"));
    assertEquals(-1, TelemetryText.cameraNumber("April_Tag_"));
    assertEquals(-1, TelemetryText.cameraNumber("April_Tag_2b"));
    assertEquals(-1, TelemetryText.cameraNumber("Front_Cam"));
    assertEquals(-1, TelemetryText.cameraNumber(null));
  }
}
