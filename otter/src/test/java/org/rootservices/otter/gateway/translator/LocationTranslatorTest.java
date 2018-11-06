package org.rootservices.otter.gateway.translator;

import helper.FixtureFactory;
import helper.entity.DummyPayload;
import helper.entity.DummySession;
import helper.entity.DummyUser;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rootservices.otter.controller.entity.EmptyPayload;
import org.rootservices.otter.dispatch.HtmlRouteRun;
import org.rootservices.otter.gateway.entity.target.Target;
import org.rootservices.otter.router.entity.Location;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.router.factory.BetweenFactory;
import org.rootservices.otter.security.builder.entity.Betweens;

import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LocationTranslatorTest {
    private LocationTranslator<DummySession, DummyUser, EmptyPayload> subject;
    @Mock
    private BetweenFactory<DummySession, DummyUser, EmptyPayload> mockBetweenFactory;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        subject = new LocationTranslator<DummySession, DummyUser, EmptyPayload>(mockBetweenFactory);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void toShouldBeOk() {
        Betweens<DummySession, DummyUser, EmptyPayload> betweens = FixtureFactory.makeBetweens();
        when(mockBetweenFactory.make(any(), any())).thenReturn(betweens);

        Target<DummySession, DummyUser, EmptyPayload> target = FixtureFactory.makeTarget();

        Map<Method, Location> actual =  subject.to(target);

        assertThat(actual.size(), is(2));

        // GET
        assertThat(actual.get(Method.GET).getRouteRunner(), Is.is(notNullValue()));

        assertThat(actual.get(Method.GET).getPattern(), Is.is(notNullValue()));
        assertThat(actual.get(Method.GET).getContentTypes(), Is.is(notNullValue()));
        assertThat(actual.get(Method.GET).getContentTypes().size(), Is.is(1));

        assertThat(actual.get(Method.GET).getErrorRouteRunners(), Is.is(notNullValue()));
        assertThat(actual.get(Method.GET).getErrorRouteRunners().size(), Is.is(1));

        HtmlRouteRun<DummySession, DummyUser, EmptyPayload> getRouteRunner = (HtmlRouteRun<DummySession, DummyUser, EmptyPayload>) actual.get(Method.GET).getRouteRunner();

        // ordering of before.
        assertThat(getRouteRunner.getRoute().getBefore().get(0), is(betweens.getBefore().get(0)));
        assertThat(getRouteRunner.getRoute().getBefore().get(1), is(target.getBefore().get(0)));
        assertThat(getRouteRunner.getRoute().getBefore().get(2), is(target.getBefore().get(1)));

        // ordering of after
        assertThat(getRouteRunner.getRoute().getAfter().get(0), is(betweens.getAfter().get(0)));
        assertThat(getRouteRunner.getRoute().getAfter().get(1), is(target.getAfter().get(0)));
        assertThat(getRouteRunner.getRoute().getAfter().get(2), is(target.getAfter().get(1)));

        // POST
        assertThat(actual.get(Method.POST).getRouteRunner(), Is.is(notNullValue()));

        assertThat(actual.get(Method.POST).getPattern(), Is.is(notNullValue()));
        assertThat(actual.get(Method.POST).getContentTypes(), Is.is(notNullValue()));
        assertThat(actual.get(Method.POST).getContentTypes().size(), Is.is(1));

        assertThat(actual.get(Method.POST).getErrorRouteRunners(), Is.is(notNullValue()));
        assertThat(actual.get(Method.POST).getErrorRouteRunners().size(), Is.is(1));

        HtmlRouteRun<DummySession, DummyUser, EmptyPayload> postRouteRunner = (HtmlRouteRun<DummySession, DummyUser, EmptyPayload>) actual.get(Method.POST).getRouteRunner();

        // ordering of before.
        assertThat(postRouteRunner.getRoute().getBefore().get(0), is(betweens.getBefore().get(0)));
        assertThat(postRouteRunner.getRoute().getBefore().get(1), is(target.getBefore().get(0)));
        assertThat(postRouteRunner.getRoute().getBefore().get(2), is(target.getBefore().get(1)));

        // ordering of after.
        assertThat(postRouteRunner.getRoute().getAfter().get(0), is(betweens.getAfter().get(0)));
        assertThat(postRouteRunner.getRoute().getAfter().get(1), is(target.getAfter().get(0)));
        assertThat(postRouteRunner.getRoute().getAfter().get(2), is(target.getAfter().get(1)));
    }
}