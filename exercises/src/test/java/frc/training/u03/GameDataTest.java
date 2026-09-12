package frc.training.u03;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import frc.training.u03.GameData.AllianceColor;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameDataTest {
  @Test
  @DisplayName("R and B map to alliance colors")
  void lettersMapToColors() {
    assertEquals(Optional.of(AllianceColor.RED), GameData.inactiveFirst("R"));
    assertEquals(Optional.of(AllianceColor.BLUE), GameData.inactiveFirst("B"));
    assertEquals(Optional.of(AllianceColor.BLUE), GameData.inactiveFirst(" B "));
  }

  @Test
  @DisplayName("missing or unknown data gives an empty Optional instead of crashing")
  void missingData() {
    assertEquals(Optional.empty(), GameData.inactiveFirst(""));
    assertEquals(Optional.empty(), GameData.inactiveFirst("   "));
    assertEquals(Optional.empty(), GameData.inactiveFirst(null));
    assertEquals(Optional.empty(), GameData.inactiveFirst("X"));
    assertEquals(Optional.empty(), GameData.inactiveFirst("r"));
  }

  @Test
  @DisplayName("inactiveFirstOr falls back to the default")
  void fallback() {
    assertEquals(AllianceColor.BLUE, GameData.inactiveFirstOr("", AllianceColor.BLUE));
    assertEquals(AllianceColor.RED, GameData.inactiveFirstOr("R", AllianceColor.BLUE));
  }

  @Test
  @DisplayName("parseCanId accepts whole numbers from 1 to 62, ignoring spaces")
  void parsesValidIds() {
    assertEquals(12, GameData.parseCanId(" 12 "));
    assertEquals(1, GameData.parseCanId("1"));
    assertEquals(62, GameData.parseCanId("62"));
  }

  @Test
  @DisplayName("parseCanId explains non-numbers")
  void rejectsNonNumbers() {
    IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> GameData.parseCanId("twelve"));
    assertEquals("Not a number: twelve", error.getMessage());
    IllegalArgumentException nullError = assertThrows(IllegalArgumentException.class, () -> GameData.parseCanId(null));
    assertEquals("Not a number: null", nullError.getMessage());
  }

  @Test
  @DisplayName("parseCanId explains out-of-range IDs")
  void rejectsOutOfRange() {
    IllegalArgumentException zero = assertThrows(IllegalArgumentException.class, () -> GameData.parseCanId("0"));
    assertEquals("CAN ID out of range: 0", zero.getMessage());
    assertThrows(IllegalArgumentException.class, () -> GameData.parseCanId("63"));
  }

  @Test
  @DisplayName("tryParseCanId turns failures into an empty Optional")
  void tryParse() {
    assertEquals(Optional.of(12), GameData.tryParseCanId("12"));
    assertEquals(Optional.empty(), GameData.tryParseCanId("abc"));
    assertEquals(Optional.empty(), GameData.tryParseCanId("99"));
    assertEquals(Optional.empty(), GameData.tryParseCanId(null));
  }

  @Test
  @DisplayName("countValidCanIds skips bad entries without crashing")
  void countValid() {
    assertEquals(2, GameData.countValidCanIds(new String[] {"1", "x", "70", " 5 ", null}));
    assertEquals(0, GameData.countValidCanIds(new String[] {}));
  }
}
