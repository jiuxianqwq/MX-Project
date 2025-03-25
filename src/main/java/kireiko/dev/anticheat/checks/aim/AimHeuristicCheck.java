package kireiko.dev.anticheat.checks.aim;

import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.events.UseEntityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.heuristic.AimConstant;
import kireiko.dev.anticheat.checks.aim.heuristic.HeuristicComponent;
import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.vectors.Vec2;
import lombok.Getter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class AimHeuristicCheck implements PacketCheckHandler {

    @Getter
    private final PlayerProfile profile;
    private final List<Vec2> rawRotations;
    private final Set<HeuristicComponent> components;
    private int streak = 0;
    private long lastAttack;
    private float vl = 0, vlL2 = 0;
    private String reason = "";

    public AimHeuristicCheck(PlayerProfile profile) {
        this.profile = profile;
        this.rawRotations = new CopyOnWriteArrayList<>();
        this.lastAttack = System.currentTimeMillis() + 3500;
        this.components = new HashSet<>();
        { // components
            this.components.add(new AimConstant(this));
        }
    }

    @Override
    public void event(Object o) {
        if (o instanceof RotationEvent) {
            RotationEvent event = (RotationEvent) o;
            if (System.currentTimeMillis() > this.lastAttack + 3500 || profile.ignoreCinematic()) return;
            this.rawRotations.add(new Vec2(event.getTo().getX(), event.getTo().getY()));
            if ((event.getDelta().getY() > 1.5f || event.getDelta().getX() > 3.0f)
                    && (profile.getTo().getPitch() == 0 || profile.getTo().getPitch() % 0.01f == 0)) {
                this.profile.punish("Aim", "Randomizer", "[Heuristic] Randomizer flaw", 1.0f);
            }
            for (HeuristicComponent component : components) component.process(event);
            if (this.rawRotations.size() >= 10) checkDefaultAim();
        } else if (o instanceof UseEntityEvent) {
            UseEntityEvent event = (UseEntityEvent) o;
            if (event.isAttack()) {
                this.lastAttack = System.currentTimeMillis();
            }
        }
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
            final int sens = profile.calculateSensitivity();
            if (sens > 65) {
                if (robotizedAmount > 7) addNewPunish("heuristic(sync)", 125);
                if (aggressiveAim > 8) addNewPunish("heuristic(aggressive)", 50);
                if (machineKnownMovement > 5) addNewPunish("heuristic(aim)", 100);
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
                this.profile.punish("Aim", "Heuristic", "[Component] " + this.reason, 0.0f);
                this.profile.setAttackBlockToTime(System.currentTimeMillis() + 3000L);
                this.vl = 360;
            }
            if (this.vlL2 > 400) {
                this.profile.punish("Aim", "Flaw", "[Flaw] Interpolation", 0.0f);
                this.profile.setAttackBlockToTime(System.currentTimeMillis() + 1600L);
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
        profile.debug("&7Aim Component: " + reason
                + " " + this.vl + " (+" + vl + ")");
    }

    private void addNewPunishL2(String reason, float vl) {
        this.vlL2 += vl;
        profile.debug("&7Interpolation Component: " + reason
                + " " + this.vlL2 + " (+" + vl + ")");
    }
}
