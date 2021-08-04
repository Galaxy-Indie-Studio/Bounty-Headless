package me.bigtallahasee.headless.datastorage;


import me.bigtallahasee.headless.Bounty;

import java.util.List;

public interface DataStorageInterface {
    int getNumBounties() throws DataStorageException;

    int getNumUnclaimedHeads(String paramString) throws DataStorageException;

    List<Bounty> getUnclaimedBounties(String paramString) throws DataStorageException;

    List<Bounty> getBounties(int paramInt1, int paramInt2) throws DataStorageException;

    List<Bounty> getBounties(String paramString) throws DataStorageException;

    List<Bounty> getOwnBounties(String paramString) throws DataStorageException;

    Bounty getBounty(String paramString) throws DataStorageException;

    Bounty getBounty(String paramString1, String paramString2) throws DataStorageException;

    Bounty addBounty(Bounty paramBounty) throws DataStorageException;

    void updateBounty(Bounty paramBounty) throws DataStorageException;

    void deleteBounty(Bounty paramBounty) throws DataStorageException;
}

