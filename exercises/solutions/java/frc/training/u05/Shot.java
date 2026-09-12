package frc.training.u05;

/** Reference solution for exercise u05-interp, part 1. */
public record Shot(double rpm, double hoodAngle) implements Blendable<Shot> {
  @Override
  public Shot blend(Shot end, double t) {
    return new Shot(rpm + (end.rpm - rpm) * t, hoodAngle + (end.hoodAngle - hoodAngle) * t);
  }
}
