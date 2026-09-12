package frc.training.u01;

/** Reference solution for exercise u01-hello. */
public final class HelloIronLions {
  private HelloIronLions() {}

  /** Returns the Iron Lions' FRC team number. */
  public static int teamNumber() {
    return 967;
  }

  /**
   * Builds a greeting for a new programmer.
   *
   * @param name the new programmer's name
   * @return the greeting text
   */
  public static String greeting(String name) {
    return "Welcome to 967, " + name + "!";
  }
}
