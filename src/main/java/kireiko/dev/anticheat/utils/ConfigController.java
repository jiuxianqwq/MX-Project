package kireiko.dev.anticheat.utils;

import kireiko.dev.anticheat.MX;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigController {
    public FileConfiguration config() {
        return MX.getInstance().getConfig();
    }
}
