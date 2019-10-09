package org.ogin.client;

import org.ogin.message.Request;
import org.ogin.message.Response;

import java.io.IOException;

public interface HttpClient {
    int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    long PROGRESS_THRESHOLD = 1024;


    Response execute(Request request, RequestCallback requestCallback) throws IOException;
    void cancel(Request request);

    interface RequestCallback {
        void onProgress(int progress);
    }
}
