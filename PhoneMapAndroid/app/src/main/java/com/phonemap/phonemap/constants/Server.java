package com.phonemap.phonemap.constants;

import java.net.URI;

public class Server {
    public static final String PROTOCOL = "http";
    public static final String IP = "146.169.45.121";
    public static final String PORT = "5000";
    public static final String WS_NAMESPACE = "test";

    public static final String WS_URL = PROTOCOL + "://" + IP + ":" + PORT + "/" + WS_NAMESPACE;
    public static final URI WS_URI = URI.create(WS_URL);
}
