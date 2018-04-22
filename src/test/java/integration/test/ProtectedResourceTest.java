package integration.test;



import helper.FixtureFactory;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.asynchttpclient.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rootservices.otter.controller.entity.StatusCode;
import suite.IntegrationTestSuite;
import suite.ServletContainerTest;

import java.util.ArrayList;
import java.util.List;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@Category(ServletContainerTest.class)
public class ProtectedResourceTest {
    private static String SUBJECT_URI;
    private static String SUBJECT_PATH = "protected";

    @BeforeClass
    public static void beforeClass() {
        SUBJECT_URI = IntegrationTestSuite.getServletContainerURI().toString() + SUBJECT_PATH;
    }

    @Test
    public void getShouldReturn200() throws Exception {

        Cookie sessionCookie = FixtureFactory.sessionCookie();
        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .prepareGet(SUBJECT_URI)
                .addCookie(sessionCookie)
                .execute();

        Response response = f.get();

        String errorMsg = "Attempted GET " + SUBJECT_URI;
        assertThat(errorMsg, response.getStatusCode(), is(StatusCode.OK.getCode()));

        // the session cookie should still be here.
        Cookie actual = null;
        for(Cookie cookie: response.getCookies()){
            if("session".equals(cookie.name())) {
                actual = cookie;
                break;
            }
        }
        assertThat(actual, is(notNullValue()));
        // TODO: maybe make sure its a jwe?
    }

    @Test
    public void getWhenNoSessionShouldReturn401() throws Exception {

        AsyncHttpClient httpClient = IntegrationTestSuite.getHttpClient();

        ListenableFuture<Response> f = httpClient
                .prepareGet(SUBJECT_URI + "?401")
                .execute();

        Response response = f.get();

        String errorMsg = "Attempted GET " + SUBJECT_URI;
        assertThat(errorMsg, response.getStatusCode(), is(StatusCode.UNAUTHORIZED.getCode()));
    }

    @Test
    public void postShouldReturn200() throws Exception {
        Cookie sessionCookie = FixtureFactory.sessionCookie();
        List<Param> formData = new ArrayList<>();
        formData.add(new Param("email", "obi-wan@rootservices.org"));
        formData.add(new Param("password", "foo"));

        AsyncHttpClient httpClient = IntegrationTestSuite.getHttpClient();

        // this is the POST request
        ListenableFuture<Response> f = httpClient
                .preparePost(SUBJECT_URI)
                .setFormParams(formData)
                .addCookie(sessionCookie)
                .execute();

        Response postResponse = f.get();

        String errorMsg = "Attempted POST " + SUBJECT_URI;
        assertThat(errorMsg, postResponse.getStatusCode(), is(StatusCode.OK.getCode()));
    }

    @Test
    public void postWhenNoSessionShouldReturn403() throws Exception {
        List<Param> formData = new ArrayList<>();
        formData.add(new Param("email", "obi-wan@rootservices.org"));

        AsyncHttpClient httpClient = IntegrationTestSuite.getHttpClient();

        // this is the POST request
        ListenableFuture<Response> f = httpClient
                .preparePost(SUBJECT_URI + "?401")
                .setFormParams(formData)
                .execute();

        Response postResponse = f.get();

        String errorMsg = "Attempted POST " + SUBJECT_URI;
        assertThat(errorMsg, postResponse.getStatusCode(), is(StatusCode.UNAUTHORIZED.getCode()));
    }
}