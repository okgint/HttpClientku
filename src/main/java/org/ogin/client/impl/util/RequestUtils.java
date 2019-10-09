package org.ogin.client.impl.util;

import org.ogin.message.Request;

import java.io.IOException;

public class RequestUtils {
    public static final Object TAG_CANCELED = new Object();

    public static void throwIfCanceled(Request request) throws IOException {
        if (request.tag == TAG_CANCELED) {
            throw new IOException("Request is canceled");
        }
    }
}
