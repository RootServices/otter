package net.tokensmith.otter.gateway;

import helper.FixtureFactory;
import helper.entity.model.DummySession;
import helper.entity.model.DummyUser;
import org.junit.Before;
import org.junit.Test;
import net.tokensmith.otter.controller.Resource;
import net.tokensmith.otter.controller.entity.StatusCode;
import net.tokensmith.otter.gateway.entity.ErrorTarget;
import net.tokensmith.otter.gateway.entity.Shape;
import net.tokensmith.otter.gateway.translator.LocationTranslator;

import java.util.HashMap;
import java.util.Map;
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
        Map<StatusCode, Resource<DummySession, DummyUser>> errorResources = new HashMap<>();
        Map<StatusCode, ErrorTarget<DummySession, DummyUser>> dispatchErrors = new HashMap<>();
        Map<StatusCode, ErrorTarget<DummySession, DummyUser>> defaultDispatchErrors = new HashMap<>();
        LocationTranslator<DummySession, DummyUser> actual = subject.make(
                DummySession.class,
                Optional.empty(),
                Optional.empty(),
                errorResources,
                dispatchErrors,
                defaultDispatchErrors
        );

        assertThat(actual, is(notNullValue()));
    }
}