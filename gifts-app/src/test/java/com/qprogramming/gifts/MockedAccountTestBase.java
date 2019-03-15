package com.qprogramming.gifts;


import com.qprogramming.gifts.account.Account;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;

public class MockedAccountTestBase {

    @Mock
    protected MockSecurityContext securityMock;
    @Mock
    protected Authentication authMock;

    protected Account testAccount;
    protected MockMvc mvc;


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        testAccount = TestUtil.createAccount();
        when(securityMock.getAuthentication()).thenReturn(authMock);
        when(authMock.getPrincipal()).thenReturn(testAccount);
        SecurityContextHolder.setContext(securityMock);
    }
}
