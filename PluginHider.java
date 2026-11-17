package com.yourserver.giveaway;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Hides this plugin from /plugins and /pl, and its commands from tab-complete, for players (ops included).
 * The console still sees the real list. Players get the same list minus GiveawayGUI.
 */
public class PluginHider implements Listener {

    private static final Set<String> LIST_COMMANDS = Set.of(
            "pl", "plugins", "bukkit:pl", "bukkit:plugins", "minecraft:pl", "minecraft:plugins");

    private final GiveawayPlugin plugin;

    public PluginHider(GiveawayPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage().trim();
        if (message.length() < 2) {
            return;
        }
        String label = message.substring(1).split("\\s+")[0].toLowerCase();
        if (!LIST_COMMANDS.contains(label)) {
            return;
        }
        event.setCancelled(true);

        Player player = event.getPlayer();
        List<Plugin> visible = Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .filter(p -> !p.equals(plugin))
                .sorted(Comparator.comparing(p -> p.getName().toLowerCase()))
                .toList();

        player.sendMessage(Component.text("\u2139 Server Plugins (" + visible.size() + "):", NamedTextColor.WHITE));
        Component names = Component.text("- ", NamedTextColor.DARK_GRAY);
        for (int i = 0; i < visible.size(); i++) {
            Plugin p = visible.get(i);
            names = names.append(Component.text(p.getName(), p.isEnabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            if (i < visible.size() - 1) {
                names = names.append(Component.text(", ", NamedTextColor.WHITE));
            }
        }
        player.sendMessage(names);
    }

    /** Removes /giveaway and /giveawaygui from the command suggestions sent to the client. */
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().removeIf(c -> c.equals("giveaway")
                || c.equals("giveawaygui")
                || c.startsWith("giveawaygui:"));
    }
}
