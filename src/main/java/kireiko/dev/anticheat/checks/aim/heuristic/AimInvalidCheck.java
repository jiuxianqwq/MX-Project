package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.math.Statistics;
import kireiko.dev.millennium.vectors.Vec2f;

public final class AimInvalidCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private int buffer = 0;

    public AimInvalidCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public void process(final RotationEvent event) {
        //if (check.getProfile().ignoreCinematic()) return;
        final PlayerProfile profile = check.getProfile();
        final Vec2f delta = event.getAbsDelta();
        if ((Statistics.isExponentiallySmall(delta.getY())
                        && delta.getY() > 0.0
                        && delta.getX() > 0.5f)) {
            buffer += 20;
            if (buffer > 70) {
                profile.punish("Aim", "Invalid", "Invalid Pitch " + event.getDelta().getY(), 10.0f);
            }
        } else buffer--;
        if (delta.getY() > 90.1f) {
            profile.punish("Aim", "Invalid", "Unlimited Pitch " + event.getDelta().getY(), 10.0f);
        }
    }
}