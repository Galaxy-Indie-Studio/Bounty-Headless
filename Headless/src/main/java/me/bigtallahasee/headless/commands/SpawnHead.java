package me.bigtallahasee.headless.commands;

import me.bigtallahasee.headless.BountyHeadless;
import me.bigtallahasee.headless.exception.BadPlayerMatchException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

public class SpawnHead implements CommandExecutor {
    BountyHeadless plugin;

    public SpawnHead(BountyHeadless plugin) {
        this.plugin = plugin;
    }

    private Player getPlayer(String target, CommandSender searcher) throws BadPlayerMatchException {
        List<Player> players = new ArrayList<Player>();
        for (Player player : this.plugin.getServer().getOnlinePlayers()) {
            if (!(searcher instanceof Player) || ((Player)searcher).canSee(player)) {
                if (player.getName().equalsIgnoreCase(target))
                    return player;
                if (player.getName().toLowerCase().contains(target.toLowerCase()))
                    players.add(player);
            }
        }
        if (players.size() > 1) {
            StringBuilder sb = new StringBuilder();
            for (Player player : players) {
                sb.append(player.getName());
                sb.append(", ");
            }
            sb.setLength(sb.length() - 2);
            throw new BadPlayerMatchException("Matches too many players (" + sb.toString() + ")");
        }
        if (players.size() == 0)
            throw new BadPlayerMatchException("No players matched");
        return players.get(0);
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player player;
        int quantity = 1;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "/spawnhead [username] <quantity> <player>");
            return true;
        }
        if (!args[0].matches("[A-Za-z0-9_]{2,16}")) {
            sender.sendMessage(ChatColor.RED + "That doesn't appear to be a valid username");
            return true;
        }
        if (args.length > 1) {
            try {
                quantity = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That's not a number");
                return true;
            }
            if (quantity < 1) {
                quantity = 1;
            } else if (quantity > 64) {
                quantity = 64;
            }
        }
        if (args.length > 2) {
            try {
                player = getPlayer(args[2], sender);
            } catch (BadPlayerMatchException e) {
                sender.sendMessage(ChatColor.RED + e.getMessage());
                return true;
            }
        } else if (sender instanceof Player) {
            player = (Player)sender;
        } else {
            sender.sendMessage(ChatColor.RED + "Specify a player to give the weapon to");
            return true;
        }
        ItemStack i = new ItemStack(Material.PLAYER_HEAD, quantity, (short)3);
        SkullMeta meta = (SkullMeta)i.getItemMeta();
        meta.setOwner(args[0]);
        i.setItemMeta((ItemMeta)meta);
        player.getInventory().addItem(new ItemStack[] { i });
        return true;
    }
}

