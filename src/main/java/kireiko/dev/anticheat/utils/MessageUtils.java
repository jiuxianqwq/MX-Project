package kireiko.dev.anticheat.utils;

import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class MessageUtils {
    public static void sendMessagesToPlayers(String permission, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerProfile profile = PlayerContainer.getProfile(player);
            if (profile == null || !profile.isAlerts()) {
                continue;
            }
            if (player.hasPermission(permission)) {
                player.sendMessage(wrapColors(message));
            }
        }
    }

    public static void sendMessagesToPlayersNative(String permission, String permission2, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission) || player.hasPermission(permission2)) {
                player.sendMessage(wrapColors(message));
            }
        }
    }

    public static String wrapColors(String v) {
        return ChatColor.translateAlternateColorCodes('&', v);
    }

    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd:MM:yyyy");
        return sdf.format(date);
    }
}
