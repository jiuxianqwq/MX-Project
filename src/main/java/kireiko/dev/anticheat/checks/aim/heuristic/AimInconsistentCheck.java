package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.vectors.Pair;

import java.util.ArrayList;
import java.util.List;

public final class AimInconsistentCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private float lastDeltaYaw = 0.0f, lastDeltaPitch = 0.0f;
    private final List<Float> samplesYaw = new ArrayList<>();
    private final List<Float> samplesPitch = new ArrayList<>();

    public AimInconsistentCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public void process(final RotationEvent rotationUpdate) {
        final boolean invalidSensitivity = check.getProfile().calculateSensitivity() < 75 || check.getProfile().calculateSensitivity() > 175;
        if (check.getProfile().ignoreCinematic() || invalidSensitivity) return;
        final float deltaYaw = Math.abs(rotationUpdate.getAbsDelta().getX());
        final float deltaPitch = Math.abs(rotationUpdate.getAbsDelta().getY());

        final float differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final float differencePitch = Math.abs(deltaPitch - lastDeltaPitch);

        final float joltX = Math.abs(deltaYaw - differenceYaw);
        final float joltY = Math.abs(deltaPitch - differencePitch);

        samplesYaw.add((float) Statistics.roundToPlace(joltX, 2));
        samplesPitch.add((float) Statistics.roundToPlace(joltY, 2));

        if (samplesYaw.size() + samplesPitch.size() >= 60) {
            if (!(joltX == 0.0 || joltY == 0.0)) {
                final Pair<List<Double>, List<Double>> outliersYaw = Statistics.getOutliers(samplesYaw);
                final Pair<List<Double>, List<Double>> outliersPitch = Statistics.getOutliers(samplesPitch);

                final int duplicatesX = Statistics.getDuplicates(samplesYaw);
                final int duplicatesY = Statistics.getDuplicates(samplesPitch);
                final int duplicatesSum = duplicatesX + duplicatesY;
                final int outliersX = outliersYaw.getX().size() + outliersYaw.getY().size();
                final int outliersY = outliersPitch.getX().size() + outliersPitch.getY().size();
                check.getProfile().debug("&7Aim Inconsistent: " + outliersX + " "
                                + outliersY + "; duplicates: " + duplicatesX + " " + duplicatesY);
                if (duplicatesSum <= 10 && outliersX < 10 && outliersY < 7) {
                    check.getProfile().punish("Aim", "Heuristic", "Inconsistent rotations ("
                                    + outliersX + ", " + outliersY + ", duplicates: " + duplicatesSum + ") [Too low values]", 0.0f);
                    check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 4500);
                } else if ((outliersX == 0 || outliersY == 0) && (outliersX > 1 || outliersY > 1) && duplicatesSum <= 7) {
                    check.getProfile().punish("Aim", "Heuristic", "Inconsistent rotations ("
                                    + outliersX + ", " + outliersY + ", duplicates: " + duplicatesSum + ") [Zero value]", 0.0f);
                    check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 3500);
                }
            }
            samplesYaw.clear();
            samplesPitch.clear();
        }

        this.lastDeltaYaw = deltaYaw;
        this.lastDeltaPitch = deltaPitch;
    }
}
