package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppEventService {

    private AppEventRepo eventRepo;
    private AccountService accountService;

    @Autowired
    public AppEventService(AppEventRepo eventRepo, AccountService accountService) {
        this.eventRepo = eventRepo;
        this.accountService = accountService;
    }

    public List<AppEvent> findAllNotProcessed() {
        return eventRepo.findAll();
    }

    /**
     * Returns all events from database , grouping them per account
     *
     * @return Map of all events grouped per user
     */
    public Map<Account, List<AppEvent>> getEventsGroupedByAccount() {
        List<AppEvent> allNotProcessed = eventRepo.findAll();
        return allNotProcessed.stream().collect(Collectors.groupingBy(AppEvent::getAccount));
    }

    /**
     * Deletes all passed events , as they been processed by scheduler
     *
     * @param appEvents list of events to be deleted
     */
    public void processEvents(List<AppEvent> appEvents) {
        eventRepo.deleteAll(appEvents);
    }

    public void addEvent(Gift gift, AppEventType type) throws AccountNotFoundException {
        AppEvent event = new AppEvent();
        event.setAccount(accountService.findById(gift.getUserId()));
        event.setGift(gift);
        event.setType(type);
        eventRepo.save(event);
    }

    /**
     * Tries to find app event regarding this gift , and if it was not yet processed it will be deleted
     *
     * @param gift
     */
    public void tryToUndoEvent(Gift gift) throws AccountNotFoundException {
        AppEvent event = eventRepo.findByAccountAndGiftAndType(accountService.findById(gift.getUserId()), gift, AppEventType.REALISED);
        if (event != null) {
            eventRepo.delete(event);
        }
    }

    /**
     * Tries to find all app event regarding this gift
     *
     * @param gift gift for which all events will be deleted
     */
    public void deleteGiftEvents(Gift gift) throws AccountNotFoundException {
        List<AppEvent> events = eventRepo.findByAccountAndGift(accountService.findById(gift.getUserId()), gift);
        eventRepo.deleteAll(events);
    }

    /**
     * Deletes all events produced by passed account. Used while deleting account
     *
     * @param account account for which all events will be deleted
     */
    public void deleteUserEvents(Account account) {
        List<AppEvent> accountEvents = eventRepo.findByAccount(account);
        eventRepo.deleteAll(accountEvents);
    }
}
