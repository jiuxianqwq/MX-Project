package kireiko.dev.anticheat.api;

import kireiko.dev.anticheat.api.player.PlayerProfile;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerContainer {

    @Getter
    private static final Map<UUID, PlayerProfile> uuidPlayerProfileMap = new ConcurrentHashMap<>();
    public static void init(Player player) {
        PlayerProfile profile = new PlayerProfile(player);
        uuidPlayerProfileMap.put(player.getUniqueId(), profile);
        profile.initChecks();
    }
    public static void unload(Player player) {
        PlayerProfile profile = uuidPlayerProfileMap.get(player.getUniqueId());
        if (profile == null) {
            return;
        }
        uuidPlayerProfileMap.remove(player.getUniqueId());
        if (profile.getBanAnimInfo() != null && !profile.isIgnoreExitBan()) {
            profile.forcePunish(profile.getBanAnimInfo().getX(), profile.getBanAnimInfo().getY());
        }
    }

    @Nullable
    public static PlayerProfile getProfile(Player player) {
        return uuidPlayerProfileMap.get(player.getUniqueId());
    }

    @Nullable
    public static PlayerProfile getProfileByName(String name) {
        for (PlayerProfile profile : uuidPlayerProfileMap.values()) {
            if (profile.getPlayer().getName().equalsIgnoreCase(name)) {
                return profile;
            }
        }
        return null;
    }
}
