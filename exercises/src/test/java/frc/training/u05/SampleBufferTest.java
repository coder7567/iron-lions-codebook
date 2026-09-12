package frc.training.u05;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

class SampleBufferTest {
  private static final double EPSILON = 1e-9;

  @Test
  @DisplayName("drain returns samples oldest first and empties the buffer")
  void orderAndEmpty() {
    SampleBuffer buffer = new SampleBuffer(10);
    buffer.offer(1.00, 10);
    buffer.offer(1.01, 11);
    buffer.offer(1.02, 12);
    assertEquals(3, buffer.size());
    double[][] samples = buffer.drain();
    assertArrayEquals(new double[] {1.00, 1.01, 1.02}, samples[0], EPSILON);
    assertArrayEquals(new double[] {10, 11, 12}, samples[1], EPSILON);
    assertEquals(0, buffer.size());
    double[][] again = buffer.drain();
    assertEquals(0, again[0].length);
    assertEquals(0, again[1].length);
  }

  @Test
  @DisplayName("a full buffer drops its oldest samples and counts them")
  void dropsOldest() {
    SampleBuffer buffer = new SampleBuffer(3);
    for (int i = 1; i <= 5; i++) {
      buffer.offer(i, i * 10);
    }
    assertEquals(3, buffer.size());
    assertEquals(2, buffer.getDroppedCount());
    double[][] samples = buffer.drain();
    assertArrayEquals(new double[] {3, 4, 5}, samples[0], EPSILON);
    assertArrayEquals(new double[] {30, 40, 50}, samples[1], EPSILON);
  }

  @Test
  @DisplayName("a capacity below 1 still holds one sample")
  void minimumCapacity() {
    SampleBuffer buffer = new SampleBuffer(0);
    buffer.offer(1, 1);
    buffer.offer(2, 2);
    assertEquals(1, buffer.size());
    assertEquals(1, buffer.getDroppedCount());
    assertArrayEquals(new double[] {2}, buffer.drain()[0], EPSILON);
  }

  @Test
  @Timeout(30)
  @DisplayName("a real sampling thread and the main thread share the buffer without losing or scrambling data")
  void concurrentHandoff() throws InterruptedException {
    SampleBuffer buffer = new SampleBuffer(50);
    int total = 20_000;
    Thread sampler = new Thread(() -> {
      for (int i = 0; i < total; i++) {
        buffer.offer(i, i * 2.0);
      }
    }, "test-sampler");

    List<Double> seen = new ArrayList<>();
    sampler.start();
    while (sampler.isAlive() || buffer.size() > 0) {
      collect(buffer.drain(), seen);
      Thread.onSpinWait();
    }
    sampler.join();
    collect(buffer.drain(), seen);

    assertEquals(total, seen.size() + buffer.getDroppedCount(), "every sample is either delivered or counted as dropped");
    for (int k = 1; k < seen.size(); k++) {
      assertTrue(seen.get(k) > seen.get(k - 1), "timestamps must stay in order");
    }
  }

  private static void collect(double[][] batch, List<Double> seen) {
    assertEquals(batch[0].length, batch[1].length, "timestamps and values must have the same length");
    for (int k = 0; k < batch[0].length; k++) {
      assertEquals(batch[0][k] * 2.0, batch[1][k], EPSILON, "each value must stay paired with its timestamp");
      seen.add(batch[0][k]);
    }
  }
}
