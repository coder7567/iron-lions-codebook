package frc.training.u05;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Exercise u05-sampler: hands timestamped samples from a background sampling thread to the robot loop, like
 * the queues in SparkOdometryThread.
 *
 * <p>Every public method may be called from two threads at the same time. Protect all shared state with the
 * same lock, always using {@code lock.lock(); try { ... } finally { lock.unlock(); }}.
 */
public class SampleBuffer {
  private final Lock lock = new ReentrantLock();
  private final int capacity;
  // TODO: storage for timestamps and values (for example, two ArrayDeque<Double>), and a dropped-sample count

  /** @param capacity the most samples held at once; values below 1 act like 1 */
  public SampleBuffer(int capacity) {
    this.capacity = Math.max(1, capacity);
  }

  /**
   * Called by the sampling thread. Adds one sample. When the buffer is already full, first drop the oldest
   * sample and count it as dropped.
   */
  public void offer(double timestampSeconds, double value) {
    // TODO
  }

  /**
   * Called by the robot loop. Returns {@code {timestamps, values}}, oldest first, and empties the buffer. For
   * example, after {@code offer(1.0, 10)} and {@code offer(1.01, 11)}, returns {@code {{1.0, 1.01}, {10, 11}}}.
   */
  public double[][] drain() {
    // TODO
    return new double[][] {new double[0], new double[0]};
  }

  /** Returns how many samples are waiting to be drained. */
  public int size() {
    // TODO
    return 0;
  }

  /** Returns how many samples have been dropped because the buffer was full. */
  public long getDroppedCount() {
    // TODO
    return 0;
  }
}
