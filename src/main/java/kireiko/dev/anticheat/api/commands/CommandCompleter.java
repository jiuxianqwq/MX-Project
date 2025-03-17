package kireiko.dev.anticheat.api.commands;

import kireiko.dev.anticheat.MX;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CommandCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase(MX.command)) {
            if (args.length == 1) {
                suggestions.addAll(Arrays.asList("alerts", "ban",
                        "crash", "horrow", "reload", "stat", "bc", "debug", "fun"));
            } else if (args.length == 2 && !args[1].equals("bc")) {
                for (Player player : Bukkit.getOnlinePlayers())
                    suggestions.add(player.getName());
                Collections.sort(suggestions);
            }
        }
        String currentInput = args[args.length - 1];
        suggestions.removeIf(s -> !s.toLowerCase().startsWith(currentInput.toLowerCase()));

        return suggestions;
    }

}
