package net.tokensmith.otter.security.csrf.between;

import helper.FixtureFactory;
import helper.entity.model.DummySession;
import helper.entity.model.DummyUser;
import net.tokensmith.jwt.entity.jwt.JsonWebToken;
import net.tokensmith.otter.config.CookieConfig;
import net.tokensmith.otter.controller.entity.Cookie;
import net.tokensmith.otter.controller.entity.request.Request;
import net.tokensmith.otter.controller.entity.response.Response;
import net.tokensmith.otter.router.entity.Method;
import net.tokensmith.otter.security.csrf.CsrfClaims;
import net.tokensmith.otter.security.csrf.DoubleSubmitCSRF;
import net.tokensmith.otter.security.csrf.between.html.PrepareCSRF;
import net.tokensmith.otter.security.csrf.exception.CsrfException;
import net.tokensmith.otter.security.entity.ChallengeToken;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;



public class PrepareCSRFTest {
    private static String COOKIE_NAME = "CSRF";
    @Mock
    private DoubleSubmitCSRF mockDoubleSubmitCSRF;
    private PrepareCSRF<DummySession, DummyUser> subject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        CookieConfig cookieConfig = new CookieConfig(COOKIE_NAME, false, -1, true);
        subject = new PrepareCSRF<DummySession, DummyUser>(cookieConfig, mockDoubleSubmitCSRF);
    }

    @Test
    public void processShouldSetCookie() throws Exception {
        String challengeToken = "challenge-token";

        when(mockDoubleSubmitCSRF.makeChallengeToken()).thenReturn(challengeToken);
        Cookie cookie = FixtureFactory.makeCookie(COOKIE_NAME);
        when(mockDoubleSubmitCSRF.makeCsrfCookie(eq(COOKIE_NAME), any(), eq(false), eq(-1), eq(true))).thenReturn(cookie);

        ByteArrayOutputStream formValueJwt = new ByteArrayOutputStream();
        formValueJwt.write("formValueJwt".getBytes());

        when(mockDoubleSubmitCSRF.toJwt(any())).thenReturn(formValueJwt);
        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        Response<DummySession> response = FixtureFactory.makeResponse();

        subject.process(Method.GET, request, response);

        assertThat(response.getCookies().get(COOKIE_NAME), is(notNullValue()));
        assertThat(response.getCookies().get(COOKIE_NAME), is(cookie));
        assertThat(request.getCsrfChallenge().isPresent(), is(true));
        assertThat(request.getCsrfChallenge().get(), is("formValueJwt"));

        verify(mockDoubleSubmitCSRF, times(3)).makeChallengeToken();
        verify(mockDoubleSubmitCSRF).makeCsrfCookie(eq(COOKIE_NAME), any(), eq(false), eq(-1), eq(true));
    }

    @Test
    public void processWhenCookieAlreadyThereShouldNotSetCookie() throws Exception {
        String challengeToken = "challenge-token";
        Cookie cookie = FixtureFactory.makeCookie(COOKIE_NAME);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        Response<DummySession> response = FixtureFactory.makeResponse();
        response.getCookies().put(COOKIE_NAME, cookie);

        // set up the csrf cookie value which is a jwt.
        CsrfClaims csrfClaims = new CsrfClaims();
        csrfClaims.setChallengeToken(challengeToken);
        csrfClaims.setNoise("cookie-noise");
        JsonWebToken<CsrfClaims> csrfJwt = new JsonWebToken<CsrfClaims>();
        csrfJwt.setClaims(csrfClaims);

        // create the value to assign to the request.
        when(mockDoubleSubmitCSRF.csrfToJwt(cookie.getValue())).thenReturn(csrfJwt);
        ByteArrayOutputStream formValueJwt = new ByteArrayOutputStream();
        formValueJwt.write("formValueJwt".getBytes());
        when(mockDoubleSubmitCSRF.makeChallengeToken()).thenReturn("form-noise");
        ArgumentCaptor<ChallengeToken> inputsForRequestJwt = ArgumentCaptor.forClass(ChallengeToken.class);
        when(mockDoubleSubmitCSRF.toJwt(inputsForRequestJwt.capture())).thenReturn(formValueJwt);

        subject.process(Method.GET, request, response);

        assertThat(response.getCookies().get(COOKIE_NAME), is(notNullValue()));
        assertThat(response.getCookies().get(COOKIE_NAME), is(cookie));
        assertThat(request.getCsrfChallenge(), is(notNullValue()));
        assertThat(request.getCsrfChallenge().isPresent(), is(true));
        assertThat(request.getCsrfChallenge().get(), is("formValueJwt"));

        verify(mockDoubleSubmitCSRF, times(1)).makeChallengeToken();
        verify(mockDoubleSubmitCSRF, never()).makeCsrfCookie(eq(COOKIE_NAME), any(), eq(false), eq(-1), eq(true));

        // inputs to make the request's jwt.
        assertThat(inputsForRequestJwt.getValue().getNoise(), is("form-noise"));
        assertThat(inputsForRequestJwt.getValue().getToken(), is(notNullValue()));
        assertThat(inputsForRequestJwt.getValue().getToken(), is(challengeToken));
    }

    @Test
    public void processWhenCsrfExceptionShouldNotSetCookie() throws Exception {
        String challengeToken = "challenge-token";
        when(mockDoubleSubmitCSRF.makeChallengeToken()).thenReturn(challengeToken);

        CsrfException csrfException = new CsrfException("", null);
        when(mockDoubleSubmitCSRF.makeCsrfCookie(eq(COOKIE_NAME), any(), eq(false), eq(-1), eq(true))).thenThrow(csrfException);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        Response<DummySession> response = FixtureFactory.makeResponse();

        subject.process(Method.GET, request, response);

        assertThat(response.getCookies().get(COOKIE_NAME), is(nullValue()));

        verify(mockDoubleSubmitCSRF, times(3)).makeChallengeToken();
        verify(mockDoubleSubmitCSRF).makeCsrfCookie(eq(COOKIE_NAME), any(), eq(false), eq(-1), eq(true));
    }
}