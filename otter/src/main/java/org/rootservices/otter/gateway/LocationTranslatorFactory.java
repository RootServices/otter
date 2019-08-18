package org.rootservices.otter.gateway;

import org.rootservices.otter.controller.Resource;
import org.rootservices.otter.controller.entity.DefaultSession;
import org.rootservices.otter.controller.entity.DefaultUser;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.gateway.entity.ErrorTarget;
import org.rootservices.otter.gateway.entity.Shape;
import org.rootservices.otter.gateway.translator.LocationTranslator;
import org.rootservices.otter.router.entity.between.Between;
import org.rootservices.otter.router.factory.BetweenFlyweight;
import org.rootservices.otter.security.builder.BetweenBuilder;
import org.rootservices.otter.security.builder.entity.Betweens;
import org.rootservices.otter.security.exception.SessionCtorException;
import org.rootservices.otter.translator.config.TranslatorAppFactory;

import java.util.Map;
import java.util.Optional;


/**
 * Responsible for constructing a LocationTranslator.
 * This is not in OtterAppFactory due to it's complexity
 */
public class LocationTranslatorFactory {
    private Shape shape;

    public LocationTranslatorFactory(Shape shape) {
        this.shape = shape;
    }

    public <S extends DefaultSession, U extends DefaultUser> LocationTranslator<S, U> make(Class<S> sessionClazz, Optional<Between<S,U>> authRequired, Optional<Between<S,U>> authOptional, Map<StatusCode, Resource<S, U>> errorResources, Map<StatusCode, ErrorTarget<S, U>> dispatchErrors, Map<StatusCode, ErrorTarget<S, U>> defaultDispatchErrors) throws SessionCtorException {
        return new LocationTranslator<S, U>(
                betweenFlyweight(sessionClazz, authRequired, authOptional),
                errorResources,
                dispatchErrors,
                defaultDispatchErrors
        );
    }

    /**
     * Construct a flyweight for betweens that will be used when a Target is translated to a Location. Each target that
     * is translated will use the same flyweight to add csrf, session, and authentication betweens to a Location.
     * Therefore many Locations will use the same betweens instead of creating many identical ones.
     *
     * @param sessionClazz The Class of the session
     * @param authRequired The between that requires authentication
     * @param authOptional The between that optionally authenticates.
     * @param <S> Session
     * @param <U> User
     * @return BetweenFlyweight that will be used the LocationTranslator.
     * @throws SessionCtorException if S does not have a copy constructor.
     */
    public <S, U> BetweenFlyweight<S, U> betweenFlyweight(Class<S> sessionClazz, Optional<Between<S,U>> authRequired, Optional<Between<S,U>> authOptional) throws SessionCtorException {
        TranslatorAppFactory appFactory = new TranslatorAppFactory();

        return new BetweenFlyweight<S, U>(
                csrfPrepare(appFactory),
                csrfProtect(appFactory),
                session(appFactory, sessionClazz),
                sessionOptional(appFactory, sessionClazz),
                authRequired,
                authOptional
        );
    }

    protected <S, U> Betweens<S, U> csrfPrepare(TranslatorAppFactory appFactory) {
        return new BetweenBuilder<S, U>()
                .routerAppFactory(appFactory)
                .secure(shape.getSecure())
                .signKey(shape.getSignkey())
                .rotationSignKeys(shape.getRotationSignKeys())
                .csrfPrepare()
                .build();

    }

    protected <S, U> Betweens<S, U> csrfProtect(TranslatorAppFactory appFactory) {
        return new BetweenBuilder<S, U>()
                .routerAppFactory(appFactory)
                .secure(shape.getSecure())
                .signKey(shape.getSignkey())
                .rotationSignKeys(shape.getRotationSignKeys())
                .csrfProtect()
                .build();

    }

    protected <S, U> Betweens<S, U> session(TranslatorAppFactory appFactory, Class<S> sessionClazz) throws SessionCtorException {
        return new BetweenBuilder<S, U>()
                .routerAppFactory(appFactory)
                .secure(shape.getSecure())
                .encKey(shape.getEncKey())
                .rotationEncKey(shape.getRotationEncKeys())
                .sessionClass(sessionClazz)
                .session()
                .build();
    }


    protected <S, U> Betweens<S, U> sessionOptional(TranslatorAppFactory appFactory, Class<S> sessionClazz) throws SessionCtorException {
        return new BetweenBuilder<S, U>()
                .routerAppFactory(appFactory)
                .secure(shape.getSecure())
                .encKey(shape.getEncKey())
                .rotationEncKey(shape.getRotationEncKeys())
                .sessionClass(sessionClazz)
                .optionalSession()
                .build();
    }
}
