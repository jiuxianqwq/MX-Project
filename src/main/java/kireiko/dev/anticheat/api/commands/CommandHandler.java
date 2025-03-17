package kireiko.dev.anticheat.api.commands;

import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.api.player.fun.FunItemsService;
import kireiko.dev.anticheat.utils.ConfigController;
import kireiko.dev.anticheat.utils.FunnyPackets;
import kireiko.dev.millennium.math.Statistics;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static kireiko.dev.anticheat.utils.MessageUtils.wrapColors;

public class CommandHandler extends ConfigController implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player) && !(sender instanceof ConsoleCommandSender)) {
            sender.sendMessage("Invalid Sender");
            return true;
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            if (!player.hasPermission(MX.permission)) {
                player.sendMessage("You don't have permission!");
                return true;
            }
        }
        if ((!label.equalsIgnoreCase(MX.command))) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                player.sendMessage("Usage: /" + MX.command);
            }
            return true;
        }
        if (args.length == 0) {
            String[] s = new String[]{
                    wrapColors("&9&l" + MX.name + " &fCommands"),
                    "",
                    wrapColors("&e/" + MX.command + " alerts &f- &cturn on/off alerts"),
                    wrapColors("&e/" + MX.command + " ban <player> &f- &cforce ban"),
                            /*
                    wrapColors("&e/" + MX.command + " crash <player> &f- &cgame crash"),
                    wrapColors("&e/" + MX.command + " crash <player> <msg>/silent &f- &ccustom game crash"),
                    wrapColors("&e/" + MX.command + " horrow <player> &f- &cgame crash with horror ;)"),
                             */
                    wrapColors("&e/" + MX.command + " reload &f- &cconfig reload"),
                    wrapColors("&e/" + MX.command + " stat &f- &cglobal statistics"),
                    wrapColors("&e/" + MX.command + " bc &f- &cmessage for all players"),
                    wrapColors("&e/" + MX.command + " debug &f- &cverbose checks"),
                    wrapColors("&e/" + MX.command + " fun &f- &cfun things"),
                    ""
            };
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
                /*
                case ("crash"): {
                    Player t = null;
                    for (Player profile : PlayerContainer.getUuid2Player().values()) {
                        if (profile.getName().equalsIgnoreCase(args[1])) {
                            t = profile;
                            break;
                        }
                    }
                    if (t == null) {
                        sendToSender(sender, "§cPlayer not found... Sorry!");
                    } else {
                        if (args.length > 3) {
                            if (args[3].equalsIgnoreCase("silent")) {
                                FunnyPackets.closeMinecraftCustom(t, "");
                            } else {
                                StringBuilder builder = new StringBuilder();
                                for (int i = 3; i < args.length; i++)
                                    builder.append(" ").append(args[i]);

                                FunnyPackets.closeMinecraftCustom(t, builder.toString());
                            }
                        } else {
                            FunnyPackets.closeMinecraft(t);
                        }
                    }
                    break;
                }
                case ("horrow"): {
                    Player t = null;
                    for (Player profile : PlayerContainer.getUuid2Player().values()) {
                        if (profile.getName().equalsIgnoreCase(args[1])) {
                            t = profile;
                            break;
                        }
                    }
                    if (t == null) {
                        sendToSender(sender, "§cPlayer not found... Sorry!");
                    } else {
                        PlayerContainer.getProfile(t).horrowStage = 1;
                    }
                    break;
                }
                 */
                case ("ban"): {
                    Player t = null;
                    for (Player profile : PlayerContainer.getUuid2Player().values()) {
                        if (profile.getName().equalsIgnoreCase(args[1])) {
                            t = profile;
                            break;
                        }
                    }
                    if (t == null) {
                        sendToSender(sender, "§cPlayer not found... Sorry!");
                    } else {
                        PlayerProfile profile = PlayerContainer.getProfile(t);
                        profile.punish("Skill issue", "Bad guy", "ForceBan (Staff)", 99.0f);
                    }
                    break;
                }
                case ("reload"): {
                    MX.getInstance().reloadConfig();
                    sendToSender(sender, wrapColors("&cConfig reloaded!"));
                    break;
                }
                case ("debug"): {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        PlayerProfile profile = PlayerContainer.getProfile(player);
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
