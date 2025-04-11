package kireiko.dev.anticheat.commands.subcommands;

import com.google.common.collect.ImmutableList;
import kireiko.dev.anticheat.MX;
import kireiko.dev.anticheat.api.PlayerContainer;
import kireiko.dev.anticheat.api.RotationsContainer;
import kireiko.dev.anticheat.api.player.PlayerProfile;
import kireiko.dev.anticheat.commands.MXSubCommand;
import kireiko.dev.anticheat.core.AsyncScheduler;
import kireiko.dev.anticheat.utils.MessageUtils;
import kireiko.dev.anticheat.utils.NetworkUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ActivityCommand extends MXSubCommand {
    public ActivityCommand() {
        super("activity");
    }

    @Override
    public String getDescription() {
        return "Link to show mouse movements in the web panel";
    }

    @Override
    public String getUsage() {
        return "/" + MX.command + " " + getName() + " <player>";
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean onlyPlayerCanUse() {
        return false;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, String[] args) {
        PlayerProfile target = PlayerContainer.getProfileByName(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found... Sorry!");
            return true;
        }
        { // uploading
            AsyncScheduler.run(() -> {
                sender.sendMessage(MessageUtils.wrapColors("&9Uploading, wait for it..."));
                final String result = NetworkUtil.createPaste(RotationsContainer.getJson(target.getPlayer().getUniqueId()));
                if (result == null) {
                    sender.sendMessage(MessageUtils.wrapColors("&cUnknown error while loading :("));
                } else {
                    sender.sendMessage(MessageUtils.wrapColors(
                                    "",
                                    "&e" + target.getPlayer().getName() + "&9 data: &7" + result,
                                    "&cPut this link here:",
                                    "&e",
                                    ""
                                    )
                    );
                }
            });
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) return null;
        return ImmutableList.of(); // return empty list
    }
}
