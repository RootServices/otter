package org.rootservices.otter.servlet.async;


import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


import javax.servlet.AsyncContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


public class WriteListenerImpl implements WriteListener {
    protected static Logger logger = LogManager.getLogger(WriteListenerImpl.class);
    private ServletOutputStream output = null;
    private Queue queue = null;
    private AsyncContext context = null;

    public WriteListenerImpl(ServletOutputStream sos, Queue q, AsyncContext c) {
        output = sos;
        queue = q;
        context = c;
    }

    @Override
    public void onWritePossible() throws IOException {
        int i = 0;
        while (queue.peek() != null && output.isReady()) {
            byte[] data = (byte[]) queue.poll();
            output.write(data, i, data.length);

            if(i==0) {
                i++;
            }

            i=(i*data.length)+1;
        }
        if (queue.peek() == null) {
            context.complete();
        }
    }

    @Override
    public void onError(Throwable t) {
        context.complete();
        logger.error(t.getMessage(), t);
    }
}