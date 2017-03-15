package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.avatar.Avatar;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.MediaType;
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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Khobar on 05.03.2017.
 */

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class AccountService implements UserDetailsService {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

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
        generateID(account);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setRole(Roles.ROLE_USER);
        account.setType(AccountType.LOCAL);
        accountRepository.save(account);
        return account;
    }

    private void generateID(Account account) {
        String uuid = UUID.randomUUID().toString();
        while (accountRepository.findOneById(uuid) != null) {
            uuid = UUID.randomUUID().toString();
        }
        account.setId(uuid);
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

    public Avatar createAvatar(Account account) {
        ClassLoader loader = this.getClass().getClassLoader();
        byte[] imgBytes;
        try (InputStream avatarFile = loader.getResourceAsStream("static/images/logo-white.png")) {
            imgBytes = IOUtils.toByteArray(avatarFile);
            return avatarRepository.save(createAvatar(account, imgBytes));
        } catch (IOException e) {
            LOG.error("Failed to get avatar from resources");
        }
        return null;
    }

    public Avatar createAvatar(Account account, String url) throws MalformedURLException {
        byte[] bytes = downloadFromUrl(new URL(url));
        return createAvatar(account, bytes);
    }


    /**
     * Creates avatar from bytes
     *
     * @param account
     * @param bytes
     * @return
     * @throws IOException
     */
    public Avatar createAvatar(Account account, byte[] bytes) {
        Avatar avatar = new Avatar();
        avatar.setId(account.getId());
        avatar.setImage(bytes);
        String type;
        try {
            type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            LOG.error("Failed to determine type from bytes, presuming jpg");
            type = MediaType.IMAGE_JPEG_VALUE;
        }
        avatar.setType(type);
        return avatarRepository.save(avatar);
    }

    private byte[] downloadFromUrl(URL url) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream stream = url.openStream()) {
            byte[] chunk = new byte[4096];
            int bytesRead;
            while ((bytesRead = stream.read(chunk)) > 0) {
                outputStream.write(chunk, 0, bytesRead);
            }
        } catch (IOException e) {
            LOG.error("Failed to download from URL ");
            return null;
        }
        return outputStream.toByteArray();
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

    public Account findByUsername(String username) {
        return accountRepository.findOneByUsername(username);

    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }
}
