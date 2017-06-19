package com.phonemap.phonemap.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phonemap.phonemap.R;
import com.phonemap.phonemap.TaskDescription;
import com.phonemap.phonemap.objects.Task;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.phonemap.phonemap.constants.Intents.UPDATED_PREFERRED_TASK;
import static com.phonemap.phonemap.constants.Other.TASK;
import static com.phonemap.phonemap.constants.Preferences.CURRENT_TASK;
import static com.phonemap.phonemap.constants.Preferences.INVALID_TASK_ID;
import static com.phonemap.phonemap.constants.Preferences.PREFERENCES;
import static com.phonemap.phonemap.services.Utils.startJSRunner;

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

    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.task_list_item, null);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.description = (TextView) convertView.findViewById(R.id.description);
            holder.selectTask = (CheckBox) convertView.findViewById(R.id.select_task);

            holder.selectTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    for (int i = 0; i < listView.getChildCount(); i++) {
                        RelativeLayout layout = (RelativeLayout) listView.getChildAt(i);
                        CheckBox checkBox = (CheckBox) layout.findViewById(R.id.select_task);
                        checkBox.setChecked(false);
                    }

                    SharedPreferences preferences =
                            activity.getSharedPreferences(PREFERENCES, MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();


                    if (preferences.getInt(CURRENT_TASK, INVALID_TASK_ID) == (int) holder.selectTask.getTag()) {
                        holder.selectTask.setChecked(false);
                        editor.putInt(CURRENT_TASK, INVALID_TASK_ID);
                        editor.apply();
                    } else {
                        holder.selectTask.setChecked(true);
                        editor.putInt(CURRENT_TASK, (int) holder.selectTask.getTag());
                        editor.apply();

                        startJSRunner(activity);
                    }

                    Intent intent = new Intent(UPDATED_PREFERRED_TASK);
                    LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);

                }
            });

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // ToDo: Fix serialization
                    Intent intent = new Intent(activity, TaskDescription.class);
                    intent.putExtra(TASK, tasks.get(position));
                    activity.startActivity(intent);
                }
            });
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Task task = tasks.get(position);

        holder.name.setText(task.getName());
        holder.description.setText(task.getDescriptionUnformatted());
        holder.selectTask.setTag(task.getId());

        holder.description.post(new Runnable() {
            @Override
            public void run() {
                int lineEndIndex = holder.description.getLayout().getLineEnd(2 - 1);
                String readMore = activity.getString(R.string.read_more);
                String dots = "... ";

                if (lineEndIndex > readMore.length() + dots.length()) {
                    int offset = lineEndIndex - readMore.length() - dots.length();

                    String text = String.valueOf(holder.description.getText().subSequence(0, offset)) + dots;
                    holder.description.setText(text);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        holder.description.setText(Html.fromHtml(text + "<font color=blue>" + readMore + "</font>", 0));
                    } else {
                        holder.description.setText(Html.fromHtml(text + "<font color=blue>" + readMore + "</font>"));
                    }
                }
            }
        });

        SharedPreferences preferences = activity.getSharedPreferences(PREFERENCES, MODE_PRIVATE);

        if (preferences.getInt(CURRENT_TASK, INVALID_TASK_ID) == task.getId()) {
            holder.selectTask.setChecked(true);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView name;
        TextView description;
        CheckBox selectTask;
    }
}