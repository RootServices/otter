package org.rootservices.otter.gateway.builder;

import helper.entity.*;
import org.junit.Test;
import org.rootservices.otter.controller.builder.MimeTypeBuilder;
import org.rootservices.otter.controller.entity.StatusCode;
import org.rootservices.otter.controller.entity.mime.MimeType;
import org.rootservices.otter.gateway.entity.*;
import org.rootservices.otter.router.entity.Method;

import java.util.ArrayList;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class RestTargetBuilderTest {

    public RestTargetBuilder<DummyUser, DummyPayload> subject() {
        return  new RestTargetBuilder<>();
    }

    @Test
    public void buildShouldHaveEmptyLists() {
        RestTargetBuilder<DummyUser, DummyPayload> subject = subject();

        RestTarget<DummyUser, DummyPayload> actual = subject.build();

        assertThat(actual.getContentTypes().size(), is(0));
        assertThat(actual.getMethods().size(), is(0));
        assertThat(actual.getLabels().size(), is(0));
        assertThat(actual.getBefore().size(), is(0));
        assertThat(actual.getAfter().size(), is(0));
    }

    @Test
    public void buildShouldBeOk() {
        RestTargetBuilder<DummyUser, DummyPayload> subject = subject();

        OkRestResource notFoundResource = new OkRestResource();
        RestErrorTarget<DummyUser, DummyPayload> notFound = new RestErrorTarget<>(
                notFoundResource, new ArrayList<>(), new ArrayList<>()
        );

        OkRestResource okRestResource = new OkRestResource();
        MimeType json = new MimeTypeBuilder().json().build();

        RestTarget<DummyUser, DummyPayload> actual = subject
                .regex("/foo")
                .method(Method.GET)
                .method(Method.POST)
                .contentType(json)
                .restResource(okRestResource)
                .before(new DummyRestBetween<>())
                .before(new DummyRestBetween<>())
                .after(new DummyRestBetween<>())
                .after(new DummyRestBetween<>())
                .label(Label.CSRF)
                .label(Label.SESSION_REQUIRED)
                .errorTarget(StatusCode.NOT_FOUND, notFound)
                .build();

        assertThat(actual.getRegex(), is("/foo"));
        assertThat(actual.getMethods().get(0), is(Method.GET));
        assertThat(actual.getMethods().get(1), is(Method.POST));
        assertThat(actual.getRestResource(), is(okRestResource));
        assertThat(actual.getContentTypes().size(), is(9));
        assertThat(actual.getContentTypes().get(Method.GET).get(0), is(json));
        assertThat(actual.getContentTypes().get(Method.POST).get(0), is(json));
        assertThat(actual.getBefore().size(), is(2));
        assertThat(actual.getAfter().size(), is(2));
        assertThat(actual.getLabels().size(), is(2));
        assertThat(actual.getLabels().get(0), is(Label.CSRF));
        assertThat(actual.getLabels().get(1), is(Label.SESSION_REQUIRED));
        assertThat(actual.getErrorTargets().size(), is(1));
        assertThat(actual.getErrorTargets().get(StatusCode.NOT_FOUND).getResource(), is(notFoundResource));
    }

    @Test
    public void buildWhenMethodContentTypeShouldBeOk() {
        RestTargetBuilder<DummyUser, DummyPayload> subject = subject();

        OkRestResource notFoundResource = new OkRestResource();
        RestErrorTarget<DummyUser, DummyPayload> notFound = new RestErrorTarget<>(
                notFoundResource, new ArrayList<>(), new ArrayList<>()
        );

        OkRestResource okRestResource = new OkRestResource();

        MimeType json = new MimeTypeBuilder().json().build();
        MimeType jwt = new MimeTypeBuilder().jwt().build();

        RestTarget<DummyUser, DummyPayload> actual = subject
                .regex("/foo")
                .method(Method.GET)
                .method(Method.POST)
                .contentType(Method.GET, jwt)
                .contentType(json)
                .restResource(okRestResource)
                .before(new DummyRestBetween<>())
                .before(new DummyRestBetween<>())
                .after(new DummyRestBetween<>())
                .after(new DummyRestBetween<>())
                .label(Label.CSRF)
                .label(Label.SESSION_REQUIRED)
                .errorTarget(StatusCode.NOT_FOUND, notFound)
                .build();


        assertThat(actual.getContentTypes().size(), is(9));
        assertThat(actual.getContentTypes().get(Method.GET).get(0), is(jwt));
        assertThat(actual.getContentTypes().get(Method.GET).get(1), is(json));
        assertThat(actual.getContentTypes().get(Method.POST).get(0), is(json));
    }
}