package kireiko.dev.anticheat.listeners;

import kireiko.dev.anticheat.api.PlayerContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;


public class DisconnectListener implements Listener {

    @EventHandler
    public void exit(PlayerQuitEvent event) {
        PlayerContainer.unload(event.getPlayer());
    }
}
