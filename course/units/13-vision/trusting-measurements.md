---
summary: Standard deviations are how vision tells the pose estimator how much to believe it. Here is how ours are computed, how two cameras get combined, and the arithmetic error that makes the result overconfident.
objectives:
  - Explain what a standard deviation does inside the pose estimator
  - Compute our linear and angular standard deviations for any observation
  - Explain inverse-variance weighting and the fusion bug in finding F13
  - Tune vision trust from what you see in a log
files:
  - src/main/java/frc/robot/subsystems/vision/AprilTagVision.java
  - src/main/java/frc/robot/subsystems/vision/VisionConstants.java
  - src/main/java/frc/robot/subsystems/drive/Drive.java
---

## What a standard deviation buys

::source file="src/main/java/frc/robot/subsystems/drive/Drive.java" from="public void addVisionMeasurement(" lines=6

The third argument is a vector of three standard deviations: x, y, and rotation. The pose estimator uses them to decide **how far to move its estimate toward the measurement**. A small standard deviation says "this measurement is good, move most of the way"; a large one says "note it and keep trusting the wheels."

That is the entire interface between vision and odometry. Vision does not overwrite the pose, and it does not vote. It provides a number and a confidence, and the estimator does the blending.

## Our quality model

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="double stdFactor = obs.avgTagDistance() / obs.tagCount();" lines=8

```text
stdFactor = avgTagDistance / tagCount
linear    = 0.2   × stdFactor      (doubled when tagCount == 1)
angular   = 0.524 × stdFactor      (99999 when tagCount == 1)
```

| Observation | stdFactor | Linear σ | Angular σ |
|---|---|---|---|
| Two tags, 2 m away | 1.0 | 0.20 m | 0.52 rad |
| Two tags, 4 m away | 2.0 | 0.40 m | 1.05 rad |
| Four tags, 4 m away | 1.0 | 0.20 m | 0.52 rad |
| One tag, 2 m away | 2.0 | **0.80 m** | 99999 |

The comment in the code explains the baseline honestly: it is how far off you think the pose is about 68% of the time. A 0.2 m baseline says a good two-tag sighting at a meter is worth about 20 cm of confidence, which is a reasonable claim for a well-calibrated camera.

The single-tag rules are the interesting part. Doubling the linear value says "believe this less"; the 99999 says "ignore its heading entirely." Both are the right shape, because a single tag constrains position far better than rotation.

## Combining two cameras

When both cameras see something in the same loop, our code fuses them into one measurement: a weighted average of positions, a weighted circular mean of yaws, and a fused standard deviation.

::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="sumCosYaw += Math.cos(yaw)" lines=2

Averaging yaw through sines and cosines is exactly right, and worth noticing as good practice: averaging 179° and −179° numerically gives 0°, which points the robot backwards. The sine and cosine version gives 180°.

The weights are where it goes wrong.

:::danger Finding F13: 1/σ is not 1/σ²
::source file="src/main/java/frc/robot/subsystems/vision/AprilTagVision.java" from="double fusedLinearVar = 1.0 / Math.pow(totalLinearWeight, 2);" lines=5

Our code weights each observation by **1/σ** and reports a fused standard deviation of **1 / Σ(1/σ)**. The statistics for combining independent measurements call for weights of **1/σ²** and a fused standard deviation of **√(1 / Σ(1/σ²))**.

Work it with two cameras that are equally good, each with σ = 0.3 m:

| | Ours | Correct |
|---|---|---|
| Weights | 1/0.3 = 3.33 each | 1/0.09 = 11.1 each |
| Fused σ | 1/6.67 = **0.15 m** | √(1/22.2) = **0.212 m** |
| In words | σ/2 | σ/√2 |

Two measurements never halve the error; they divide it by the square root of two. Our fused number claims about 40% more confidence than the data supports, so the estimator moves further toward vision than it should. The symptom is a pose that jumps a little more than it ought to when both cameras agree.

The weighting between *unequal* cameras is off in the same direction: a near camera should outvote a far one by the square of the ratio, not the ratio.

Fixing it is a handful of lines, and the exercise below has you write both versions so the difference is something you have seen rather than read.
:::

## Tuning trust from a log

| What you see | What it means | What to change |
|---|---|---|
| Pose jumps whenever a tag appears | Vision is trusted too much | Raise the linear baseline |
| Pose drifts and vision never pulls it back | Vision is trusted too little, or measurements are being rejected | Lower the baseline; check the filter's rejection reasons |
| Heading wanders while position is fine | Single-tag sightings only | Normal; more tags in view is the fix |
| Pose jitters while the robot sits still | Noisy measurements at the trust level you set | Raise the baseline, or tighten the ambiguity limit |
| Pose corrects sharply after contact | Working as designed | Nothing |

Graph `Odometry/Robot` against the vision poses in AdvantageScope's Field 2D view, and watch a practice match. Vision corrections should look like small, frequent nudges, not occasional teleports.

:::exercise id="u13-fusion"
Write the trust math: the linear and angular standard deviations for an observation, a weighted circular mean for yaw, and the fusion twice, once with our 1/σ weights and once with correct 1/σ² weights.

One test proves the overconfidence with our real numbers, and another shows how differently a near camera outvotes a far one under each scheme.
---hint
`linearStdDev` is the baseline times `avgTagDistance / tagCount`, doubled when `tagCount` is 1. `angularStdDev` has the same shape, except a single tag returns 99999 outright.
---hint
The circular mean sums `sin(angle) * weight` and `cos(angle) * weight` separately, then takes `Math.atan2(sumSin, sumCos)`.
---hint
Write one private fusion method with a boolean for which weighting to use. The only differences are the weight formula and whether the fused standard deviation takes a square root.
:::

:::quiz
? What does the standard deviation passed to `addVisionMeasurement` control?
+ How far the pose estimator moves its estimate toward the measurement
- Whether the measurement is accepted at all
- How long the measurement stays in the history buffer
- The camera's exposure
> Vision provides a value and a confidence; the estimator does the blending.

?num Two tags averaging 4 m away. What is the linear standard deviation, with a 0.2 m baseline?
= 0.4 ± 0.01 m
> 0.2 × (4 / 2).

? Why is a single-tag observation's angular standard deviation set to 99999?
+ To tell the estimator to use its position but ignore its heading
- To reject the measurement
- Because a single tag is always ambiguous
- It is an unfinished placeholder
> One tag pins down position far better than rotation.

? Two cameras of equal quality, σ = 0.3 m each, both see tags. What should the fused standard deviation be?
+ About 0.21 m, which is σ divided by the square root of two
- 0.15 m, which is σ divided by two
- 0.3 m, unchanged
- 0.6 m, the sum
> Our code reports 0.15, which is finding F13: about 40% more confidence than the data supports.

? Why does our code average yaw with sines and cosines instead of numerically?
+ Numeric averaging of angles near the wrap gives an answer pointing the wrong way
- It is faster
- The estimator requires radians
- To handle single-tag observations
> Averaging 179° and −179° numerically gives 0°.

? In a log, the pose jumps noticeably every time both cameras see tags. Which change is most likely to help?
+ Raise the linear standard deviation baseline, or fix the fusion to use inverse-variance weights
- Lower the ambiguity limit to 0.05
- Disable one camera
- Increase the maximum speed filter
> Jumps mean the estimator is being told to trust vision more than it should.
:::
