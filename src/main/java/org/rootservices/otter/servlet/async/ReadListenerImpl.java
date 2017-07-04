package org.rootservices.otter.servlet.async;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rootservices.otter.gateway.servlet.ServletGateway;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * https://webtide.com/servlet-3-1-async-io-and-jetty/
 * http://www.oracle.com/webfolder/technetwork/tutorials/obe/java/HTML5andServlet31/HTML5andServlet%203.1.html#section3
 * https://www.youtube.com/watch?v=uGXsnB2S_vc
 * https://www.youtube.com/watch?v=j1VjAHTxCBo&t=8s
 */
public class ReadListenerImpl implements ReadListener {
    protected static Logger logger = LogManager.getLogger(ReadListenerImpl.class);
    private ServletGateway servletGateway;
    private ServletInputStream input = null;
    private AsyncContext ac = null;
    private Queue queue = new LinkedBlockingQueue();

    public ReadListenerImpl(ServletGateway sg, ServletInputStream in, AsyncContext c) {
        servletGateway = sg;
        input = in;
        ac = c;
    }

    @Override
    public void onDataAvailable() throws IOException {
        StringBuilder sb = new StringBuilder();
        int len = -1;
        byte b[] = new byte[1024];
        while (input.isReady() && (len = input.read(b)) != -1 && !input.isFinished()) {
            String data = new String(b, 0, len);
            sb.append(data);
        }
        queue.add(sb.toString());
    }

    @Override
    public void onAllDataRead() throws IOException {
        HttpServletRequest request = (HttpServletRequest) ac.getRequest();
        HttpServletResponse response = (HttpServletResponse) ac.getResponse();
        Optional<byte[]> payload = servletGateway.processRequest(ac, request, response);

        if (payload.isPresent()) {
            Queue out = byteArrayToQueue(payload.get(), 1024);
            ServletOutputStream output = response.getOutputStream();
            WriteListener writeListener = new WriteListenerImpl(output, out, ac);
            output.setWriteListener(writeListener);
        } else {
            Queue out = new LinkedBlockingQueue();
            ServletOutputStream output = response.getOutputStream();
            WriteListener writeListener = new WriteListenerImpl(output, out, ac);
            output.setWriteListener(writeListener);
        }
    }

    @Override
    public void onError(Throwable t) {
        logger.error(t.getMessage(), t);
        ac.complete();
    }

    /**
     * https://stackoverflow.com/questions/3405195/divide-array-into-smaller-parts
     *
     * @param source
     * @param chunksize
     * @return
     */
    public Queue byteArrayToQueue(byte[] source, int chunksize) {
        Queue out = new LinkedBlockingQueue();

        int start = 0;
        while (start < source.length) {
            int end = Math.min(source.length, start + chunksize);
            out.add(Arrays.copyOfRange(source, start, end));
            start += chunksize;
        }

        return out;
    }
}