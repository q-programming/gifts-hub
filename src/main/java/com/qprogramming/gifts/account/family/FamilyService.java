package com.qprogramming.gifts.account.family;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.support.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Khobar on 04.04.2017.
 */
@Service
public class FamilyService {

    private FamilyRepository familyRepository;

    @Autowired
    public FamilyService(FamilyRepository familyRepository) {
        this.familyRepository = familyRepository;
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
     * Returns Family where passed account is member
     *
     * @param account account for which family will be returned
     * @return {@link Family}
     */
    public Family getFamily(Account account) {
        return familyRepository.findByMembersContaining(account);
    }


    public List<Family> findAll() {
        return familyRepository.findAll();
    }

    public Family update(Family family) {
        return familyRepository.save(family);
    }
}
