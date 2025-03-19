package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.events.UseEntityEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.core.AsyncEntityFetcher;
import lombok.SneakyThrows;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class UseEntityListener extends PacketAdapter {

    private static final boolean modern = ProtocolLibrary.getProtocolManager().getMinecraftVersion()
                    .compareTo(new MinecraftVersion("1.13")) >= 0;
    public UseEntityListener() {
        super(MX.getInstance(), ListenerPriority.HIGHEST,
                        PacketType.Play.Client.USE_ENTITY);
    }
    @SneakyThrows
    @Override
    public void onPacketReceiving(PacketEvent event) {
        Player player = event.getPlayer();
        PlayerProfile profile = PlayerContainer.getProfile(player);
        if (profile == null) {
            return;
        }
        PacketContainer packet = event.getPacket();
        if (profile.getAttackBlockToTime() > System.currentTimeMillis()) {
            event.setCancelled(true);
            profile.debug("UseEntity packet blocked");
            MX.blockedPerMinuteCount++;
            return;
        }
        boolean attack = !packet.getEntityUseActions().getValues().isEmpty() ?
                        packet.getEntityUseActions().read(0).toString().equals("ATTACK")
                        : packet.getEnumEntityUseActions().read(0).getAction().equals(
                        EnumWrappers.EntityUseAction.ATTACK);
        if (packet.getIntegers().getValues().isEmpty()) return;
        int entityId = packet.getIntegers().read(0);
        Entity entity = (modern) ? AsyncEntityFetcher.getEntityFromIDAsync(event.getPlayer().getWorld(), entityId).get()
                        : ProtocolLibrary.getProtocolManager().
                        getEntityFromID(event.getPlayer().getWorld(), entityId);
        UseEntityEvent e = new UseEntityEvent(entity, attack, entityId, false);
        profile.run(e);
        if (e.isCancelled()) {
            event.setCancelled(true);
            profile.debug("UseEntity packet blocked after checking");
            MX.blockedPerMinuteCount++;
        }
    }
}
