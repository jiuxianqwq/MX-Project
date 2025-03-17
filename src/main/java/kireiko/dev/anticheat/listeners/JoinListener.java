package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class JoinListener extends PacketAdapter {
    public JoinListener() {
        super(MX.getInstance(), ListenerPriority.HIGHEST,
                        PacketType.Play.Server.LOGIN);
    }
    @Override
    public void onPacketSending(PacketEvent event) {
        initPlayer(event);
    }

    private void initPlayer(PacketEvent event) {
        Player player = event.getPlayer();
        try {
            PlayerContainer.init(player);
        } catch (Exception e) {
            Bukkit.getScheduler().runTaskLater(MX.getInstance(), () -> {
                initPlayer(event);
            }, 1L);
        }
    }
}
