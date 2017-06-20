package com.phonemap.phonemap.requests;

import android.graphics.Bitmap;

import com.phonemap.phonemap.objects.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.phonemap.phonemap.constants.Requests.COMPLETED_SUBTASKS;
import static com.phonemap.phonemap.constants.Requests.GET_TASKS;
import static com.phonemap.phonemap.constants.Requests.OWNER_FULLNAME;
import static com.phonemap.phonemap.constants.Requests.OWNER_ID;
import static com.phonemap.phonemap.constants.Requests.OWNER_ORG;
import static com.phonemap.phonemap.constants.Requests.TASK_DESCRIPTION;
import static com.phonemap.phonemap.constants.Requests.TASK_ID;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Requests.TIME_SUBMITTED;
import static com.phonemap.phonemap.constants.Requests.TOTAL_SUBTASKS;
import static com.phonemap.phonemap.constants.Server.HTTP_URL;

public class GetTasks implements AsyncStringDownloadListener {

    public static final String LOG_TAG = "GetTasks";
    public static final String DEFAULT_JSON = "[]";

    private ServerListener callback;

    public GetTasks(ServerListener callback){
        this.callback = callback;
        new DownloadString(this).execute(HTTP_URL + GET_TASKS);
    }

    private void parseJSON(String json){
        List<Task> tasks = new ArrayList<>();

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.getJSONObject(i);
                int taskID = object.getInt(TASK_ID);
                int ownerID = object.getInt(OWNER_ID);
                String name = object.getString(TASK_NAME);
                String description = object.getString(TASK_DESCRIPTION);
                int total_subtasks = object.getInt(TOTAL_SUBTASKS);
                int completed_subtasks = object.getInt(COMPLETED_SUBTASKS);
                String owner_fullname = object.getString(OWNER_FULLNAME);
                String owner_org = object.getString(OWNER_ORG);
                String time_submitted = object.getString(TIME_SUBMITTED);

                Task task = new Task(name, description, taskID, ownerID, total_subtasks, completed_subtasks, owner_fullname, owner_org, time_submitted);
                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        callback.gotTasks(tasks);
    }

    @Override
    public void onStringDownloaded(String str){
        parseJSON(str == null ? DEFAULT_JSON : str);
    }
}