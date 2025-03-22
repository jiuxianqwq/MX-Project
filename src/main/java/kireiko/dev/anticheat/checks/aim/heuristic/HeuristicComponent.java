package kireiko.dev.anticheat.checks.aim.heuristic;

import kireiko.dev.anticheat.api.events.RotationEvent;

public interface HeuristicComponent {
    void process(final RotationEvent event);
}
