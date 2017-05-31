package com.phonemap.phonemap.constants;

import static com.phonemap.phonemap.constants.API.RETURN;

public class Sockets {
    //Incoming events
    public static final String SOCKET_SET_ID = "set_id";
    public static final String SOCKET_SET_CODE = "set_code";

    //Outgoing events
    public static final String SOCKET_GET_CODE = "get_code";
    public static final String SOCKET_FAILED_EXECUTING = "execution_failed";
    public static final String SOCKET_RETURN = RETURN;

    //Parameters
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String PATH = "path";
    public static final String EXCEPTION = "exception";

    //Message Handler
    public static final int RETURN_DATA_AND_CODE = 0;
    public static final int RETURN_RESULTS = 1;

    public static final int FAILED_EXECUTING_CODE = 400;

}
