package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.events.SVelocityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class VelocityListener extends PacketAdapter {


    public VelocityListener() {
        super(
                        MX.getInstance(),
                        ListenerPriority.MONITOR,
                        PacketType.Play.Server.ENTITY_VELOCITY
        );
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final Player player = event.getPlayer();
        final PlayerProfile protocol = PlayerContainer.getProfile(player);
        if (protocol == null) {
            return;
        }
        PacketContainer packet = event.getPacket();
        if (!packet.getIntegers().getValues().isEmpty()) {
            int id = packet.getIntegers().getValues().get(0);
            if (protocol.getEntityId() == id) {
                double x = packet.getIntegers().read(1).doubleValue() / 8000.0D,
                                y = packet.getIntegers().read(2).doubleValue() / 8000.0D,
                                z = packet.getIntegers().read(3).doubleValue() / 8000.0D;
                SVelocityEvent velocityEvent = new SVelocityEvent(new Vector(x, y, z));
                protocol.run(velocityEvent);
            }
        }
    }
}
