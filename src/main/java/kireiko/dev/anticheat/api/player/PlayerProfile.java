package kireiko.dev.anticheat.api.player;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.CheckPacketRegister;
import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.events.MXFlagEvent;
import kireiko.dev.anticheat.checks.AimAnalysisCheck;
import kireiko.dev.anticheat.checks.AimComplexCheck;
import kireiko.dev.anticheat.checks.AimHeuristicCheck;
import kireiko.dev.anticheat.checks.AimStatisticsCheck;
import kireiko.dev.anticheat.utils.AnimatedPunishUtil;
import kireiko.dev.anticheat.utils.ConfigController;
import kireiko.dev.anticheat.utils.MessageUtils;
import kireiko.dev.anticheat.utils.protocol.ProtocolTools;
import kireiko.dev.millennium.types.EvictingList;
import kireiko.dev.millennium.vectors.Pair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
public class PlayerProfile extends ConfigController {

    private Player player;
    private Location to, from;
    private Set<PacketCheckHandler> checks;
    private List<Location> pastLoc = new EvictingList<>(20);
    private SensitivityProcessor sensitivityProcessor;
    private float vl;

    @Setter
    public int airTicks, flagCount, punishAnimation, teleportTicks, horrowStage;
    private long attackBlockToTime;
    private boolean alerts, debug, ignoreExitBan;
    private Pair<String, String> banAnimInfo;
    private Pair<Location, Location> banAnimPositions;
    public boolean sneaking = false, sprinting = false, ground = false;

    public PlayerProfile(Player player) {
        this.player = player;
        initClass();
    }
    public void punish(String check, String component, String info, float m) {
        if (!config().getString("bypass").equalsIgnoreCase("none")
                        && this.player.hasPermission(config().getString("bypass"))) {
            return;
        }
        // this.vl += 10.0f * m;
        final float tempVl = this.vl + 10.0f * m;
        final double vlLimit = config().getDouble("vlLimit");
        MXFlagEvent event = new MXFlagEvent(this.player, check, component, info, tempVl, vlLimit);
        if (!event.callEvent()) {
            return;
        }
        this.vl = tempVl;
        this.flagCount += (m == 0.0) ? 0 : 1;
        String builder = this.wrapString(Objects.requireNonNull(
                        config().getString("alertMsg")))
                        .replace("%check%", check).replace("%component%", component).replace("%info%", info);
        MessageUtils.sendMessagesToPlayers(MX.permission, builder);
        if (this.vl >= vlLimit) {
            if (config().getBoolean("punishEffect")) {
                AnimatedPunishUtil.punish(this, new Pair<>(check, info));
            } else forcePunish(check, info);
        } else if (this.vl >= vlLimit / 1.8) {
            if (flagCount > 2) {
                MessageUtils.sendMessagesToPlayersNative(
                        MX.permissionHead + "personal",
                        MX.permission,
                        this.wrapString(Objects.requireNonNull(
                                                config().getString("suspected")))
                                .replace("%check%", check).replace("%info%", info)
                );
                this.flagCount = 0;
            }
        } else if (flagCount == 2) {
            MessageUtils.sendMessagesToPlayersNative(
                    MX.permissionHead + "personal",
                    MX.permission,
                    this.wrapString(Objects.requireNonNull(
                                            config().getString("unusual")))
                            .replace("%check%", check).replace("%info%", info)
            );
        }
    }
    public void fade(float vl) {
        this.vl -= vl;
        if (this.vl < 0) this.vl = 0;
    }
    public void initChecks() {
        if (!this.checks.isEmpty()) return;
        this.checks.add(new AimHeuristicCheck(this));
        this.checks.add(new AimComplexCheck(this));
        this.checks.add(new AimStatisticsCheck(this));
        this.checks.add(new AimAnalysisCheck(this));
    }
    public void unload() {
        this.checks.clear();
    }
    public void run(Object handler) {
        CheckPacketRegister.runCustom(handler, checks);
    }

    private String wrapString(String v) {
        return MessageUtils.wrapColors(v.replace("%player%", this.getPlayer().getName())
                        .replace("%vl%", String.valueOf(this.vl))
                        .replace("%vlLimit%", String.valueOf(config().getDouble("vlLimit"))));
    }

    public boolean toggleAlerts() {
        this.alerts = !this.alerts;
        return this.alerts;
    }
    public boolean toggleDebug() {
        this.debug = !this.debug;
        return this.debug;
    }
    private void initClass() {
        this.banAnimInfo = null;
        this.banAnimPositions = null;
        this.sensitivityProcessor = new SensitivityProcessor(player);
        this.to = ProtocolTools.getLoadLocation(player);
        this.from = ProtocolTools.getLoadLocation(player);
        this.checks = new HashSet<>();
        this.vl = 0.0f;
        this.alerts = false;
        this.debug = false;
        this.ignoreExitBan = false;
        this.flagCount = 0;
        this.punishAnimation = 0;
        this.attackBlockToTime = 0L;
        this.airTicks = 0;
        this.horrowStage = 0;
        // init checks
        this.initChecks();
    }
    public void forcePunish(String check, String info) {
        MX.bannedPerMinuteCount++;
        this.ignoreExitBan = true;
        this.vl = 0;
        Bukkit.getScheduler().runTask(MX.getInstance(), () -> {
            String banMsg = this.wrapString(Objects.requireNonNull(
                                            config().getString("banCommand")))
                            .replace("%check%", check).replace("%info%", info);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banMsg);
            this.setBanAnimInfo(null);
        });
    }
    public void debug(String msg) {
        if (debug)
            this.player.sendMessage(wrapString("&9&l[Debug] &f" + msg));
    }
    public void setAttackBlockToTime(long time) {
        if (!config().getString("bypass").equalsIgnoreCase("none")
                        && this.player.hasPermission(config().getString("bypass"))) {
            return;
        }
        this.attackBlockToTime = time;
    }
}
