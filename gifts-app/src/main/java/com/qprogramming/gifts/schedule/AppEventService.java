package com.qprogramming.gifts.schedule;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.exceptions.AccountNotFoundException;
import com.qprogramming.gifts.gift.Gift;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppEventService {

    private final AppEventRepo eventRepo;
    private final AccountService accountService;

    List<AppEvent> findAllNotProcessed() {
        return eventRepo.findAll();
    }

    /**
     * Returns all events from database , grouping them per account
     *
     * @return Map of all events grouped per user
     */
    public Map<Account, List<AppEvent>> getEventsGroupedByAccount() {
        List<AppEvent> allNotProcessed = eventRepo.findAll();
        return allNotProcessed
                .stream()
                .collect(Collectors.groupingBy(AppEvent::getAccount));
    }

    /**
     * Deletes all passed events , as they been processed by scheduler
     */
    public void processEvents() {
        List<AppEvent> all = eventRepo.findAll();
        eventRepo.deleteAll(all);
    }

    public void addEvent(Gift gift, AppEventType type) throws AccountNotFoundException {
        AppEvent event = new AppEvent();
        event.setAccount(accountService.findById(gift.getUserId()));
        if (gift.getCreatedBy() != null) {
            event.setCreatedBy(accountService.findById(gift.getCreatedBy()));
        }
        event.setGift(gift);
        event.setType(type);
        eventRepo.save(event);
    }

    /**
     * Tries to find app event regarding this gift , and if it was not yet processed it will be deleted
     *
     * @param gift gift for which app events will be searched for
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
