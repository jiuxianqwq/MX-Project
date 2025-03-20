package kireiko.dev.anticheat.api.commands;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.api.player.fun.FunItemsService;
import kireiko.dev.anticheat.utils.ConfigCache;
import kireiko.dev.anticheat.utils.ConfigController;
import kireiko.dev.millennium.math.Simplification;
import kireiko.dev.millennium.math.Statistics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static kireiko.dev.anticheat.utils.MessageUtils.wrapColors;

public class CommandHandler extends ConfigController implements CommandExecutor {

    private static final String[] s = new String[]{
                    wrapColors("&9&l" + MX.name + " &fCommands"),
                    "",
                    wrapColors("&e/" + MX.command + " alerts &f- &cturn on/off alerts"),
                    wrapColors("&e/" + MX.command + " info <player> &f- &cplayer info"),
                    wrapColors("&e/" + MX.command + " ban <player> &f- &cforce ban"),
                    wrapColors("&e/" + MX.command + " reload &f- &cconfig reload"),
                    wrapColors("&e/" + MX.command + " stat &f- &cglobal statistics"),
                    wrapColors("&e/" + MX.command + " bc &f- &cmessage for all players"),
                    wrapColors("&e/" + MX.command + " debug &f- &cverbose checks"),
                    wrapColors("&e/" + MX.command + " fun &f- &cfun things"),
                    ""
    };

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Invalid Sender");
            return true;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission(MX.permission)) {
                player.sendMessage("You don't have permission!");
                return true;
            }
        }
        if ((!label.equalsIgnoreCase(MX.command))) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.sendMessage("Usage: /" + MX.command);
            }
            return true;
        }
        if (args.length == 0) {
            StringBuilder w = new StringBuilder();
            for (String string : s) w.append(string).append("\n");
            sendToSender(sender, w.toString());
            return true;
        } else {
            final String s = args[0];
            switch (s) {
                case ("alerts"): {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerProfile profile = PlayerContainer.getProfile(player);
                        if (profile == null) {
                            player.sendMessage(wrapColors("&cProfile not initialized!"));
                            return true;
                        }
                        player.sendMessage(wrapColors("&cAlerts: &e" + profile.toggleAlerts()));
                    }
                    break;
                }
                case ("fun"): {
                    if (sender instanceof Player) {
                        final Player player = (Player) sender;
                        FunItemsService.give(player);
                    }
                    break;
                }
                case ("info"): {
                    PlayerProfile playerProfile = null;
                    for (PlayerProfile tempProfile : PlayerContainer.getUuidPlayerProfileMap().values()) {
                        if (tempProfile.getPlayer().getName().equalsIgnoreCase(args[1])) {
                            playerProfile = tempProfile;
                            break;
                        }
                    }
                    if (playerProfile == null) {
                        sendToSender(sender, "§cPlayer not found... Sorry!");
                    } else {
                        String sens = wrapColors("&4Not enough info!");
                        if (Statistics.getDistinct(playerProfile.getSensitivity()) != playerProfile.getSensitivity().size()) {
                            final Set<Integer> prev = new HashSet<>();
                            for (int i : playerProfile.getSensitivity()) {
                                if (prev.contains(i / 5)) {
                                    sens = "&9" + i;
                                } else prev.add(i / 5);
                            }
                        }
                        StringBuilder pingLabel = new StringBuilder();
                        for (long ping : playerProfile.getPing()) {
                            final String color = (ping > 1000) ? "&4" : (ping > 300) ? "&c" : (ping > 100) ? "&e" : "&a";
                            if (pingLabel.toString().isEmpty()) {
                                pingLabel = new StringBuilder(wrapColors(color + ping));
                            } else pingLabel.append(wrapColors("&f, " + color + ping));
                        }
                        final String[] info = new String[]{
                                        "",
                                        wrapColors("&fInfo about &c" + playerProfile.getPlayer().getName()),
                                        "",
                                        wrapColors("&fPing (ms): " + pingLabel),
                                        wrapColors("&fJitter (ms): &9" + Simplification.scaleVal(Statistics.getStandardDeviation(playerProfile.getPing()), 2)),
                                        wrapColors("&fSensitivity: " + sens),
                                        wrapColors("&fVL: &c" + playerProfile.getVl()),
                                        ""
                        };
                        StringBuilder w = new StringBuilder();
                        for (String string : info) w.append(string).append("\n");
                        sendToSender(sender, w.toString());
                    }
                    break;
                }
                case ("ban"): {
                    PlayerProfile playerProfile = null;
                    for (PlayerProfile tempProfile : PlayerContainer.getUuidPlayerProfileMap().values()) {
                        if (tempProfile.getPlayer().getName().equalsIgnoreCase(args[1])) {
                            playerProfile = tempProfile;
                            break;
                        }
                    }
                    if (playerProfile == null) {
                        sendToSender(sender, "§cPlayer not found... Sorry!");
                    } else {
                        playerProfile.punish("Skill issue", "Bad guy", "ForceBan (Staff)", 99.0f);
                    }
                    break;
                }
                case ("reload"): {
                    MX.getInstance().reloadConfig();
                    ConfigCache.loadConfig();
                    sendToSender(sender, wrapColors("&cConfig reloaded!"));
                    break;
                }
                case ("debug"): {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerProfile profile = PlayerContainer.getProfile(player);
                        if (profile == null) {
                            player.sendMessage(wrapColors("&cProfile not initialized!"));
                            return true;
                        }
                        player.sendMessage(wrapColors("&cDebug: &e" + profile.toggleDebug()));
                    }
                    break;
                }
                case ("bc"): {
                    if (args.length > 1) {
                        StringBuilder total = new StringBuilder();
                        for (int i = 1; i < args.length; i++) {
                            total.append(" ").append(args[i]);
                        }
                        for (Player player : Bukkit.getOnlinePlayers())
                            player.sendMessage(wrapColors(config()
                                    .getString("bcMsg")
                                    .replace("%message%", total.toString().trim())));
                    }
                    break;
                }
                case ("stat"): {
                    {
                        int banCount = 0, blockCount = 0;
                        for (int i : MX.bannedPerMinuteList) banCount += i;
                        for (int i : MX.blockedPerMinuteList) blockCount += i;
                        sendToSender(sender, wrapColors("&c&l" + MX.name + " &9Statistics \n"));
                        sendToSender(sender, wrapColors("&4Banned in the last hour&f: &e" + banCount));
                        sendToSender(sender, wrapColors("&4Average bans per minute&f: &e"
                                        + (int) Statistics.getAverage(MX.bannedPerMinuteList)));
                        sendToSender(sender, wrapColors("&4Blocked hits in the last hour&f: &e" + blockCount));
                        sendToSender(sender, wrapColors("&4Average blocked hits per minute&f: &e"
                                        + (int) Statistics.getAverage(MX.blockedPerMinuteList)));
                    }
                    break;
                }
            }
        }
        return true;
    }

    private static void sendToSender(CommandSender sender, String msg) {
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            player.sendMessage(msg);
        } else {
            MX.getInstance().getLogger().info(msg);
        }
    }

}
