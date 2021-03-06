package net.tokensmith.otter.controller.builder;

import helper.FixtureFactory;
import helper.entity.model.DummySession;
import helper.entity.model.DummyUser;
import net.tokensmith.otter.controller.entity.Cookie;
import net.tokensmith.otter.controller.entity.mime.MimeType;
import net.tokensmith.otter.controller.entity.request.Request;
import net.tokensmith.otter.router.entity.Method;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


public class RequestBuilderTest {
    private RequestBuilder<DummySession, DummyUser> subject;

    @Before
    public void setUp() {
        subject = new RequestBuilder<DummySession, DummyUser>();
    }

    @Test
    public void buildWhenMatcherShouldBeOk() {
        Request<DummySession, DummyUser> actual = subject.matcher(Optional.empty()).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getMatcher().isPresent(), is(false));
    }

    @Test
    public void buildWhenBaseURIPartsShouldBeOk() {
        String scheme = "http";
        String authority = "tokensmith.net";
        Integer port = 443;

        Request<DummySession, DummyUser> actual = subject.scheme(scheme)
                .authority(authority)
                .port(port)
                .build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getScheme(), is(scheme));
        assertThat(actual.getAuthority(), is(authority));
        assertThat(actual.getPort(), is(port));
    }

    @Test
    public void buildWhenPathWithParamsShouldBeOk() {
        String url = "/pathWithParams";
        Request<DummySession, DummyUser> actual = subject.pathWithParams(url).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getPathWithParams(), is(url));
    }

    @Test
    public void buildWhenMethodShouldBeOk() {
        Request<DummySession, DummyUser> actual = subject.method(Method.GET).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getMethod(), is(Method.GET));
    }

    @Test
    public void buildWhenContentTypeShouldBeOk() {

        MimeType contentType = new MimeType();
        Request<DummySession, DummyUser> actual = subject.contentType(contentType).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getContentType(), is(notNullValue()));
        assertThat(actual.getContentType(), is(contentType));
    }

    @Test
    public void buildWhenAcceptShouldBeOk() {

        MimeType contentType = new MimeType();
        Request<DummySession, DummyUser> actual = subject.accept(contentType).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getAccept(), is(notNullValue()));
        assertThat(actual.getAccept(), is(contentType));
    }

    @Test
    public void buildWhenHeadersShouldBeOk() {
        Map<String, String> headers = FixtureFactory.makeHeaders();

        Request<DummySession, DummyUser> actual = subject.headers(headers).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getHeaders(), is(notNullValue()));
        assertThat(actual.getHeaders(), is(headers));
    }

    @Test
    public void buildWhenCookiesShouldBeOk() {
        Map<String, Cookie> cookies = FixtureFactory.makeCookies();

        Request<DummySession, DummyUser> actual = subject.cookies(cookies).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getCookies(), is(cookies));
    }

    @Test
    public void buildWhenQueryParamsShouldBeOk() {
        Map<String, List<String>> queryParams = FixtureFactory.makeEmptyQueryParams();

        Request<DummySession, DummyUser> actual = subject.queryParams(queryParams).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getQueryParams(), is(queryParams));
    }

    @Test
    public void buildWhenFormDataShouldBeOk() {
        Map<String, List<String>> formData = new HashMap<>();
        formData.put("foo", Arrays.asList("bar"));

        Request<DummySession, DummyUser> actual = subject.formData(formData).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getFormData(), is(formData));
    }

    @Test
    public void buildWhenPayloadShouldBeOk() {
        byte[] body = "{\"integer\": 5, \"unknown_key\": \"4\", \"local_date\": \"2019-01-01\"}".getBytes();
        Optional<byte[]> json = Optional.of(body);

        Request<DummySession, DummyUser> actual = subject.body(json).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getBody(), is(json));
    }

    @Test
    public void buildWhenIpAddressShouldBeOk() {

        String ipAddress = "127.0.0.1";
        Request<DummySession, DummyUser> actual = subject.ipAddress(ipAddress).build();

        assertThat(actual, is(notNullValue()));
        assertThat(actual.getIpAddress(), is(ipAddress));
    }
}