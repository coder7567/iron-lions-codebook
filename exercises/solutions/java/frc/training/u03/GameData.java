package frc.training.u03;

import java.util.Optional;

/** Reference solution for exercise u03-gamedata. */
public final class GameData {
  private GameData() {}

  public enum AllianceColor {
    RED,
    BLUE
  }

  public static Optional<AllianceColor> inactiveFirst(String message) {
    if (message == null) {
      return Optional.empty();
    }
    String trimmed = message.trim();
    if (trimmed.isEmpty()) {
      return Optional.empty();
    }
    switch (trimmed.charAt(0)) {
      case 'R':
        return Optional.of(AllianceColor.RED);
      case 'B':
        return Optional.of(AllianceColor.BLUE);
      default:
        return Optional.empty();
    }
  }

  public static AllianceColor inactiveFirstOr(String message, AllianceColor defaultColor) {
    return inactiveFirst(message).orElse(defaultColor);
  }

  public static int parseCanId(String text) {
    if (text == null) {
      throw new IllegalArgumentException("Not a number: null");
    }
    String trimmed = text.trim();
    int id;
    try {
      id = Integer.parseInt(trimmed);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Not a number: " + trimmed, e);
    }
    if (id < 1 || id > 62) {
      throw new IllegalArgumentException("CAN ID out of range: " + id);
    }
    return id;
  }

  public static Optional<Integer> tryParseCanId(String text) {
    try {
      return Optional.of(parseCanId(text));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public static int countValidCanIds(String[] texts) {
    int count = 0;
    for (String text : texts) {
      if (tryParseCanId(text).isPresent()) {
        count++;
      }
    }
    return count;
  }
}
