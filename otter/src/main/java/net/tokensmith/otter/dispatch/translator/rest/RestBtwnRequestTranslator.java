package net.tokensmith.otter.dispatch.translator.rest;


import net.tokensmith.otter.controller.entity.request.RestRequest;
import net.tokensmith.otter.dispatch.entity.RestBtwnRequest;
import net.tokensmith.otter.router.entity.io.Ask;

import java.util.Optional;

public class RestBtwnRequestTranslator<U, P> {

    // inbound
    public RestBtwnRequest<U> to(Ask from) {
        RestBtwnRequest<U> to = new RestBtwnRequest<U>();

        to.setMatcher(from.getMatcher());
        to.setPossibleContentTypes(from.getPossibleContentTypes());
        to.setPossibleAccepts(from.getPossibleAccepts());
        to.setMethod(from.getMethod());
        to.setPathWithParams(from.getPathWithParams());
        to.setContentType(from.getContentType());
        to.setAccept(from.getAccept());
        to.setHeaders(from.getHeaders());
        to.setCookies(from.getCookies());
        to.setQueryParams(from.getQueryParams());
        to.setFormData(from.getFormData());
        to.setBody(from.getBody());
        to.setIpAddress(from.getIpAddress());
        to.setUser(Optional.empty());
        to.setBody(from.getBody());

        return to;
    }

    // outbound
    public RestBtwnRequest<U> to(RestRequest<U, P> from) {
        RestBtwnRequest<U> to = new RestBtwnRequest<U>();

        to.setMatcher(from.getMatcher());
        to.setPossibleContentTypes(from.getPossibleContentTypes());
        to.setPossibleAccepts(from.getPossibleAccepts());
        to.setMethod(from.getMethod());
        to.setPathWithParams(from.getPathWithParams());
        to.setContentType(from.getContentType());
        to.setAccept(from.getAccept());
        to.setHeaders(from.getHeaders());
        to.setCookies(from.getCookies());
        to.setQueryParams(from.getQueryParams());
        to.setFormData(from.getFormData());
        to.setBody(from.getBody());
        to.setIpAddress(from.getIpAddress());
        to.setUser(from.getUser());
        to.setBody(from.getBody());

        return to;
    }
}