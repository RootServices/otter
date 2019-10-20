package org.rootservices.hello.controller.html;


import org.rootservices.hello.security.TokenSession;
import org.rootservices.hello.security.User;
import org.rootservices.otter.controller.Resource;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.controller.entity.request.Request;
import org.rootservices.otter.controller.entity.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class GoodByeResource extends Resource<TokenSession, User> {
    protected static Logger LOGGER = LoggerFactory.getLogger(GoodByeResource.class);
    public static String URL = "/goodbye";
    private static String JSP_PATH = "/WEB-INF/jsp/goodbye.jsp";

    public Response<TokenSession> get(Request<TokenSession, User> request, Response<TokenSession> response) {
        LOGGER.info("good bye");
        response.setStatusCode(StatusCode.OK);
        response.setTemplate(Optional.of(JSP_PATH));
        return response;
    }
}