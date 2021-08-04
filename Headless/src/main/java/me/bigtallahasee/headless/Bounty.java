package me.bigtallahasee.headless;

import org.bukkit.configuration.ConfigurationSection;

import java.security.Timestamp;
import java.util.Comparator;
import java.util.Date;

public class Bounty {
    private int ID;

    private String issuer;

    private String hunted;

    private double reward;

    private Date created;

    private String hunter;

    private Date turnedIn;

    private Date redeemed;

    public void setID(int iD) {
        this.ID = iD;
    }

    public Bounty(int ID, String issuer, String hunted, double reward, Date created, String hunter, Date turnedIn, Date redeemed) {
        this.ID = ID;
        this.issuer = issuer;
        this.hunted = hunted;
        this.reward = reward;
        this.created = created;
        this.hunter = hunter;
        this.turnedIn = turnedIn;
        this.redeemed = redeemed;
    }

    public Bounty(String issuer, String hunted, double reward) {
        this.issuer = issuer;
        this.hunted = hunted;
        this.reward = reward;
    }

    public static Bounty fromConfigurationSection(int id, ConfigurationSection section) {
        return new Bounty(id, section.getString("issuer"), section.getString("hunted"), section.getDouble("reward"), new Date(section.getLong("created")), section.getString("hunter"), new Date(section.getLong("turnedIn")), new Date(section.getLong("redeemed")));
    }

    public double getReward() {
        return this.reward;
    }

    public String getHunted() {
        return this.hunted;
    }

    public String getIssuer() {
        return this.issuer;
    }

    public void setHunted(String hunted) {
        this.hunted = hunted;
    }

    public void setHunter(String hunter) {
        this.hunter = hunter;
    }

    public void setTurnedIn(Date turnedIn) {
        this.turnedIn = turnedIn;
    }

    public void setRedeemed(Date redeemed) {
        this.redeemed = redeemed;
    }

    public int getID() {
        return this.ID;
    }

    public Date getCreated() {
        return this.created;
    }

    public void setReward(double reward) {
        this.reward = reward;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getHunter() {
        return this.hunter;
    }

    public Date getTurnedIn() {
        return this.turnedIn;
    }

    public Date getRedeemed() {
        return this.redeemed;
    }

    public static class BountyRewardComparator implements Comparator<Bounty> {
        public int compare(Bounty o1, Bounty o2) {
            return (o1.getReward() < o2.getReward()) ? -1 : ((o1.getReward() > o2.getReward()) ? 1 : 0);
        }
    }
}

