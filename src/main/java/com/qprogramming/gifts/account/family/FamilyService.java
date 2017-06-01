package com.qprogramming.gifts.account.family;

import com.fasterxml.uuid.Generators;
import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    private FamilyEventRepository familyEventRepository;

    @Autowired
    public FamilyService(FamilyRepository familyRepository, FamilyEventRepository familyEventRepository) {
        this.familyRepository = familyRepository;
        this.familyEventRepository = familyEventRepository;
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
        family.getMembers().add(account);
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
        Family family = familyRepository.findById(id);
        if (family != null) {
            return addAccountToFamilyAdmins(account, family);
        }
        return null;
    }

    /**
     * Adds account to family. If family won't be found null will be returned
     *
     * @param account account which will be added.
     * @param id      ID of family to which account will be added as member
     * @return updated {@link Family}
     */
    public Family addAccountToFamily(Account account, Long id) {
        Family family = familyRepository.findById(id);
        if (family != null) {
            return addAccountToFamily(account, family);
        }
        return null;
    }

    /**
     * Removes from family .
     * If account was last member of it, family will be removed.Otherwise first found non kid account will be granted admin.
     * If no non kid accounts were found, family members will be cleared and family removed as well
     *
     * @param account
     * @param family
     */
    public void removeFromFamily(Account account, Family family) {
        family.getMembers().remove(account);
        family.getAdmins().remove(account);
        if (family.getAdmins().isEmpty() && family.getMembers().isEmpty()) {
            delete(family);
        } else if (family.getAdmins().isEmpty() && !family.getMembers().isEmpty()) {
            Optional<Account> first = family.getMembers().stream().filter(member -> !AccountType.KID.equals(member.getType())).findFirst();
            if (first.isPresent()) {
                family.getAdmins().add(first.get());
                //TODO add event about granting admin
            } else {
                family.setMembers(new HashSet<>());
                delete(family);
            }
        }
    }

    /**
     * Returns Family where passed account is member
     *
     * @param account account for which family will be returned
     * @return {@link Family}
     */
    public Family getFamily(Account account) {
        return familyRepository.findByMembersContaining(account);
    }


    public List<Family> findAll() {
        return familyRepository.findAll(new Sort("name"));
    }

    public Family update(Family family) {
        return familyRepository.save(family);
    }

    public void delete(Family family) {
        familyRepository.delete(family);
    }

    public FamilyEvent inviteAccount(Account account, Family family, FamilyEventType type) {
        FamilyEvent event = new FamilyEvent();
        event.setAccount(account);
        event.setFamily(family);
        event.setType(type);
        event.setUuid(Generators.timeBasedGenerator().generate().toString());
        return familyEventRepository.save(event);
    }
}
