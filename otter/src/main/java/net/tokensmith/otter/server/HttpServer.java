package net.tokensmith.otter.server;


import net.tokensmith.otter.config.OtterAppFactory;
import net.tokensmith.otter.server.container.ServletContainer;
import net.tokensmith.otter.server.container.ServletContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;


public class HttpServer {
    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
    private static ServletContainer server;

    public static void run(HttpServerConfig config) {
        server = makeServer(config);

        try {
            logger.info("server starting");
            server.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        try {
            server.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static ServletContainer makeServer(HttpServerConfig config) {
        OtterAppFactory otterAppFactory = new OtterAppFactory();
        ServletContainerFactory servletContainerFactory = otterAppFactory.servletContainerFactory();

        ServletContainer server = null;
        try {
            server = servletContainerFactory.makeServletContainer(config);
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return server;
    }

    public static ServletContainer getServer() {
        return server;
    }
}
