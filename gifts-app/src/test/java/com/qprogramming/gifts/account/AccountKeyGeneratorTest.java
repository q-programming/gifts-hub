package com.qprogramming.gifts.account;

import com.qprogramming.gifts.MockedAccountTestBase;
import com.qprogramming.gifts.account.group.Group;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static com.qprogramming.gifts.TestUtil.createAccount;
import static org.assertj.core.api.Assertions.assertThat;

public class AccountKeyGeneratorTest extends MockedAccountTestBase {

    private AccountKeyGenerator accountKeyGenerator;

    @Before
    public void setUp() {
        super.setup();
        accountKeyGenerator = new AccountKeyGenerator();
    }

    @Test
    public void testKeyGenerationAccountPassed() {
        val account = createAccount();
        val group = new Group();
        group.setId(1L);
        group.addMember(account);
        account.setGroups(Collections.singleton(group));
        val result = accountKeyGenerator.generate("", null, account);
        assertThat(result).isEqualTo(testAccount.getId() + "-" + group.getId());
    }

    @Test
    public void testKeyGenerationObjectPassed() {
        val result = accountKeyGenerator.generate("", null, "account");
        assertThat(result).isNull();
    }

}