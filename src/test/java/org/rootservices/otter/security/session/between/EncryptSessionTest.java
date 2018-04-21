package org.rootservices.otter.security.session.between;

import helper.FixtureFactory;
import helper.entity.DummySession;
import integration.app.hello.security.TokenSession;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.suppliers.TestedOn;
import org.rootservices.jwt.config.JwtAppFactory;
import org.rootservices.jwt.entity.jwk.SymmetricKey;
import org.rootservices.otter.config.CookieConfig;
import org.rootservices.otter.config.OtterAppFactory;
import org.rootservices.otter.controller.entity.Cookie;
import org.rootservices.otter.controller.entity.Request;
import org.rootservices.otter.controller.entity.Response;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.router.exception.HaltException;
import org.rootservices.otter.security.session.between.exception.EncryptSessionException;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class EncryptSessionTest {
    private OtterAppFactory otterAppFactory = new OtterAppFactory();
    private EncryptSession subject;

    @Before
    public void setUp() {
        CookieConfig cookieConfig = new CookieConfig("session", true, -1);
        subject = new EncryptSession(
                cookieConfig,
                new JwtAppFactory(),
                otterAppFactory.urlDecoder(),
                FixtureFactory.encKey("1234"),
                otterAppFactory.objectMapper()
        );
    }

    @Test
    public void processShouldSetSession() throws Exception {
        DummySession requestSession = new DummySession();
        requestSession.setAccessToken("123456789");
        requestSession.setRefreshToken("101112131415");

        Request request = FixtureFactory.makeRequest();
        request.setSession(Optional.of(requestSession));

        DummySession responseSession = new DummySession(requestSession);
        Response response = FixtureFactory.makeResponse();
        response.setSession(Optional.of(responseSession));

        // change the response session so it will re-encrypt.
        ((DummySession) response.getSession().get()).setAccessToken("1617181920");

        subject.process(Method.GET, request, response);

        assertThat(response.getSession().isPresent(), is(true));

        Cookie actual = response.getCookies().get("session");

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getName(), is("session"));
        assertThat(actual.getMaxAge(), is(-1));
        assertThat(actual.isSecure(), is(true));
        assertThat(actual.getValue(), is(notNullValue()));
    }

    @Test
    public void processWhenEncryptSessionExceptionShouldHalt() throws Exception {

        // force Halt with a bad key to encrypt with.
        SymmetricKey veryBadKey = FixtureFactory.encKey("1234");
        veryBadKey.setKey("MMNj8rE5m7NIDhwKYDmHSnlU1wfKuVvW6G--");

        CookieConfig cookieConfig = new CookieConfig("session", true, -1);
        EncryptSession subject = new EncryptSession(
                cookieConfig,
                new JwtAppFactory(),
                otterAppFactory.urlDecoder(),
                veryBadKey,
                otterAppFactory.objectMapper()
        );

        DummySession session = new DummySession();
        session.setAccessToken("123456789");
        session.setRefreshToken("101112131415");

        Request request = FixtureFactory.makeRequest();
        Response response = FixtureFactory.makeResponse();
        response.setSession(Optional.of(session));

        HaltException actual = null;
        try {
            subject.process(Method.GET, request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), instanceOf(EncryptSessionException.class));

        assertThat(response.getSession().isPresent(), is(true));
        assertThat(response.getCookies().get("session"), is(nullValue()));
    }

    @Test
    public void processWhenSessionEmptyShouldHalt() throws Exception {
        DummySession session = new DummySession();
        session.setAccessToken("123456789");
        session.setRefreshToken("101112131415");

        Request request = FixtureFactory.makeRequest();
        Response response = FixtureFactory.makeResponse();

        assertThat(response.getSession().isPresent(), is(false));

        HaltException actual = null;
        try {
            subject.process(Method.GET, request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), is(nullValue()));

        assertThat(response.getSession().isPresent(), is(false));
        assertThat(response.getCookies().get("session"), is(nullValue()));
    }

    @Test
    public void encryptShouldBeOk() throws Exception {
        DummySession session = new DummySession();
        session.setAccessToken("123456789");
        session.setRefreshToken("101112131415");

        ByteArrayOutputStream actual = subject.encrypt(session);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.toString().split("\\.").length, is(5));
    }

    @Test
    public void setPreferredKey() {
        CookieConfig cookieConfig = new CookieConfig("session", true, -1);
        EncryptSession subject = new EncryptSession(
                cookieConfig,
                new JwtAppFactory(),
                otterAppFactory.urlDecoder(),
                FixtureFactory.encKey("1234"),
                otterAppFactory.objectMapper()
        );

        SymmetricKey encKey = FixtureFactory.encKey("1000");
        subject.setPreferredKey(encKey);

        SymmetricKey actual = subject.getPreferredKey();
        assertThat(actual, is(encKey));
    }

    @Test
    public void setCookieConfig() {
        CookieConfig cookieConfig = new CookieConfig("session", true, -1);
        EncryptSession subject = new EncryptSession(
                cookieConfig,
                new JwtAppFactory(),
                otterAppFactory.urlDecoder(),
                FixtureFactory.encKey("1234"),
                otterAppFactory.objectMapper()
        );

        CookieConfig sessionCookieConfig = new CookieConfig("session_store", true, -1);
        subject.setCookieConfig(sessionCookieConfig);

        CookieConfig actual = subject.getCookieConfig();
        assertThat(actual, is(sessionCookieConfig));
    }
}