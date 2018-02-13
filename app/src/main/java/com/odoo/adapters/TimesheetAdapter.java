package com.odoo.adapters;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


import com.odoo.FormTimeSheetActivity;
import com.odoo.OdooUtility;
import com.odoo.R;
import com.odoo.TimesheetActivity;
import com.odoo.core.support.OUser;
import com.odoo.models.TimesheetModel;

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

public class TimesheetAdapter extends ArrayAdapter {

    private Activity context;
    private List<TimesheetModel> timesheetModels;
    private OUser user;
    private OdooUtility odoo;
    //private ImageButton imgBtnDeleteTimesheet;
    private long deleteTaskId;

    public TimesheetAdapter(Activity context, List<TimesheetModel> timesheetModels) {
        super(context, R.layout.list_item_timesheet, timesheetModels);
        user = new OUser().current(context);
        odoo = new OdooUtility(user.getHost(), "object");
        this.context = context;
        this.timesheetModels = timesheetModels;
    }

    XMLRPCCallback listenerTimesheet = new XMLRPCCallback() {

        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if(id == deleteTaskId) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (android.os.Build.VERSION.SDK_INT >= 11){
                            context.getIntent().putExtra("status", 0);
                            context.recreate();

                        }else{
                            Intent intent = context.getIntent();
                            intent.putExtra("status", 0);
                            context.finish();
                            context.startActivity(intent);
                        }
                    }
                });
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d(" errornya", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d(" errornyaserver", error.toString());
        }
    };
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_timesheet, null, true);
        if(position % 2 != 0 ){
            rowView.setBackgroundColor(Color.rgb(250,250,250));
        }
        TextView date = (TextView) rowView.findViewById(R.id.txvDate);
        TextView hours = (TextView) rowView.findViewById(R.id.txvHours);
        //imgBtnDeleteTimesheet = (ImageButton) rowView.findViewById(R.id.imgBtnDeleteTimesheet);
        date.setText(timesheetModels.get(position).getDate());
        double hoursTotal = Double.parseDouble(timesheetModels.get(position).getHours()) * 3600;
        int hour = (int) (hoursTotal / 3600);
        int minute = (int) (hoursTotal % 3600)/60;
        if(hour <10 && minute <10) {
            hours.setText("0"+hour+":"+"0"+minute);
        } else if(hour<10&&minute>=10) {
            hours.setText("0"+hour+":"+minute);
        } else if(hour>=10&&minute<10) {
            hours.setText(hour+":"+"0"+minute);
        } else {
            hours.setText(hour+":"+minute);
        }
        /*imgBtnDeleteTimesheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                List data = Arrays.asList(
                                        Arrays.asList(timesheetModels.get(position).getId()));
                                deleteTaskId = odoo.delete(listenerTimesheet, user.getDatabase(),
                                        String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", data);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
            }
        });*/
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimesheetModel tm = timesheetModels.get(position);
                Intent intent = new Intent(context, FormTimeSheetActivity.class);
                intent.putExtra("act", "update");
                intent.putExtra("id", tm.getId());
                intent.putExtra("project_id", tm.getProject_id());
                intent.putExtra("task_id", tm.getTask_id());
                intent.putExtra("project", tm.getProject());
                intent.putExtra("task", tm.getTask());
                intent.putExtra("date", tm.getDate());
                intent.putExtra("hours", tm.getHours());
                intent.putExtra("desc", tm.getDesc());
                intent.putExtra("create_uid", tm.getUid());
                intent.putExtra("create_name", tm.getUname());
                context.startActivityForResult(intent, 1);
                context.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
            }
        });
        return rowView;
    }


}
