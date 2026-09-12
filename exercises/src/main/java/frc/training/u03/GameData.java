package frc.training.u03;

import java.util.Optional;

/**
 * Exercise u03-gamedata: handle missing and bad input without crashing the robot.
 *
 * <p>Match data can be empty, and people make typos. Some problems should produce an {@link Optional}, and
 * some should throw an exception with a helpful message.
 */
public final class GameData {
  private GameData() {}

  /** The two alliance colors. */
  public enum AllianceColor {
    RED,
    BLUE
  }

  /**
   * Reads the 2026 game-specific message: after trimming spaces, a first character of {@code 'R'} means red's
   * HUB goes inactive first and {@code 'B'} means blue's. Returns an empty Optional for null, empty, or any
   * other text.
   */
  public static Optional<AllianceColor> inactiveFirst(String message) {
    // TODO
    return Optional.empty();
  }

  /** Like {@link #inactiveFirst}, but returns {@code defaultColor} when the message does not say. */
  public static AllianceColor inactiveFirstOr(String message, AllianceColor defaultColor) {
    // TODO: reuse inactiveFirst and Optional.orElse
    return null;
  }

  /**
   * Parses a CAN ID typed by a person, like {@code " 12 "}.
   *
   * @throws IllegalArgumentException with the message {@code "Not a number: <text>"} when the trimmed text is
   *     not a whole number (or is null), or {@code "CAN ID out of range: <id>"} when it is outside 1 to 62
   */
  public static int parseCanId(String text) {
    // TODO: Integer.parseInt throws NumberFormatException; catch it and throw IllegalArgumentException
    return 0;
  }

  /** Like {@link #parseCanId}, but returns an empty Optional instead of throwing. */
  public static Optional<Integer> tryParseCanId(String text) {
    // TODO
    return Optional.empty();
  }

  /** Counts how many entries are valid CAN IDs. Bad entries, including null, are skipped without crashing. */
  public static int countValidCanIds(String[] texts) {
    // TODO
    return 0;
  }
}
