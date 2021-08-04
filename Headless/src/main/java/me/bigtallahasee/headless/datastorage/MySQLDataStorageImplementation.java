package me.bigtallahasee.headless.datastorage;

import me.bigtallahasee.headless.Bounty;
import me.bigtallahasee.headless.BountyHeadless;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLDataStorageImplementation implements DataStorageInterface{
    BountyHeadless plugin;

    private Connection connection;

    public MySQLDataStorageImplementation(BountyHeadless bountyHeadless, String url, String username, String password) throws SQLException {
        this.plugin = bountyHeadless;
        this.connection = DriverManager.getConnection(url, username, password);
        ResultSet bansExists = this.connection.getMetaData().getTables(null, null, "bounties", null);
        if (!bansExists.first()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(this.plugin.getResource("mysql.sql")));
            StringBuilder builder = new StringBuilder();
            try {
                String next;
                while ((next = reader.readLine()) != null)
                    builder.append(next);
                getFreshPreparedStatementColdFromTheRefrigerator(builder.toString()).execute();
            } catch (IOException e) {
                throw new SQLException("Could not load default table creation text", e);
            }
        }
    }

    private PreparedStatement getFreshPreparedStatementColdFromTheRefrigerator(String query) throws SQLException {
        return this.connection.prepareStatement(query);
    }

    private PreparedStatement getFreshPreparedStatementWithGeneratedKeys(String query) throws SQLException {
        return this.connection.prepareStatement(query, 1);
    }

    public int getNumBounties() throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT count(*) FROM bounties WHERE hunter IS NULL");
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public ArrayList<Bounty> getBounties(int min, int max) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunter IS NULL ORDER BY bounties.reward DESC LIMIT " + min + "," + max);
            ResultSet rs = ps.executeQuery();
            ArrayList<Bounty> bounties = new ArrayList<Bounty>();
            while (rs.next())
                bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
            return bounties;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public Bounty getBounty(String hunted) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC LIMIT 1");
            ps.setString(1, hunted);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
            return null;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public Bounty addBounty(Bounty bounty) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementWithGeneratedKeys("INSERT INTO bounties (issuer, hunted, reward) VALUES (?,?,?)");
            ps.setString(1, bounty.getIssuer());
            ps.setString(2, bounty.getHunted());
            ps.setDouble(3, bounty.getReward());
            ps.execute();
            ResultSet rs = ps.getGeneratedKeys();
            rs.next();
            ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE id = ?");
            ps.setInt(1, rs.getInt(1));
            rs = ps.executeQuery();
            rs.next();
            return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public void updateBounty(Bounty bounty) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("UPDATE bounties SET issuer = ?, hunted = ?, reward = ?, created = ?, hunter = ?, turnedin = ?, redeemed = ? WHERE id = ?");
            ps.setString(1, bounty.getIssuer());
            ps.setString(2, bounty.getHunted());
            ps.setDouble(3, bounty.getReward());
            ps.setTimestamp(4, new Timestamp(bounty.getCreated().getTime()));
            if (bounty.getHunter() == null) {
                ps.setNull(5, 12);
            } else {
                ps.setString(5, bounty.getHunter());
            }
            if (bounty.getTurnedIn() == null) {
                ps.setNull(6, 93);
            } else {
                ps.setTimestamp(6, new Timestamp(bounty.getTurnedIn().getTime()));
            }
            if (bounty.getRedeemed() == null) {
                ps.setNull(7, 93);
            } else {
                ps.setTimestamp(7, new Timestamp(bounty.getRedeemed().getTime()));
            }
            ps.setInt(8, bounty.getID());
            ps.execute();
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public void deleteBounty(Bounty bounty) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("DELETE FROM bounties WHERE id = ?");
            ps.setInt(1, bounty.getID());
            ps.execute();
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public List<Bounty> getBounties(String hunted) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND hunter IS NULL ORDER BY bounties.hunted ASC");
            ps.setString(1, "%" + hunted + "%");
            ResultSet rs = ps.executeQuery();
            ArrayList<Bounty> bounties = new ArrayList<Bounty>();
            while (rs.next())
                bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
            return bounties;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public Bounty getBounty(String hunted, String issuer) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE hunted LIKE ? AND ISSUER LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC LIMIT 1");
            ps.setString(1, hunted);
            ps.setString(2, issuer);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8));
            return null;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public List<Bounty> getOwnBounties(String issuer) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE issuer LIKE ? AND hunter IS NULL ORDER BY bounties.reward DESC");
            ps.setString(1, issuer);
            ResultSet rs = ps.executeQuery();
            ArrayList<Bounty> bounties = new ArrayList<Bounty>();
            while (rs.next())
                bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
            return bounties;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public int getNumUnclaimedHeads(String issuer) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT count(*) FROM bounties WHERE issuer LIKE ? AND turnedin IS NOT NULL AND redeemed IS NULL");
            ps.setString(1, issuer);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }

    public ArrayList<Bounty> getUnclaimedBounties(String issuer) throws DataStorageException {
        try {
            PreparedStatement ps = getFreshPreparedStatementColdFromTheRefrigerator("SELECT * FROM bounties WHERE issuer LIKE ? AND turnedin IS NOT NULL AND redeemed IS NULL");
            ps.setString(1, issuer);
            ResultSet rs = ps.executeQuery();
            ArrayList<Bounty> bounties = new ArrayList<Bounty>();
            while (rs.next())
                bounties.add(new Bounty(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getTimestamp(5), rs.getString(6), rs.getTimestamp(7), rs.getTimestamp(8)));
            return bounties;
        } catch (SQLException e) {
            throw new DataStorageException(e);
        }
    }
}

