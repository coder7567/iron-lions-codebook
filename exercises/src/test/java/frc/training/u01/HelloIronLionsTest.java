package frc.training.u01;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class HelloIronLionsTest {
  @Test
  @DisplayName("teamNumber() returns 967")
  void teamNumberIs967() {
    assertEquals(967, HelloIronLions.teamNumber());
  }

  @Test
  @DisplayName("greeting(\"Ada\") welcomes Ada to 967")
  void greetsAda() {
    assertEquals("Welcome to 967, Ada!", HelloIronLions.greeting("Ada"));
  }

  @Test
  @DisplayName("greeting uses whatever name it is given")
  void greetsAnyName() {
    assertEquals("Welcome to 967, Grace!", HelloIronLions.greeting("Grace"));
  }
}
