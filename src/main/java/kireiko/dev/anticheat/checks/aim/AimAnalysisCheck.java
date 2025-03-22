package kireiko.dev.anticheat.checks.aim;

import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.events.UseEntityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.api.player.SensitivityProcessor;
import kireiko.dev.anticheat.checks.aim.heuristic.AimConstant;
import kireiko.dev.anticheat.checks.aim.heuristic.HeuristicComponent;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.vectors.Vec2f;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class AimAnalysisCheck implements PacketCheckHandler {
    private final List<Float> buffer;
    private final PlayerProfile profile;
    private final List<Vec2f> rawRotations;
    private long lastAttack;
    public AimAnalysisCheck(PlayerProfile profile) {
        this.profile = profile;
        this.rawRotations = new CopyOnWriteArrayList<>();
        this.lastAttack = 0L;
        this.buffer = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 16; i++) this.buffer.add(0.0f);
    }
    @Override
    public void event(Object o) {
        if (o instanceof RotationEvent) {
            RotationEvent event = (RotationEvent) o;
            if (System.currentTimeMillis() > this.lastAttack + 3500) return;
            Vec2f delta = event.getDelta();
            this.rawRotations.add(delta);
            if (this.rawRotations.size() >= 100) this.checkRaw();
        } else if (o instanceof UseEntityEvent) {
            UseEntityEvent event = (UseEntityEvent) o;
            if (event.isAttack()) {
                this.lastAttack = System.currentTimeMillis();
            }
        }
    }

    private void checkRaw() {
        { // uh
            final List<Float> x = new ArrayList<>(), xAbs = new ArrayList<>(), y = new ArrayList<>();
            final List<Long> xGcd = new ArrayList<>();
            final int sens = profile.calculateSensitivity();
            final float gcdValue = (sens > 0) ? Statistics.getGCDValue(SensitivityProcessor.getSENSITIVITY_MCP_VALUES()[sens - 1]) : 0;
            for (Vec2f vec2 : this.rawRotations) {
                x.add(vec2.getX());
                xAbs.add(vec2.getX());
                y.add(vec2.getY());
                xGcd.add((long) (vec2.getX() / gcdValue));
            }
            { // score
                final List<Float> yawStack = new ArrayList<>();
                final List<Double> resultDeviation = new ArrayList<>();
                int resultDistinct = 0;
                for (final float yaw : x) {
                    yawStack.add(yaw);
                    if (yawStack.size() >= 10) {
                        resultDeviation.add(Statistics.getStandardDeviation(Statistics.getJiffDelta(yawStack, 5)));
                        resultDistinct += Statistics.getDistinct(Statistics.getJiffDelta(yawStack, 4));
                        yawStack.clear();
                    }
                }
                final List<Double> outliers5 = Statistics.getZScoreOutliers(resultDeviation, 0.5f);
                final float distinctRank = (float) resultDistinct / 60;
                { // linear
                    if (outliers5.isEmpty() || outliers5.size() == 1 && Math.abs(outliers5.get(0)) > 10 &&  Math.abs(outliers5.get(0)) < 100) {
                        this.profile.punish("Aim", "Linear", "[Analysis] Invalid outliers " + Arrays.toString(outliers5.toArray()), 3.0f);
                    }
                }
                { // rank
                    final boolean valid = profile.calculateSensitivity() > 20 && sens < 140;
                    if (distinctRank < 1.0 && distinctRank > 0.7 && Statistics.getAverage(xAbs) > 1.8 && valid) {
                        if (this.buffer.get(1) < 0.01) {
                            if (distinctRank < 0.8) this.increaseBuffer(1, 0.2f);
                        } else {
                            this.increaseBuffer(1, (distinctRank > 0.9) ? 0.08f : (distinctRank > 0.8) ? 2f : 3f);
                            profile.debug("&7Aim Incorrect rank: " + this.buffer.get(1) + " (" + distinctRank + ")");
                            if (this.buffer.get(1) >= 6.0f) {
                                this.profile.punish("Aim", "Rank", "[Analysis] Incorrect rank " + distinctRank, 2.0f);
                                this.buffer.set(1, 5.0f);
                            }
                        }

                    } else this.increaseBuffer(1, -2.25f);
                }
            }
            { // distribution
                final double distinctX = Statistics.getDistinct(x);
                final double max = Math.abs(Statistics.getMax(xAbs));
                final double kurtosis = Statistics.getKurtosis(x);
                final double pearson = Statistics.getPearsonCorrelation(x, y);
                final int spikes = Statistics.getZScoreOutliers(x, 1.0f).size() + Statistics.getZScoreOutliers(y, 1.0f).size();
                if (max > 8 && pearson < 0.25 && distinctX < 85 && distinctX > 65 && kurtosis > 0 && spikes >= 40) {
                    this.increaseBuffer(0, (distinctX < 80) ? 1.1f : 0.85f);
                    profile.debug("&7Aim Incorrect distribution: " + this.buffer.get(0));
                    if (this.buffer.get(0) > 3.2f) {
                        this.profile.punish("Aim", "Distribution", "[Analysis] Incorrect distribution [" + distinctX + ", "
                                        + pearson + ", " + max + ", " + spikes + "]", 2.0f);
                        this.buffer.set(0, 2.5f);
                    }
                } else this.increaseBuffer(0, -0.5f);
            }
            //profile.getPlayer().sendMessage("f: " + distinctX + " " + pearson + " " + max + " " + kurtosis + " " + spikes);
        }
        this.rawRotations.clear();
    }

    private void increaseBuffer(int index, float v) {
        float r = this.buffer.get(index) + v;
        this.buffer.set(index, (r < 0) ? 0 : r);
    }
    private void mulBuffer(int index, float v) {
        float r = this.buffer.get(index) * v;
        this.buffer.set(index, (r < 0) ? 0 : r);
    }
}
