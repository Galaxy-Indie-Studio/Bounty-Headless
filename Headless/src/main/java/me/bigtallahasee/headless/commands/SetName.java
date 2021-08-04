package me.bigtallahasee.headless.commands;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class SetName implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players may use this command");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "/setname [username]");
            return true;
        }
        if (!args[0].matches("[A-Za-z0-9_]{2,16}")) {
            sender.sendMessage(ChatColor.RED + "That doesn't appear to be a valid username");
            return true;
        }
        Player player = (Player)sender;
        ItemStack i = player.getItemInHand();
        if (i.getType() == Material.PLAYER_HEAD) {
            i.setDurability((short)3);
            player.updateInventory();
            SkullMeta meta = (SkullMeta)i.getItemMeta();
            meta.setOwner(args[0]);
            i.setItemMeta((ItemMeta)meta);
            player.setItemInHand(i);
            sender.sendMessage(ChatColor.GREEN + "Set name " + args[0]);
        } else {
            sender.sendMessage(ChatColor.RED + "That's not a head");
        }
        return true;
    }
}

