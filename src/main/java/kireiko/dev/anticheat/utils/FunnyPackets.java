package kireiko.dev.anticheat.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.core.AsyncScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class FunnyPackets {

    public static void closeMinecraft(Player iPlayer) {
        iPlayer.getPlayer().sendTitle(
                        MessageUtils.wrapColors("&4&k1857165195015155151581751551"),
                        MessageUtils.wrapColors("&4&k1857165195015155151581751551"), 0, 70, 20);
        AsyncScheduler.run(() -> {
            for (int i = 0; i < 5; i++) {
                funnyPacketExplosion(iPlayer);
                funnyPacketHealth(iPlayer);
            }
        });
    }
    public static void closeMinecraftCustom(Player iPlayer, String msg) {
        iPlayer.getPlayer().sendTitle(
                        MessageUtils.wrapColors(msg),
                        "", 0, 70, 20);
        Bukkit.getScheduler().runTaskLaterAsynchronously(MX.getInstance(), () -> {
            AsyncScheduler.run(() -> {
                for (int i = 0; i < 5; i++) {
                    funnyPacketExplosion(iPlayer);
                    funnyPacketHealth(iPlayer);
                }
            });
        }, 3L);
    }
    public static void funnyPacketExplosion(Player iPlayer) {
        ProtocolManager m = ProtocolLibrary.getProtocolManager();
        Player player = iPlayer.getPlayer();
        Location l = player.getLocation();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.EXPLOSION);
        packet.getDoubles().write(0, l.getX()).write(1, l.getY()).write(2, l.getZ());
        packet.getFloat().write(0, 44455555.444F).write(1, Float.MAX_VALUE).write(2, Float.MAX_VALUE).write(3, Float.MAX_VALUE);
        List<BlockPosition> particleTable = new ArrayList<>();

        for (int q = 0; q < 100; q++) {
            int random = (int) (Math.random() * 20) - 10;
            particleTable.add(new BlockPosition(l.getBlockX() + random, l.getBlockY() + random, l.getBlockZ() + random));
        }
        packet.getBlockPositionCollectionModifier().write(0, particleTable);
        m.sendServerPacket(player, packet);
    }
    public static void funnyPacketHealth(Player iPlayer) {
        ProtocolManager m = ProtocolLibrary.getProtocolManager();
        Player player = iPlayer.getPlayer();
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.UPDATE_HEALTH);
        packet.getFloat().write(0, -1F).write(1, Float.MIN_VALUE);
        packet.getIntegers().write(0, -1);
        m.sendServerPacket(player, packet);
    }
}
