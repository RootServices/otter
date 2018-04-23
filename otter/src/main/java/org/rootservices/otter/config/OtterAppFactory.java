package org.rootservices.otter.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.rootservices.jwt.config.JwtAppFactory;
import org.rootservices.otter.QueryStringToMap;
import org.rootservices.otter.gateway.servlet.ServletGateway;
import org.rootservices.otter.gateway.servlet.merger.HttpServletRequestMerger;
import org.rootservices.otter.gateway.servlet.merger.HttpServletResponseMerger;
import org.rootservices.otter.gateway.servlet.translator.HttpServletRequestCookieTranslator;
import org.rootservices.otter.gateway.servlet.translator.HttpServletRequestHeaderTranslator;
import org.rootservices.otter.gateway.servlet.translator.HttpServletRequestTranslator;
import org.rootservices.otter.router.Dispatcher;
import org.rootservices.otter.router.Engine;
import org.rootservices.otter.router.entity.Between;
import org.rootservices.otter.security.RandomString;
import org.rootservices.otter.security.csrf.DoubleSubmitCSRF;
import org.rootservices.otter.security.csrf.between.CheckCSRF;
import org.rootservices.otter.security.csrf.between.PrepareCSRF;
import org.rootservices.otter.security.session.between.EncryptSession;
import org.rootservices.otter.server.container.ServletContainerFactory;
import org.rootservices.otter.server.path.CompiledClassPath;
import org.rootservices.otter.server.path.WebAppPath;
import org.rootservices.otter.translator.JsonTranslator;

import java.util.Base64;


/**
 * Application Factory to construct objects in project.
 */
public class OtterAppFactory {
    private static ObjectMapper objectMapper;

    public CompiledClassPath compiledClassPath() {
        return new CompiledClassPath();
    }

    public WebAppPath webAppPath() {
        return new WebAppPath();
    }

    public ServletContainerFactory servletContainerFactory() {
        return new ServletContainerFactory(
                compiledClassPath(),
                webAppPath()
        );
    }

    public ServletGateway servletGateway() {
        DoubleSubmitCSRF doubleSubmitCSRF = doubleSubmitCSRF();

        return new ServletGateway(
                httpServletRequestTranslator(),
                httpServletRequestMerger(),
                httpServletResponseMerger(),
                engine(),
                prepareCSRF(doubleSubmitCSRF),
                checkCSRF(doubleSubmitCSRF),
                encryptSession()
        );
    }

    public Engine engine() {
        return new Engine(new Dispatcher());
    }

    public JsonTranslator jsonTranslator() {
        return new JsonTranslator(objectMapper());
    }

    public ObjectMapper objectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper()
                    .setPropertyNamingStrategy(
                            PropertyNamingStrategy.SNAKE_CASE
                    )
                    .configure(JsonParser.Feature.STRICT_DUPLICATE_DETECTION, true)
                    .registerModule(new Jdk8Module())
                    .registerModule(new JavaTimeModule());
        }
        return objectMapper;
    }

    public HttpServletRequestTranslator httpServletRequestTranslator() {
        return new HttpServletRequestTranslator(
                httpServletRequestCookieTranslator(),
                new HttpServletRequestHeaderTranslator(),
                new QueryStringToMap()
        );
    }

    public HttpServletRequestMerger httpServletRequestMerger() {
        return new HttpServletRequestMerger();
    }

    public HttpServletResponseMerger httpServletResponseMerger() {
        return new HttpServletResponseMerger(httpServletRequestCookieTranslator());
    }

    public HttpServletRequestCookieTranslator httpServletRequestCookieTranslator() {
        return new HttpServletRequestCookieTranslator();
    }

    public JwtAppFactory jwtAppFactory() {
        return new JwtAppFactory();
    }

    public DoubleSubmitCSRF doubleSubmitCSRF() {
        return new DoubleSubmitCSRF(jwtAppFactory(), new RandomString());
    }

    public Between checkCSRF(DoubleSubmitCSRF doubleSubmitCSRF) {
        return new CheckCSRF(doubleSubmitCSRF);
    }

    public Between prepareCSRF(DoubleSubmitCSRF doubleSubmitCSRF) {
        return new PrepareCSRF(doubleSubmitCSRF);
    }

    public Base64.Decoder urlDecoder() {
        return Base64.getUrlDecoder();
    }

    public EncryptSession encryptSession() {
        return new EncryptSession(
                jwtAppFactory(),
                urlDecoder(),
                objectMapper()
        );
    }
}