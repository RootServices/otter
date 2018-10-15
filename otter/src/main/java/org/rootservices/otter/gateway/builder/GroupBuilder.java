package org.rootservices.otter.gateway.builder;

import org.rootservices.otter.gateway.entity.Group;
import org.rootservices.otter.router.entity.Between;

import java.util.Optional;


public class GroupBuilder<S, U> {
    private String name;
    private Class<S> sessionClazz;
    private Between<S, U> authRequired;
    private Between<S, U> authOptional;

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

    public Group<S, U> build() {
        return new Group<S, U>(name, sessionClazz, makeBetween(authRequired), makeBetween(authOptional));
    }

    protected Optional<Between<S, U>> makeBetween(Between<S, U> between) {
        if (between == null) {
            return Optional.empty();
        } else {
            return Optional.of(between);
        }
    }
}