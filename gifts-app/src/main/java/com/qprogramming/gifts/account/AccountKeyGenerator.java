package com.qprogramming.gifts.account;

import com.qprogramming.gifts.account.group.Group;
import com.qprogramming.gifts.support.Utils;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.stream.Collectors;

@Component
public class AccountKeyGenerator implements KeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        if (params[0] instanceof Account) {
            String currentAccountId = Utils.getCurrentAccountId();
            Account account = (Account) params[0];
            return currentAccountId + account.getGroups()
                    .stream()
                    .sorted(Comparator.comparing(Group::getId))
                    .map(group -> "-" + group.getId())
                    .collect(Collectors.joining());
        }
        return null;
    }
}
