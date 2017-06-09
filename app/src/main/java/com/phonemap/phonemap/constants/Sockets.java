package com.phonemap.phonemap.constants;

public class Sockets {
    //Incoming events
    public static final String SET_CODE = "set_code";
    public static final String NO_TASKS = "no_tasks";
    public static final String CODE_AVAILABLE = "code_available";

    //Outgoing events
    public static final String REQUEST_NEW_SUBTASK = "get_code";
    public static final String EXECUTION_FAILED = "execution_failed";
    public static final String RESULT = API.RETURN;
    public static final String SUBTASK_STARTED = "start_code";

    //Parameters
    public static final String ID = "id";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String PATH = "path";
    public static final String EXCEPTION = "exception";

    //Message Handler
    public static final int NEW_SUBTASK = 0;
    public static final int COMPLETED_SUBTASK = 1;
    public static final int NEW_TASK = 2;

    public static final int FAILED_EXECUTING_CODE = 400;

}
