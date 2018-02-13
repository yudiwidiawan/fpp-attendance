package com.odoo.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.odoo.FormTimeSheetActivity;
import com.odoo.OdooUtility;
import com.odoo.R;
import com.odoo.core.support.OUser;
import com.odoo.models.AttendanceModel;
import com.odoo.models.TimesheetModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by makan on 05/09/2017.
 */

public class CheckInAdapter extends ArrayAdapter {

    private Activity context;
    private ArrayList<AttendanceModel> attendanceModels;
    private OUser user;
    private OdooUtility odoo;
    //private ImageButton imgBtnDeleteTimesheet;
    private long deleteTaskId;

    public CheckInAdapter(Activity context, ArrayList<AttendanceModel> attendanceModels) {
        super(context, R.layout.list_item_attendance, attendanceModels);
        user = new OUser().current(context);
        odoo = new OdooUtility(user.getHost(), "object");
        this.context = context;
        this.attendanceModels = attendanceModels;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_attendance, null, true);
        if(position % 2 != 0 ){
            rowView.setBackgroundColor(Color.rgb(250,250,250));
        }
        TextView date = (TextView) rowView.findViewById(R.id.txvDate);
        TextView checkInTime = (TextView) rowView.findViewById(R.id.txvCheckInTime);
        TextView checkOutTime = (TextView) rowView.findViewById(R.id.txvCheckOutTime);
        TextView hours = (TextView) rowView.findViewById(R.id.txvHoursOfWork);
        SimpleDateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dateObj = null,checkOutObj = null;
        SimpleDateFormat writeFormat = new SimpleDateFormat("dd/MM/yy");
        SimpleDateFormat writeFormatCheckInOut = new SimpleDateFormat("HH:mm:ss");
        try {
            dateObj = readFormat.parse(attendanceModels.get(position).getCheck_in()) ;
            dateObj.setTime(dateObj.getTime() + (3600*7000));

        } catch (ParseException e) {
            Log.d("error parse ", e.toString());
        }

        String sCheckOutTime = "";
        try {
            checkOutObj = readFormat.parse(attendanceModels.get(position).getCheck_out()) ;
            checkOutObj.setTime(checkOutObj.getTime() + (3600*7000));
            sCheckOutTime = writeFormatCheckInOut.format(checkOutObj);
        } catch (ParseException e) {
            sCheckOutTime = "--:--:--";
        }
        String dateToShow = writeFormat.format(dateObj);
        String sCheckInTime = writeFormatCheckInOut.format(dateObj);
        //imgBtnDeleteTimesheet = (ImageButton) rowView.findViewById(R.id.imgBtnDeleteTimesheet);
        date.setText(dateToShow);
        checkInTime.setText(sCheckInTime);
        checkOutTime.setText(sCheckOutTime);
        double hoursTotal = Double.parseDouble(attendanceModels.get(position).getHours()) * 3600;
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
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return rowView;
    }
}
