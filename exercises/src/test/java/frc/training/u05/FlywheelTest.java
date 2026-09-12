package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FlywheelTest {
  private static final double EPSILON = 1e-9;
  private FlywheelIOFake io;
  private Flywheel flywheel;

  @BeforeEach
  void setUp() {
    io = new FlywheelIOFake();
    flywheel = new Flywheel(io);
  }

  @Test
  @DisplayName("the fake clamps voltage commands and counts them")
  void fakeClampsAndCounts() {
    io.setVoltage(15);
    assertEquals(12.0, io.getLastVolts(), EPSILON);
    io.setVoltage(-20);
    assertEquals(-12.0, io.getLastVolts(), EPSILON);
    io.stop();
    assertEquals(0.0, io.getLastVolts(), EPSILON);
    assertEquals(3, io.getVoltageCommandCount());
  }

  @Test
  @DisplayName("the fake copies its readings into inputs")
  void fakeFillsInputs() {
    FlywheelIO.FlywheelIOInputs inputs = new FlywheelIO.FlywheelIOInputs();
    io.setMeasuredRpm(2500);
    io.setCurrentAmps(31.5);
    io.setVoltage(6.0);
    io.updateInputs(inputs);
    assertEquals(2500.0, inputs.velocityRpm, EPSILON);
    assertEquals(31.5, inputs.currentAmps, EPSILON);
    assertEquals(6.0, inputs.appliedVolts, EPSILON);
    assertTrue(inputs.connected);
  }

  @Test
  @DisplayName("with no target, periodic stops the motor")
  void noTargetStops() {
    flywheel.periodic();
    assertEquals(0.0, io.getLastVolts(), EPSILON);
    assertEquals(1, io.getVoltageCommandCount());
    assertFalse(flywheel.isAtSpeed(100));
  }

  @Test
  @DisplayName("from rest toward 3000 RPM: kS + kV·3000 + kP·3000 = 8.99 V")
  void spinUpVoltage() {
    flywheel.setTargetRpm(3000);
    flywheel.periodic();
    assertEquals(8.99, io.getLastVolts(), 1e-9);
  }

  @Test
  @DisplayName("at speed, only feedforward remains: 5.99 V, and isAtSpeed is true")
  void atSpeed() {
    flywheel.setTargetRpm(3000);
    io.setMeasuredRpm(3000);
    flywheel.periodic();
    assertEquals(5.99, io.getLastVolts(), 1e-9);
    assertTrue(flywheel.isAtSpeed(100));
    assertEquals(3000.0, flywheel.getInputs().velocityRpm, EPSILON);
  }

  @Test
  @DisplayName("isAtSpeed checks both sides of the target")
  void tolerance() {
    flywheel.setTargetRpm(3000);
    io.setMeasuredRpm(2850);
    flywheel.periodic();
    assertFalse(flywheel.isAtSpeed(100));
    io.setMeasuredRpm(3050);
    flywheel.periodic();
    assertTrue(flywheel.isAtSpeed(100));
  }

  @Test
  @DisplayName("large requests clamp to 12 V")
  void clampsHigh() {
    flywheel.setTargetRpm(6000);
    flywheel.periodic();
    assertEquals(12.0, io.getLastVolts(), EPSILON);
  }

  @Test
  @DisplayName("a disconnected motor is commanded 0 V even with a target")
  void disconnectedIsSafe() {
    flywheel.setTargetRpm(3000);
    io.setConnected(false);
    flywheel.periodic();
    assertEquals(0.0, io.getLastVolts(), EPSILON);
  }

  @Test
  @DisplayName("stop() returns the flywheel to 0 V on the next periodic")
  void stopMethod() {
    flywheel.setTargetRpm(3000);
    flywheel.periodic();
    flywheel.stop();
    flywheel.periodic();
    assertEquals(0.0, io.getLastVolts(), EPSILON);
  }

  @Test
  @DisplayName("any FlywheelIO works, even an anonymous do-nothing one like our replay IO")
  void acceptsAnyImplementation() {
    Flywheel replay = new Flywheel(new FlywheelIO() {});
    replay.setTargetRpm(3000);
    assertDoesNotThrow(replay::periodic);
    assertFalse(replay.getInputs().connected);
  }
}
