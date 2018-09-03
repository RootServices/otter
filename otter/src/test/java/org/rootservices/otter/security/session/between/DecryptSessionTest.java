package org.rootservices.otter.security.session.between;

import helper.FixtureFactory;
import helper.entity.DummySession;
import helper.entity.DummyUser;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.jwt.entity.jwk.SymmetricKey;
import org.rootservices.jwt.exception.InvalidJWT;
import org.rootservices.otter.config.OtterAppFactory;
import org.rootservices.otter.controller.entity.Cookie;
import org.rootservices.otter.controller.entity.Request;
import org.rootservices.otter.controller.entity.Response;
import org.rootservices.otter.router.exception.HaltException;
import org.rootservices.otter.security.session.between.exception.InvalidSessionException;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.security.session.between.exception.SessionDecryptException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class DecryptSessionTest {
    private static OtterAppFactory otterAppFactory = new OtterAppFactory();

    public DecryptSession<DummySession, DummyUser> subject(Boolean required) {
        return new DecryptDummySession(
                "session",
                otterAppFactory.jwtAppFactory(),
                FixtureFactory.encKey("1234"),
                FixtureFactory.encRotationKey("5678"),
                otterAppFactory.objectReader(),
                required
        );
    }

    @Test
    public void processWhenRequiredShouldBeOk() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.TRUE);

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        request.getCookies().put("session", sessionCookie);

        Response<DummySession> response = FixtureFactory.makeResponse();

        subject.process(Method.GET, request, response);

        DummySession actual = (DummySession) request.getSession().get();
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAccessToken(), is("123456789"));
        assertThat(actual.getRefreshToken(), is("101112131415"));
    }

    @Test
    public void processWhenRequiredAndNoSessionShouldHalt() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.TRUE);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        Response<DummySession> response = FixtureFactory.makeResponse();

        HaltException actual = null;
        try {
            subject.process(Method.GET, request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), is(nullValue()));
    }

    @Test
    public void processWhenRequiredAndInvalidSessionExceptionShouldHalt() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.TRUE);

        String encryptedSession = new StringBuilder()
                .append("notAJWE")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        request.getCookies().put("session", sessionCookie);

        Response<DummySession> response = FixtureFactory.makeResponse();

        HaltException actual = null;
        try {
            subject.process(Method.GET, request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), instanceOf(InvalidSessionException.class));
    }

    @Test
    public void processWhenRequiredAndSessionDecryptExceptionShouldHalt() throws Exception {
        // to force a Halt we will use a bad key.
        SymmetricKey veryBadKey = FixtureFactory.encKey("1234");
        veryBadKey.setKey("MMNj8rE5m7NIDhwKYDmHSnlU1wfKuVvW6G--1234567");

        DecryptSession<DummySession, DummyUser> subject = new DecryptDummySession(
                "session",
                otterAppFactory.jwtAppFactory(),
                veryBadKey,
                FixtureFactory.encRotationKey("5678"),
                otterAppFactory.objectReader(),
                Boolean.TRUE
        );

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        request.getCookies().put("session", sessionCookie);

        Response<DummySession> response = FixtureFactory.makeResponse();

        HaltException actual = null;
        try {
            subject.process(Method.GET, request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), instanceOf(SessionDecryptException.class));
    }


    @Test
    public void decryptWhenRequiredShouldBeOk() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.TRUE);

        String encryptedSession = new StringBuilder()
            .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
            .append(".")
            .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
            .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
            .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
            .toString();

        DummySession session = subject.decrypt(encryptedSession);

        assertThat(session, is(notNullValue()));
        assertThat(session.getAccessToken(), is("123456789"));
        assertThat(session.getRefreshToken(), is("101112131415"));
    }

    @Test
    public void decryptWhenRequiredAndJsonToJwtExceptionShouldThrowInvalidJWTException() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.TRUE);

        String encryptedSession = new StringBuilder()
                .append("notAJWE")
                .toString();

        InvalidSessionException actual = null;
        try {
            subject.decrypt(encryptedSession);
        } catch(InvalidSessionException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCause(), is(instanceOf(InvalidJWT.class)));
    }

    @Test
    public void processWhenNotRequiredShouldBeOk() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.FALSE);

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        request.getCookies().put("session", sessionCookie);

        Response<DummySession> response = FixtureFactory.makeResponse();

        subject.process(Method.GET, request, response);

        DummySession actual = (DummySession) request.getSession().get();
        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAccessToken(), is("123456789"));
        assertThat(actual.getRefreshToken(), is("101112131415"));
    }

    @Test
    public void processWhenNotRequiredAndNoSessionShouldBeOk() throws Exception {
        DecryptSession<DummySession, DummyUser> subject = subject(Boolean.FALSE);

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        Response<DummySession> response = FixtureFactory.makeResponse();

        subject.process(Method.GET, request, response);
        assertThat(request.getSession().isPresent(), is(false));
    }
}