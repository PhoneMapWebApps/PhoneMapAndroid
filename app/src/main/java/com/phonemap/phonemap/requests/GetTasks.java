package com.phonemap.phonemap.requests;

import android.util.Log;

import com.phonemap.phonemap.objects.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.phonemap.phonemap.constants.Requests.GET_TASKS;
import static com.phonemap.phonemap.constants.Requests.TASK_DESCRIPTION;
import static com.phonemap.phonemap.constants.Requests.TASK_ID;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Server.HTTP_URL;

public class GetTasks implements AsyncTaskListener {

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
                int id = object.getInt(TASK_ID);
                String name = object.getString(TASK_NAME);
                String description = object.getString(TASK_DESCRIPTION);
                Task task = new Task(name, description, id);
                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (tasks.isEmpty()) {
            tasks.add(new Task("Test Task", "This task does jack shit just to fill up space", -1));
        }

        callback.gotTasks(tasks);
    }

    @Override
    public void onStringDownloaded(String str){
        parseJSON(str == null ? DEFAULT_JSON : str);
    }
}