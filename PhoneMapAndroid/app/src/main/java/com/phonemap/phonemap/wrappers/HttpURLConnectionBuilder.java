package com.phonemap.phonemap.wrappers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

public class HttpURLConnectionBuilder {
    private final HttpURLConnection urlConnection;

    public HttpURLConnectionBuilder(String url) throws IOException {
        urlConnection = (HttpURLConnection) new URL(url).openConnection();
    }

    public HttpURLConnectionBuilder setRequestMethod(String method) throws ProtocolException {
        urlConnection.setRequestMethod(method);
        return this;
    }

    public HttpURLConnectionBuilder setDoOutput(boolean doOutput) {
        urlConnection.setDoOutput(doOutput);
        return this;
    }

    public HttpURLConnection connect() throws IOException {
        urlConnection.connect();
        return urlConnection;
    }
}
