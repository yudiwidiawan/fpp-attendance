package com.odoo.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.FormTimeSheetActivity;
import com.odoo.OdooUtility;
import com.odoo.ProjectActivity;
import com.odoo.R;
import com.odoo.SharedData;
import com.odoo.TaskActivity;
import com.odoo.TimesheetActivity;
import com.odoo.core.support.OUser;
import com.odoo.models.ProjectModel;
import com.odoo.models.TimesheetModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by makan on 03/08/2017.
 */

public class ProjectAdapter extends ArrayAdapter {

    private Activity context;
    private List<ProjectModel> projectModels;
    private OUser user;
    private OdooUtility odoo;
    private long deleteTaskId;

    public ProjectAdapter(Activity context, List<ProjectModel> projectModels) {
        super(context, R.layout.list_item_project, projectModels);
        user = new OUser().current(context);
        odoo = new OdooUtility(user.getHost(), "object");
        this.context = context;
        this.projectModels = projectModels;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_project, null, true);
        final ImageButton imBPin = (ImageButton) rowView.findViewById(R.id.btnPinProject);
        if(projectModels.get(position).is_pinned()) {
            imBPin.setColorFilter(Color.RED);
        }
        TextView name = (TextView) rowView.findViewById(R.id.txvProjectName);
        TextView taskCount = (TextView) rowView.findViewById(R.id.txvTaskCount);
        TextView docCount = (TextView) rowView.findViewById(R.id.txvDocCount);
        //TextView recentLogDate = (TextView) rowView.findViewById(R.id.txvRecentLogDate);
        TextView lastUpdatedOn = (TextView) rowView.findViewById(R.id.txvLastUpdatedOn);
        ImageView imageProjectIsFavorite = (ImageView) rowView.findViewById(R.id.imgProjectIsFavorite);
        name.setText(projectModels.get(position).getName());
        taskCount.setText(String.valueOf(projectModels.get(position).getTask_count()));
        docCount.setText(String.valueOf(projectModels.get(position).getDoc_count()));
        //recentLogDate.setText("Recent Log Date: " + projectModels.get(position).getRecentLogDate());
        lastUpdatedOn.setText("Last Updated On: " + projectModels.get(position).getLastUpdatedOn());
        if(projectModels.get(position).getIs_favorite().equals("true")) {
            imageProjectIsFavorite.setImageResource(R.drawable.ic_star_black_24dp);
            imageProjectIsFavorite.setColorFilter(Color.rgb(255,215,0));
        }

        imBPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String prevJson = SharedData.getKey(context, "pinned_project");
                if(prevJson!="") {
                    Type type = new TypeToken<ArrayList<ProjectModel>>() {}.getType();
                    ArrayList<ProjectModel> arrayList = gson.fromJson(prevJson, type);
                    ProjectModel projectModel = projectModels.get(position);
                    if(checkIsBookmarked(projectModel.getId(), arrayList)) {
                        unBookmark(projectModel.getId(), arrayList);
                        String nextJson = gson.toJson(arrayList);
                        SharedData.setKey(context, "pinned_project", nextJson);
                        context.recreate();
                    } else {
                        projectModel.setIs_pinned(true);
                        arrayList.add(projectModel);
                        String nextJson = gson.toJson(arrayList);
                        SharedData.setKey(context, "pinned_project", nextJson);
                        //projectModels = bookmarkProject(projectModel.getId(), projectModels);
                        //notifyDataSetChanged();
                        context.recreate();
                    }
                } else {
                    ArrayList<ProjectModel> arrayList = new ArrayList<>();
                    ProjectModel projectModel = projectModels.get(position);
                    projectModel.setIs_pinned(true);
                    arrayList.add(projectModel);
                    String nextJson = gson.toJson(arrayList);
                    SharedData.setKey(context, "pinned_project", nextJson);
                    //projectModels = bookmarkProject(projectModel.getId(), projectModels);
                    //notifyDataSetChanged();
                    context.recreate();
                }
                //SharedData.setKey(context, "pinned_project", );
            }
        });
        rowView.setOnClickListener(new AdapterView.OnClickListener() {

            @Override
            public void onClick(View v) {
                ProjectModel projectModel = projectModels.get(position);
                Intent intent = new Intent(context, TaskActivity.class);
                intent.putExtra("id", projectModel.getId());
                intent.putExtra("project_name", projectModel.getName());
                intent.putExtra("company_id", projectModel.getCompany_id());
                intent.putExtra("company", projectModel.getCompany());
                intent.putExtra("partner_id", projectModel.getPartner_id());
                intent.putExtra("customer", projectModel.getCustomer());
                context.startActivity(intent);
            }
        });

        return rowView;
    }


    public ArrayList<ProjectModel> unBookmark(int id, ArrayList<ProjectModel> listProject) {
        ArrayList<ProjectModel> projectModels = listProject;
        int i = 0;
        for (ProjectModel projectModel : listProject) {
            if(projectModel.getId() == id) {
                projectModels.remove(i);
                break;
            }
            i++;
        }
        return projectModels;
    }


    public List<ProjectModel> bookmarkProject(int id, List<ProjectModel> listProject) {
        List<ProjectModel> projectModels = listProject;
        List<ProjectModel> projectModelsBaru = new ArrayList<>();
        int i = 0;
        for (ProjectModel projectModel : listProject) {
            if(projectModel.getId() == id) {
                projectModelsBaru.add(projectModel);
                projectModels.remove(i);
                break;
            }
            i++;
        }
        projectModelsBaru.addAll(projectModels);
        return projectModelsBaru;
    }

    public boolean checkIsBookmarked(int id, ArrayList<ProjectModel> listProject) {
        boolean isBookmark = false;
        for (ProjectModel projectModel : listProject) {
            if(projectModel.getId() == id) {
                isBookmark = true;
                break;
            }
        }
        return isBookmark;
    }


}
