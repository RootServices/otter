package org.rootservices.otter.dispatch;

import helper.FixtureFactory;
import helper.entity.*;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.otter.config.OtterAppFactory;
import org.rootservices.otter.controller.entity.request.Request;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.dispatch.translator.AnswerTranslator;
import org.rootservices.otter.dispatch.translator.RequestTranslator;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.router.entity.Route;
import org.rootservices.otter.router.entity.io.Answer;
import org.rootservices.otter.router.entity.io.Ask;
import org.rootservices.otter.router.exception.HaltException;
import org.rootservices.otter.translator.JsonTranslator;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class RouteRunTest {
    private static OtterAppFactory otterAppFactory = new OtterAppFactory();
    private RouteRun<DummySession, DummyUser> subject;

    @Before
    public void setUp(){
        Route<DummySession, DummyUser> route = FixtureFactory.makeRoute();
        JsonTranslator<DummyPayload> jsonTranslator = otterAppFactory.jsonTranslator(DummyPayload.class);
        OkResourceLegacy okResource = new OkResourceLegacy(jsonTranslator);
        route.setResource(okResource);

        RequestTranslator<DummySession, DummyUser> requestTranslator = new RequestTranslator<DummySession, DummyUser>();
        AnswerTranslator<DummySession> answerTranslator = new AnswerTranslator<DummySession>();

        subject = new RouteRun<DummySession, DummyUser>(
                route, requestTranslator, answerTranslator
        );
    }

    @Test
    public void whenGetShouldReturnOk() throws Exception {
        testRun(Method.GET, StatusCode.OK, Optional.empty());
    }

    @Test
    public void whenPostShouldReturnOk() throws Exception {
        DummyPayload dummyPayload = new DummyPayload();
        dummyPayload.setInteger(123);
        byte[] body = otterAppFactory.objectWriter().writeValueAsBytes(dummyPayload);

        testRun(Method.POST, StatusCode.CREATED, Optional.of(body));
    }

    @Test
    public void whenPutShouldReturnOk() throws Exception {
        DummyPayload dummyPayload = new DummyPayload();
        dummyPayload.setInteger(123);
        byte[] body = otterAppFactory.objectWriter().writeValueAsBytes(dummyPayload);

        testRun(Method.PUT, StatusCode.OK, Optional.of(body));
    }

    @Test
    public void whenPatchShouldReturnOk() throws Exception {
        DummyPayload dummyPayload = new DummyPayload();
        dummyPayload.setInteger(123);
        byte[] body = otterAppFactory.objectWriter().writeValueAsBytes(dummyPayload);

        testRun(Method.PATCH, StatusCode.OK, Optional.of(body));
    }

    @Test
    public void whenDeletedShouldReturnOk() throws Exception {
        testRun(Method.DELETE, StatusCode.OK, Optional.empty());
    }

    @Test
    public void whenConnectShouldReturnOk() throws Exception {
        testRun(Method.CONNECT, StatusCode.OK, Optional.empty());
    }

    @Test
    public void whenOptionsShouldReturnOk() throws Exception {
        testRun(Method.OPTIONS, StatusCode.OK, Optional.empty());
    }

    @Test
    public void whenTraceShouldReturnOk() throws Exception {
        testRun(Method.TRACE, StatusCode.OK, Optional.empty());
    }

    @Test
    public void whenHeadShouldReturnOk() throws Exception {
        testRun(Method.HEAD, StatusCode.OK, Optional.empty());
    }

    public void testRun(Method method, StatusCode statusCode, Optional<byte[]> body) throws Exception {
        Ask ask = FixtureFactory.makeAsk();
        ask.setMethod(method);
        ask.setBody(body);
        Answer answer = FixtureFactory.makeAnswer();

        Answer actual = subject.run(ask, answer);

        assertThat(actual.getStatusCode(), is(statusCode));
    }

    @Test
    public void whenHaltExceptionShouldReturn403() throws Exception {

        Ask ask = FixtureFactory.makeAsk();
        Answer answer = FixtureFactory.makeAnswer();

        Request<DummySession, DummyUser> request = FixtureFactory.makeRequest();
        request.setMethod(Method.GET);

        // add a before that will throw halt.
        HaltBetween haltBetween = new HaltBetween();
        subject.getRoute().getBefore().add(haltBetween);

        HaltException actual = null;
        try {
            subject.run(ask, answer);
        } catch (HaltException e) {
            actual = e;
        }

        assertThat(actual, is(notNullValue()));
        assertThat(answer.getStatusCode(), is(StatusCode.UNAUTHORIZED));
    }
}