package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.data.ConfigLabel;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Interpolation;
import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.math.Statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public final class AimFilterCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private final List<Float> stack = new ArrayList<>();
    private float buffer = 0;
    private Map<String, Object> localCfg = new TreeMap<>();

    public AimFilterCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public ConfigLabel config() {
        localCfg.put("hitCancelTimeMS", 5000);
        localCfg.put("addGlobalVl", 5);
        localCfg.put("buffer", 9);
        return new ConfigLabel("filter_check(a/b)", localCfg);
    }

    @Override
    public void applyConfig(Map<String, Object> params) {
        localCfg = params;
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
        final float vlLimit = ((Number) localCfg.get("buffer")).floatValue();
        if (vlLimit <= 0) return;
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
                if (r > 1.0) {
                    buffer = 0;
                } else {
                    buffer += (r < 0.4) ? 2.0f : (r < 0.7) ? 1.0f : -0.5f;
                    profile.debug("&7Aim A/B: " + buffer + " (r: " + r + ")");
                }
                if (buffer < 0) {
                    buffer = 0;
                } else if (buffer >= 9 && r < 0.3) {
                    profile.punish("Aim", "A/B",
                                    "Rate: " + Simplification.scaleVal((1.0 - r), 2) + " [Machine-like rotations]",
                                    ((Number) localCfg.get("addGlobalVl")).floatValue() / 10);
                    profile.setAttackBlockToTime(System.currentTimeMillis() + ((Number) localCfg.get("hitCancelTimeMS")).longValue());
                    buffer = 7;
                }
            }
            stack.clear();
        }
    }
}