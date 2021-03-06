package net.tokensmith.otter.server.container.builder;


import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.WebAppContext;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WebAppContextBuilder {
    private ClassLoader classLoader;
    private String resourceBase;
    private Configuration[] configurations;
    private File tempDirectory;
    private PathResource containerResource;
    private Boolean parentLoaderPriority;
    private Map<String, String> initParameters = new HashMap<>();
    private Map<String, String> attributes = new HashMap<>();
    private List<ServletHolder> servletHolders = new ArrayList<>();
    private List<String> gzipMimeTypes = new ArrayList<>();
    private List<ErrorPage> errorPages = new ArrayList<>();
    private SessionHandler sessionHandler;

    public WebAppContextBuilder classLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public WebAppContextBuilder resourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
        return this;
    }

    public WebAppContextBuilder configurations(Configuration[] configurations) {
        this.configurations = configurations;
        return this;
    }

    public WebAppContextBuilder tempDirectory(File tempDirectory) {
        this.tempDirectory = tempDirectory;
        return this;
    }

    public WebAppContextBuilder containerResource(PathResource containerResource) {
        this.containerResource = containerResource;
        return this;
    }

    public WebAppContextBuilder parentLoaderPriority(Boolean parentLoaderPriority) {
        this.parentLoaderPriority = parentLoaderPriority;
        return this;
    }

    public WebAppContextBuilder initParameter(String key, String value) {
        this.initParameters.put(key, value);
        return this;
    }

    public WebAppContextBuilder attribute(String key, String value) {
        this.attributes.put(key, value);
        return this;
    }

    public WebAppContextBuilder jspServlet(String className) {
        ServletHolder jspHolder = new ServletHolder();
        jspHolder.setName("jsp");
        jspHolder.setClassName(className);
        jspHolder.setInitParameter("async-supported", "true");
        jspHolder.setInitParameter("fork", "false");

        servletHolders.add(jspHolder);

        return this;
    }

    /**
     * Configure delivery of static assets if you know the absolute path to the assets.
     *
     * @param resourceBase absolute file path to the webapp directory in your project.
     * @return an instance of, WebAppContextBuilder
     */
    public WebAppContextBuilder staticAssetServlet(String resourceBase) {
        ServletHolder defaultServletHolder = new ServletHolder("default", DefaultServlet.class);
        defaultServletHolder.setInitParameter("resourceBase", resourceBase);

        servletHolders.add(defaultServletHolder);
        return this;
    }

    /**
     * Configure delivery of static assets if they are included in the war file.
     *
     * @param resourceBase absolute file path to the webapp directory in your project.
     * @return an instance of, WebAppContextBuilder
     */
    public WebAppContextBuilder staticAssetServletWar(String resourceBase) {
        ServletHolder defaultServletHolder = new ServletHolder("default", DefaultServlet.class);
        defaultServletHolder.setInitParameter("relativeResourceBase", resourceBase);

        servletHolders.add(defaultServletHolder);
        return this;
    }

    public WebAppContextBuilder gzipMimeTypes(List<String> gzipMimeTypes) {
        this.gzipMimeTypes = gzipMimeTypes;
        return this;
    }

    public WebAppContextBuilder errorPages(List<ErrorPage> errorPages) {
        errorPages.addAll(errorPages);
        return this;
    }

    public WebAppContextBuilder stateless() {
        this.sessionHandler = new SessionHandler();
        this.sessionHandler.setUsingCookies(false);
        return this;
    }

    public WebAppContext build() {
        WebAppContext webAppContext = new WebAppContext();

        webAppContext.setClassLoader(this.classLoader);

        if (Objects.nonNull(resourceBase)){
            webAppContext.setResourceBase(resourceBase);
        }

        webAppContext.setConfigurations(configurations);

        webAppContext.setTempDirectory(tempDirectory);

        if (Objects.nonNull(containerResource)) {
            webAppContext.getMetaData().addContainerResource(containerResource);
        }

        webAppContext.setParentLoaderPriority(parentLoaderPriority);

        for(Map.Entry<String, String> param: initParameters.entrySet()) {
            webAppContext.setInitParameter(param.getKey(), param.getValue());
        }

        for(Map.Entry<String, String> attr: attributes.entrySet()) {
            webAppContext.setAttribute(attr.getKey(), attr.getValue());
        }

        int maxInitOrder = 0;
        for (ServletHolder servletHolder: webAppContext.getServletHandler().getServlets()) {
            if (servletHolder.getInitOrder() > maxInitOrder) {
                maxInitOrder = servletHolder.getInitOrder();
            }
        }

        for(ServletHolder holder: servletHolders) {
            maxInitOrder+=1;
            holder.setInitOrder(maxInitOrder);
            webAppContext.getServletHandler().addServlet(holder);
        }

        // 114: need to inject in custom error handler here.
        ErrorPageErrorHandler errorHandler = (ErrorPageErrorHandler) webAppContext.getErrorHandler();
        for(ErrorPage errorPage: errorPages) {
            errorHandler.addErrorPage(errorPage.getErrorCode(), errorPage.getLocation());
        }

        if (Objects.nonNull(gzipMimeTypes) && gzipMimeTypes.size() > 0) {
            GzipHandler gzipHandler = new GzipHandler();
            gzipHandler.setIncludedMimeTypes(
                    gzipMimeTypes.toArray(new String[gzipMimeTypes.size()])
            );
            webAppContext.setGzipHandler(gzipHandler);
        }
        webAppContext.setSessionHandler(this.sessionHandler);
        return webAppContext;
    }

}
