package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.events.CPacketEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;

import java.util.Set;

public class OmniPacketListener extends PacketAdapter {

    public OmniPacketListener(Set<PacketType> list) {
        super(
                MX.getInstance(),
                ListenerPriority.HIGHEST,
                list
        );
    }

    @Override
    public void onPacketReceiving(PacketEvent event) {
        PlayerProfile protocol = PlayerContainer.getProfile(event.getPlayer());
        if (protocol == null) {
            return;
        }
        protocol.run(new CPacketEvent(event));
    }
}