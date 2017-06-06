package com.phonemap.phonemap.objects;

import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;

public class NullTask extends Task {

    public NullTask() {
        super("No task selected", "", INVALID_TASK_ID);
    }
}
