package frc.training.u05;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/** Reference solution for exercise u05-sampler. */
public class SampleBuffer {
  private final Lock lock = new ReentrantLock();
  private final int capacity;
  private final ArrayDeque<Double> timestamps = new ArrayDeque<>();
  private final ArrayDeque<Double> values = new ArrayDeque<>();
  private long dropped = 0;

  public SampleBuffer(int capacity) {
    this.capacity = Math.max(1, capacity);
  }

  public void offer(double timestampSeconds, double value) {
    lock.lock();
    try {
      if (timestamps.size() >= capacity) {
        timestamps.pollFirst();
        values.pollFirst();
        dropped++;
      }
      timestamps.addLast(timestampSeconds);
      values.addLast(value);
    } finally {
      lock.unlock();
    }
  }

  public double[][] drain() {
    lock.lock();
    try {
      double[] t = new double[timestamps.size()];
      double[] v = new double[values.size()];
      int i = 0;
      for (double timestamp : timestamps) {
        t[i++] = timestamp;
      }
      i = 0;
      for (double value : values) {
        v[i++] = value;
      }
      timestamps.clear();
      values.clear();
      return new double[][] {t, v};
    } finally {
      lock.unlock();
    }
  }

  public int size() {
    lock.lock();
    try {
      return timestamps.size();
    } finally {
      lock.unlock();
    }
  }

  public long getDroppedCount() {
    lock.lock();
    try {
      return dropped;
    } finally {
      lock.unlock();
    }
  }
}
