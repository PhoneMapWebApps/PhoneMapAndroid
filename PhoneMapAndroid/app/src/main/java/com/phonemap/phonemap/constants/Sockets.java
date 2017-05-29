package com.phonemap.phonemap.constants;

public class Sockets {
    //Incoming events
    public static final String SET_ID = "set_id";
    public static final String SET_CODE = "set_code";

    //Outgoing events
    public static final String GET_CODE = "get_code";

    //Parameters
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String PATH = "path";
    public static final String EXCEPTION = "exception";

    //Message Handler
    public static final int CONNECT_AND_RETURN_DATA = 0;
    public static final int RETURN_RESULTS = 1;
    public static final int RETURN_DATA = 2;

    public static final int FAILED_EXECUTING_CODE = 400;

}
