package com.phonemap.phonemap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonemap.phonemap.R;
import com.phonemap.phonemap.objects.Task;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.phonemap.phonemap.constants.Intents.UPDATED_PREFERRED_TASK;
import static com.phonemap.phonemap.constants.Preferences.CURRENT_TASK;
import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;

public class TaskListAdapter extends BaseAdapter {
    private final List<Task> tasks;
    private final LayoutInflater inflater;
    private final Activity activity;
    private final ListView listView;

    public TaskListAdapter(Context context, List<Task> tasks) {
        this.tasks = tasks;
        this.inflater = LayoutInflater.from(context);

        this.activity = ((Activity) context);
        this.listView = (ListView) activity.findViewById(R.id.taskListView);
    }

    public int getCount() {
        return tasks.size();
    }

    public Object getItem(int position) {
        return tasks.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.selectTask = (Button) convertView.findViewById(R.id.select_task);

            holder.selectTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        RelativeLayout layout = (RelativeLayout) listView.getChildAt(i);
                        Button button = (Button) layout.findViewById(R.id.select_task);
                        button.setText(activity.getString(R.string.select_task));
                    }

                    holder.selectTask.setText(activity.getString(R.string.preferred_task));

                    SharedPreferences preferences =
                            activity.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putInt(CURRENT_TASK, (int) holder.selectTask.getTag());
                    editor.apply();

                    Intent intent = new Intent(UPDATED_PREFERRED_TASK);
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

                }
            });

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task =  tasks.get(position);

        holder.name.setText(task.getName());
        holder.description.setText(task.getDescription());
        holder.selectTask.setTag(task.getId());

        SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        if (preferences.getInt(CURRENT_TASK, INVALID_TASK_ID) == task.getId()) {
            holder.selectTask.setText(activity.getString(R.string.preferred_task));
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
        Button selectTask;
    }
}