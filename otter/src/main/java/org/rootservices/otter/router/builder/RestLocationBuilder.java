package org.rootservices.otter.router.builder;

import org.rootservices.otter.config.OtterAppFactory;
import org.rootservices.otter.controller.RestResource;
import org.rootservices.otter.controller.entity.DefaultUser;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.controller.entity.mime.MimeType;
import org.rootservices.otter.dispatch.JsonErrorHandler;
import org.rootservices.otter.dispatch.JsonRouteRun;
import org.rootservices.otter.dispatch.RouteRunner;
import org.rootservices.otter.dispatch.translator.RestErrorHandler;
import org.rootservices.otter.dispatch.translator.rest.*;
import org.rootservices.otter.gateway.entity.rest.RestError;
import org.rootservices.otter.router.entity.Location;
import org.rootservices.otter.router.entity.RestRoute;
import org.rootservices.otter.router.entity.between.RestBetween;
import org.rootservices.otter.translatable.Translatable;
import org.rootservices.otter.translator.JsonTranslator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


public class RestLocationBuilder<U extends DefaultUser, P> {
    private Pattern pattern;
    private List<MimeType> contentTypes = new ArrayList<>();
    private RestResource<U, P> restResource;
    private Class<P> payload;
    private List<RestBetween<U>> before = new ArrayList<>();
    private List<RestBetween<U>> after = new ArrayList<>();

    // error route runners that are called from engine.
    private Map<StatusCode, RouteRunner> errorRouteRunners = new HashMap<>();

    // used when building routeRunner, errorRouteRunners
    private RestRequestTranslator<U, P> restRequestTranslator = new RestRequestTranslator<U, P>();
    private RestResponseTranslator<P> restResponseTranslator = new RestResponseTranslator<P>();
    private RestBtwnRequestTranslator<U, P> restBtwnRequestTranslator = new RestBtwnRequestTranslator<>();
    private RestBtwnResponseTranslator<P> restBtwnResponseTranslator = new RestBtwnResponseTranslator<>();

    // error resources that will be called from within the routerunner.
    private Map<StatusCode, RestErrorHandler<U>> errorHandlers = new HashMap<>();

    private OtterAppFactory otterAppFactory = new OtterAppFactory();

    public RestLocationBuilder<U, P> path(String path) {
        this.pattern = Pattern.compile(path);
        return this;
    }

    public RestLocationBuilder<U, P> contentTypes(List<MimeType> contentTypes) {
        this.contentTypes = contentTypes;
        return this;
    }

    public RestLocationBuilder<U, P> contentType(MimeType contentType) {
        this.contentTypes.add(contentType);
        return this;
    }

    public RestLocationBuilder<U, P> restResource(RestResource<U, P> restResource) {
        this.restResource = restResource;
        return this;
    }

    public RestLocationBuilder<U, P> payload(Class<P> payload) {
        this.payload = payload;
        return this;
    }

    public RestLocationBuilder<U, P> before(List<RestBetween<U>> before) {
        this.before = before;
        return this;
    }

    public RestLocationBuilder<U, P> after(List<RestBetween<U>> after) {
        this.after = after;
        return this;
    }

    // used in engine
    @SuppressWarnings("unchecked")
    public <E extends Translatable> RestLocationBuilder<U, P> errorRouteRunner(StatusCode statusCode, RestRoute<U, ? extends Translatable> errorRestRoute, Class<? extends Translatable> payload) {
        JsonTranslator<E> jsonTranslator = otterAppFactory.jsonTranslator((Class<E>)payload);

        // 113: might be able to put these in a flyweight if worried about memory footprint.
        RestRequestTranslator<U, E> restRequestTranslator = new RestRequestTranslator<U, E>();
        RestResponseTranslator<E> restResponseTranslator = new RestResponseTranslator<E>();
        RestBtwnRequestTranslator<U, E> restBtwnRequestTranslator = new RestBtwnRequestTranslator<>();
        RestBtwnResponseTranslator<E> restBtwnResponseTranslator = new RestBtwnResponseTranslator<>();

        RouteRunner errorRouteRunner = new JsonRouteRun<U, E>(
                (RestRoute<U, E>) errorRestRoute,
                restResponseTranslator,
                restRequestTranslator,
                restBtwnRequestTranslator,
                restBtwnResponseTranslator,
                jsonTranslator,
                new HashMap<>(),
                new RestErrorRequestTranslator<>(),
                new RestErrorResponseTranslator()
        );
        errorRouteRunners.put(statusCode, errorRouteRunner);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <E extends Translatable> RestLocationBuilder<U, P> restErrorResources(Map<StatusCode, RestError<U, ? extends E>> restErrors) {

        for(Map.Entry<StatusCode, RestError<U, ? extends E>> restError: restErrors.entrySet()) {
            RestError<U, E> castedRestErrorValue = (RestError<U, E>) restError.getValue();
            JsonTranslator<E> jsonTranslator = otterAppFactory.jsonTranslator(castedRestErrorValue.getPayload());

            RestErrorHandler<U> errorHandler = new JsonErrorHandler<U, E>(
                    jsonTranslator,
                    castedRestErrorValue.getRestResource(),
                    new RestRequestTranslator<>(),
                    new RestResponseTranslator<>()
            );

            errorHandlers.put(restError.getKey(), errorHandler);
        }
        return this;
    }


    public Location build() {
        RestRoute<U, P> restRoute = new RestRouteBuilder<U, P>()
                .restResource(restResource)
                .before(before)
                .after(after)
                .build();

        JsonTranslator<P> jsonTranslator = otterAppFactory.jsonTranslator(payload);

        RouteRunner routeRunner = new JsonRouteRun<U, P>(
                restRoute,
                restResponseTranslator,
                restRequestTranslator,
                restBtwnRequestTranslator,
                restBtwnResponseTranslator,
                jsonTranslator,
                errorHandlers,
                new RestErrorRequestTranslator<>(),
                new RestErrorResponseTranslator()
        );

        return new Location(pattern, contentTypes, routeRunner, errorRouteRunners);
    }
}
