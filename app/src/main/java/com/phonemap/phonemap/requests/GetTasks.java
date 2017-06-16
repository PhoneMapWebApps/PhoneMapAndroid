package com.phonemap.phonemap.requests;

import com.phonemap.phonemap.objects.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.phonemap.phonemap.constants.Requests.COMPLETED_SUBTASKS;
import static com.phonemap.phonemap.constants.Requests.GET_TASKS;
import static com.phonemap.phonemap.constants.Requests.OWNER_FULLNAME;
import static com.phonemap.phonemap.constants.Requests.OWNER_ORG;
import static com.phonemap.phonemap.constants.Requests.TASK_DESCRIPTION;
import static com.phonemap.phonemap.constants.Requests.TASK_ID;
import static com.phonemap.phonemap.constants.Requests.TASK_NAME;
import static com.phonemap.phonemap.constants.Requests.TIME_SUBMITTED;
import static com.phonemap.phonemap.constants.Requests.TOTAL_SUBTASKS;
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
                int total_subtasks = object.getInt(TOTAL_SUBTASKS);
                int completed_subtasks = object.getInt(COMPLETED_SUBTASKS);
                String owner_fullname = object.getString(OWNER_FULLNAME);
                String owner_org = object.getString(OWNER_ORG);
                String time_submitted = object.getString(TIME_SUBMITTED);
                Task task = new Task(name, description, id, total_subtasks, completed_subtasks, owner_fullname, owner_org, time_submitted);
                tasks.add(task);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (tasks.isEmpty()) {
            Task task = new Task("Protein folding", "Proteins are necklaces of amino acids, long chain molecules. They are the basis of how biology gets things done. As enzymes, they are the driving force behind all of the biochemical reactions that make biology work. As structural elements, they are the main constituent of our bones, muscles, hair, skin and blood vessels. As antibodies, they recognize invading elements and allow the immune system to get rid of the unwanted invaders. For these reasons, scientists have sequenced the human genome – the blueprint for all of the proteins in biology – but how can we understand what these proteins do and how they work?\n\n" +
                    "However, only knowing this sequence tells us little about what the protein does and how it does it. In order to carry out their function (e.g. as enzymes or antibodies), they must take on a particular shape, also known as a “fold.” Thus, proteins are truly amazing machines: before they do their work, they assemble themselves! This self-assembly is called 'folding.'", 0, 10, 1, "Jeff Kramer", "Imperial College London", "16/06/2017 13:15");
            tasks.add(task);
        }

        callback.gotTasks(tasks);
    }

    @Override
    public void onStringDownloaded(String str){
        parseJSON(str == null ? DEFAULT_JSON : str);
    }
}