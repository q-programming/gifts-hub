package com.qprogramming.gifts.account.group;

import com.qprogramming.gifts.account.Account;
import com.qprogramming.gifts.account.AccountType;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.exceptions.GroupNotAdminException;
import com.qprogramming.gifts.exceptions.GroupNotFoundException;
import com.qprogramming.gifts.support.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Khobar on 04.04.2017.
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final AccountEventRepository accountEventRepository;

    /**
     * Creates new group. After creation currently logged in user is added as member and administrator of group
     *
     * @return new {@link Group}
     */
    public Group createGroup(String name) {
        Group group = new Group();
        group.setName(name);
        return groupRepository.save(group);
    }

    /**
     * Adds account to group admins
     *
     * @param account account which will be added.
     * @param group   group to which account will be added as admin
     * @return updated {@link Group}
     */
    public Group addAccountToGroupAdmins(Account account, Group group) {
        group.getAdmins().add(account);
        return update(group);
    }

    /**
     * Adds account to group admins. If group won't be found null will be returned
     *
     * @param account account which will be added.
     * @param id      ID of group to which account will be added as admin
     * @return updated {@link Group}
     */
    public Group addAccountToGroupAdmins(Account account, Long id) {
        Optional<Group> familyOptional = groupRepository.findById(id);
        return familyOptional.map(family -> addAccountToGroupAdmins(account, family)).orElse(null);
    }

    /**
     * Removes from group .
     * If account was last member of it, group will be removed.Otherwise first found non kid account will be granted admin.
     * If no non kid accounts were found, group members will be cleared and group removed as well
     *
     * @param account account to be removed
     * @param group   group from which account is removed
     */
    public Group removeFromGroup(Account account, Group group) {
        group.removeMember(account);
        group.getAdmins().remove(account);
        if (group.getAdmins().isEmpty() && group.getMembers().isEmpty()) {
            delete(group);
            return null;
        } else if (group.getAdmins().isEmpty() && !group.getMembers().isEmpty()) {
            Optional<Account> first = group.getMembers().stream().filter(member -> !AccountType.KID.equals(member.getType())).findFirst();
            if (first.isPresent()) {
                group.getAdmins().add(first.get());
                //TODO add event about granting admin
            } else {
                group.getMembers().forEach(a -> a.getGroups().remove(group));
                group.setMembers(new HashSet<>());
                delete(group);
                return null;
            }
            return update(group);
        }
        return group;
    }

    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }

    /**
     * Returns currently logged in user group and checks if that user is admin in that group
     *
     * @return Group of currently logged in user
     * @throws GroupNotFoundException when current user is not part of any group
     * @throws GroupNotAdminException when current user is not administator of found group
     */
    public Group getGroupAsGroupAdmin(Long id) throws GroupNotFoundException, GroupNotAdminException {
        Account currentAccount = Utils.getCurrentAccount();
        Optional<Group> optionalGroup = getGroupById(id);
        if (!optionalGroup.isPresent()) {
            throw new GroupNotFoundException();
        }
        Group userGroup = optionalGroup.get();
        if (!userGroup.getAdmins().contains(currentAccount)) {
            throw new GroupNotAdminException();
        }
        return userGroup;
    }

    public Set<Group> findAllAccountGroups(Account account) {
        return groupRepository.findAllByMembers(account);
    }


    public List<Group> findAll() {
        return groupRepository.findAll(Sort.by("name"));
    }

    @CacheEvict(value = { "groups" }, allEntries = true)
    public Group update(Group group) {
        return groupRepository.save(group);
    }

    public void delete(Group group) {
        List<AccountEvent> accountEvents = accountEventRepository.findAllByGroup(group);
        accountEventRepository.deleteAll(accountEvents);
        groupRepository.delete(group);
    }


    public Group getGroupFromEvent(AccountEvent event) throws GroupNotFoundException {
        if (event.getGroup() == null) {
            throw new GroupNotFoundException();
        }
        Optional<Group> optionalGroup = getGroupById(event.getGroup().getId());
        return optionalGroup.orElseThrow(GroupNotFoundException::new);
    }
}
