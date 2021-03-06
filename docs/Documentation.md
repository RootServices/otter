# Documentation

## Contents
- [Scaffolding](#scaffolding)
- [Fundamentals](#fundamentals)
    - [Resource](#resource)
    - [RestResource](#resource)
    - [Between](#between)
    - [Target](#target)
    - [Group](#group)
    - [RestBetween](#restbetween)
    - [RestTarget](#resttarget)
    - [RestGroup](#restgroup)
    - [Request Body Validation](#request-body-validation)
- [Authentication](#authentication)
    - [Session](#session)
    - [Session fail](#session-failure)
    - [Custom Session Management](#custom-session-implementation)
    - [User](#user)
    - [Required Authentication](#required-authentication-between)
    - [Optional Authentication](#optional-authentication-between)
    - [Resource Authentication](#resource-authentication)
    - [RestResource Authentication](#restresource-authentication)    
- [Error Handling](#error-handling)
- [Not Founds](#not-founds)
- [Configuration](#configuration)
    - [Configure](#configure)
    - [Entry Servlet](#entry-servlet)
    - [Main Method](#main-method)
    - [Compression](#compression)
- [CSRF protection](#csrf)
    - [Resource](#resource-protection)
    - [RestResource](#restresource-protection)
    - [CSRF fail](#csrf-failure)
    - [Customize](#custom-csrf-implemention)
- [Delivery of static assets](#static-assets)

### Scaffolding

Below is one approach to a project layout. Which can be observed in the [hello world application](https://github.com/tokensmith/otter/tree/development/examples/hello-world).
```bash
    project/
        src/
            main/
                java/{groupId}.{artifactId}
                    config/
                        AppConfig.java
                        AppEntryServlet.java
                    server/
                        AppServer.java
                resources/
            webapp/
                public/
                WEB-INF/
                    jsp/    
            test/
```

`AppConfig.java` contains the [configuration](#configuration) to set up your web application. 

`AppEntryServlet.java` allows [servlet container requests](#entry-servlet) to be sent to otter.

`AppServer.java` has the application's [main method](#main-method) to start the web application.

### Fundamentals
#### Resource
A [Resource](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/controller/Resource.java) handles an http request. it's typically used to accept `text/html`, however, It can accept any `Content-Type`.

#### RestResource
A [RestResource](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/controller/RestResource.java) is designed to accept and reply `application/json`. Sorry, there is no support for `application/xml`.

#### Between
A [Between](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/router/entity/between/Between.java) allows a rule to be executed before a request reaches a Resource or after a Resource executes. Also referred to as a before and a after.

#### Target
A [Target](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/gateway/entity/Target.java) instructs otter which `Resource` to use for a given url and http methods. 

```java
    Target<TokenSession, User> hello = new TargetBuilder<TokenSession, User>()
        .groupName(WEB_SITE_GROUP)
        .method(Method.GET)
        .resource(new HelloResource())
        .regex(HelloResource.URL)
        .build();
```

#### Group
A [Group](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/gateway/entity/Group.java) allows sharing betweens and error handling with `Targets`.

```java
    var serverErrorResource = new ServerErrorResource();
    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
            .name(WEB_SITE_GROUP)
            .sessionClazz(TokenSession.class)
            .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
            .before(Label.AUTH_REQUIRED, new AuthBetween())
            .onError(StatusCode.SERVER_ERROR, serverErrorResource)
            .build();
```
To share a `Group's` feature with a `Target` set the `.groupName(..)` ot the `name(..)` of the `Group`.
```java
    Target<TokenSession, User> hello = new TargetBuilder<TokenSession, User>()
        .groupName(WEB_SITE_GROUP)
        .method(Method.GET)
        .resource(new HelloResource())
        .regex(HelloResource.URL)
        .build();
```

All `Targets` must relate to a `Group`.

#### RestBetween
A [RestBetween](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/router/entity/between/RestBetween.java) allows a rule to be executed before a request reaches a RestResource or after a RestResource executes. Also referred to as a before and a after.

#### RestTarget

A [RestTarget](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/gateway/entity/rest/RestTarget.java) instructs otter which `RestResource` to use for a given url and http methods.

```java
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
```

#### RestGroup
A [RestGroup](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/gateway/entity/rest/RestGroup.java) allows sharing betweens and error handling with `RestTargets`.

```java
    BadRequestResource badRequestResource = new BadRequestResource();
    ServerErrorResource serverErrorResource = new ServerErrorResource();
    RestGroup<DummySession, ApiUser> apiGroupV3 = new RestGroupBuilder<DummySession, ApiUser>()
            .name(API_GROUP_V3)
            .sessionClazz(DefaultSession.class)
            .before(Label.AUTH_OPTIONAL, authRestBetween)            
            .before(Label.AUTH_REQUIRED, authRestBetween)
            .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
            .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
            .build();
```

To share a `RestGroup's` feature with a `RestTarget` set the `.groupName(..)` to the `name(..)` of the `RestGroup`.

```java
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
```

All `RestTargets` must relate to a `RestGroup`.

#### Request Body Validation
A request body that is intended to be delivered to a `RestResource` will be validated by 
using `javax.validation` defined by `JSR 380`. To use it annotate a class implementation with constraints then if 
there are any errors Otter will send the request to the configured `RestBadRequestResource`. 
See [error handling](#error-handling) for how that works.

If `javax.validation` is not your preference then it can be replaced by passing in an implementation of Otter's 
`Validate` interface into the `RestTargetBuilder`.

```java
    Validate customValidate = new CustomValidate();
    var helloRestResourceV3 = new HelloRestResource();
    RestTarget<ApiUser, Hello> helloApiV3 = new RestTargetBuilder<ApiUser, Hello>()
            .groupName(API_GROUP_V3)
            .method(Method.GET)
            .method(Method.POST)
            .restResource(helloRestResourceV3)
            .regex(helloRestResourceV3.URL)
            .validate(customValidate) // your preferred validate implemention.
            .contentType(json)
            .accept(json)
            .payload(Hello.class)
            .build();
```

### Authentication
Authentication in otter is dependent on the value objects:
 - [Session](#session)
 - [User](#user)

Two different authentication betweens must be implemented.
 - [required authentication](#required-authentication-between)
 - [optional authentication](#optional-authentication-between)

Configure the betweens to be used by a `Group` or `RestGroup`.
 - [Resource Authentication](#resource-authentication)
 - [RestResource Authentication](#restresource-authentication)
  
#### Session 
A `Session` is a cookie that is `http-only` and it's value is a `JWE`. It is required to have authentication for 
`Resources`

Sessions in otter are stateless. 
 - There is no state stored on the web server.
 - You can use data stored in the session to retrieve the user's profile, such as a access token or session id.

Session don'ts:
 - Do not put data into the session that may become stale, such as RBAC.
   
Session implementations:
 - Must extend [DefaultSession](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/controller/entity/DefaultSession.java)
 - Must have a copy constructor.

**Why JWE?**
 
The threats are: 
 - Session hijacking by modifying values of the session cookie to take over a different session.
 - In the instance the session cookie is revealed then sensitive data is not easily accessible.
 
#### Session failure

When a session fails to be read or does not exist when it should then Otter will return:
- status code of 401
- remove session cookie

This behavior can be overridden by implementing a halt bifunction and passing it into it's group.

```java
    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
        .name(WEB_SITE_GROUP)
        .sessionClazz(TokenSession.class)
        .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
        .before(Label.AUTH_REQUIRED, new AuthBetween())
        .onHalt(Halt.SESSION, (Response<TokenSession> response, HaltException e) -> {
            response.setTemplate(Optional.of("/WEB-INF/jsp/401.jsp"));
            response.setStatusCode(StatusCode.UNAUTHORIZED);
            return response;
        })
        .onError(StatusCode.SERVER_ERROR, serverErrorResource)
        .build();
```
 
#### Custom session implementation

Custom implementations of session managment can be injected into [Groups]((#group)) and [RestGroups](#restgroup)

##### Group
Implement betweens that will handle reading and writing sessions. Then inject them into the group.

```java
    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
        .name(WEB_SITE_GROUP)
        .sessionClazz(TokenSession.class)
        .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
        .before(Label.AUTH_REQUIRED, new AuthBetween())
        .before(Label.SESSION_REQUIRED, readSession) // <-- custom required session reading 
        .before(Label.SESSION_OPTIONAL, readSessionOptional) // <-- custom optional session reading
        .before(Label.SESSION_REQUIRED, writeSession) // <-- custom required session writing 
        .before(Label.SESSION_OPTIONAL, writeSessionOptional) // <-- custom optional session writing
        .onError(StatusCode.SERVER_ERROR, serverErrorResource)
        .build();
    
```

These would then be used instead of the defaults for all the targets in that group.

##### RestGroup

Implement betweens that will handle reading and writing sessions. Then inject them into the restGroup.
```java
    BadRequestResource badRequestResource = new BadRequestResource();
    ServerErrorResource serverErrorResource = new ServerErrorResource();
    RestGroup<DummySession, ApiUser> apiGroupV3 = new RestGroupBuilder<DummySession, ApiUser>()
            .name(API_GROUP_V3)
            .sessionClazz(DefaultSession.class)
            .before(Label.AUTH_OPTIONAL, authRestBetween)            
            .before(Label.AUTH_REQUIRED, authRestBetween)
            .before(Label.SESSION_REQUIRED, readSession) // <-- custom required session reading 
            .before(Label.SESSION_OPTIONAL, readSessionOptional) // <-- custom optional session reading
            .before(Label.SESSION_REQUIRED, writeSession) // <-- custom required session writing 
            .before(Label.SESSION_OPTIONAL, writeSessionOptional) // <-- custom optional session writing
            .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
            .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
            .build();
```

It would then be used instead of the default for all the restTargets in that restGroup.

 
#### User

User implementations:
 - Must extend [DefaultUser](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/controller/entity/DefaultUser.java)

#### Authentication Between Implementations
##### Required Authentication Between
```gherkin
Given the required authentication between

When authentication succeeds
Then assign the request user to the appropriate user
 
When authentication fails
Then possibly set the status code to 401
And throw a halt exception. 
```

##### Optional Authentication Between
```gherkin
Given the optional authentication between

When the Session is present 
and authentication succeeds
Then assign the request user to the appropriate user
 
When the Session is present 
and authentication fails
Then possibly set the status code to 401
And throw a halt exception. 

When the Session is not present 
Then allow the request to reach the resource. 
```
#### Resource Authentication
```java
    var serverErrorResource = new ServerErrorResource();
    
    ErrorTarget<TokenSession, User> mediaType = new ErrorTargetBuilder<TokenSession, User>()
            .resource(new MediaTypeResource())
            .build();

    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
            .name(WEB_SITE_GROUP)
            .sessionClazz(TokenSession.class)
            .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
            .before(Label.AUTH_REQUIRED, new AuthBetween())
            .onError(StatusCode.SERVER_ERROR, serverErrorResource)
            .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaType)
            .build();
```

Then to require authentication for a `Resource` use, `.authenticate()`.

```java
    Target<TokenSession, User> hello = new TargetBuilder<TokenSession, User>()
        .groupName(WEB_SITE_GROUP)
        .method(Method.GET)
        .resource(new HelloResource())
        .regex(HelloResource.URL)
        .authenticate()
        .build();
```

If `authenticate()` is not used then the optional authenticate between will be executed.

Use `anonymous()` to not require authentication or optionally authenticate.

#### RestResource Authentication

```java
    AuthRestBetween authRestBetween = new AuthRestBetween();

    RestGroup<DummySession, ApiUser> apiGroupV3 = new RestGroupBuilder<DummySession, ApiUser>()
            .name(API_GROUP_V3)
            .sessionClazz(DefaultSession.class)
            .before(Label.AUTH_OPTIONAL, authRestBetween)            
            .before(Label.AUTH_REQUIRED, authRestBetween)
            .build();
```

Then, to require authentication for a `RestResource` use, `.authenticate()`.

```java
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
```

#### RestResource Sessions

Here is an example on how to get read access to the session in the before rest betweens. 
This *requires* the session is present or it will halt the request.
```java
var helloRestResourceV3 = new HelloRestResource();
    RestTarget<ApiUser, Hello> helloApiV3 = new RestTargetBuilder<ApiUser, Hello>()
            .groupName(API_GROUP_V3)
            .method(Method.GET)
            .method(Method.POST)
            .restResource(helloRestResourceV3)
            .regex(helloRestResourceV3.URL)
            .session() // <-- this will set the session.
            .authenticate()
            .contentType(json)
            .accept(json)
            .payload(Hello.class)
            .build();
```

If `authenticate()` is not used then the optional authenticate between will be executed.

Use `anonymous()` to not require authentication or optionally authenticate.

### Error Handling

#### Resource

The errors that can be recovered from are:
 - Server Error `500`
 - Unsuppored Media Type `415`
 - Not Acceptable `406`
 
Everything else should be able to be handled with in a `Resource`.

Otter does not have default error handling when an error occurs attempting to reach a `Resource`.

To configure a `Group` to apply error handlers to all its related `Targets`.
```java
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
            .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
            .before(Label.AUTH_REQUIRED, new AuthBetween())
            .onError(StatusCode.SERVER_ERROR, serverErrorResource)
            .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaType)
            .onDispatchError(StatusCode.NOT_ACCEPTABLE, notAcceptable)
            .build();
```

To override or add error handling to a `Target`.
```java
    var serverErrorResource = new ServerErrorResource<TokenSession, User>("/WEB-INF/jsp/500.jsp");
    
    ErrorTarget<TokenSession, User> mediaType = new ErrorTargetBuilder<TokenSession, User>()
            .resource(new MediaTypeResource<TokenSession, User>("/WEB-INF/jsp/415.jsp"))
            .build();

    ErrorTarget<TokenSession, User> notAcceptable = new ErrorTargetBuilder<TokenSession, User>()
            .resource(new NotAcceptableResource<TokenSession, User>("/WEB-INF/jsp/406.jsp"))
            .build();

    Target<TokenSession, User> hello = new TargetBuilder<TokenSession, User>()
        .groupName(WEB_SITE_GROUP)
        .method(Method.GET)
        .resource(new HelloResource())
        .regex(HelloResource.URL)
        .onError(StatusCode.SERVER_ERROR, serverErrorResource)
        .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaType)
        .onDispatchError(StatusCode.NOT_ACCEPTABLE, notAcceptable)
        .build();
```

#### RestResource
Otter will use it's own default handling for:
 - Server Error `500`
 - Bad Request `400`
 - Not Acceptable `406`
 - UnSupported Media Type `415`

Server Error `500`
```bash
$ curl -H "Content-Type: application/json; charset=utf-8" -i http://localhost:8080/rest/v2/broken
```

```json
HTTP/1.1 500 Server Error
Date: Sat, 17 Aug 2019 16:38:53 GMT
Content-Length: 43

{
  "message": "An unexpected error occurred."
}
```

Bad Request `400`
```bash
$ curl -X POST -H "Content-Type: application/json; charset=utf-8" -H "Accept: application/json; charset=utf-8;" -i http://localhost:8080/rest/v3/hello
```

```json
HTTP/1.1 400 Bad Request
Date: Sat, 17 Aug 2019 16:35:54 GMT
Content-Length: 102

{
  "causes": [
      {
          "source": "BODY",
          "key": null,
          "actual": null,
          "expected": null,
          "reason": "The payload could not be parsed."
      }
  ]
}
```

Not Supported `406`
```bash
$ curl -X GET -H "Content-Type: application/json; charset=utf-8" -i http://localhost:8080/rest/v2/hello
```

```json
HTTP/1.1 406 Not Acceptable
Date: Mon, 21 Oct 2019 11:53:29 GMT
Content-Length: 110

{
"causes": [
  {
      "source": "HEADER",
      "key": "ACCEPT",
      "actual": null,
      "expected":
        ["application/json; charset=utf-8;"],
      "reason":null
  }
]
}
```

Unsupported Media Type `415`
```bash
$ curl -i http://localhost:8080/rest/v2/hello
```

```json
HTTP/1.1 415 Unsupported Media Type
Date: Sat, 17 Aug 2019 16:30:01 GMT
Content-Length: 124

{
  "causes": [
  {
      "source": "HEADER",
      "key": "CONTENT_TYPE",
      "actual": "null/null;",
      "expected": [
        "application/json; charset=utf-8;"
      ],
      "reason": null
  }
]
}
```

The errors that can be recovered from are:
 - Server Error `500`
 - Bad Request `400`
 - Not Supported `406`
 - Unsuppored Media Type `415`
 
Everything else should be able to be handled with in a `RestResource`.

To configure a `RestGroup` to apply error handlers to all its related `RestTargets`.
```java
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

    RestGroup<DummySession, ApiUser> apiGroupV3 = new RestGroupBuilder<DummySession, ApiUser>()
            .name(API_GROUP_V3)
            .sessionClazz(DefaultSession.class)
            .before(Label.AUTH_OPTIONAL, authRestBetween)            
            .before(Label.AUTH_REQUIRED, authRestBetween)
            .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
            .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
            .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaTypeTarget)
            .onDispatchError(StatusCode.NOT_ACCEPTABLE, notAcceptableTarget)
            .build();
```

To override or add error handling to a `RestTarget`.
```java
    BadRequestResource badRequestResource = new BadRequestResource();
    ServerErrorResource serverErrorResource = new ServerErrorResource();

    RestResource<ApiUser, ClientError> mediaTypeResource = new MediaTypeResource<>();
    RestErrorTarget<ApiUser, ClientError> mediaTypeTarget = new RestErrorTargetBuilder<ApiUser, ClientError>()
            .payload(ClientError.class)
            .resource(mediaTypeResource)
            .build();

    RestTarget<ApiUser, Hello> helloApiV2 = new RestTargetBuilder<ApiUser, Hello>()
            .groupName(API_GROUP_V2)
            .method(Method.GET)
            .method(Method.POST)
            .restResource(new HelloRestResource())
            .regex(HelloRestResource.URL)
            .authenticate()
            .contentType(json)
            .payload(Hello.class)
            .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
            .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
            .onDispatchError(StatusCode.UNSUPPORTED_MEDIA_TYPE, mediaTypeTarget)
            .build();
```


### Not Founds
To configure how to handle urls that are not found use the interface, `gateway.notFound(..)` for both `Target` and 
`RestTarget`. The regex must be specified which will be used to determine which `Resource` or `RestResouce` to execute.
This allows applications to have many ways to react to a not found url based on the url regex.

### Configuration

#### Configure
Configuring otter is done by implementing [Configure](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/gateway/Configure.java). 
The implementation instructs otter how to:
 - Set cookie configuration for CSRF, Session, and other cookies
 - Set CSRF signature key
 - Set the status code when CSRF fails
 - Set the template when CSRF fails
 - Set Session encryption signature key
 - Set the status code when the Session is not present
 - Set the template when the Session is not present
 - Read and Write chunk sizes - use for async i/o.
 - Route requests to Resources
 - Route requests to RestResources
 - Group Resources together to use the same Session and User
 - Group RestResources together to use the same User
 - Handle Errors

Have a look a the hello world application for an [example](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/config/AppConfig.java).

#### Entry Servlet
An otter application needs to extend [OtterEntryServlet](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/servlet/OtterEntryServlet.java). This is needed to route requests from the servlet conatiner
to otter. 

It must override the following:
```java
    @Override
    public Configure makeConfigure() {
        return new AppConfig(new AppFactory());
    }
```

`makeConfigure()` must return your `configure` implementation.

Have a look a the hello world application for an [example](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/config/AppEntryServlet.java).

#### Main Method

Otter runs in a Jetty powered [embedded servlet container](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/server/HelloServer.java).
The port, document root, and the request log are all configurable.

Have a look a the hello world application for an [example](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/server/HelloServer.java)

#### Compression

Otter is able to compress the response body with `gzip`. To enable it then add the `MimeTypes` to compress in the [Server 
implementation](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/server/HelloServer.java#L19-L22).

With the hello world project:
```bash
$ curl -H "Content-Type: application/json; charset=utf-8" -H "Accept-Encoding: gzip" -i http://localhost:8080/rest/v2/hello

HTTP/1.1 200 OK
Date: Sat, 24 Aug 2019 13:03:54 GMT
Vary: Accept-Encoding, User-Agent
Content-Encoding: gzip
Content-Length: 55

O?S?N??O?T?jW&?#
```

### CSRF

Otter supports CSRF protection by implementing the double submit strategy.

#### Resource Protection
Here is an example of how to protect a login page:

In the configure implementation:
```java
    Target<TokenSession, User> login = new TargetBuilder<TokenSession, User>()
            .groupName(WEB_SITE_GROUP)
            .form()
            .resource(new LoginResource())
            .regex(LoginResource.URL)
            .build();
    
    gateway.add(login);
```

In the [Login Resource](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/controller/html/LoginResource.java#L20) 
set the csrf challenge token to the appropriate ivar in the [login presenter](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/java/net/tokensmith/hello/controller/html/presenter/LoginPresenter.java).
```java
    LoginPresenter presenter = new LoginPresenter("", request.getCsrfChallenge().get());
```

Render the [CSRF challenge token](https://github.com/tokensmith/otter/blob/development/examples/hello-world/src/main/webapp/WEB-INF/jsp/login.jsp#L12) 
on the page.
```java
    <input id="csrfToken" type="hidden" name="csrfToken" value="${presenter.getCsrfChallengeToken()}" / >
```

Done, it is CSRF protected.

#### RestResource Protection
Here is an example of how to protect an API. The use case is when javascript in a browser wants to call an API that is protected.

- Browser calls a URI backed by a `Resource` that is CSRF protected, `text/html`.
- Place the `csrfToken` into a meta tag. See [OSWAP's recommendation](https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html#storing-the-csrf-token-value-in-the-dom)
```java
<meta name="csrf-token" content="${presenter.getCsrfChallengeToken()}">
```
  
- Then use javascript to read the value and send it in the header, `X-CSRF`.


Configuration.
```java
    RestTarget<TokenSession, ApiUser, Hello> helloCsrfApiV2 = new RestTargetBuilder<TokenSession, ApiUser, Hello>()
            .groupName(API_GROUP_V2)
            .method(Method.GET)
            .restResource(new HelloCsrfRestResource())
            .regex(HelloCsrfRestResource.URL)
            .csrf() // <-- csrf protects all methods.
            .authenticate()
            .contentType(json)
            .payload(Hello.class)
            .build();
```

To pass it will need the CSRF cookie and and the header, `X-CSRF`.

#### CSRF failure

When a CSRF fails then Otter will return:
- status code of 403
- remove csrf cookie

This behavior can be overridden by implementing a halt bifunction and passing it into it's group.

```java
    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
        .name(WEB_SITE_GROUP)
        .sessionClazz(TokenSession.class)
        .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
        .before(Label.AUTH_REQUIRED, new AuthBetween())
        .onHalt(Halt.CSRF, (Response<TokenSession> response, HaltException e) -> {
            response.setTemplate(Optional.of("/WEB-INF/jsp/403.jsp"));
            response.setStatusCode(StatusCode.FORBIDDEN);
            return response;
        })
        .onError(StatusCode.SERVER_ERROR, serverErrorResource)
        .build();
```

#### Custom CSRF implemention.

Custom implementations of CSRF can be injected into [Groups]((#group)) and [RestGroups](#restgroup)

##### Group
Implement two betweens that handle preparing and protecting CSRF then inject them with the `before` interface.
```java
    Group<TokenSession, User> webSiteGroup = new GroupBuilder<TokenSession, User>()
        .name(WEB_SITE_GROUP)
        .sessionClazz(TokenSession.class)
        .before(Label.AUTH_OPTIONAL, new AuthOptBetween())            
        .before(Label.AUTH_REQUIRED, new AuthBetween())
        .before(Label.CSRF_PROTECT, csrfProtect) // <-- custom csrf protect
        .before(Label.CSRF_PREPARE, csrfPrepare) // <-- custom csrf prepare
        .onError(StatusCode.SERVER_ERROR, serverErrorResource)
        .build();
    
```

These would then be used instead of the defaults for all the targets in that group.

##### RestGroup

Implement one betweens that handle protecting CSRF then inject it with the `before` interface.
```java
    BadRequestResource badRequestResource = new BadRequestResource();
    ServerErrorResource serverErrorResource = new ServerErrorResource();
    RestGroup<DummySession, ApiUser> apiGroupV3 = new RestGroupBuilder<DummySession, ApiUser>()
            .name(API_GROUP_V3)
            .sessionClazz(DefaultSession.class)
            .before(Label.AUTH_OPTIONAL, authRestBetween)            
            .before(Label.AUTH_REQUIRED, authRestBetween)
            .before(Label.CSRF_PROTECT, csrfProtect) // <-- custom csrf protect
            .onError(StatusCode.BAD_REQUEST, badRequestResource, BadRequestPayload.class)
            .onError(StatusCode.SERVER_ERROR, serverErrorResource, ServerErrorPayload.class)
            .build();
```

It would then be used instead of the default for all the restTargets in that restGroup.


### Static Assets

Files that are placed in, `src/main/webapp/public` are public as long as they pass the entry filter [regex](https://github.com/tokensmith/otter/blob/development/otter/src/main/java/net/tokensmith/otter/servlet/EntryFilter.java#L18).

For example, `src/main/webapp/public/assets/js/jquery-3.3.1.min.js` can be retrieved from, `assets/js/jquery-3.3.1.min.js`