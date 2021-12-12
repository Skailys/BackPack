package de.fredie1104.projects.backpacks.commands;

import de.fredie1104.projects.backpacks.BackPacks;
import de.fredie1104.projects.backpacks.listener.ModifyShulker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


public class Backpack implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            printHelp(sender);
            return true;
        }

        switch (args[0]) {
            case "list" -> list(sender);
            default -> printHelp(sender);
        }
        return true;
    }

    private void printHelp(@NotNull CommandSender sender) {
        if (sender.hasPermission("backpack.command.list")) {
            sender.sendMessage("Bitte benutze /backpack list");
            return;
        }

        sender.sendMessage(new String[]{
                "Rechtsklicke mit einer Shulker in die Luft, um sie zu öffnen."});
    }

    private void list(CommandSender sender) {
        if (!sender.hasPermission("backpack.command.list")) {
            sender.sendMessage("Du hast keine Berechtigung diesen Befehl zu nutzen!");
            return;
        }

        if (ModifyShulker.getOpenedShulkers().isEmpty()) {
            sender.sendMessage("Momentan sind keine Shulker geöffnet");
            return;
        }

        sender.sendMessage("### Geöffnete Shulker ###");
        for (Player player : ModifyShulker.getOpenedShulkers()) {
            sender.sendMessage(String.format("   - %s (%s)", player.getName(), player.getUniqueId()));
        }
    }

}
