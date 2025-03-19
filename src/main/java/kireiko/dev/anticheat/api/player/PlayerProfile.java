package kireiko.dev.anticheat.api.player;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.CheckPacketRegister;
import kireiko.dev.anticheat.api.PacketCheckHandler;
import kireiko.dev.anticheat.api.events.MXFlagEvent;
import kireiko.dev.anticheat.checks.aim.AimAnalysisCheck;
import kireiko.dev.anticheat.checks.aim.AimComplexCheck;
import kireiko.dev.anticheat.checks.aim.AimHeuristicCheck;
import kireiko.dev.anticheat.checks.aim.AimStatisticsCheck;
import kireiko.dev.anticheat.checks.velocity.VelocityCheck;
import kireiko.dev.anticheat.services.AnimatedPunishService;
import kireiko.dev.anticheat.utils.ConfigCache;
import kireiko.dev.anticheat.utils.ConfigController;
import kireiko.dev.anticheat.utils.MessageUtils;
import kireiko.dev.anticheat.utils.protocol.ProtocolLib;
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

import java.util.*;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
public class PlayerProfile extends ConfigController {

    public boolean transactionSentKeep;
    public boolean transactionBoot = true;
    public long transactionTime, transactionLastTime, transactionPing;
    public short transactionId;
    private Player player;
    private Location to = ProtocolTools.getLoadLocation(player);
    private Location from = ProtocolTools.getLoadLocation(player);
    private Set<PacketCheckHandler> checks = new HashSet<>();
    private List<Location> pastLoc = new EvictingList<>(20);
    private SensitivityProcessor sensitivityProcessor = new SensitivityProcessor(player);
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
    }

    public void punish(final String check, final String component, final String info, final float m) {
        Bukkit.getScheduler().runTask(MX.getInstance(), () -> this.punishAsync(check, component, info, m));
    }
    public void punishAsync(final String check, final String component, final String info, final float m) {
        if (!ConfigCache.BAN_COMMAND.equalsIgnoreCase("none")
                && this.player.hasPermission(ConfigCache.BYPASS)) {
            return;
        }
        // this.vl += 10.0f * m;
        final float tempVl = this.vl + 10.0f * m;
        final double vlLimit = ConfigCache.VL_LIMIT;
        MXFlagEvent event = new MXFlagEvent(this.player, check, component, info, tempVl, vlLimit);
        if (!event.callEvent()) {
            return;
        }
        this.vl = tempVl;
        this.flagCount += (m == 0.0) ? 0 : 1;
        String builder = this.wrapString(ConfigCache.ALERT_MSG.replace("%check%", check).replace("%component%", component).replace("%info%", info));
        MessageUtils.sendMessagesToPlayers(MX.permission, builder);
        if (this.vl >= vlLimit) {
            if (ConfigCache.PUNISH_EFFECT) {
                AnimatedPunishService.punish(this, new Pair<>(check, info));
            } else {
                forcePunish(check, info);
            }
        } else if (this.vl >= vlLimit / 1.8) {
            if (flagCount > 2) {
                MessageUtils.sendMessagesToPlayersNative(
                        MX.permissionHead + "personal",
                        MX.permission,
                        this.wrapString(ConfigCache.SUSPECTED.replace("%check%", check).replace("%info%", info))
                );
                this.flagCount = 0;
            }
        } else if (flagCount == 2) {
            MessageUtils.sendMessagesToPlayersNative(
                    MX.permissionHead + "personal",
                    MX.permission,
                    this.wrapString(ConfigCache.UNUSUAL.replace("%check%", check).replace("%info%", info))
            );
        }
    }
    public void fade(float vl) {
        this.vl -= vl;
        if (this.vl < 0) this.vl = 0;
    }
    public void initChecks() {
        this.checks.add(new AimHeuristicCheck(this));
        this.checks.add(new AimComplexCheck(this));
        this.checks.add(new AimStatisticsCheck(this));
        this.checks.add(new AimAnalysisCheck(this));
        this.checks.add(new VelocityCheck(this));
    }

    public void run(Object handler) {
        CheckPacketRegister.runCustom(handler, checks);
    }

    private String wrapString(String v) {
        return MessageUtils.wrapColors(v.replace("%player%", this.getPlayer().getName())
                        .replace("%vl%", String.valueOf(this.vl))
                        .replace("%vlLimit%", String.valueOf(ConfigCache.VL_LIMIT))
        );
    }

    public boolean toggleAlerts() {
        this.alerts = !this.alerts;
        return this.alerts;
    }
    public boolean toggleDebug() {
        this.debug = !this.debug;
        return this.debug;
    }

    public void forcePunish(String check, String info) {
        MX.bannedPerMinuteCount++;
        this.ignoreExitBan = true;
        this.vl = 0;
        Bukkit.getScheduler().runTask(MX.getInstance(), () -> {
            String banMsg = this.wrapString(ConfigCache.BAN_COMMAND.replace("%check%", check).replace("%info%", info));
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), banMsg);
            this.setBanAnimInfo(null);
        });
    }
    public void debug(String msg) {
        if (debug)
            this.player.sendMessage(wrapString("&9&l[Debug] &f" + msg));
    }
    public void setAttackBlockToTime(long time) {
        if (!ConfigCache.BAN_COMMAND.equalsIgnoreCase("none")
                && this.player.hasPermission(ConfigCache.BYPASS)) {
            return;
        }
        this.attackBlockToTime = time;
    }
    public int getEntityId() {
        return ProtocolLib.isTemporary(this.getPlayer())
                        ? new Random().nextInt()
                        : this.getPlayer().getEntityId();
    }
}
