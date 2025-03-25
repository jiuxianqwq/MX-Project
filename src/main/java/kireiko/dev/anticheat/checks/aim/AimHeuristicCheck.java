package kireiko.dev.anticheat.checks.aim;

import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.events.RotationEvent;
import kireiko.dev.anticheat.api.events.UseEntityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.checks.aim.heuristic.AimBasicCheck;
import kireiko.dev.anticheat.checks.aim.heuristic.AimConstantCheck;
import kireiko.dev.anticheat.checks.aim.heuristic.AimInvalidCheck;
import kireiko.dev.anticheat.checks.aim.heuristic.HeuristicComponent;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

public class AimHeuristicCheck implements PacketCheckHandler {

    @Getter
    private final PlayerProfile profile;
    private final Set<HeuristicComponent> components;
    private long lastAttack;

    public AimHeuristicCheck(PlayerProfile profile) {
        this.profile = profile;
        this.lastAttack = System.currentTimeMillis() + 3500;
        this.components = new HashSet<>();
        { // components
            this.components.add(new AimBasicCheck(this));
            this.components.add(new AimConstantCheck(this));
            this.components.add(new AimInvalidCheck(this));
        }
    }

    @Override
    public void event(Object o) {
        if (o instanceof RotationEvent) {
            RotationEvent event = (RotationEvent) o;
            if (System.currentTimeMillis() > this.lastAttack + 3500) return;
            for (HeuristicComponent component : components) component.process(event);
        } else if (o instanceof UseEntityEvent) {
            UseEntityEvent event = (UseEntityEvent) o;
            if (event.isAttack()) {
                this.lastAttack = System.currentTimeMillis();
            }
        }
    }
}
