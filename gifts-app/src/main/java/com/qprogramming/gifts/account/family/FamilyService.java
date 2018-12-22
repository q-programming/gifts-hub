package com.qprogramming.gifts.account.family;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.exceptions.FamilyNotAdminException;
import com.qprogramming.gifts.exceptions.FamilyNotFoundException;
import com.qprogramming.gifts.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Created by Khobar on 04.04.2017.
 */
@Service
public class FamilyService {

    private FamilyRepository familyRepository;
    private AccountEventRepository accountEventRepository;

    @Autowired
    public FamilyService(FamilyRepository familyRepository, AccountEventRepository accountEventRepository) {
        this.familyRepository = familyRepository;
        this.accountEventRepository = accountEventRepository;
    }

    /**
     * Creates new family. After creation currently logged in user is added as member and administrator of family
     *
     * @return new {@link Family}
     */
    public Family createFamily() {
        Family family = new Family();
        family = familyRepository.save(family);
        Account currentAccount = Utils.getCurrentAccount();
        family = addAccountToFamily(currentAccount, family);
        return addAccountToFamilyAdmins(currentAccount, family);
    }

    /**
     * Adds account to family
     *
     * @param account account which will be added.
     * @param family  family to which account will be added as member
     * @return updated {@link Family}
     */
    public Family addAccountToFamily(Account account, Family family) {
        Optional<Family> optionalFamily = familyRepository.findByMembersContaining(account);
        if (!optionalFamily.isPresent()) {
            family.getMembers().add(account);
        }
        return familyRepository.save(family);
    }

    /**
     * Adds account to family admins
     *
     * @param account account which will be added.
     * @param family  family to which account will be added as admin
     * @return updated {@link Family}
     */
    public Family addAccountToFamilyAdmins(Account account, Family family) {
        family.getAdmins().add(account);
        return familyRepository.save(family);
    }

    /**
     * Adds account to family admins. If family won't be found null will be returned
     *
     * @param account account which will be added.
     * @param id      ID of family to which account will be added as admin
     * @return updated {@link Family}
     */
    public Family addAccountToFamilyAdmins(Account account, Long id) {
        Optional<Family> familyOptional = familyRepository.findById(id);
        return familyOptional.map(family -> addAccountToFamilyAdmins(account, family)).orElse(null);
    }

    /**
     * Adds account to family. If family won't be found null will be returned
     *
     * @param account account which will be added.
     * @param id      ID of family to which account will be added as member
     * @return updated {@link Family}
     */
    public Family addAccountToFamily(Account account, Long id) {
        Optional<Family> familyOptional = familyRepository.findById(id);
        return familyOptional.map(family -> addAccountToFamily(account, family)).orElse(null);
    }

    /**
     * Removes from family .
     * If account was last member of it, family will be removed.Otherwise first found non kid account will be granted admin.
     * If no non kid accounts were found, family members will be cleared and family removed as well
     *
     * @param account
     * @param family
     */
    public Family removeFromFamily(Account account, Family family) {
        family.getMembers().remove(account);
        family.getAdmins().remove(account);
        if (family.getAdmins().isEmpty() && family.getMembers().isEmpty()) {
            delete(family);
            return null;
        } else if (family.getAdmins().isEmpty() && !family.getMembers().isEmpty()) {
            Optional<Account> first = family.getMembers().stream().filter(member -> !AccountType.KID.equals(member.getType())).findFirst();
            if (first.isPresent()) {
                family.getAdmins().add(first.get());
                //TODO add event about granting admin
            } else {
                family.setMembers(new HashSet<>());
                delete(family);
                return null;
            }
            return update(family);
        }
        return family;
    }

    /**
     * Returns Family where passed account is member
     *
     * @param account account for which family will be returned
     * @return {@link Family}
     */
    public Optional<Family> getFamily(Account account) {
        return familyRepository.findByMembersContaining(account);
    }

    public Optional<Family> getFamilyById(Long id) {
        return familyRepository.findById(id);
    }

    /**
     * Returns currently logged in user family and checks if that user is admin in that family
     *
     * @return Family of currently logged in user
     * @throws FamilyNotFoundException when current user is not part of any family
     * @throws FamilyNotAdminException when current user is not administator of found family
     */
    public Family getFamilyAsFamilyAdmin() throws FamilyNotFoundException, FamilyNotAdminException {
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Family> optionalFamily = getFamily(currentAccount);
        if (!optionalFamily.isPresent()) {
            throw new FamilyNotFoundException();
        }
        Family userFamily = optionalFamily.get();
        if (!userFamily.getAdmins().contains(currentAccount)) {
            throw new FamilyNotAdminException();
        }
        return userFamily;
    }


    public List<Family> findAll() {
        return familyRepository.findAll(Sort.by("name"));
    }

    public Family update(Family family) {
        return familyRepository.save(family);
    }

    public void delete(Family family) {
        List<AccountEvent> accountEvents = accountEventRepository.findAllByFamily(family);
        accountEventRepository.deleteAll(accountEvents);
        familyRepository.delete(family);
    }

    public AccountEvent inviteAccount(Account account, Family family, AccountEventType type) {
        AccountEvent event = new AccountEvent();
        event.setAccount(account);
        event.setFamily(family);
        event.setType(type);
        event.setToken(generateToken());
        return accountEventRepository.save(event);
    }

    public String generateToken() {
        String token = Generators.timeBasedGenerator().generate().toString();
        while (accountEventRepository.findByToken(token).isPresent()) {
            token = Generators.timeBasedGenerator().generate().toString();
        }
        return token;
    }
}
