package net.tokensmith.hello.controller;


import net.tokensmith.otter.controller.entity.StatusCode;
import org.asynchttpclient.ListenableFuture;
import org.asynchttpclient.Response;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import suite.IntegrationTestSuite;
import suite.ServletContainerTest;

import java.net.URI;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@Category(ServletContainerTest.class)
public class HelloResourceTest {
    private static URI BASE_URI;

    @BeforeClass
    public static void beforeClass() {
        BASE_URI = IntegrationTestSuite.getServletContainerURI();
    }

    @Test
    public void getShouldReturn200() throws Exception {

        String helloURI = BASE_URI.toString() + "hello";

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .prepareGet(helloURI)
                .addHeader("Content-Type", "text/html")
                .execute();

        Response response = f.get();

        String errorMsg = "Attempted GET " + helloURI;
        assertThat(errorMsg, response.getStatusCode(), is(StatusCode.OK.getCode()));

        // make sure jsp was executed.
        assertThat(response.getResponseBody().contains("<div id=\"hello\">Hello World</>"), is(true));

    }

    @Test
    public void getWhenNoContentTypeShouldReturn200() throws Exception {

        String helloURI = BASE_URI.toString() + "hello";

        ListenableFuture<Response> f = IntegrationTestSuite.getHttpClient()
                .prepareGet(helloURI)
                .execute();

        Response response = f.get();

        String errorMsg = "Attempted GET " + helloURI;
        assertThat(errorMsg, response.getStatusCode(), is(StatusCode.OK.getCode()));
    }

}