package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.UUID;

/**
 * Created by Khobar on 05.03.2017.
 */

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountService implements UserDetailsService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private AccountRepository accountRepository;
    private PasswordEncoder passwordEncoder;
    private AvatarRepository avatarRepository;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder, AvatarRepository avatarRepository) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.avatarRepository = avatarRepository;
    }

    @PostConstruct
    protected void initialize() {
//        create(new Account("user", "demo", "ROLE_USER"));
//        create(new Account("admin", "admin", "ROLE_ADMIN"));
    }

    @Transactional
    public Account create(Account account) {
        String uuid = UUID.randomUUID().toString();
        while (accountRepository.findOneById(uuid) != null) {
            uuid = UUID.randomUUID().toString();
        }
        account.setId(uuid);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setRole(Roles.ROLE_USER);
        accountRepository.save(account);
        return account;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findOneByEmail(username);
        if (account == null) {
            account = accountRepository.findOneByUsername(username);
            if (account == null) {
                throw new UsernameNotFoundException("user not found");
            }
        }
        account.setAuthority(account.getRole());
        return account;
    }

    public void signin(Account account) {
        SecurityContextHolder.getContext().setAuthentication(authenticate(account));
    }

    private Authentication authenticate(Account account) {
        return new UsernamePasswordAuthenticationToken(account, null, Collections.singleton(createAuthority(account)));
    }

//    private Account createUser(Account account) {
//        return new User(account.getEmail(), account.getPassword(), Collections.singleton(createAuthority(account)));
//    }

    private GrantedAuthority createAuthority(Account account) {
        return new SimpleGrantedAuthority(account.getRole().toString());
    }

    public Account findById(String id) {
        return accountRepository.findOneById(id);
    }

    //User avatar handling

    public Avatar getAccountAvatar(Account account) {
        return avatarRepository.findOneById(account.getId());
    }

    public Avatar createAvatar(Account account, byte[] imgBytes) {
        Avatar avatar = new Avatar();
        avatar.setId(account.getId());
        avatar.setImage(imgBytes);
        return avatarRepository.save(avatar);
    }

    /**
     * Just save passed account
     *
     * @param account account to be saved
     * @return updated account
     */
    public Account update(Account account) {
        return accountRepository.save(account);
    }
}
