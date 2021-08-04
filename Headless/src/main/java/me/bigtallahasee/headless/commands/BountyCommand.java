package me.bigtallahasee.headless.commands;

import me.bigtallahasee.headless.Bounty;
import me.bigtallahasee.headless.BountyHeadless;
import me.bigtallahasee.headless.datastorage.DataStorageException;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class BountyCommand implements CommandExecutor {
    BountyHeadless plugin;

    public BountyCommand(BountyHeadless bountyHeadless) {
        this.plugin = bountyHeadless;
    }

    private void sendHelp(CommandSender sender) {
        if (sender.hasPermission("headless.bounty.search"))
            sender.sendMessage(ChatColor.RED + "/bounty search [username] - search for a bounty on a player");
        if (sender.hasPermission("headless.bounty.list"))
            sender.sendMessage(ChatColor.RED + "/bounty list <page> - list current bounties");
        if (sender.hasPermission("headless.bounty.place"))
            sender.sendMessage(ChatColor.RED + "/bounty place [username] [price] - place a bounty on a player");
        if (sender.hasPermission("headless.bounty.claim"))
            sender.sendMessage(ChatColor.RED + "/bounty claim - claim the bounty of the head you are holding");
        if (sender.hasPermission("headless.bounty.remove"))
            sender.sendMessage(ChatColor.RED + "/bounty remove [username] - remove the bounty of a player");
        if (sender.hasPermission("headless.bounty.listown"))
            sender.sendMessage(ChatColor.RED + "/bounty listown - list unclaimed bounties you have created");
        if (sender.hasPermission("headless.bounty.place")) {
            sender.sendMessage(ChatColor.RED + "/bounty redeem - claim any heads that are owed to you");
            sender.sendMessage(ChatColor.RED + "Current tax rate is " + this.plugin.getTax() + "%");
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Players only");
            return true;
        }
        if (!this.plugin.bounties) {
            sender.sendMessage(ChatColor.RED + "Bounties are not enabled");
            return true;
        }
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        Player p = (Player)sender;
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("list") && sender.hasPermission("headless.bounty.list")) {
                try {
                    List<Bounty> bounties = this.plugin.getDsi().getBounties(0, 9);
                    if (bounties.size() > 0) {
                        sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + " Bounties [Page 1 of " + ((this.plugin.getDsi().getNumBounties() + 8) / 9) + "] " + ChatColor.GREEN + "=========");
                        for (Bounty b : bounties) {
                            if (sender.hasPermission("headless.bounty.viewissuer")) {
                                sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted() + " placed by " + b.getIssuer());
                                continue;
                            }
                            sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "There are no bounties");
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("claim") && sender.hasPermission("headless.bounty.claim")) {
                if (p.getItemInHand().getType() != Material.PLAYER_HEAD) {
                    sender.sendMessage(ChatColor.RED + "That's not a head");
                    return true;
                }
                ItemStack i = p.getItemInHand();
                SkullMeta meta = (SkullMeta)i.getItemMeta();
                if (!meta.hasOwner()) {
                    sender.sendMessage(ChatColor.RED + "That head is not named");
                    return true;
                }
                if (!this.plugin.canClaimOwn && meta.getOwner().equals(p.getName())) {
                    sender.sendMessage(ChatColor.RED + "You may not turn in your own head");
                    return true;
                }
                try {
                    String hunted = meta.getOwner();
                    Bounty b = this.plugin.getDsi().getBounty(hunted);
                    if (b != null) {
                        b.setHunter(p.getName());
                        b.setHunted(hunted);
                        b.setTurnedIn(new Timestamp((new Date()).getTime()));
                        this.plugin.getDsi().updateBounty(b);
                        BountyHeadless.economy.depositPlayer(p.getName(), b.getReward());
                        sender.sendMessage(ChatColor.GREEN + "Sucessfully turned in bounty on " + b.getHunted() + " for " + BountyHeadless.economy.format(b.getReward()));
                        p.setItemInHand(null);
                        Player issuer = this.plugin.getServer().getPlayer(b.getIssuer());
                        if (issuer != null) {
                            ItemStack c = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
                            SkullMeta me = (SkullMeta)c.getItemMeta();
                            me.setOwner(hunted);
                            c.setItemMeta((ItemMeta)me);
                            if (!issuer.getInventory().addItem(new ItemStack[] { c }).isEmpty()) {
                                issuer.sendMessage(ChatColor.RED + "Not enough room in your inventory to give you a skull");
                                return true;
                            }
                            b.setRedeemed(new Timestamp((new Date()).getTime()));
                            this.plugin.getDsi().updateBounty(b);
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "There does not appear to be a bounty on that head");
                        return true;
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error claiming bounty", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                    return true;
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("listown") && sender.hasPermission("headless.bounty.listown")) {
                try {
                    List<Bounty> bounties = this.plugin.getDsi().getOwnBounties(p.getName());
                    if (bounties.size() > 0) {
                        sender.sendMessage(ChatColor.GREEN + "========= " + ChatColor.GOLD + "Your bounties " + ChatColor.GREEN + "=========");
                        for (Bounty b : bounties) {
                            if (sender.hasPermission("headless.bounty.viewissuer")) {
                                sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted() + " placed by " + b.getIssuer());
                                continue;
                            }
                            sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You have no bounties");
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("redeem") && sender.hasPermission("headless.bounty.place")) {
                try {
                    List<Bounty> bounties = this.plugin.getDsi().getUnclaimedBounties(p.getName());
                    if (bounties.size() == 0)
                        sender.sendMessage(ChatColor.RED + "Nothing to redeem");
                    for (Bounty b : bounties) {
                        ItemStack i = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
                        SkullMeta meta = (SkullMeta)i.getItemMeta();
                        meta.setOwner(b.getHunted());
                        i.setItemMeta((ItemMeta)meta);
                        if (!p.getInventory().addItem(new ItemStack[] { i }).isEmpty()) {
                            p.sendMessage(ChatColor.RED + "Not enough room in your inventory to give you a skull");
                            return true;
                        }
                        b.setRedeemed(new Timestamp((new Date()).getTime()));
                        this.plugin.getDsi().updateBounty(b);
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error getting list of unredeemed bounties", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                }
                return true;
            }
            sendHelp(sender);
            return true;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("search") && sender.hasPermission("headless.bounty.search")) {
                try {
                    List<Bounty> bounties = this.plugin.getDsi().getBounties(args[1]);
                    int count = 0;
                    if (bounties.size() > 0) {
                        sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + " Bounties matching " + args[1] + " " + ChatColor.GREEN + "=========");
                        for (Bounty b : bounties) {
                            if (sender.hasPermission("headless.bounty.viewissuer")) {
                                sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted() + " placed by " + b.getIssuer());
                            } else {
                                sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                            }
                            count++;
                            if (count == 8) {
                                sender.sendMessage(ChatColor.GOLD + "Plus " + (bounties.size() - count) + " more");
                                return true;
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "No bounties match your query");
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error searching for bounties", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                    return true;
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("list") && sender.hasPermission("headless.bounty.list")) {
                try {
                    int page = Integer.parseInt(args[1]);
                    List<Bounty> bounties = this.plugin.getDsi().getBounties((page - 1) * 9, page * 9);
                    if (bounties.size() > 0) {
                        sender.sendMessage(ChatColor.GREEN + "=========" + ChatColor.GOLD + "Bounties [Page " + page + " of " + ((this.plugin.getDsi().getNumBounties() + 8) / 9) + "]" + ChatColor.GREEN + "=========");
                        for (Bounty b : bounties) {
                            if (sender.hasPermission("headless.bounty.viewissuer")) {
                                sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted() + " placed by " + b.getIssuer());
                                continue;
                            }
                            sender.sendMessage(ChatColor.GOLD + "" + b.getReward() + " - " + b.getHunted());
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "There are no bounties");
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "That's not a number");
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error getting list of bounties", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("remove") && sender.hasPermission("headless.bounty.remove")) {
                if (!args[1].matches("[A-Za-z0-9_]{2,16}")) {
                    sender.sendMessage(ChatColor.RED + "That doesn't appear to be a valid username");
                    return true;
                }
                try {
                    Bounty bounty = this.plugin.getDsi().getBounty(args[1], p.getName());
                    if (bounty != null) {
                        this.plugin.getDsi().deleteBounty(bounty);
                        BountyHeadless.economy.depositPlayer(p.getName(), bounty.getReward() - bounty.getReward() * this.plugin.getTax());
                        sender.sendMessage(ChatColor.GREEN + "Deleted bounty against " + bounty.getHunted() + " for " + BountyHeadless.economy.format(bounty.getReward()));
                        sender.sendMessage(ChatColor.GREEN + "You have been refunded " + BountyHeadless.economy.format(bounty.getReward() - bounty.getReward() * this.plugin.getTax()));
                    } else {
                        sender.sendMessage(ChatColor.RED + "No matches");
                    }
                } catch (DataStorageException e) {
                    this.plugin.getLogger().log(Level.SEVERE, "Error deleting bounty", (Throwable)e);
                    sender.sendMessage(ChatColor.RED + "Something went wrong :(");
                }
                return true;
            }
            sendHelp(sender);
            return true;
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("place") && sender.hasPermission("headless.bounty.place")) {
            if (!args[1].matches("[A-Za-z0-9_]{2,16}")) {
                sender.sendMessage(ChatColor.RED + "That doesn't appear to be a valid username");
                return true;
            }
            try {
                Bounty bounty = this.plugin.getDsi().getBounty(args[1], p.getName());
                int reward = Integer.parseInt(args[2]);
                if (reward <= 0) {
                    sender.sendMessage(ChatColor.RED + "You must set a positive bounty");
                    return true;
                }
                if (reward < this.plugin.minimumBounty) {
                    sender.sendMessage(ChatColor.RED + "You must place a bounty worth at least " + BountyHeadless.economy.format(this.plugin.minimumBounty));
                    return true;
                }
                if (BountyHeadless.economy.has(p.getName(), reward + reward * this.plugin.getTax())) {
                    if (bounty == null) {
                        bounty = new Bounty(p.getName(), args[1], reward);
                        this.plugin.getDsi().addBounty(bounty);
                        BountyHeadless.economy.withdrawPlayer(p.getName(), reward + reward * this.plugin.getTax());
                        sender.sendMessage(ChatColor.GREEN + "Added bounty against " + args[1]);
                        sender.sendMessage(ChatColor.GREEN + "You have been charged " + BountyHeadless.economy.format(reward + reward * this.plugin.getTax()));
                    } else {
                        bounty.setReward(bounty.getReward() + reward);
                        this.plugin.getDsi().updateBounty(bounty);
                        BountyHeadless.economy.withdrawPlayer(p.getName(), reward + reward * this.plugin.getTax());
                        sender.sendMessage(ChatColor.GREEN + "Added money to existing bounty against " + args[1]);
                        sender.sendMessage(ChatColor.GREEN + "You have been charged " + BountyHeadless.economy.format(reward + reward * this.plugin.getTax()));
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "You don't have enough money (" + BountyHeadless.economy.format(reward + reward * this.plugin.getTax()) + ")");
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "That's not a number");
            } catch (DataStorageException e) {
                this.plugin.getLogger().log(Level.SEVERE, "Error adding bounty", (Throwable)e);
                sender.sendMessage(ChatColor.RED + "Something went wrong :(");
            }
            return true;
        }
        sendHelp(sender);
        return true;
    }
}

