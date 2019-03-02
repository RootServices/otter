package hello.controller.api.v1;


import hello.controller.api.model.ApiSession;
import hello.controller.api.model.ApiUser;
import hello.model.Hello;
import org.rootservices.otter.controller.LegacyRestResource;
import org.rootservices.otter.controller.entity.request.Request;
import org.rootservices.otter.controller.entity.response.Response;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.translator.JsonTranslator;
import org.rootservices.otter.translator.exception.ToJsonException;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

public class HelloLegacyRestResource extends LegacyRestResource<Hello, ApiSession, ApiUser> {
    public static String URL = "/rest/hello";

    public HelloLegacyRestResource(JsonTranslator<Hello> translator) {
        super(translator);
    }

    @Override
    public Response<ApiSession> get(Request<ApiSession, ApiUser> request, Response<ApiSession> response) {
        response.setStatusCode(StatusCode.OK);

        Hello hello = new Hello("Hello, " + request.getUser().get().getFirstName() + " " + request.getUser().get().getLastName());
        Optional<byte[]> payload = Optional.empty();

        try {
            payload = Optional.of(translator.to(hello));
        } catch (ToJsonException e) {
            response.setStatusCode(StatusCode.SERVER_ERROR);
        }

        response.setPayload(payload);
        return response;
    }

    @Override
    public Response<ApiSession> post(Request<ApiSession, ApiUser> request, Response<ApiSession> response, Hello entity) {
        response.setStatusCode(StatusCode.CREATED);

        Optional<byte[]> payload = Optional.empty();

        try {
            payload = Optional.of(translator.to(entity));
        } catch (ToJsonException e) {
            response.setStatusCode(StatusCode.SERVER_ERROR);
        }

        response.setPayload(payload);
        return response;
    }
}