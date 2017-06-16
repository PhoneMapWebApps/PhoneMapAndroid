package com.phonemap.phonemap.objects;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import java.io.Serializable;

public class Task implements Serializable {
    private final String name;
    private final String description;
    private final int id;
    private final int totalSubtasks;
    private final int completedSubtasks;
    private final String ownerFullname;
    private final String ownerOrg;
    private final String timeSubmitted;

    public Task(String name, String description, int id, int totalSubtasks, int completedSubtasks,
                String ownerFullname, String ownerOrg, String timeSubmitted) {
        this.name = name;
        this.description = description;
        this.id = id;
        this.totalSubtasks = totalSubtasks;
        this.completedSubtasks = completedSubtasks;
        this.ownerFullname = ownerFullname;
        this.ownerOrg = ownerOrg;
        this.timeSubmitted = timeSubmitted;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescriptionUnformatted() {
        return description;
    }

    public Spanned getDescription() {
        return formatPrefix("Description", description);
    }

    public int getTotalSubtasks() {
        return totalSubtasks;
    }

    public int getCompletedSubtasks() {
        return completedSubtasks;
    }

    public Spanned getOwnerFullname() {
        return formatPrefix("Name", ownerFullname);
    }

    public Spanned getOwnerOrg() {
        return formatPrefix("Organization", ownerOrg);
    }

    public Spanned getTimeSubmitted() {
        return formatPrefix("Time Submitted", timeSubmitted);
    }

    public Spanned getCompletedPercentage() {
        return formatPrefix("Total Progress",  String.valueOf(getCompletedSubtasks() * 100 / getTotalSubtasks()) + "%");
    }

    private Spanned formatPrefix(String prefix, String text) {
        String formatted = "<font color=black><b>" + prefix + ": </b></font>" + fixNewLine(text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(formatted, 0);
        } else {
            return Html.fromHtml(formatted);
        }
    }

    private String fixNewLine(String string) {
        return string.replaceAll("\n", "<br />");
    }
}
