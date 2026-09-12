package frc.training.u03;

/** Reference solution for exercise u03-counter. */
public class ConsecutiveCounter {
  public static final int DEFAULT_REQUIRED_LOOPS = 50;

  private static int createdCount = 0;

  private final int requiredLoops;
  private int count = 0;

  public ConsecutiveCounter() {
    this(DEFAULT_REQUIRED_LOOPS);
  }

  public ConsecutiveCounter(int requiredLoops) {
    this.requiredLoops = Math.max(1, requiredLoops);
    createdCount++;
  }

  public boolean update(boolean condition) {
    if (condition) {
      count++;
    } else {
      count = 0;
    }
    return isTriggered();
  }

  public boolean isTriggered() {
    return count >= requiredLoops;
  }

  public int getCount() {
    return count;
  }

  public int getRequiredLoops() {
    return requiredLoops;
  }

  public void reset() {
    count = 0;
  }

  public static int getCreatedCount() {
    return createdCount;
  }

  public static int loopsForSeconds(double seconds) {
    // Subtract a tiny amount so values like 0.5 * 50 = 25.000000000000004 do not round up to 26.
    return (int) Math.ceil(seconds * 50.0 - 1e-9);
  }
}
