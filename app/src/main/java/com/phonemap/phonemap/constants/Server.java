package com.phonemap.phonemap.constants;

import java.net.URI;

public class Server {
    public static final String PROTOCOL = "http";
    public static final String IP = "146.169.45.121";
    public static final String WS_NAMESPACE = "phone";

    public static final String WS_URL = PROTOCOL + "://" + IP  + "/" + WS_NAMESPACE;
    public static final URI WS_URI = URI.create(WS_URL);

    public static final String HTTP_URL = PROTOCOL + "://" + IP + "/";
}
