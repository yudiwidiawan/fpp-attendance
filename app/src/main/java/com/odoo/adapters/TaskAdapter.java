package com.odoo.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.OdooUtility;
import com.odoo.R;
import com.odoo.SharedData;
import com.odoo.TaskActivity;
import com.odoo.TimesheetActivity;
import com.odoo.core.support.OUser;
import com.odoo.models.ProjectModel;
import com.odoo.models.TaskModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by makan on 23/08/2017.
 */

public class TaskAdapter extends ArrayAdapter {

    private Activity context;
    private List<TaskModel> taskModels;
    private OUser user;
    private OdooUtility odoo;
    private long deleteTaskId;

    public TaskAdapter(Activity context, List<TaskModel> taskModels) {
        super(context, R.layout.list_item_task, taskModels);
        user = new OUser().current(context);
        odoo = new OdooUtility(user.getHost(), "object");
        this.context = context;
        this.taskModels = taskModels;
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_task, null, true);
        TextView name = (TextView) rowView.findViewById(R.id.txvTaskName);
        TextView projectName = (TextView) rowView.findViewById(R.id.txvProjectName);
        TextView deadline = (TextView) rowView.findViewById(R.id.txvDeadlineTask);
        TextView stage = (TextView) rowView.findViewById(R.id.txvStageTask);
        ImageButton imBPinTask = (ImageButton) rowView.findViewById(R.id.btnPinTask);
        if(taskModels.get(position).is_pinned()) {
            imBPinTask.setColorFilter(Color.RED);
        }
        ImageView imageProjectIsFavorite = (ImageView) rowView.findViewById(R.id.imgProjectIsFavorite);
        name.setText(taskModels.get(position).getName());
        projectName.setText(String.valueOf(taskModels.get(position).getProjectName()));
        deadline.setText(String.valueOf(taskModels.get(position).getDeadline()));
        stage.setText(String.valueOf(taskModels.get(position).getStage()));
        /*
        if(taskModels.get(position).getIs_favorite().equals("true")) {
            imageProjectIsFavorite.setImageResource(R.drawable.ic_star_black_24dp);
            imageProjectIsFavorite.setColorFilter(Color.rgb(255,215,0));
        }*/
        imBPinTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String prevJson = SharedData.getKey(context, "pinned_task");
                if(prevJson!="") {
                    Type type = new TypeToken<ArrayList<TaskModel>>() {}.getType();
                    ArrayList<TaskModel> arrayList = gson.fromJson(prevJson, type);
                    TaskModel taskModel = taskModels.get(position);
                    if(checkIsBookmarked(taskModel.getId(), arrayList)) {
                        unBookmark(taskModel.getId(), arrayList);
                        String nextJson = gson.toJson(arrayList);
                        SharedData.setKey(context, "pinned_task", nextJson);
                        context.recreate();
                    } else {
                        taskModel.setIs_pinned(true);
                        arrayList.add(taskModel);
                        String nextJson = gson.toJson(arrayList);
                        SharedData.setKey(context, "pinned_task", nextJson);
                        //projectModels = bookmarkProject(projectModel.getId(), projectModels);
                        //notifyDataSetChanged();
                        context.recreate();
                    }
                } else {
                    ArrayList<TaskModel> arrayList = new ArrayList<>();
                    TaskModel taskModel = taskModels.get(position);
                    taskModel.setIs_pinned(true);
                    arrayList.add(taskModel);
                    String nextJson = gson.toJson(arrayList);
                    SharedData.setKey(context, "pinned_task", nextJson);
                    //projectModels = bookmarkProject(projectModel.getId(), projectModels);
                    //notifyDataSetChanged();
                    context.recreate();
                }
            }
        });
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskModel taskModel = taskModels.get(position);
                Intent intent = new Intent(context, TimesheetActivity.class);
                intent.putExtra("task_id", taskModel.getId());
                intent.putExtra("project_id", taskModel.getProject_id());
                context.startActivity(intent);
            }
        });
        return rowView;
    }

    public ArrayList<TaskModel> unBookmark(int id, ArrayList<TaskModel> listTask) {
        ArrayList<TaskModel> taskModels = listTask;
        int i = 0;
        for (TaskModel taskModel : listTask) {
            if(taskModel.getId() == id) {
                taskModels.remove(i);
                break;
            }
            i++;
        }
        return taskModels;
    }


    public List<TaskModel> bookmarkProject(int id, List<TaskModel> listTask) {
        List<TaskModel> taskModels = listTask;
        List<TaskModel> taskModelsBaru = new ArrayList<>();
        int i = 0;
        for (TaskModel taskModel : listTask) {
            if(taskModel.getId() == id) {
                taskModelsBaru.add(taskModel);
                taskModels.remove(i);
                break;
            }
            i++;
        }
        taskModelsBaru.addAll(taskModels);
        return taskModelsBaru;
    }

    public boolean checkIsBookmarked(int id, ArrayList<TaskModel> listTask) {
        boolean isBookmark = false;
        for (TaskModel taskModel : listTask) {
            if(taskModel.getId() == id) {
                isBookmark = true;
                break;
            }
        }
        return isBookmark;
    }
}
