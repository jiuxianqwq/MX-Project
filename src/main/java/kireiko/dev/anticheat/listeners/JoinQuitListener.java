package kireiko.dev.anticheat.listeners;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.data.PlayerContainer;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.utils.ConfigCache;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static kireiko.dev.anticheat.utils.MessageUtils.wrapColors;

public final class JoinQuitListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        PlayerContainer.init(event.getPlayer());

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerContainer.unload(event.getPlayer());
    }
}
