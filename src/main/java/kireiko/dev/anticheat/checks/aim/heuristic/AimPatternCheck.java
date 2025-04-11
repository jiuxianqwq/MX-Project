package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.vectors.Vec2f;

import java.util.ArrayList;
import java.util.List;

public final class AimPatternCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private Vec2f oldDelta = new Vec2f(0, 0), oldAbsDelta = new Vec2f(0, 0);
    private List<Vec2f> sample = new ArrayList<>();
    private int buffer = 0;

    public AimPatternCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public void process(final RotationEvent event) {
        //if (check.getProfile().ignoreCinematic()) return;
        final PlayerProfile profile = check.getProfile();
        final Vec2f delta = event.getDelta(), absDelta = event.getAbsDelta();
        { // check logic
            final float yawFactor = delta.getX() - oldDelta.getX();
            final float pitchFactor = delta.getY() - oldDelta.getY();
            final Vec2f vec = new Vec2f(yawFactor, pitchFactor);
            this.sample.add(vec);
            if (this.sample.size() >= 100) {
                { // algorithm
                    int invalid = 0, history = 0;
                    for (final Vec2f rotation : this.sample) {
                        final float x = rotation.getX();
                        final float y = rotation.getY();
                        if (Math.abs(x) + Math.abs(y) > 0 && (x == 0.0f)) {
                            invalid += (history < 1) ? 1 : -1;
                            history++;
                        } else {
                           history = 0;
                        }
                    }
                    profile.getPlayer().sendMessage("invalid: " + invalid);
                }
                this.sample.clear();
            }
            profile.getPlayer().sendMessage("yaw: " + yawFactor + " pitch: " + pitchFactor);
        }
        oldDelta = delta;
        oldAbsDelta = absDelta;
    }
}