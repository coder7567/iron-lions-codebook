package frc.training.u03;

/**
 * Exercise u03-counter: counts consecutive robot loops where a condition is true.
 *
 * <p>This is the pattern behind {@code jamCount} in our Intake: a jam is only declared after high current
 * has lasted many loops in a row. Here the counting is <em>encapsulated</em>: all fields are private, and
 * code outside the class can only use the public methods.
 */
public class ConsecutiveCounter {
  /** 50 loops is one second at the robot's 20 ms loop period. */
  public static final int DEFAULT_REQUIRED_LOOPS = 50;

  // TODO: a private static int that counts how many ConsecutiveCounter objects have been created
  // TODO: a private final int for the required loop count
  // TODO: a private int for the current streak

  /** Creates a counter that triggers after {@link #DEFAULT_REQUIRED_LOOPS} loops. */
  public ConsecutiveCounter() {
    this(DEFAULT_REQUIRED_LOOPS);
  }

  /**
   * Creates a counter that triggers after {@code requiredLoops} consecutive true loops. Values below 1 are
   * treated as 1. Every counter created adds one to {@link #getCreatedCount()}.
   */
  public ConsecutiveCounter(int requiredLoops) {
    // TODO
  }

  /**
   * Call exactly once per robot loop. A true condition extends the streak; a false condition resets it
   * to 0.
   *
   * @return whether the counter is triggered after this update
   */
  public boolean update(boolean condition) {
    // TODO
    return false;
  }

  /** Returns true when the streak has reached the required number of loops. */
  public boolean isTriggered() {
    // TODO
    return false;
  }

  /** Returns the current streak length. */
  public int getCount() {
    // TODO
    return 0;
  }

  /** Returns how many consecutive loops are required to trigger. */
  public int getRequiredLoops() {
    // TODO
    return 0;
  }

  /** Clears the streak. */
  public void reset() {
    // TODO
  }

  /** Returns how many ConsecutiveCounter objects have been created so far in this program. */
  public static int getCreatedCount() {
    // TODO
    return 0;
  }

  /**
   * Converts a duration to a number of 20 ms robot loops, rounding up. Examples: 1.0 s is 50 loops,
   * 0.05 s is 3 loops (2.5 rounded up).
   */
  public static int loopsForSeconds(double seconds) {
    // TODO
    return 0;
  }
}
