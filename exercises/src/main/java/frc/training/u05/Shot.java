package frc.training.u05;

/** Exercise u05-interp, part 1: a shot setpoint that can blend toward another. */
public record Shot(double rpm, double hoodAngle) implements Blendable<Shot> {
  /** Blends both the RPM and the hood angle {@code t} of the way toward {@code end}. */
  @Override
  public Shot blend(Shot end, double t) {
    // TODO
    return this;
  }
}
