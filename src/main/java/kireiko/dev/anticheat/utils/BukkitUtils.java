package kireiko.dev.anticheat.utils;

import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public final class BukkitUtils {

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }
}
