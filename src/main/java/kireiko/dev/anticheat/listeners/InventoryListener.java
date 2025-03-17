package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.events.WindowClickEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;

public class InventoryListener extends PacketAdapter {

    public InventoryListener() {
        super(
                MX.getInstance(),
                ListenerPriority.HIGHEST,
                PacketType.Play.Client.WINDOW_CLICK
        );
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PlayerProfile protocol = PlayerContainer.getProfile(event.getPlayer());
        protocol.run(new WindowClickEvent(event));
    }
}