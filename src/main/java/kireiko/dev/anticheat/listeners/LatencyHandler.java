package kireiko.dev.anticheat.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.utility.MinecraftVersion;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.events.CTransactionEvent;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LatencyHandler extends PacketAdapter {

    public LatencyHandler() {
        super(
                MX.getInstance(),
                ListenerPriority.MONITOR,
                Arrays.asList((ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.17")) >= 0)
                                ? PacketType.Play.Client.PONG
                                : (ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.12")) >= 0)
                                ? PacketType.Play.Client.TRANSACTION
                                : PacketType.Play.Client.KEEP_ALIVE,
                        (ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.17")) >= 0)
                                ? PacketType.Play.Server.PING
                                : (ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.12")) >= 0)
                                ? PacketType.Play.Server.TRANSACTION
                                : PacketType.Play.Server.KEEP_ALIVE),
                ListenerOptions.ASYNC
        );
    }


    @Override
    public void onPacketReceiving(PacketEvent event) {
        final Player player = event.getPlayer();
        final PlayerProfile protocol = PlayerContainer.getProfile(player);
        if (protocol == null) {
            return;
        }
        final PacketContainer packet = event.getPacket();
        int id;
        if (!packet.getShorts().getFields().isEmpty()) {
            id = packet.getShorts().read(0);
        } else if (!packet.getIntegers().getFields().isEmpty()) {
            id = packet.getIntegers().read(0);
        } else if (!packet.getLongs().getFields().isEmpty()) {
            id = Math.toIntExact(packet.getLongs().read(0));
        } else return;
        if (id <= -1939 && id >= -1945) {
            protocol.transactionPing = System.currentTimeMillis() - protocol.transactionTime;
            protocol.getPing().add(protocol.transactionPing);
            protocol.transactionLastTime = System.currentTimeMillis();
            protocol.transactionSentKeep = false;
            CTransactionEvent transactionEvent = new CTransactionEvent(protocol);
            protocol.run(transactionEvent);
            Bukkit.getScheduler().runTaskLaterAsynchronously(MX.getInstance(),
                    () -> sendTransaction(protocol, protocol.transactionId), 10L);
        }
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final Player player = event.getPlayer();
        final PlayerProfile protocol = PlayerContainer.getProfile(player);
        if (protocol == null) {
            return;
        }
        protocol.transactionSentKeep = true;
        protocol.transactionTime = System.currentTimeMillis();
    }

    public static void startChecking(PlayerProfile protocol) {
        protocol.transactionId = -1939;
        protocol.transactionBoot = false;
        sendTransaction(protocol, protocol.transactionId);
    }

    public static void sendTransaction(PlayerProfile protocol, short id) {

        PacketContainer packet = new PacketContainer(
                (ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.17")) >= 0)
                        ? PacketType.Play.Server.PING
                        : (ProtocolLibrary.getProtocolManager().getMinecraftVersion().compareTo(new MinecraftVersion("1.12")) >= 0)
                        ? PacketType.Play.Server.TRANSACTION
                        : PacketType.Play.Server.KEEP_ALIVE
        );


        if (!packet.getShorts().getFields().isEmpty()) {
            packet.getShorts().write(0, id);
            if (packet.getType().equals(PacketType.Play.Server.TRANSACTION)) {
                packet.getIntegers().write(0, 0);
                packet.getBooleans().write(0, false);
            }
        } else if (!packet.getIntegers().getFields().isEmpty()) {
            packet.getIntegers().write(0, (int) id);
        } else if (!packet.getLongs().getFields().isEmpty()) {
            packet.getLongs().write(0, (long) id);
        } else return;

        ProtocolLibrary.getProtocolManager().sendServerPacket(protocol.getPlayer(), packet);
        protocol.transactionId--;
        if (protocol.transactionId < -1945)
            protocol.transactionId = -1939;
    }
}