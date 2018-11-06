package org.rootservices.otter.gateway;

import helper.FixtureFactory;
import helper.entity.DummySession;
import helper.entity.DummyUser;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.otter.controller.entity.EmptyPayload;
import org.rootservices.otter.gateway.entity.Shape;
import org.rootservices.otter.gateway.translator.LocationTranslator;

import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.*;

public class LocationTranslatorFactoryTest {
    private LocationTranslatorFactory subject;

    @Before
    public void setUp() throws Exception {
        Shape shape = FixtureFactory.makeShape("test-enc-key", "test-sign-key");
        subject = new LocationTranslatorFactory(shape);
    }

    @Test
    public void shouldMakeLocationTranslator() throws Exception {
        LocationTranslator<DummySession, DummyUser, EmptyPayload> actual = subject.make(DummySession.class, Optional.empty(), Optional.empty());

        assertThat(actual, is(notNullValue()));
    }
}