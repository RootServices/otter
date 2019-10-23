package net.tokensmith.otter.gateway.servlet.translator;


import net.tokensmith.otter.controller.entity.Cookie;

import java.util.function.Function;

public class HttpServletRequestCookieTranslator {

    public Function<javax.servlet.http.Cookie, Cookie> from
            = new Function<javax.servlet.http.Cookie, Cookie>() {

        public Cookie apply(javax.servlet.http.Cookie containerCookie) {
            Cookie otterCookie = new Cookie();
            otterCookie.setName(containerCookie.getName());
            otterCookie.setValue(containerCookie.getValue());
            otterCookie.setDomain(containerCookie.getDomain());
            otterCookie.setMaxAge(containerCookie.getMaxAge());
            otterCookie.setPath(containerCookie.getPath());
            otterCookie.setVersion(containerCookie.getVersion());
            otterCookie.setSecure(containerCookie.getSecure());
            otterCookie.setHttpOnly(containerCookie.isHttpOnly());

            return otterCookie;
        }
    };

    public Function<Cookie, javax.servlet.http.Cookie> to
            = new Function<Cookie, javax.servlet.http.Cookie>() {

        public javax.servlet.http.Cookie apply(Cookie otterCookie) {
            javax.servlet.http.Cookie containerCookie = new javax.servlet.http.Cookie(otterCookie.getName(), otterCookie.getValue());
            containerCookie.setComment(otterCookie.getComment());

            if(otterCookie.getDomain() != null) {
                containerCookie.setDomain(otterCookie.getDomain());
            }

            containerCookie.setMaxAge(otterCookie.getMaxAge());
            containerCookie.setPath(otterCookie.getPath());
            containerCookie.setSecure(otterCookie.isSecure());
            containerCookie.setVersion(otterCookie.getVersion());
            containerCookie.setHttpOnly(otterCookie.isHttpOnly());
            return containerCookie;
        }
    };
}