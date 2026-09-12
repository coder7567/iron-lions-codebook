package frc.training.u05;

/**
 * Something that can blend part of the way toward another value of the same type. This mirrors WPILib's
 * {@code Interpolatable} interface. Provided; you do not need to change it.
 *
 * @param <T> the type being blended, usually the implementing type itself
 */
public interface Blendable<T> {
  /**
   * Returns a value {@code t} of the way from this value toward {@code end}: {@code t = 0} gives this value,
   * {@code t = 1} gives {@code end}, and {@code t = 0.5} is halfway.
   */
  T blend(T end, double t);
}
