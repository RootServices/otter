package net.tokensmith.otter.dispatch.json;

import net.tokensmith.otter.controller.entity.DefaultSession;
import net.tokensmith.otter.controller.entity.DefaultUser;
import net.tokensmith.otter.controller.entity.StatusCode;
import net.tokensmith.otter.dispatch.json.validator.Validate;
import net.tokensmith.otter.dispatch.translator.RestErrorHandler;
import net.tokensmith.otter.dispatch.translator.rest.RestBtwnRequestTranslator;
import net.tokensmith.otter.dispatch.translator.rest.RestBtwnResponseTranslator;
import net.tokensmith.otter.dispatch.translator.rest.RestErrorRequestTranslator;
import net.tokensmith.otter.dispatch.translator.rest.RestErrorResponseTranslator;
import net.tokensmith.otter.dispatch.translator.rest.RestRequestTranslator;
import net.tokensmith.otter.dispatch.translator.rest.RestResponseTranslator;
import net.tokensmith.otter.router.entity.RestRoute;
import net.tokensmith.otter.translator.JsonTranslator;
import net.tokensmith.otter.translator.exception.DeserializationException;

import java.util.Map;
import java.util.Optional;

/**
 * Used for Dispatch Errors. Look at Engine.java for scenarios this is used.
 * 404, 406, 415
 *
 * @param <U> The user
 * @param <P> The Payload.
 */
public class JsonDispatchErrorRouteRun<S extends DefaultSession, U extends DefaultUser, P> extends JsonRouteRun<S, U, P> {

    public JsonDispatchErrorRouteRun(RestRoute<S, U, P> restRoute, RestResponseTranslator<P> restResponseTranslator, RestRequestTranslator<S, U, P> restRequestTranslator, RestBtwnRequestTranslator<S, U, P> restBtwnRequestTranslator, RestBtwnResponseTranslator<P> restBtwnResponseTranslator, JsonTranslator<P> jsonTranslator, Validate validate, Map<StatusCode, RestErrorHandler<U>> errorHandlers, RestErrorRequestTranslator<S, U> errorRequestTranslator, RestErrorResponseTranslator errorResponseTranslator) {
        super(restRoute, restResponseTranslator, restRequestTranslator, restBtwnRequestTranslator, restBtwnResponseTranslator, jsonTranslator, validate, errorHandlers, errorRequestTranslator, errorResponseTranslator);
    }

    @Override
    protected Optional<P> to(Optional<byte[]> body) throws DeserializationException {
        return Optional.empty();
    }
}
