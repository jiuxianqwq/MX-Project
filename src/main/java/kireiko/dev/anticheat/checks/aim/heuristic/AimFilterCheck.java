package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Interpolation;
import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.math.Statistics;

import java.util.ArrayList;
import java.util.List;

public final class AimFilterCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private final List<Float> stack = new ArrayList<>();
    private float buffer = 0;

    public AimFilterCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    private static List<Float> predict(final float a, final float b, final Interpolation.Type type, final Interpolation.Ease ease) {
        final List<Float> predicted = new ArrayList<>();
        for (double d = 0.0d; d <= 1.0d; d += 0.05)
            predicted.add((float) Interpolation.interpolate(a, b, d, type, ease));
        return predicted;
    }

    @Override
    public void process(final RotationEvent event) {
        //if (check.getProfile().ignoreCinematic()) return;
        final PlayerProfile profile = check.getProfile();
        stack.add(event.getTo().getX());
        if (stack.size() >= 20) {
            /*
            Machine-like rotations detection by A/B prediction model.
            Checking for excessively uniform noises.
             */
            {
                final float a = stack.get(0), b = stack.get(stack.size() - 1);
                final List<Float> linearPredict = predict(a, b, Interpolation.Type.LINEAR, Interpolation.Ease.IN_OUT);
                final List<Float> jiffA = Statistics.getJiffDelta(stack, 2), jiffB = Statistics.getJiffDelta(linearPredict, 2);
                final double r = Math.abs(Statistics.getRSquared(jiffA, jiffB));
                if (r > 1.2) {
                    buffer = 0;
                } else {
                    buffer += (r < 0.4) ? 2.0f : (r < 0.9) ? 1.25f : -0.25f;
                    profile.debug("&7Aim A/B: " + buffer + " (r: " + r + ")");
                }
                if (buffer < 0) {
                    buffer = 0;
                } else if (buffer > 8) {
                    profile.punish("Aim", "A/B", "Rate: " + Simplification.scaleVal((1.0 - r), 2) + " [Machine-like rotations]", 0.0f);
                    profile.setAttackBlockToTime(System.currentTimeMillis() + 5000);
                    buffer = 7;
                }
            }
            stack.clear();
        }
    }
}