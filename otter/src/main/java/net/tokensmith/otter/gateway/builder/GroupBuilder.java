package net.tokensmith.otter.gateway.builder;

import net.tokensmith.otter.controller.Resource;
import net.tokensmith.otter.controller.entity.DefaultSession;
import net.tokensmith.otter.controller.entity.DefaultUser;
import net.tokensmith.otter.controller.entity.StatusCode;
import net.tokensmith.otter.gateway.entity.ErrorTarget;
import net.tokensmith.otter.gateway.entity.Group;
import net.tokensmith.otter.router.entity.between.Between;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class GroupBuilder<S extends DefaultSession, U extends DefaultUser> {
    private String name;
    private Class<S> sessionClazz;
    private Between<S, U> authRequired;
    private Between<S, U> authOptional;
    private Map<StatusCode, Resource<S, U>> errorResources = new HashMap<>();
    private Map<StatusCode, ErrorTarget<S, U>> dispatchErrors = new HashMap<>();

    public GroupBuilder<S, U> name(String name) {
        this.name = name;
        return this;
    }

    public GroupBuilder<S, U> sessionClazz(Class<S> sessionClazz) {
        this.sessionClazz = sessionClazz;
        return this;
    }

    public GroupBuilder<S, U> authRequired(Between<S, U> authRequired) {
        this.authRequired = authRequired;
        return this;
    }

    public GroupBuilder<S, U> authOptional(Between<S, U> authOptional) {
        this.authOptional = authOptional;
        return this;
    }

    public GroupBuilder<S, U> onError(StatusCode statusCode, Resource<S, U> errorResource) {
        this.errorResources.put(statusCode, errorResource);
        return this;
    }

    public GroupBuilder<S, U> onDispatchError(StatusCode statusCode, ErrorTarget<S, U> dispatchError) {
        this.dispatchErrors.put(statusCode, dispatchError);
        return this;
    }

    public Group<S, U> build() {
        return new Group<S, U>(
                name,
                sessionClazz,
                makeBetween(authRequired),
                makeBetween(authOptional),
                errorResources,
                dispatchErrors
        );
    }

    protected Optional<Between<S, U>> makeBetween(Between<S, U> between) {
        if (between == null) {
            return Optional.empty();
        } else {
            return Optional.of(between);
        }
    }
}