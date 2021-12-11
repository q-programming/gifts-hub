package com.qprogramming.gifts.account.authority;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Created by Jakub Romaniszyn on 20.07.2018.
 */
@Service
@RequiredArgsConstructor
public class AuthorityService {

    private final AuthorityRepository authorityRepository;

    /**
     * Find by role name. If authority was not yet created , create it and then return it
     *
     * @param role Searched role
     * @return Authority with that name ,or new freshly created one
     */
    public Authority findByRole(Role role) {
        Authority authority = authorityRepository.findByName(role);
        if (authority == null) {
            authority = new Authority();
            authority.setName(role);
            authority = authorityRepository.save(authority);
        }
        return authority;
    }
}
