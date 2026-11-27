package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.authority.AuthorityRepository;
import com.qprogramming.gifts.account.authority.AuthorityService;
import com.qprogramming.gifts.account.avatar.AvatarRepository;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.account.event.AccountEventRepository;
import com.qprogramming.gifts.account.event.AccountEventType;
import com.qprogramming.gifts.account.group.GroupService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.gift.GiftService;
import org.junit.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Verifies that converting a kid through Google, which is called straight from the controller, evicts cached accounts
 */
public class AccountServiceCacheTest {

    @Test
    public void convertKidAccountWithGoogleEvictsAccountsAndGroupsCaches() {
        AccountEventRepository eventRepository = mock(AccountEventRepository.class);
        AccountRepository accountRepository = mock(AccountRepository.class);
        Account kid = new Account();
        kid.setType(AccountType.KID);
        AccountEvent event = new AccountEvent();
        event.setAccount(kid);
        event.setType(AccountEventType.KID_CONVERT);
        event.setEmail("kid@test.com");
        String token = com.fasterxml.uuid.Generators.timeBasedGenerator().generate().toString();
        event.setToken(token);
        when(eventRepository.findByToken(token)).thenReturn(Optional.of(event));
        when(accountRepository.save(org.mockito.ArgumentMatchers.any(Account.class))).then(returnsFirstArg());

        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.registerBean(AccountEventRepository.class, () -> eventRepository);
            context.registerBean(AccountRepository.class, () -> accountRepository);
            context.register(CacheConfig.class);
            context.refresh();
            CacheManager cacheManager = context.getBean(CacheManager.class);
            cacheManager.getCache("accounts").put("kid", kid);
            cacheManager.getCache("groups").put("kid", kid);

            context.getBean(AccountService.class).convertKidAccountWithGoogle(token, "kid@test.com");

            assertThat(cacheManager.getCache("accounts").get("kid")).isNull();
            assertThat(cacheManager.getCache("groups").get("kid")).isNull();
        }
    }

    @Configuration
    @EnableCaching(proxyTargetClass = true)
    static class CacheConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("accounts", "groups");
        }

        @Bean
        KeyGenerator accountKeyGenerator() {
            return (target, method, params) -> "key";
        }

        @Bean
        AccountService accountService(AccountRepository accountRepository, AccountEventRepository eventRepository) {
            return new AccountService(accountRepository, mock(AccountPasswordEncoder.class), mock(AvatarRepository.class),
                    mock(GroupService.class), mock(PropertyService.class), eventRepository, mock(GiftService.class),
                    new AuthorityService(mock(AuthorityRepository.class)));
        }
    }
}
