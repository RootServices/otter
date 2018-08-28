package org.rootservices.otter.gateway.servlet;

import helper.FixtureFactory;
import helper.entity.DummySession;
import helper.entity.DummyUser;
import helper.entity.FakeResource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.rootservices.otter.controller.Resource;
import org.rootservices.otter.controller.entity.Request;
import org.rootservices.otter.controller.entity.Response;
import org.rootservices.otter.gateway.servlet.merger.HttpServletRequestMerger;
import org.rootservices.otter.gateway.servlet.merger.HttpServletResponseMerger;
import org.rootservices.otter.gateway.servlet.translator.HttpServletRequestTranslator;
import org.rootservices.otter.router.Dispatcher;
import org.rootservices.otter.router.Engine;
import org.rootservices.otter.router.builder.CoordinateBuilder;
import org.rootservices.otter.router.entity.Between;
import org.rootservices.otter.router.entity.Coordinate;
import org.rootservices.otter.router.entity.Method;
import org.rootservices.otter.router.entity.Route;
import org.rootservices.otter.router.exception.NotFoundException;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class ServletGatewayTest {
    @Mock
    private HttpServletRequestTranslator<DummySession, DummyUser> mockHttpServletRequestTranslator;
    @Mock
    private HttpServletRequestMerger mockHttpServletRequestMerger;
    @Mock
    private HttpServletResponseMerger<DummySession> mockHttpServletResponseMerger;
    @Mock
    private Engine<DummySession, DummyUser> mockEngine;
    @Mock
    private Dispatcher<DummySession, DummyUser> mockDispatcher;
    @Mock
    private Between<DummySession, DummyUser> mockPrepareCSRF;
    @Mock
    private Between<DummySession, DummyUser> mockCheckCSRF;

    private ServletGateway<DummySession, DummyUser> subject;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(mockEngine.getDispatcher()).thenReturn(mockDispatcher);
        subject = new ServletGateway<DummySession, DummyUser>(
                mockHttpServletRequestTranslator,
                mockHttpServletRequestMerger,
                mockHttpServletResponseMerger,
                mockEngine,
                mockPrepareCSRF,
                mockCheckCSRF
        );
    }

    @Test
    public void processRequestResourceFoundShouldBeOk() throws Exception {
        HttpServletRequest mockContainerRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockContainerResponse = mock(HttpServletResponse.class);
        byte[] containerBody = null;

        Request<DummySession, DummyUser> request = new Request<DummySession, DummyUser>();

        when(mockHttpServletRequestTranslator.from(mockContainerRequest, containerBody))
                .thenReturn(request);

        Response<DummySession> resourceResponse = FixtureFactory.makeResponse();

        when(mockEngine.route(eq(request), any())).thenReturn(resourceResponse);

        subject.processRequest(mockContainerRequest, mockContainerResponse, containerBody);

        // should never call the not found resource.
        verify(mockEngine, never()).executeResourceMethod(any(), any(), any());

        verify(mockHttpServletResponseMerger).merge(mockContainerResponse, null, resourceResponse);
        verify(mockHttpServletRequestMerger).merge(mockContainerRequest, resourceResponse);
    }

    @Test
    public void processRequestResourceNotFoundShouldExecuteNotFound() throws Exception {
        Route<DummySession, DummyUser> notFoundRoute = FixtureFactory.makeRoute();
        subject.setNotFoundRoute(notFoundRoute);

        HttpServletRequest mockContainerRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockContainerResponse = mock(HttpServletResponse.class);
        byte[] containerBody = null;

        Request<DummySession, DummyUser> request = new Request<DummySession, DummyUser>();

        when(mockHttpServletRequestTranslator.from(mockContainerRequest, containerBody))
                .thenReturn(request);

        // original engine call does NOT return a response.
        NotFoundException nfe = new NotFoundException("");
        doThrow(nfe).when(mockEngine).route(eq(request), any());

        Response<DummySession> resourceResponse = FixtureFactory.makeResponse();
        when(mockEngine.executeResourceMethod(
                eq(notFoundRoute),
                eq(request),
                any()
        )).thenReturn(resourceResponse);

        subject.processRequest(mockContainerRequest, mockContainerResponse, containerBody);

        // should call the not found resource.
        verify(mockEngine).executeResourceMethod(
                eq(notFoundRoute),
                eq(request),
                any()
        );

        verify(mockHttpServletResponseMerger).merge(mockContainerResponse, null, resourceResponse);
        verify(mockHttpServletRequestMerger).merge(mockContainerRequest, resourceResponse);

    }

    @Test
    public void processRequestWhenExceptionShouldReturnServerError() throws Exception {
        Route<DummySession, DummyUser> notFoundRoute = FixtureFactory.makeRoute();
        subject.setNotFoundRoute(notFoundRoute);

        HttpServletRequest mockContainerRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockContainerResponse = mock(HttpServletResponse.class);
        byte[] containerBody = null;

        doThrow(new RuntimeException()).when(mockHttpServletRequestTranslator).from(mockContainerRequest, containerBody);

        GatewayResponse actual = subject.processRequest(mockContainerRequest, mockContainerResponse, containerBody);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getPayload(), is(notNullValue()));
        assertThat(actual.getPayload().isPresent(), is(false));
        assertThat(actual.getTemplate(), is(notNullValue()));
        assertThat(actual.getTemplate().isPresent(), is(false));

        verify(mockContainerResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void processRequestWhenIOExceptionShouldReturnServerError() throws Exception {
        Route<DummySession, DummyUser> notFoundRoute = FixtureFactory.makeRoute();
        subject.setNotFoundRoute(notFoundRoute);

        HttpServletRequest mockContainerRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockContainerResponse = mock(HttpServletResponse.class);
        byte[] containerBody = null;

        doThrow(new IOException()).when(mockHttpServletRequestTranslator).from(mockContainerRequest, containerBody);

        GatewayResponse actual = subject.processRequest(mockContainerRequest, mockContainerResponse, containerBody);

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getPayload(), is(notNullValue()));
        assertThat(actual.getPayload().isPresent(), is(false));
        assertThat(actual.getTemplate(), is(notNullValue()));
        assertThat(actual.getTemplate().isPresent(), is(false));

        verify(mockContainerResponse).setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    @Test
    public void getShouldAddRouteWithEmptyBeforeAfter() {

        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.GET)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.get("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void getCsrfProtectShouldAddRouteWithCsrfBeforeEmptyAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.GET)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.getCsrfProtect("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(1));
        assertThat(coordinates.get(0).getRoute().getBefore().get(0), is(mockPrepareCSRF));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));

    }

    @Test
    public void postShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.POST)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.post("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void postCsrfProtectShouldAddRouteWithCsrfBeforeEmptyAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.POST)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.postCsrfProtect("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(1));
        assertThat(coordinates.get(0).getRoute().getBefore().get(0), is(mockCheckCSRF));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void putShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.PUT)).thenReturn(coordinates);

        Resource<DummySession, DummyUser> resource = new FakeResource();
        subject.put("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));
    }

    @Test
    public void patchShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.PATCH)).thenReturn(coordinates);

        Resource<DummySession, DummyUser> resource = new FakeResource();
        subject.patch("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void deleteShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.DELETE)).thenReturn(coordinates);

        Resource<DummySession, DummyUser> resource = new FakeResource();
        subject.delete("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void connectShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.CONNECT)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.connect("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void optionsShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.OPTIONS)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.options("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void traceShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.TRACE)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.trace("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void headShouldAddRouteWithEmptyBeforeAfter() {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.HEAD)).thenReturn(coordinates);

        FakeResource resource = new FakeResource();
        subject.head("/path", resource);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0).getRoute(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getResource(), is(resource));
        assertThat(coordinates.get(0).getPattern(), is(notNullValue()));
        assertThat(coordinates.get(0).getPattern().pattern(), is("/path"));
        assertThat(coordinates.get(0).getRoute().getBefore(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getBefore().size(), is(0));
        assertThat(coordinates.get(0).getRoute().getAfter(), is(notNullValue()));
        assertThat(coordinates.get(0).getRoute().getAfter().size(), is(0));

        assertThat(coordinates.get(0).getErrorRoutes(), is(notNullValue()));
        assertThat(coordinates.get(0).getErrorRoutes().size(), is(0));
    }

    @Test
    public void getRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.GET)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.getCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));

    }

    @Test
    public void postRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.POST)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.postCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));
    }

    @Test
    public void putRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.PUT)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.putCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));

    }

    @Test
    public void patchRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.PATCH)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.patchCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));

    }

    @Test
    public void deleteRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.DELETE)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.deleteCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));
    }

    @Test
    public void connectRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.CONNECT)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.connectCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));
    }

    @Test
    public void optionsRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.OPTIONS)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> route = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.optionsCoordinate(route);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(route));
    }

    @Test
    public void traceRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.TRACE)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> coordinate = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.traceCoordinate(coordinate);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(coordinate));
    }

    @Test
    public void headRouteShouldAddRoute() throws Exception {
        List<Coordinate<DummySession, DummyUser>> coordinates = new ArrayList<>();
        when(mockDispatcher.coordinates(Method.HEAD)).thenReturn(coordinates);

        Coordinate<DummySession, DummyUser> coordinate = new CoordinateBuilder<DummySession, DummyUser>().build();
        subject.headCoordinate(coordinate);

        assertThat(coordinates, is(notNullValue()));
        assertThat(coordinates.size(), is(1));
        assertThat(coordinates.get(0), is(coordinate));
    }
}