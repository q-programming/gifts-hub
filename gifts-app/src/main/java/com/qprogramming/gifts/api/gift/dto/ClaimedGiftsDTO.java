package com.qprogramming.gifts.api.gift.dto;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.gift.Gift;

import java.util.List;
import java.util.Map;

public class ClaimedGiftsDTO {

    public ClaimedGiftsDTO() {
    }

    public ClaimedGiftsDTO(List<Account> accounts, Map<String, List<Gift>> claimedGifts) {
        this.accounts = accounts;
        this.claimedGifts = claimedGifts;
    }

    private List<Account> accounts;
    private Map<String, List<Gift>> claimedGifts;

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public Map<String, List<Gift>> getClaimedGifts() {
        return claimedGifts;
    }

    public void setClaimedGifts(Map<String, List<Gift>> claimedGifts) {
        this.claimedGifts = claimedGifts;
    }
}
