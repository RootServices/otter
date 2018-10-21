package org.rootservices.otter.gateway;

import org.rootservices.otter.config.OtterAppFactory;
import org.rootservices.otter.controller.entity.DefaultSession;
import org.rootservices.otter.controller.entity.DefaultUser;
import org.rootservices.otter.gateway.entity.Shape;
import org.rootservices.otter.gateway.translator.LocationTranslator;
import org.rootservices.otter.router.factory.BetweenFactory;
import org.rootservices.otter.security.builder.BetweenBuilder;
import org.rootservices.otter.security.builder.entity.Betweens;
import org.rootservices.otter.security.exception.SessionCtorException;


/**
 * Responsible for constructing a LocationTranslator.
 */
public class LocationTranslatorFactory {
    private Shape shape;

    public LocationTranslatorFactory(Shape shape) {
        this.shape = shape;
    }

    public <S extends DefaultSession, U extends DefaultUser> LocationTranslator<S, U> make(Class<S> sessionClazz) throws SessionCtorException {
        return new LocationTranslator<S, U>(
                betweenFactory(sessionClazz)
        );
    }

    public <S, U> BetweenFactory<S, U> betweenFactory(Class<S> sessionClazz) throws SessionCtorException {
        OtterAppFactory otterAppFactory = new OtterAppFactory();

        return new BetweenFactory<S, U>(
                csrfPrepare(otterAppFactory),
                csrfProtect(otterAppFactory),
                session(otterAppFactory, sessionClazz),
                sessionOptional(otterAppFactory, sessionClazz)
        );
    }

    protected <S, U> Betweens<S, U> csrfPrepare(OtterAppFactory otterAppFactory) {
        return new BetweenBuilder<S, U>()
                .otterFactory(otterAppFactory)
                .secure(shape.getSecure())
                .signKey(shape.getSignkey())
                .rotationSignKeys(shape.getRotationSignKeys())
                .csrfPrepare()
                .build();

    }

    protected <S, U> Betweens<S, U> csrfProtect(OtterAppFactory otterAppFactory) {
        return new BetweenBuilder<S, U>()
                .otterFactory(otterAppFactory)
                .secure(shape.getSecure())
                .signKey(shape.getSignkey())
                .rotationSignKeys(shape.getRotationSignKeys())
                .csrfProtect()
                .build();

    }

    protected <S, U> Betweens<S, U> session(OtterAppFactory otterAppFactory, Class<S> sessionClazz) throws SessionCtorException {
        return new BetweenBuilder<S, U>()
                .otterFactory(otterAppFactory)
                .secure(shape.getSecure())
                .encKey(shape.getEncKey())
                .rotationEncKey(shape.getRotationEncKeys())
                .sessionClass(sessionClazz)
                .session()
                .build();
    }


    protected <S, U> Betweens<S, U> sessionOptional(OtterAppFactory otterAppFactory, Class<S> sessionClazz) throws SessionCtorException {
        return new BetweenBuilder<S, U>()
                .otterFactory(otterAppFactory)
                .secure(shape.getSecure())
                .encKey(shape.getEncKey())
                .rotationEncKey(shape.getRotationEncKeys())
                .sessionClass(sessionClazz)
                .optionalSession()
                .build();
    }
}
