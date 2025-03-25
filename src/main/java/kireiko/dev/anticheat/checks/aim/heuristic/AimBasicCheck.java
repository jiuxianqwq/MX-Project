package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.vectors.Vec2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class AimBasicCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private final List<Vec2> rawRotations;
    private int streak = 0;
    private float vl = 0, vlL2 = 0;
    private String reason = "";

    public AimBasicCheck(final AimHeuristicCheck check) {
        this.check = check;
        this.rawRotations = new CopyOnWriteArrayList<>();
    }

    @Override
    public void process(final RotationEvent event) {
        if (check.getProfile().ignoreCinematic()) return;
        this.rawRotations.add(new Vec2(event.getTo().getX(), event.getTo().getY()));
        if ((event.getDelta().getY() > 1.5f || event.getDelta().getX() > 3.0f)
                && (check.getProfile().getTo().getPitch() == 0
                || check.getProfile().getTo().getPitch() % 0.01f == 0)) {
            this.check.getProfile().punish("Aim", "Randomizer", "[Heuristic] Randomizer flaw", 1.0f);
        }
        if (this.rawRotations.size() >= 10) checkDefaultAim();
    }

    private void checkDefaultAim() {
        { // standard checks for bad aim assist
            final List<Vec2> rotations = this.rawRotations;
            Set<Double> yaws = new HashSet<>();
            {
                double oldYaw = rotations.get(0).getX();
                for (Vec2 r : rotations) {
                    yaws.add(Math.abs(r.getX() - oldYaw));
                    oldYaw = r.getX();
                }
            }
            double oldYawResult = rotations.get(0).getX();
            double oldPitchResult = rotations.get(0).getY();
            double oldYawChange = Math.abs(rotations.get(0).getX() - oldYawResult);
            double yawChangeFirst = Math.abs(rotations.get(0).getX() - rotations.get(1).getX());
            int machineKnownMovement = 0,
                    constantRotations = 0, gcd = 0, aggressivePatternI = 0,
                    aggressivePatternD = 0, aggressivePatternI2 = 0, aggressivePatternD2 = 0,
                    robotizedAmount = 0, aggressiveAim = 0, infinitives = 0;
            for (Vec2 rotation : rotations) {
                double yawChange = Math.abs(rotation.getX() - oldYawResult);
                double pitchChange = Math.abs(rotation.getY() - oldPitchResult);
                double robotized = Math.abs(yawChange - yawChangeFirst);
                double diffBetweenYawChanges = yawChange - oldYawChange;
                double interpolation;
                float yaw = (float) rotation.getX();

                if (robotized < 2 && yawChange > 2.5) robotizedAmount += 1;
                if (robotized < 0.99 && yawChange > 4) machineKnownMovement++;
                if (robotized < 0.02 && yawChange > 3) constantRotations++;
                if (robotized < 2 && yawChange > 3) aggressiveAim++;
                interpolation = Simplification.scaleVal(yawChange / robotized, 2);
                if (Double.isInfinite(interpolation) && yawChange > 0) {
                    infinitives++;
                    if (infinitives > 1 && yawChange < 0.4) {
                        infinitives--;
                    }
                }
                if (yawChange == 0.1 || pitchChange == 0.1) gcd++;
                if (yawChange == 0.01 || pitchChange == 0.01) gcd++;
                if ((diffBetweenYawChanges > 0.01 && diffBetweenYawChanges < 2)) aggressivePatternI++;
                if ((diffBetweenYawChanges < -0.01 && diffBetweenYawChanges > -2)) aggressivePatternD++;
                if (diffBetweenYawChanges > 2) aggressivePatternI2++;
                if (diffBetweenYawChanges < -2) aggressivePatternD2++;
                oldYawResult = yaw;
                oldPitchResult = rotation.getY();
                oldYawChange = yawChange;
            }
            final int sens = check.getProfile().calculateSensitivity();
            if (sens > 65) {
                if (robotizedAmount > 8) addNewPunish("heuristic(sync)", 125);
                if (aggressiveAim > 8) addNewPunish("heuristic(aggressive)", 50);
                if (machineKnownMovement > 7) addNewPunish("heuristic(aim)", 100);
                if (constantRotations > 3) addNewPunish("heuristic(constant)", 60);
            } else {
                if (machineKnownMovement > 8) addNewPunish("heuristic(aim)", 100);
                if (constantRotations > 6) addNewPunish("heuristic(constant)", 60);
            }
            if (infinitives > 1 && Math.abs(Statistics.getAverage(yaws)) > 3.2) {
                addNewPunishL2("heuristic(interpolation)", 55);
                //profile.debug("avg: " + Math.abs(Statistics.getAverage(yaws)));
            }
            if (gcd > 0) addNewPunish("pattern(gcd)", 1000);
            if (aggressivePatternI > 3 && aggressivePatternD > 3)
                addNewPunishL2("pattern(random)", 25);
            if (aggressivePatternI2 > 3 && aggressivePatternD2 > 3
                    && (aggressivePatternI2 + aggressivePatternD2) > 8) {
                streak++;
                if (streak > 2) addNewPunish("pattern(snap)", 55);
            } else streak = 0;
            if (this.vl > 400) {
                this.check.getProfile().punish("Aim", "Heuristic", "[Component] " + this.reason, 0.0f);
                this.check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 3500L);
                this.vl = 360;
            }
            if (this.vlL2 > 400) {
                this.check.getProfile().punish("Aim", "Flaw", "[Flaw] Interpolation", 0.0f);
                this.check.getProfile().setAttackBlockToTime(System.currentTimeMillis() + 2600L);
                this.vlL2 -= 65;
            }
            if (this.vl > 0) this.vl -= 5;
            if (this.vl > 400) this.vl -= 10;
            if (this.vlL2 > 0) this.vlL2 -= 6.5f;
            if (this.vlL2 > 380) this.vlL2 -= 10;
        }
        this.rawRotations.clear();
    }

    private void addNewPunish(String reason, float vl) {
        this.reason = reason;
        this.vl += vl;
        check.getProfile().debug("&7Aim Component: " + reason
                + " " + this.vl + " (+" + vl + ")");
    }

    private void addNewPunishL2(String reason, float vl) {
        this.vlL2 += vl;
        check.getProfile().debug("&7Interpolation Component: " + reason
                + " " + this.vlL2 + " (+" + vl + ")");
    }
}