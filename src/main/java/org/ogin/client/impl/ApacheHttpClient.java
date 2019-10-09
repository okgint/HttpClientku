package org.ogin.client.impl;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.ogin.body.HttpBody;
import org.ogin.client.HttpClient;
import org.ogin.client.impl.internal.ProgressOutputStream;
import org.ogin.client.impl.util.RequestUtils;
import org.ogin.message.Header;
import org.ogin.message.Request;
import org.ogin.message.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class ApacheHttpClient implements HttpClient {
    private final org.apache.http.client.HttpClient client;

    private static org.apache.http.client.HttpClient  createDefaultClient() {
       /* HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECT_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(params, READ_TIMEOUT_MILLIS);*/
        org.apache.http.client.HttpClient httpClient = HttpClients.createDefault();
        return httpClient;
    }

    public ApacheHttpClient() {
        this(createDefaultClient());
    }

    public ApacheHttpClient(org.apache.http.client.HttpClient client) {
        this.client = client;
    }

    @Override
    public Response execute(Request request, RequestCallback requestCallback) throws IOException {
        HttpUriRequest apacheRequest = createRequest(request, requestCallback);
        RequestUtils.throwIfCanceled(request);
        request.tag = apacheRequest; //mark for cancellation
        HttpResponse apacheResponse = execute(client, apacheRequest);
        RequestUtils.throwIfCanceled(request);
        return parseResponse(request.getUrl(), apacheResponse);
    }

    protected HttpResponse execute(org.apache.http.client.HttpClient client, HttpUriRequest request) throws IOException {
        return client.execute(request);
    }

    @Override
    public void cancel(Request request) {
        if (request.tag != null && (request.tag instanceof HttpUriRequest)) {
            HttpUriRequest apacheRequest = (HttpUriRequest) request.tag;
            apacheRequest.abort();
        }
        request.tag = RequestUtils.TAG_CANCELED;
    }

    static Response parseResponse(String url, HttpResponse response) throws IOException {
        StatusLine statusLine = response.getStatusLine();
        int status = statusLine.getStatusCode();
        String reason = statusLine.getReasonPhrase();

        List<Header> headers = new ArrayList<Header>();
        String contentType = "application/octet-stream";
        for (org.apache.http.Header header : response.getAllHeaders()) {
            String name = header.getName();
            String value = header.getValue();
            if ("Content-Type".equalsIgnoreCase(name)) {
                contentType = value;
            }
            headers.add(new Header(name, value));
        }

        HttpBody body = null;
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            body = new ResponseActionBody(entity);
        }

        return new Response(url, status, reason, headers, body);
    }

    static HttpUriRequest createRequest(Request request, RequestCallback requestCallback) {
        if (request.getBody() != null) {
            return new GenericEntityHttpRequest(request, requestCallback);
        }
        return new GenericHttpRequest(request);
    }

    private static class GenericEntityHttpRequest extends HttpEntityEnclosingRequestBase {
        private final String method;

        GenericEntityHttpRequest(Request request, RequestCallback requestCallback) {
            super();
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }

            // Add the content body.
            setEntity(new TypedOutputEntity(request.getBody(), requestCallback));
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    static class TypedOutputEntity extends AbstractHttpEntity {
        private final HttpBody requestBody;
        private final RequestCallback requestCallback;

        TypedOutputEntity(HttpBody requestBody, RequestCallback requestCallback) {
            this.requestBody = requestBody;
            this.requestCallback = requestCallback;
            setContentType(requestBody.mimeType());
        }

        @Override
        public boolean isRepeatable() {
            return true;
        }

        @Override
        public long getContentLength() {
            return requestBody.length();
        }

        @Override
        public InputStream getContent() throws IOException {
            return requestBody.getContent();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            requestBody.writeContentTo(new ProgressOutputStream(out, new ProgressOutputStream.ProgressListener() {
                @Override public void onProgressChanged(long bytesWritten) {
                    requestCallback.onProgress((int) ((bytesWritten * 100) / getContentLength()));
                }
            }));
        }

        @Override
        public boolean isStreaming() {
            return false;
        }
    }
    private static class GenericHttpRequest extends HttpRequestBase {
        private final String method;

        public GenericHttpRequest(Request request) {
            method = request.getMethod();
            setURI(URI.create(request.getUrl()));

            // Add all headers.
            for (Header header : request.getHeaders()) {
                addHeader(new BasicHeader(header.getName(), header.getValue()));
            }
        }

        @Override
        public String getMethod() {
            return method;
        }
    }

    private static class ResponseActionBody implements HttpBody {

        private HttpEntity body;

        public ResponseActionBody(HttpEntity body) {
//            super(body.getContentType() == null ? null : body.getContentType().getValue());
            this.body = body;
        }

        @Override public long length() {
            return body.getContentLength();
        }

        @Override public InputStream getContent() throws IOException {
            return body.getContent();
        }

        @Override
        public String mimeType() {
           return body.getContentType().getValue();
        }

        @Override public void writeContentTo(OutputStream os) throws IOException {
            body.writeTo(os);
        }
    }
}
