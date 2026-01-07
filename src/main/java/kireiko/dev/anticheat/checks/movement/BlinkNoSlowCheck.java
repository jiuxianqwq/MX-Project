package kireiko.dev.anticheat.checks.movement;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketEvent;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.data.ConfigLabel;
import kireiko.dev.anticheat.api.events.CPacketEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.managers.CheckManager;
import kireiko.dev.anticheat.services.SimulationFlagService;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class BlinkNoSlowCheck implements PacketCheckHandler {

    private static final Map<UUID, BlinkNoSlowCheck> instances = new ConcurrentHashMap<>();
    private static volatile boolean started;

    private final PlayerProfile profile;
    private Map<String, Object> localCfg = new TreeMap<>();
    private volatile long lastC03Time = System.currentTimeMillis();
    private volatile Location lastC03Location;
    private long usingStartTime;
    private long lastActionTime;

    public BlinkNoSlowCheck(PlayerProfile profile) {
        this.profile = profile;
        this.lastC03Location = profile.getPlayer().getLocation().clone();
        if (CheckManager.classCheck(this.getClass())) {
            this.localCfg = CheckManager.getConfig(this.getClass());
        }
        instances.put(profile.getPlayer().getUniqueId(), this);
        ensureTickerStarted();
    }

    public void unregister() {
        instances.remove(profile.getPlayer().getUniqueId());
    }

    @Override
    public void event(Object o) {
        if (!(boolean) localCfg.get("enabled")) return;
        if (!(o instanceof CPacketEvent)) return;

        PacketEvent event = ((CPacketEvent) o).getPacketEvent();
        PacketType type = event.getPacket().getType();
        if (isC03(type)) {
            lastC03Time = System.currentTimeMillis();
            lastC03Location = profile.getTo().clone();
        }
    }

    @Override
    public void applyConfig(Map<String, Object> params) {
        localCfg = params;
    }

    @Override
    public Map<String, Object> getConfig() {
        return localCfg;
    }

    @Override
    public ConfigLabel config() {
        localCfg.put("enabled", false);
        localCfg.put("maxNoC03Ticks", 5);
        localCfg.put("cooldownTicks", 20);
        localCfg.put("setback", 0.35);
        localCfg.put("addGlobalVl", 10);
        return new ConfigLabel("blink_noslow", localCfg);
    }

    private static boolean isC03(PacketType type) {
        return type.equals(PacketType.Play.Client.POSITION)
                || type.equals(PacketType.Play.Client.LOOK)
                || type.equals(PacketType.Play.Client.POSITION_LOOK)
                || type.equals(PacketType.Play.Client.FLYING)
                || type.equals(PacketType.Play.Client.GROUND);
    }

    private static void ensureTickerStarted() {
        if (started) return;
        started = true;
        Bukkit.getScheduler().runTaskTimer(MX.getInstance(), () -> {
            long now = System.currentTimeMillis();
            for (BlinkNoSlowCheck check : instances.values()) {
                check.tick(now);
            }
        }, 1L, 1L);
    }

    private void tick(long now) {
        if (!(boolean) localCfg.get("enabled")) return;
        Player player = profile.getPlayer();
        if (player == null || !player.isOnline()) {
            unregister();
            return;
        }

        if (profile.isIgnoreFirstTick() || profile.getLastTeleport() + 1000 > now) {
            usingStartTime = 0;
            return;
        }

        boolean usingItem = player.isHandRaised();
        if (!usingItem) {
            usingStartTime = 0;
            return;
        }

        if (usingStartTime == 0) {
            usingStartTime = now;
        }

        int maxNoC03Ticks = ((Number) localCfg.get("maxNoC03Ticks")).intValue();
        int cooldownTicks = ((Number) localCfg.get("cooldownTicks")).intValue();
        long maxNoC03Ms = maxNoC03Ticks * 50L;
        long cooldownMs = cooldownTicks * 50L;

        if (now - usingStartTime < maxNoC03Ms) return;
        if (now - lastC03Time <= maxNoC03Ms) return;
        if (now - lastActionTime < cooldownMs) return;

        lastActionTime = now;

        int slot = ThreadLocalRandom.current().nextInt(9);
        player.getInventory().setHeldItemSlot(slot);

        double strength = ((Number) localCfg.get("setback")).doubleValue();
        Vector direction = player.getLocation().getDirection();
        Vector vector = direction.clone().multiply(-strength);
        vector.setY(0.0);

        Location base = lastC03Location;
        if (base == null || base.getWorld() == null || !base.getWorld().equals(player.getWorld())) {
            base = player.getLocation().clone();
        } else {
            base = base.clone();
        }
        SimulationFlagService.getFlags().add(new SimulationFlagService.Flag(profile, base, vector));

        float vl = ((Number) localCfg.get("addGlobalVl")).floatValue() / 10f;
        profile.punish("BlinkNoSlow", "C03", "No movement packet while using item (" + maxNoC03Ticks + "t)", vl);
    }
}
