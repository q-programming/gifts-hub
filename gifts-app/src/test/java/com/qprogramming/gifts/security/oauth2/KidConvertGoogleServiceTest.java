package com.qprogramming.gifts.security.oauth2;

import com.qprogramming.gifts.account.AccountService;
import com.qprogramming.gifts.account.event.AccountEvent;
import com.qprogramming.gifts.config.mail.MailService;
import com.qprogramming.gifts.config.property.PropertyService;
import com.qprogramming.gifts.security.TokenService;
import com.qprogramming.gifts.settings.Settings;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

import javax.servlet.http.Cookie;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class KidConvertGoogleServiceTest {

    private static final String LANDING = "http://localhost/gifts/#/convert/tok";

    private AccountService accountService;
    private TokenService tokenService;
    private ClientRegistrationRepository registrations;
    private KidConvertGoogleService service;
    private MockHttpServletRequest request;

    @Before
    public void setUp() {
        accountService = mock(AccountService.class);
        tokenService = mock(TokenService.class);
        registrations = mock(ClientRegistrationRepository.class);
        PropertyService propertyService = mock(PropertyService.class);
        when(propertyService.getProperty(Settings.APP_URL)).thenReturn("https://q-programming.pl/gifts");
        service = new KidConvertGoogleService(registrations, tokenService, accountService, mock(MailService.class), propertyService);
        request = new MockHttpServletRequest();
        request.setServerName("localhost");
    }

    private void cookies(String token, String redirect) {
        when(tokenService.getCookie(any(), anyString())).thenAnswer(i -> {
            String name = i.getArgument(1);
            String value = "kid_convert_redirect".equals(name) ? redirect : token;
            return value == null ? Optional.empty() : Optional.of(new Cookie(name, value));
        });
    }

    @Test
    public void redirectAddsResultToHashRoute() {
        assertThat(service.redirect("http://h/gifts/#/convert/tok", "converted", "true")).isEqualTo("http://h/gifts/#/convert/tok?converted=true");
        assertThat(service.redirect("http://h/#/convert/tok?a=b", "error", "x")).isEqualTo("http://h/#/convert/tok?a=b&error=x");
        assertThat(service.redirect("http://h/login", "error", "x")).isEqualTo("http://h/login?error=x");
    }

    @Test
    public void startWithInvalidInviteReturnsToLandingPageWithError() throws Exception {
        when(accountService.findValidKidConvertEvent("bad")).thenReturn(Optional.empty());
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.start("bad", LANDING, request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo(LANDING + "?error=kid_convert_invalid");
        verify(tokenService, never()).addCookie(any(), anyString(), anyString(), anyInt());
    }

    @Test
    public void startRejectsForeignReturnAddress() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.start("tok", "https://evil.example/#/convert/tok", request, response);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getRedirectedUrl()).isNull();
        verifyNoInteractions(accountService);
    }

    @Test
    public void startWithValidInviteStoresCookiesAndRedirectsToGoogle() throws Exception {
        when(accountService.findValidKidConvertEvent("tok")).thenReturn(Optional.of(new AccountEvent()));
        when(registrations.findByRegistrationId("google")).thenReturn(ClientRegistration.withRegistrationId("google")
                .clientId("cid").authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).redirectUri("{baseUrl}/x")
                .scope("email", "profile").authorizationUri("https://accounts.google.com/auth").tokenUri("https://t").build());
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.start("tok", LANDING, request, response);

        assertThat(response.getRedirectedUrl()).startsWith("https://accounts.google.com/auth?")
                .contains("client_id=cid", "state=tok", "oauth2/kid-convert/callback");
        verify(tokenService).addCookie(response, "kid_convert_token", "tok", 600);
        verify(tokenService).addCookie(response, "kid_convert_redirect", LANDING, 600);
    }

    @Test
    public void completeWithStateNotMatchingInviteIsRejected() throws Exception {
        cookies("tok", LANDING);
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.complete("code", "other", request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo(LANDING + "?error=kid_convert_invalid");
        verify(accountService, never()).convertKidAccountWithGoogle(anyString(), any());
        verify(tokenService).deleteCookie(any(), any(), eq("kid_convert_token"));
    }

    @Test
    public void completeWithoutCookiesOrCodeIsRejected() throws Exception {
        cookies(null, null);
        MockHttpServletResponse response = new MockHttpServletResponse();
        service.complete("code", "tok", request, response);
        assertThat(response.getRedirectedUrl()).isEqualTo("/?error=kid_convert_invalid");

        cookies("tok", LANDING);
        response = new MockHttpServletResponse();
        service.complete(null, "tok", request, response);
        assertThat(response.getRedirectedUrl()).isEqualTo(LANDING + "?error=kid_convert_invalid");
        verify(accountService, never()).convertKidAccountWithGoogle(anyString(), any());
    }

    @Test
    public void completeIgnoresForeignReturnAddressFromCookie() throws Exception {
        cookies("tok", "https://evil.example/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        service.complete("code", "other", request, response);

        assertThat(response.getRedirectedUrl()).isEqualTo("/?error=kid_convert_invalid");
    }

    @Test
    public void localReturnAddressMatchesApplicationUrlOrRequestHost() {
        assertThat(service.isLocalRedirect("https://q-programming.pl/gifts/#/convert/x", request)).isTrue();
        assertThat(service.isLocalRedirect("http://localhost:4200/#/convert/x", request)).isTrue();
        assertThat(service.isLocalRedirect("https://evil.example/#/x", request)).isFalse();
        assertThat(service.isLocalRedirect("https://q-programming.pl.evil.example/", request)).isFalse();
        assertThat(service.isLocalRedirect("not a uri", request)).isFalse();
        assertThat(service.isLocalRedirect(null, request)).isFalse();
    }
}
