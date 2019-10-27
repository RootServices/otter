package net.tokensmith.hello.config;


import net.tokensmith.hello.controller.api.v3.BrokenRestResource;
import net.tokensmith.hello.controller.api.v3.HelloRestResource;
import net.tokensmith.hello.controller.html.*;
import net.tokensmith.hello.controller.html.authenticate.AuthBetween;
import net.tokensmith.hello.controller.html.authenticate.AuthOptBetween;
import net.tokensmith.hello.security.TokenSession;
import net.tokensmith.hello.controller.api.between.AuthRestBetween;
import net.tokensmith.hello.controller.api.model.ApiUser;

import net.tokensmith.hello.controller.api.v2.BrokenRestResourceV2;
import net.tokensmith.hello.controller.api.v3.handler.BadRequestResource;
import net.tokensmith.hello.controller.api.v3.handler.ServerErrorRestResource;
import net.tokensmith.hello.controller.api.v3.model.BadRequestPayload;
import net.tokensmith.hello.controller.api.v3.model.BrokenPayload;
import net.tokensmith.hello.controller.api.v3.model.ServerErrorPayload;
import net.tokensmith.hello.model.Hello;
import net.tokensmith.hello.security.User;
import net.tokensmith.jwt.entity.jwk.SymmetricKey;
import net.tokensmith.otter.controller.RestResource;
import net.tokensmith.otter.controller.builder.MimeTypeBuilder;
import net.tokensmith.otter.controller.entity.ClientError;
import net.tokensmith.otter.controller.entity.DefaultSession;
import net.tokensmith.otter.controller.entity.DefaultUser;
import net.tokensmith.otter.controller.entity.StatusCode;
import net.tokensmith.otter.controller.entity.mime.MimeType;
import net.tokensmith.otter.controller.error.html.MediaTypeResource;
import net.tokensmith.otter.controller.error.html.NotAcceptableResource;
import net.tokensmith.otter.controller.error.html.ServerErrorResource;
import net.tokensmith.otter.controller.error.rest.MediaTypeRestResource;
import net.tokensmith.otter.controller.error.rest.NotAcceptableRestResource;
import net.tokensmith.otter.controller.error.rest.NotFoundRestResource;
import net.tokensmith.otter.gateway.Configure;
import net.tokensmith.otter.gateway.Gateway;
import net.tokensmith.otter.gateway.builder.*;
import net.tokensmith.otter.gateway.entity.*;
import net.tokensmith.otter.gateway.entity.rest.RestErrorTarget;
import net.tokensmith.otter.gateway.entity.rest.RestGroup;
import net.tokensmith.otter.gateway.entity.rest.RestTarget;
import net.tokensmith.otter.router.entity.Method;


import java.util.ArrayList;
import java.util.List;


public class AppConfig implements Configure {
    public static final String API_GROUP_V2 = "API_V2";
    public static final String API_GROUP_V3 = "API_V3";
    public static final String WEB_SITE_GROUP = "WebSite";
    private AppFactory appFactory;

    public AppConfig(AppFactory appFactory) {
        this.appFactory = appFactory;
    }

    @Override
    public Shape shape() {
        // You should vault your keys, this is shown for simplicity.
        SymmetricKey encKey = appFactory.encKey();
        SymmetricKey signKey = appFactory.signKey();

        return new ShapeBuilder()
                .secure(false)
                .encKey(encKey)
                .signkey(signKey)
                .build();
    }

    @Override
    public List<Group<? extends DefaultSession, ? extends DefaultUser>> groups() {
        List<Group<? extends DefaultSession, ? extends DefaultUser>> groups = new ArrayList<>();

        var serverErrorResource = new ServerErrorResource<TokenSession, User>("/WEB-INF/jsp/500.jsp");

        ErrorTarget<TokenSession, User> mediaType = new ErrorTargetBuilder<TokenSession, User>()
                .resource(new MediaTypeResource<TokenSession, User>("/WEB-INF/jsp/415.jsp"))
                .build();

        ErrorTarget<TokenSession, User> notAcceptable = new ErrorTargetBuilder<TokenSession, User>()
                .resource(new NotAcceptableResource<TokenSession, User>("/WEB-INF/jsp/406.jsp"))
                .build();

        Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
                .name(WEB_SITE_GROUP)
                .sessionClazz(TokenSession.class)
                .authOptional(new AuthOptBetween())
                .authRequired(new AuthBetween())
                .onError(StatusCode.SERVER_ERROR, serverErrorResource)
                .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaType)
                .onDispatchError(StatusCode.NOT_ACCEPTABLE, notAcceptable)
                .build();

        groups.add(webSiteGroup);

        return groups;
    }

    @Override
    public List<RestGroup<? extends DefaultUser>> restGroups() {
        List<RestGroup<? extends DefaultUser>> restGroups = new ArrayList<>();

        AuthRestBetween authRestBetween = new AuthRestBetween();

        // uses default bad request handling.
        RestGroup<ApiUser> apiGroupV2 = new RestGroupBuilder<ApiUser>()
                .name(API_GROUP_V2)
                .authRequired(authRestBetween)
                .build();

        restGroups.add(apiGroupV2);

        // has overrides for error handling.
        BadRequestResource badRequestResource = new BadRequestResource();
        ServerErrorRestResource serverErrorResource = new ServerErrorRestResource();

        RestResource<ApiUser, ClientError> notAcceptableRestResource = new NotAcceptableRestResource<>();
        RestErrorTarget<ApiUser, ClientError> notAcceptableTarget = new RestErrorTargetBuilder<ApiUser, ClientError>()
                .payload(ClientError.class)
                .resource(notAcceptableRestResource)
                .build();

        RestResource<ApiUser, ClientError> mediaTypeResource = new MediaTypeRestResource<>();
        RestErrorTarget<ApiUser, ClientError> mediaTypeTarget = new RestErrorTargetBuilder<ApiUser, ClientError>()
                .payload(ClientError.class)
                .resource(mediaTypeResource)
                .build();

        RestGroup<ApiUser> apiGroupV3 = new RestGroupBuilder<ApiUser>()
                .name(API_GROUP_V3)
                .authRequired(authRestBetween)
                .authOptional(authRestBetween)
                .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
                .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
                .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaTypeTarget)
                .onDispatchError(StatusCode.NOT_ACCEPTABLE, notAcceptableTarget)
                .build();

        restGroups.add(apiGroupV3);

        return restGroups;
    }

    @Override
    public void routes(Gateway gateway) {
        notFoundTargets(gateway);

        MimeType json = new MimeTypeBuilder().json().build();

        // resource for v2 api
        RestTarget<ApiUser, Hello> helloApiV2 = new RestTargetBuilder<ApiUser, Hello>()
                .groupName(API_GROUP_V2)
                .method(Method.GET)
                .method(Method.POST)
                .restResource(new net.tokensmith.hello.controller.api.v2.HelloRestResource())
                .regex(net.tokensmith.hello.controller.api.v2.HelloRestResource.URL)
                .authenticate()
                .contentType(json)
                .payload(Hello.class)
                .build();

        gateway.add(helloApiV2);

        // this will always throw a runtime exception and force the default error handler.
        BrokenRestResourceV2 brokenRestResourceV2 = new BrokenRestResourceV2();
        RestTarget<ApiUser, BrokenPayload> brokenApiV2 = new RestTargetBuilder<ApiUser, BrokenPayload>()
                .groupName(API_GROUP_V2)
                .crud()
                .restResource(brokenRestResourceV2)
                .regex(brokenRestResourceV2.URL)
                .authenticate()
                .contentType(json)
                .payload(BrokenPayload.class)
                .build();

        gateway.add(brokenApiV2);

        // resource for v3 api
        var helloRestResourceV3 = new HelloRestResource();
        RestTarget<ApiUser, Hello> helloApiV3 = new RestTargetBuilder<ApiUser, Hello>()
                .groupName(API_GROUP_V3)
                .method(Method.GET)
                .method(Method.POST)
                .restResource(helloRestResourceV3)
                .regex(helloRestResourceV3.URL)
                .authenticate()
                .contentType(json)
                .accept(json)
                .payload(Hello.class)
                .build();

        gateway.add(helloApiV3);

        // this will always throw a runtime exception and force the error handler.
        BrokenRestResource brokenRestResource = new BrokenRestResource();
        RestTarget<ApiUser, BrokenPayload> brokenApiV3 = new RestTargetBuilder<ApiUser, BrokenPayload>()
                .groupName(API_GROUP_V3)
                .crud()
                .restResource(brokenRestResource)
                .regex(brokenRestResource.URL)
                .authenticate()
                .contentType(json)
                .payload(BrokenPayload.class)
                .build();

        gateway.add(brokenApiV3);

        // does not require content-type
        Target<TokenSession, User> hello = new TargetBuilder<TokenSession, User>()
            .groupName(WEB_SITE_GROUP)
            .method(Method.GET)
            .resource(new HelloResource())
            .regex(HelloResource.URL)
            .build();

        gateway.add(hello);

        // csrf
        Target<TokenSession, User> login = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .form()
                .resource(new LoginResource())
                .regex(LoginResource.URL)
                .build();

        gateway.add(login);

        // csrf & session
        Target<TokenSession, User> loginWithSession = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .form()
                .resource(new LoginSessionResource())
                .regex(LoginSessionResource.URL)
                .authenticate()
                .build();

        gateway.add(loginWithSession);

        // set session
        Target<TokenSession, User> loginSetSessionResource = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .form()
                .resource(new LoginSetSessionResource())
                .regex(LoginSetSessionResource.URL)
                .build();

        gateway.add(loginSetSessionResource);

        // session
        Target<TokenSession, User> protectedTarget = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .method(Method.GET)
                .method(Method.POST)
                .resource(new ProtectedResource())
                .regex(ProtectedResource.URL)
                .authenticate()
                .build();

        gateway.add(protectedTarget);

        // content type and accepts are required
        MimeType html = new MimeTypeBuilder().html().build();
        Target<TokenSession, User> goodByeTarget = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .method(Method.GET)
                .accept(Method.GET, html)
                .contentType(Method.GET, html)
                .resource(new GoodByeResource())
                .regex(GoodByeResource.URL)
                .build();

        gateway.add(goodByeTarget);

        // should be handled by server error resource.
        Target<TokenSession, User> exceptionTarget = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .method(Method.GET)
                .method(Method.POST)
                .resource(new RunTimeExceptionResource())
                .regex(RunTimeExceptionResource.URL)
                .build();

        gateway.add(exceptionTarget);
    }

    public void notFoundTargets(Gateway gateway) {

        // 157 need to instruct gateway this is a not found resource so it wont attempt to
        // serialize the request body.
        var restNotFoundResource = new NotFoundRestResource<ApiUser>();
        RestTarget<ApiUser, ClientError> notFoundV2 = new RestTargetBuilder<ApiUser, ClientError>()
                .groupName(API_GROUP_V2)
                .crud()
                .restResource(restNotFoundResource)
                .regex("/rest/v2/(.*)")
                .payload(ClientError.class)
                .build();

        gateway.notFound(notFoundV2);

        RestTarget<ApiUser, ClientError> notFoundV3 = new RestTargetBuilder<ApiUser, ClientError>()
                .groupName(API_GROUP_V3)
                .crud()
                .restResource(restNotFoundResource)
                .regex("/rest/v3/(.*)")
                .payload(ClientError.class)
                .build();

        gateway.notFound(notFoundV3);

        // html
        Target<TokenSession, User> notFoundTarget = new TargetBuilder<TokenSession, User>()
                .groupName(WEB_SITE_GROUP)
                .method(Method.GET)
                .method(Method.POST)
                .resource(new NotFoundResource())
                .regex("(.*)")
                .build();

        gateway.notFound(notFoundTarget);
    }
}