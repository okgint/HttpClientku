package org.ogin.message;

import org.ogin.body.HttpBody;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Request {
    private final String method;
    private final String url;
    private final List<Header> headers;
    private final HttpBody body;
    /**
     * Some object to mark a request.
     * For example, it'is using for cancellation in OkClient, ApacheClient, UrlConnectionClient
     */
    public volatile Object tag;

    public Request(String method, String url, List<Header> headers, HttpBody body) {
        if (method == null) {
            throw new NullPointerException("Method must not be null.");
        }
        if (url == null) {
            throw new NullPointerException("URL must not be null.");
        }
        this.method = method;
        this.url = url;

        if (headers == null) {
            this.headers = Collections.emptyList();
        } else {
            this.headers = Collections.unmodifiableList(new ArrayList<Header>(headers));
        }

        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public HttpBody getBody() {
        return body;
    }
}
