package com.phonemap.phonemap.constants;

public class Sockets {
    //Incoming events
    public static final String SET_CODE = "set_code";

    //Outgoing events
    public static final String GET_CODE = "get_code";
    public static final String EXECUTION_FAILED = "execution_failed";
    public static final String RETURN = API.RETURN;
    public static final String START_CODE = "start_code";

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
