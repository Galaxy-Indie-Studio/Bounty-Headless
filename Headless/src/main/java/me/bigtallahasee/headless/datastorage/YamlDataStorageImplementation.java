package me.bigtallahasee.headless.datastorage;

import me.bigtallahasee.headless.Bounty;
import me.bigtallahasee.headless.BountyHeadless;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class YamlDataStorageImplementation {
    private BountyHeadless plugin;

    private File configFile;

    private YamlConfiguration config;

    private int lastId;

    public YamlDataStorageImplementation(BountyHeadless plugin) throws IOException {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "bounties.yml");
        if (!this.configFile.exists() || this.configFile.isDirectory())
            this.configFile.createNewFile();
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        if (!this.config.isConfigurationSection("bounties"))
            this.config.createSection("bounties");
        this.lastId = 0;
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int value = Integer.valueOf(key).intValue();
                if (value < 0) {
                    this.plugin.getLogger().warning("Picked up an invalid id at bounties." + key);
                    continue;
                }
                if (value > this.lastId)
                    this.lastId = value;
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
    }

    private void saveConfig() throws DataStorageException {
        try {
            this.config.save(this.configFile);
        } catch (IOException e) {
            throw new DataStorageException(e);
        }
    }

    public int getNumBounties() throws DataStorageException {
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        int bounties = 0;
        for (String key : keys) {
            try {
                Integer.valueOf(key);
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (section.getString("hunter") == null)
                    bounties++;
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        return bounties;
    }

    public int getNumUnclaimedHeads(String issuer) throws DataStorageException {
        int count = 0;
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                Integer.valueOf(key);
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (issuer.equalsIgnoreCase(section.getString("issuer")) &&
                        section.getLong("turnedin") != 0L && section.getLong("redeemed") == 0L)
                    count++;
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        return count;
    }

    public List<Bounty> getUnclaimedBounties(String issuer) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (issuer.equalsIgnoreCase(section.getString("issuer")) &&
                        section.getLong("turnedin") != 0L && section.getLong("reedemed") == 0L)
                    bounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        return bounties;
    }

    public List<Bounty> getBounties(int min, int max) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        List<Bounty> allBounties = new ArrayList<Bounty>();
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (section.getString("hunter") == null)
                    allBounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        Collections.sort(allBounties, Collections.reverseOrder((Comparator<? super Bounty>)new Bounty.BountyRewardComparator()));
        if (allBounties.size() - 1 < max)
            max = allBounties.size() - 1;
        if (min > allBounties.size())
            return bounties;
        for (int i = min; i <= max; i++)
            bounties.add(allBounties.get(i));
        return bounties;
    }

    public List<Bounty> getBounties(String hunted) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (section.getString("hunted") != null && section.getString("hunted").matches(".*" + hunted + ".*") && section.getString("hunter") == null)
                    bounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        Collections.sort(bounties, (Comparator<? super Bounty>)new Bounty.BountyRewardComparator());
        return bounties;
    }

    public List<Bounty> getOwnBounties(String issuer) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (issuer.equalsIgnoreCase(section.getString("issuer")) && section.getString("hunter") == null)
                    bounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        return bounties;
    }

    public Bounty getBounty(String hunted) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (hunted.equalsIgnoreCase(section.getString("hunted")) && section.getString("hunter") == null)
                    bounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        Collections.sort(bounties, Collections.reverseOrder((Comparator<? super Bounty>)new Bounty.BountyRewardComparator()));
        return (bounties.size() == 0) ? null : bounties.get(0);
    }

    public Bounty getBounty(String hunted, String issuer) throws DataStorageException {
        List<Bounty> bounties = new ArrayList<Bounty>();
        Set<String> keys = this.config.getConfigurationSection("bounties").getKeys(false);
        for (String key : keys) {
            try {
                int keyValue = Integer.valueOf(key).intValue();
                ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(key);
                if (hunted.equalsIgnoreCase(section.getString("hunted")) && issuer.equalsIgnoreCase(section.getString("issuer")) && section.getString("hunter") == null)
                    bounties.add(Bounty.fromConfigurationSection(keyValue, section));
            } catch (NumberFormatException e) {
                this.plugin.getLogger().warning("Picked up an invalid key at bounties." + key);
            }
        }
        Collections.sort(bounties, Collections.reverseOrder((Comparator<? super Bounty>)new Bounty.BountyRewardComparator()));
        return (bounties.size() == 0) ? null : bounties.get(0);
    }

    public Bounty addBounty(Bounty bounty) throws DataStorageException {
        int newId = this.lastId + 1;
        this.config.getConfigurationSection("bounties").createSection(Integer.toString(newId));
        ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(newId));
        section.set("issuer", bounty.getIssuer());
        section.set("hunted", bounty.getHunted());
        section.set("reward", Double.valueOf(bounty.getReward()));
        this.lastId = newId;
        saveConfig();
        return new Bounty(newId, bounty.getIssuer(), bounty.getHunted(), bounty.getReward(), null, null, null, null);
    }

    public void updateBounty(Bounty bounty) throws DataStorageException {
        if (this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID())) == null)
            throw new DataStorageException("Tried to update a bounty whose id didn't exist!");
        ConfigurationSection section = this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID()));
        section.set("issuer", bounty.getIssuer());
        section.set("hunted", bounty.getHunted());
        section.set("reward", Double.valueOf(bounty.getReward()));
        section.set("created", Long.valueOf(bounty.getCreated().getTime()));
        section.set("hunter", bounty.getHunter());
        section.set("turnedin", Long.valueOf(bounty.getTurnedIn().getTime()));
        section.set("redeemed", Long.valueOf(bounty.getRedeemed().getTime()));
        saveConfig();
    }

    public void deleteBounty(Bounty bounty) throws DataStorageException {
        if (this.config.getConfigurationSection("bounties").getConfigurationSection(Integer.toString(bounty.getID())) == null)
            throw new DataStorageException("Tried to delete a bounty whose id didn't exist!");
        this.config.set("bounties." + Integer.toString(bounty.getID()), null);
        if (bounty.getID() == this.lastId)
            this.lastId--;
        saveConfig();
    }
}

