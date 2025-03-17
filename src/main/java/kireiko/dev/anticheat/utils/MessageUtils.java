package kireiko.dev.anticheat.utils;

import kireiko.dev.anticheat.api.PlayerContainer;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.Date;

@UtilityClass
public class MessageUtils {
    public static void sendMessagesToPlayers(String permission, String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission) && PlayerContainer.getProfile(player).isAlerts()) {
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
        return v.replace("&", "§");
    }
    public static String getDate() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd:MM:yyyy");
        return sdf.format(date);
    }
}
