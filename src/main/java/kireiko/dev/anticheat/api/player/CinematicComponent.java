package kireiko.dev.anticheat.api.player;

import com.google.common.collect.Lists;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.utils.GraphUtil;

import java.util.List;

public final class CinematicComponent {

    private final List<Double> yawSamples = Lists.newArrayList();
    private final List<Double> pitchSamples = Lists.newArrayList();
    private final PlayerProfile profile;
    private long lastSmooth = 0L, lastHighRate = 0L;
    private double lastDeltaYaw = 0.0d, lastDeltaPitch = 0.0d;

    public CinematicComponent(final PlayerProfile profile) {
        this.profile = profile;
    }

    public void process(final RotationEvent rotationUpdate) {
        final long now = System.currentTimeMillis();

        final double deltaYaw = rotationUpdate.getDelta().getX();
        final double deltaPitch = rotationUpdate.getDelta().getY();

        final double differenceYaw = Math.abs(deltaYaw - lastDeltaYaw);
        final double differencePitch = Math.abs(deltaPitch - lastDeltaPitch);

        final double joltYaw = Math.abs(differenceYaw - deltaYaw);
        final double joltPitch = Math.abs(differencePitch - deltaPitch);

        final boolean cinematic = (now - lastHighRate > 250L) || now - lastSmooth < 9000L;

        if (joltYaw > 1.0 && joltPitch > 1.0) {
            this.lastHighRate = now;
        }
        yawSamples.add(deltaYaw);
        pitchSamples.add(deltaPitch);
        if (yawSamples.size() == 20 && pitchSamples.size() == 20) {
            final GraphUtil.GraphResult resultsYaw = GraphUtil.getGraph(yawSamples);
            final GraphUtil.GraphResult resultsPitch = GraphUtil.getGraph(pitchSamples);
            final int negativesYaw = resultsYaw.getNegatives();
            final int negativesPitch = resultsPitch.getNegatives();
            final int positivesYaw = resultsYaw.getPositives();
            final int positivesPitch = resultsPitch.getPositives();
            if (positivesYaw > negativesYaw || positivesPitch > negativesPitch) {
                this.lastSmooth = now;
            }

            yawSamples.clear();
            pitchSamples.clear();
        }

        profile.setCinematic(cinematic);

        this.lastDeltaYaw = deltaYaw;
        this.lastDeltaPitch = deltaPitch;
    }
}