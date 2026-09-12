package frc.training.u15;

/**
 * Exercise u15-budget: a loop-time budget, like the watchdog epochs the CommandScheduler prints when a
 * loop runs long, except you can query it.
 *
 * <p>Each loop records how long its named sections took, then ends. The budget tracks overruns and
 * which section costs the most across the whole run.
 */
public final class LoopBudget {
  /** A section name and a duration in seconds. */
  public record Section(String name, double seconds) {}

  private final double budgetSeconds;

  public LoopBudget(double budgetSeconds) {
    this.budgetSeconds = budgetSeconds;
  }

  public double budgetSeconds() {
    return budgetSeconds;
  }

  /** Adds time to a named section of the loop in progress. The same name may be recorded twice. */
  public void record(String name, double seconds) {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The total recorded so far in the loop in progress. */
  public double currentLoopSeconds() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * Ends the loop in progress: counts it, counts an overrun when its total was above the budget, clears
   * the in-progress sections, and returns whether it overran.
   */
  public boolean endLoop() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  public int loopCount() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  public int overrunCount() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /** The mean total time of the completed loops, or 0.0 when none have finished. */
  public double averageLoopSeconds() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }

  /**
   * The section with the largest total across every completed loop, with that total, or null when
   * nothing has been recorded. Ties go to the section recorded first.
   */
  public Section worstSection() {
    // TODO
    throw new UnsupportedOperationException("TODO");
  }
}
