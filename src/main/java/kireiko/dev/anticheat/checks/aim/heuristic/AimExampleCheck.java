package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.millennium.vectors.Vec2f;

public final class AimExampleCheck implements HeuristicComponent {
    private final AimHeuristicCheck check;
    private Vec2f oldDelta = new Vec2f(0, 0), oldAbsDelta = new Vec2f(0, 0);
    private int buffer = 0;

    public AimExampleCheck(final AimHeuristicCheck check) {
        this.check = check;
    }

    @Override
    public void process(final RotationEvent event) {
        //if (check.getProfile().ignoreCinematic()) return;
        final PlayerProfile profile = check.getProfile();
        final Vec2f delta = event.getDelta(), absDelta = event.getAbsDelta();
        { // check logic

        }
        oldDelta = delta;
        oldAbsDelta = absDelta;
    }
}