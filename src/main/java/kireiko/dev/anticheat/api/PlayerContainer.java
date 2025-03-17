package kireiko.dev.anticheat.api;

import kireiko.dev.anticheat.api.player.PlayerProfile;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class PlayerContainer {

    @Getter
    private static final Map<UUID, Player> uuid2Player = new ConcurrentHashMap<>();
    @Getter
    private static final Map<UUID, PlayerProfile> uuidPlayerProfileMap = new ConcurrentHashMap<>();
    public static void init(Player player) {
        PlayerProfile profile = new PlayerProfile(player);
        UUID uuid = player.getUniqueId();
        if (!uuidPlayerProfileMap.containsKey(uuid))
            uuidPlayerProfileMap.put(uuid, profile);
        else profile = uuidPlayerProfileMap.get(uuid);
        uuid2Player.put(uuid, player);
        profile.initChecks();
        profile.setPlayer(player);
        profile.setPunishAnimation(0);
    }
    public static void unload(Player player) {
        if (uuidPlayerProfileMap.containsKey(player.getUniqueId())) {
            PlayerProfile profile = uuidPlayerProfileMap.get(player.getUniqueId());
            if (profile.getBanAnimInfo() != null && !profile.isIgnoreExitBan()) {
                profile.forcePunish(profile.getBanAnimInfo().getX(), profile.getBanAnimInfo().getY());
            }
            profile.unload();
        }
    }
    public static PlayerProfile getProfile(Player player) {
        return uuidPlayerProfileMap.get(player.getUniqueId());
    }
}
