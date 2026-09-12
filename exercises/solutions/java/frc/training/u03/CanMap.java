package frc.training.u03;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** Reference solution for exercise u03-canmap. */
public class CanMap {
  public static final int MIN_ID = 1;
  public static final int MAX_ID = 62;

  // TreeMap keeps keys sorted, so ids() and byController() come out in order for free.
  private final Map<Integer, CanDevice> devices = new TreeMap<>();

  public boolean add(CanDevice device) {
    int id = device.getId();
    if (id < MIN_ID || id > MAX_ID || devices.containsKey(id)) {
      return false;
    }
    devices.put(id, device);
    return true;
  }

  public CanDevice get(int id) {
    return devices.get(id);
  }

  public int size() {
    return devices.size();
  }

  public boolean isUsed(int id) {
    return devices.containsKey(id);
  }

  public List<CanDevice> byController(String controller) {
    List<CanDevice> matches = new ArrayList<>();
    for (CanDevice device : devices.values()) {
      if (device.getController().equals(controller)) {
        matches.add(device);
      }
    }
    return matches;
  }

  public int[] ids() {
    int[] result = new int[devices.size()];
    int i = 0;
    for (int id : devices.keySet()) {
      result[i++] = id;
    }
    return result;
  }

  public int lowestFreeId() {
    for (int id = MIN_ID; id <= MAX_ID; id++) {
      if (!devices.containsKey(id)) {
        return id;
      }
    }
    return -1;
  }

  public static CanMap robot2026() {
    CanMap map = new CanMap();
    map.add(new CanDevice(1, "Front-left drive", "SparkMax"));
    map.add(new CanDevice(2, "Front-left turn", "SparkMax"));
    map.add(new CanDevice(3, "Back-left drive", "SparkMax"));
    map.add(new CanDevice(4, "Back-left turn", "SparkMax"));
    map.add(new CanDevice(5, "Back-right drive", "SparkMax"));
    map.add(new CanDevice(6, "Back-right turn", "SparkMax"));
    map.add(new CanDevice(7, "Front-right drive", "SparkMax"));
    map.add(new CanDevice(8, "Front-right turn", "SparkMax"));
    map.add(new CanDevice(9, "Flywheel leader", "SparkFlex"));
    map.add(new CanDevice(10, "Flywheel follower", "SparkFlex"));
    map.add(new CanDevice(11, "Hood", "SparkFlex"));
    map.add(new CanDevice(12, "Turret", "SparkFlex"));
    map.add(new CanDevice(13, "Intake rollers", "SparkFlex"));
    map.add(new CanDevice(14, "Intake arm", "SparkFlex"));
    map.add(new CanDevice(15, "Feeder", "SparkFlex"));
    map.add(new CanDevice(16, "Horizontal roller 1", "SparkMax"));
    map.add(new CanDevice(17, "Horizontal roller 2", "SparkMax"));
    map.add(new CanDevice(18, "Intake roller follower", "SparkFlex"));
    return map;
  }
}
