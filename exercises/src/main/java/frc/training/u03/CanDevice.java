package frc.training.u03;

/** One device on the robot's CAN bus. This class is provided; you do not need to change it. */
public class CanDevice {
  private final int id;
  private final String name;
  private final String controller;

  /**
   * @param id the device's CAN ID
   * @param name what the device does, like "Turret"
   * @param controller the controller type, like "SparkFlex" or "SparkMax"
   */
  public CanDevice(int id, String name, String controller) {
    this.id = id;
    this.name = name;
    this.controller = controller;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getController() {
    return controller;
  }

  @Override
  public String toString() {
    return id + " " + name + " (" + controller + ")";
  }
}
