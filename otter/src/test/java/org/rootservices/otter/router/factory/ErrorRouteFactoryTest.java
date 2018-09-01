package org.rootservices.otter.router.factory;

import helper.FixtureFactory;
import helper.entity.DummySession;
import helper.entity.DummyUser;
import org.junit.Before;
import org.junit.Test;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.router.entity.Coordinate;
import org.rootservices.otter.router.entity.MatchedCoordinate;
import org.rootservices.otter.router.entity.Route;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class ErrorRouteFactoryTest {
    private ErrorRouteFactory<DummySession, DummyUser> subject;

    @Before
    public void setUp() {
        subject = new ErrorRouteFactory<>();
    }

    @Test
    public void fromCoordianteShouldBeCoordinateUnSupportedErrorRoute() {
        Coordinate<DummySession, DummyUser> coordinate = FixtureFactory.makeCoordinateWithErrorRoutes("foo");

        Optional<MatchedCoordinate<DummySession, DummyUser>> match = Optional.of(
                new MatchedCoordinate<DummySession, DummyUser>(coordinate)
        );

        Map<StatusCode, Route<DummySession, DummyUser>> errorRoutes = FixtureFactory.makeErrorRoutes();

        Route<DummySession, DummyUser> actual = subject.fromCoordiante(match, errorRoutes);

        assertThat(actual, is(coordinate.getErrorRoutes().get(StatusCode.UNSUPPORTED_MEDIA_TYPE)));

    }

    @Test
    public void fromCoordianteShouldBeGlobalUnSupportedErrorRoute() {
        Coordinate<DummySession, DummyUser> coordinate = FixtureFactory.makeCoordinate("foo");

        Optional<MatchedCoordinate<DummySession, DummyUser>> match = Optional.of(
                new MatchedCoordinate<DummySession, DummyUser>(coordinate)
        );

        Map<StatusCode, Route<DummySession, DummyUser>> errorRoutes = FixtureFactory.makeErrorRoutes();

        Route<DummySession, DummyUser> actual = subject.fromCoordiante(match, errorRoutes);

        assertThat(actual, is(errorRoutes.get(StatusCode.UNSUPPORTED_MEDIA_TYPE)));

    }

    @Test
    public void fromCoordianteShouldBeGlobalNotFoundErrorRoute() {
        Optional<MatchedCoordinate<DummySession, DummyUser>> match = Optional.empty();
        Map<StatusCode, Route<DummySession, DummyUser>> errorRoutes = FixtureFactory.makeErrorRoutes();

        Route<DummySession, DummyUser> actual = subject.fromCoordiante(match, errorRoutes);

        assertThat(actual, is(errorRoutes.get(StatusCode.NOT_FOUND)));
    }
}