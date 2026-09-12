package frc.training.u03;

import java.util.ArrayList;
import java.util.List;

/**
 * Exercise u03-canmap: keep track of every device on the CAN bus and refuse duplicate IDs.
 *
 * <p>Two devices with the same CAN ID make both misbehave, so a map that catches duplicates is genuinely
 * useful when wiring a new robot.
 */
public class CanMap {
  /** Lowest CAN ID a SPARK can use. */
  public static final int MIN_ID = 1;
  /** Highest CAN ID a SPARK can use. */
  public static final int MAX_ID = 62;

  // TODO: choose a collection that stores devices by ID. (A sorted one makes later methods easier.)

  /**
   * Adds a device. Returns false and adds nothing if the ID is outside {@link #MIN_ID}..{@link #MAX_ID}
   * or already used.
   */
  public boolean add(CanDevice device) {
    // TODO
    return false;
  }

  /** Returns the device with this ID, or null if there is none. */
  public CanDevice get(int id) {
    // TODO
    return null;
  }

  /** Returns how many devices are in the map. */
  public int size() {
    // TODO
    return 0;
  }

  /** Returns true if a device already uses this ID. */
  public boolean isUsed(int id) {
    // TODO
    return false;
  }

  /**
   * Returns every device whose controller type equals {@code controller}, sorted by ID from lowest to
   * highest. Returns an empty list, never null, when nothing matches.
   */
  public List<CanDevice> byController(String controller) {
    // TODO
    return new ArrayList<>();
  }

  /** Returns every used ID, sorted from lowest to highest. */
  public int[] ids() {
    // TODO
    return new int[0];
  }

  /** Returns the lowest unused ID from MIN_ID to MAX_ID, or -1 if every ID is taken. */
  public int lowestFreeId() {
    // TODO
    return 0;
  }

  /**
   * Builds the 2026 robot's CAN map (IDs 1 to 18). Use the names and controller types from the Codebook's
   * 967 Robot Reference, with controller types exactly "SparkMax" or "SparkFlex".
   */
  public static CanMap robot2026() {
    // TODO: create a CanMap and add all 18 devices
    return new CanMap();
  }
}
