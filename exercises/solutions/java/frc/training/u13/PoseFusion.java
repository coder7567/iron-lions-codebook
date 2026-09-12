package frc.training.u13;

import java.util.List;

/** Reference solution for exercise u13-fusion. */
public final class PoseFusion {
  public record Observation(double x, double y, double yawRad, double timestamp, double avgTagDistance, int tagCount) {}

  public record Fused(double x, double y, double yawRad, double timestamp, double linearStdDev, double angularStdDev) {}

  public static final double LINEAR_STD_DEV_BASELINE = 0.2;

  public static final double ANGULAR_STD_DEV_BASELINE = 0.524;

  public static final double SINGLE_TAG_ANGULAR_STD_DEV = 99999;

  private PoseFusion() {}

  public static double linearStdDev(Observation observation) {
    double factor = observation.avgTagDistance() / observation.tagCount();
    double stdDev = LINEAR_STD_DEV_BASELINE * factor;
    return observation.tagCount() == 1 ? stdDev * 2.0 : stdDev;
  }

  public static double angularStdDev(Observation observation) {
    if (observation.tagCount() == 1) {
      return SINGLE_TAG_ANGULAR_STD_DEV;
    }
    return ANGULAR_STD_DEV_BASELINE * observation.avgTagDistance() / observation.tagCount();
  }

  public static double circularMean(double[] anglesRad, double[] weights) {
    if (anglesRad.length != weights.length) {
      throw new IllegalArgumentException("angles and weights must be the same length");
    }
    double sumSin = 0.0;
    double sumCos = 0.0;
    for (int i = 0; i < anglesRad.length; i++) {
      sumSin += Math.sin(anglesRad[i]) * weights[i];
      sumCos += Math.cos(anglesRad[i]) * weights[i];
    }
    return Math.atan2(sumSin, sumCos);
  }

  public static Fused fuseWithInverseSigma(List<Observation> observations) {
    return fuse(observations, false);
  }

  public static Fused fuseWithInverseVariance(List<Observation> observations) {
    return fuse(observations, true);
  }

  private static Fused fuse(List<Observation> observations, boolean inverseVariance) {
    if (observations.isEmpty()) {
      return null;
    }

    int count = observations.size();
    double[] yaws = new double[count];
    double[] angularWeights = new double[count];

    double sumX = 0.0;
    double sumY = 0.0;
    double sumTimestamp = 0.0;
    double totalLinearWeight = 0.0;
    double totalAngularWeight = 0.0;

    for (int i = 0; i < count; i++) {
      Observation observation = observations.get(i);
      double linearSigma = linearStdDev(observation);
      double angularSigma = angularStdDev(observation);
      double linearWeight = inverseVariance ? 1.0 / (linearSigma * linearSigma) : 1.0 / linearSigma;
      double angularWeight = inverseVariance ? 1.0 / (angularSigma * angularSigma) : 1.0 / angularSigma;

      sumX += observation.x() * linearWeight;
      sumY += observation.y() * linearWeight;
      sumTimestamp += observation.timestamp() * linearWeight;
      totalLinearWeight += linearWeight;

      yaws[i] = observation.yawRad();
      angularWeights[i] = angularWeight;
      totalAngularWeight += angularWeight;
    }

    double fusedLinearStdDev =
        inverseVariance ? Math.sqrt(1.0 / totalLinearWeight) : 1.0 / totalLinearWeight;
    double fusedAngularStdDev =
        inverseVariance ? Math.sqrt(1.0 / totalAngularWeight) : 1.0 / totalAngularWeight;

    return new Fused(
        sumX / totalLinearWeight,
        sumY / totalLinearWeight,
        circularMean(yaws, angularWeights),
        sumTimestamp / totalLinearWeight,
        fusedLinearStdDev,
        fusedAngularStdDev);
  }
}
