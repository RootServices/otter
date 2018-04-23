package org.rootservices.otter.server.path;

import integration.app.hello.controller.HelloResource;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import suite.UnitTest;

import java.net.URI;
import java.security.ProtectionDomain;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;


@Category(UnitTest.class)
public class CompiledClassPathTest {

    private CompiledClassPath subject;

    @Before
    public void setUp() throws Exception {
        subject = new CompiledClassPath();
    }

    @Test
    public void getForClassShouldReturnPathToCompliedClasses() throws Exception {
        URI actual = subject.getForClass(WebAppPath.class);

        String actualPath = actual.getPath();

        String expected = "/otter/build/classes/java/main/";
        assertThat(actualPath + " does not end with " + expected, actualPath.endsWith(expected), is(true));
    }
}