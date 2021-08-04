package me.bigtallahasee.headless;

import me.bigtallahasee.headless.commands.BountyCommand;
import me.bigtallahasee.headless.commands.ClearName;
import me.bigtallahasee.headless.commands.SetName;
import me.bigtallahasee.headless.commands.SpawnHead;
import me.bigtallahasee.headless.datastorage.DataStorageException;
import me.bigtallahasee.headless.datastorage.DataStorageInterface;
import me.bigtallahasee.headless.datastorage.MySQLDataStorageImplementation;
import me.bigtallahasee.headless.datastorage.YamlDataStorageImplementation;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Skull;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

public final class BountyHeadless extends JavaPlugin implements Listener {
    double allDeaths;

    double killedByPlayer;

    public boolean bounties = false;

    private boolean huntedDropOnly;

    private boolean placeInKillerInv;

    public boolean canClaimOwn;

    private double tax;

    public double minimumBounty;

    private DataStorageInterface dsi;

    public static Economy economy = null;

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null)
            economy = (Economy)economyProvider.getProvider();
        return (economy != null);
    }

    public DataStorageInterface getDsi() {
        return this.dsi;
    }

    public double getTax() {
        return this.tax;
    }

    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveConfig();
        this.allDeaths = getConfig().getDouble("dropSkulls.allDeaths", 0.0D);
        this.killedByPlayer = getConfig().getDouble("dropSkulls.killedByPlayer", 1.0D);
        this.placeInKillerInv = getConfig().getBoolean("dropSkulls.placeInKillerInv", false);
        this.tax = getConfig().getDouble("bounty.tax", 0.05D);
        this.minimumBounty = getConfig().getDouble("bounty.minimum", 10.0D);
        this.huntedDropOnly = getConfig().getBoolean("bounty.huntedDropOnly", false);
        this.canClaimOwn = getConfig().getBoolean("bounty.canClaimOwn", true);
        getServer().getPluginManager().registerEvents(this, (Plugin)this);
        getCommand("setname").setExecutor((CommandExecutor)new SetName());
        getCommand("clearname").setExecutor((CommandExecutor)new ClearName());
        getCommand("spawnhead").setExecutor((CommandExecutor)new SpawnHead(this));
        getCommand("bounty").setExecutor((CommandExecutor)new BountyCommand(this));
        if (getConfig().getBoolean("bounty.enabled")) {
            this.bounties = setupEconomy();
            if (this.bounties) {
                getLogger().info("Econ detected");
            } else {
                getLogger().info("Econ not detected");
            }
        }
        if (this.bounties)
            if (getConfig().getString("datastorage").equalsIgnoreCase("mysql")) {
                try {
                    this.dsi = (DataStorageInterface)new MySQLDataStorageImplementation(this, getConfig().getString("database.url"), getConfig().getString("database.username"), getConfig().getString("database.password"));
                } catch (SQLException e) {
                    getLogger().log(Level.SEVERE, "Error connecting to mysql database", e);
                    this.bounties = false;
                }
            } else if (getConfig().getString("datastorage").equalsIgnoreCase("yaml")) {
                try {
                    this.dsi = (DataStorageInterface)new YamlDataStorageImplementation(this);
                } catch (IOException e) {
                    getLogger().log(Level.SEVERE, "Error setting up yaml storage", e);
                    this.bounties = false;
                }
            }
        if (this.bounties) {
            getLogger().info("Bounties enabled");
        } else {
            getLogger().info("Bounties not enabled");
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Player k = p.getKiller();
        try {
            if (this.bounties && this.huntedDropOnly && this.dsi.getBounty(p.getName()) == null)
                return;
        } catch (DataStorageException e) {
            getLogger().log(Level.SEVERE, "Error getting if player has bounty", (Throwable)e);
        }
        if (p.hasPermission("headless.dropheads") && (this.allDeaths > Math.random() || (this.killedByPlayer > Math.random() && k != null)) && (k == null || (k != null && k.hasPermission("headless.collectheads")))) {
            ItemStack i = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
            SkullMeta meta = (SkullMeta)i.getItemMeta();
            meta.setOwner(event.getEntity().getName());
            i.setItemMeta((ItemMeta)meta);
            if (this.placeInKillerInv && k != null) {
                if (!k.getInventory().addItem(new ItemStack[] { i }).isEmpty())
                    k.getWorld().dropItem(k.getLocation(), i);
            } else {
                event.getDrops().add(i);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.bounties) {
            int unclaimedHeads = 0;
            try {
                unclaimedHeads = this.dsi.getNumUnclaimedHeads(event.getPlayer().getName());
            } catch (DataStorageException e) {
                getLogger().log(Level.SEVERE, "Error getting number of unclaimed heads", (Throwable)e);
            }
            if (unclaimedHeads > 0) {
                if (unclaimedHeads == 1) {
                    event.getPlayer().sendMessage(ChatColor.GOLD + "You have " + unclaimedHeads + " unclaimed head.");
                } else {
                    event.getPlayer().sendMessage(ChatColor.GOLD + "You have " + unclaimedHeads + " unclaimed heads.");
                }
                event.getPlayer().sendMessage(ChatColor.GOLD + "Type /bounty redeem to receive them");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getPlayer().hasPermission("headless.info") && event.getClickedBlock().getType() == Material.PLAYER_HEAD) {
            Skull s = (Skull)event.getClickedBlock().getState();
            if (s.hasOwner()) {
                event.getPlayer().sendMessage(ChatColor.GREEN + "The head of " + s.getOwner());
            } else {
                event.getPlayer().sendMessage(ChatColor.GREEN + "That head has no name attached");
            }
        }
    }
}

