package hello.controller;


import hello.controller.presenter.LoginPresenter;
import hello.security.TokenSession;
import hello.security.User;
import org.rootservices.otter.controller.Resource;
import org.rootservices.otter.controller.entity.EmptyPayload;
import org.rootservices.otter.controller.entity.Request;
import org.rootservices.otter.controller.entity.Response;
import org.rootservices.otter.controller.entity.StatusCode;

import java.util.Optional;

public class LoginResource extends Resource<TokenSession, User, EmptyPayload> {
    public static String URL = "/login";
    private static String JSP_PATH = "/WEB-INF/jsp/login.jsp";

    @Override
    public Response<TokenSession> get(Request<TokenSession, User, EmptyPayload> request, Response<TokenSession> response) {
        LoginPresenter presenter = new LoginPresenter("", request.getCsrfChallenge().get());
        response.setPresenter(Optional.of(presenter));
        response.setStatusCode(StatusCode.OK);
        response.setTemplate(Optional.of(JSP_PATH));
        return response;
    }

    @Override
    public Response<TokenSession> post(Request<TokenSession, User, EmptyPayload> request, Response<TokenSession> response) {
        LoginPresenter presenter = new LoginPresenter("", request.getCsrfChallenge().get());
        response.setPresenter(Optional.of(presenter));
        response.setStatusCode(StatusCode.OK);
        response.setTemplate(Optional.of(JSP_PATH));

        return response;
    }
}
