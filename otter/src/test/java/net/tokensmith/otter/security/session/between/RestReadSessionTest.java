package net.tokensmith.otter.security.session.between;

import com.fasterxml.jackson.databind.ObjectReader;
import helper.FixtureFactory;
import helper.entity.model.DummyPayload;
import helper.entity.model.DummySession;
import helper.entity.model.DummyUser;
import net.tokensmith.jwt.config.JwtAppFactory;
import net.tokensmith.otter.controller.entity.Cookie;
import net.tokensmith.otter.controller.entity.StatusCode;
import net.tokensmith.otter.controller.entity.request.Request;
import net.tokensmith.otter.controller.entity.request.RestRequest;
import net.tokensmith.otter.controller.entity.response.Response;
import net.tokensmith.otter.controller.entity.response.RestResponse;
import net.tokensmith.otter.dispatch.entity.RestBtwnRequest;
import net.tokensmith.otter.dispatch.entity.RestBtwnResponse;
import net.tokensmith.otter.gateway.entity.Shape;
import net.tokensmith.otter.router.entity.Method;
import net.tokensmith.otter.router.exception.HaltException;
import net.tokensmith.otter.security.session.between.util.Decrypt;
import net.tokensmith.otter.translator.config.TranslatorAppFactory;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.*;

public class RestReadSessionTest {

    public RestReadSession<DummySession, DummyUser> subject(Boolean required) {
        // this is some reverse engineer foo from BetweenBuilder.
        Shape shape = FixtureFactory.makeShape("1234", "5678");
        ObjectReader sessionObjectReader = new TranslatorAppFactory().objectReader().forType(DummySession.class);
        Decrypt<DummySession> decrypt = new Decrypt<>(new JwtAppFactory(), sessionObjectReader, shape.getEncKey(), shape.getRotationEncKeys());

        return new RestReadSession<>("session", required, shape.getSessionFailStatusCode(), decrypt);
    }

    @Test
    public void processsWhenRequiredShouldBeOk() throws Exception {
        RestReadSession<DummySession, DummyUser> subject = subject(true);

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        RestBtwnRequest<DummyUser> request = FixtureFactory.makeRestBtwnRequest();
        request.getCookies().put("session", sessionCookie);

        RestBtwnResponse response = FixtureFactory.makeRestBtwnResponse();

        subject.process(Method.GET, request, response);

        // default method sets this so it can do a simple walk through test.
        assertThat(response.getStatusCode(), is(StatusCode.ACCEPTED));
    }

    @Test
    public void readSessionWhenRequiredShouldBeOk() throws Exception {
        RestReadSession<DummySession, DummyUser> subject = subject(true);

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        RestBtwnRequest<DummyUser> request = FixtureFactory.makeRestBtwnRequest();
        request.getCookies().put("session", sessionCookie);

        RestBtwnResponse response = FixtureFactory.makeRestBtwnResponse();

        Optional<DummySession> actual = subject.readSession(request, response);

        assertThat(actual.isPresent(), is(true));
        assertThat(actual.get().getAccessToken(), is("123456789"));
        assertThat(actual.get().getRefreshToken(), is("101112131415"));
    }

    @Test
    public void readSessionWhenRequiredAndNoSessionThenThrowHalt() throws Exception {
        RestReadSession<DummySession, DummyUser> subject = subject(true);

        RestBtwnRequest<DummyUser> request = FixtureFactory.makeRestBtwnRequest();
        RestBtwnResponse response = FixtureFactory.makeRestBtwnResponse();

        HaltException actual = null;
        try {
            subject.readSession(request, response);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(response.getStatusCode(), is(StatusCode.UNAUTHORIZED));
    }

    @Test
    public void readSessionWhenNotRequiredShouldBeOk() throws Exception {
        RestReadSession<DummySession, DummyUser> subject = subject(false);

        String encryptedSession = new StringBuilder()
                .append("eyJhbGciOiJkaXIiLCJraWQiOiIxMjM0IiwiZW5jIjoiQTI1NkdDTSJ9.")
                .append(".")
                .append("AkRUVwJboJnzM5Pt0uqK-Ju15_YSn8x0DCrxDcKUszdQei2Fa7hYxENHJytWK1iMfl4lmcMb-fVTCnUC_bBa1abfeJ1NWWzRNwPEc-zhXvFV2-255lJe8EZYSSwE7cDf.")
                .append("pvvpZcAtxSFpzjqmgJEjh6oJLAoRAWv9WAQJ6BY08TDLpqZATSP4f4RPLMc8g7ArdMIJQI2coRBDjSg.")
                .append("Z4eCgEJ-RIfWX1jKYeP5Bw")
                .toString();

        Cookie sessionCookie = FixtureFactory.makeCookie("session");
        sessionCookie.setValue(encryptedSession);

        RestBtwnRequest<DummyUser> request = FixtureFactory.makeRestBtwnRequest();
        request.getCookies().put("session", sessionCookie);

        RestBtwnResponse response = FixtureFactory.makeRestBtwnResponse();

        Optional<DummySession> actual = subject.readSession(request, response);

        assertThat(actual.isPresent(), is(true));
        assertThat(actual.get().getAccessToken(), is("123456789"));
        assertThat(actual.get().getRefreshToken(), is("101112131415"));
    }

    @Test
    public void readSessionWhenNotRequiredAndNoSessionShouldBeOk() throws Exception {
        RestReadSession<DummySession, DummyUser> subject = subject(false);

        RestBtwnRequest<DummyUser> request = FixtureFactory.makeRestBtwnRequest();
        RestBtwnResponse response = FixtureFactory.makeRestBtwnResponse();

        Optional<DummySession> actual = subject.readSession(request, response);

        assertThat(actual.isPresent(), is(false));
    }
}