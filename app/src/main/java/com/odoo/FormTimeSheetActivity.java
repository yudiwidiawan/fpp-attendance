package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.models.TimesheetModel;

import java.text.DateFormat;
import java.text.Normalizer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


public class FormTimeSheetActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener
, View.OnClickListener, TimePickerDialog.OnTimeSetListener{

    String theDate, theHours;
    int project_id, task_id;
    EditText edtDate, edtDesc, edtHours;
    Button btnAddItemToTimesheet;
    private TextView titleForm;
    ImageButton imgBtnDate, closeAddRecord, plusHours, minusHours;
    private String currentProjectID, currentTaskID;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    DatePickerDialog datePickerDialog;
    TimePickerDialog timePickerDialog;
    private String act = "add";
    private int timesheet_id = 0;
    private long updateTaskId, deleteTaskId;
    private Calendar calendar;
    private int currentYear, currentMonth, currentDay;
    private ImageButton btnDeleteTimesheet, btnDeleteYes, btnDeleteNo;
    private LinearLayout layoutConfirmDelete;
    private TextView txvUsername;
    private int create_uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_time_sheet);
        edtDate = (EditText) findViewById(R.id.edtDate);
        edtDesc = (EditText) findViewById(R.id.edtDesc);
        edtHours = (EditText)  findViewById(R.id.edtHours);
        titleForm = (TextView) findViewById(R.id.txvTitleForm);
        plusHours = (ImageButton) findViewById(R.id.btnPlusHours);
        minusHours = (ImageButton) findViewById(R.id.btnMinusHours);
        btnDeleteTimesheet = (ImageButton) findViewById(R.id.btnDeleteTimesheet);
        layoutConfirmDelete = (LinearLayout) findViewById(R.id.layoutConfirmDelete);

        txvUsername = (TextView) findViewById(R.id.txvUserTimesheet);

        getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        project_id = getIntent().getIntExtra("project_id", 0);
        task_id = getIntent().getIntExtra("task_id", 0);

        btnAddItemToTimesheet = (Button) findViewById(R.id.btnAddItemtoTimesheet);
        btnDeleteTimesheet.setOnClickListener(this);
        imgBtnDate = (ImageButton) findViewById(R.id.btnExpandDate);
        edtHours.setOnClickListener(this);
        edtDate.setOnClickListener(this);

        calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        url = SharedData.getKey(FormTimeSheetActivity.this, "url").toString();
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            try {
                user = OUser.current(this);
                uid = user.getUserId().toString();
                password = user.getPassword();
                serverAddress = user.getHost();
                database = user.getDatabase();
                odoo = new OdooUtility(user.getHost(), "object");

                if (getIntent().getStringExtra("act").toString().equals("update")) {
                    btnDeleteTimesheet.setVisibility(View.VISIBLE);
                    act = "update";
                    setTitle("Edit Timesheet");
                    titleForm.setText("Edit Timesheet");
                    String dateString = getIntent().getStringExtra("date");
                        Date date = null;
                        DateFormat readFormat = new SimpleDateFormat("dd/MM/yyyy");
                        DateFormat writeFormat = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            date = readFormat.parse(dateString);
                        } catch (ParseException e) {
                            Log.d("error parse ", e.toString());
                        }
                        dateString = writeFormat.format(date);
                    theDate = dateString;
                    double hoursTotal = Double.parseDouble(getIntent().getStringExtra("hours")) * 3600;
                    int hour = (int) (hoursTotal / 3600);
                    int minute = (int) (hoursTotal % 3600) / 60;
                    if (hour < 10 && minute < 10) {
                        edtHours.setText("0" + hour + ":" + "0" + minute);
                    } else if (hour < 10 && minute >= 10) {
                        edtHours.setText("0" + hour + ":" + minute);
                    } else if (hour >= 10 && minute < 10) {
                        edtHours.setText(hour + ":" + "0" + minute);
                    } else {
                        edtHours.setText(hour + ":" + minute);
                    }
                    edtDate.setText(getIntent().getStringExtra("date"));
                    edtDesc.setText(getIntent().getStringExtra("desc"));
                    project_id = getIntent().getIntExtra("project_id", 0);
                    task_id = getIntent().getIntExtra("task_id", 0);
                    timesheet_id = getIntent().getIntExtra("id", 0);
                    create_uid = getIntent().getIntExtra("create_uid", 0);
                    String create_name = getIntent().getStringExtra("create_name");

                    txvUsername.setText("User : " + create_name);

                    btnAddItemToTimesheet.setText("Save");
                    SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                    SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                    SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
                    String editYear = yearFormat.format(date);
                    String editMonth = monthFormat.format(date);
                    String editDay = dayFormat.format(date);
                    datePickerDialog = new DatePickerDialog(
                            this, FormTimeSheetActivity.this, Integer.parseInt(editYear)
                            , Integer.parseInt(editMonth), Integer.parseInt(editDay));
                    timePickerDialog = new TimePickerDialog(this, FormTimeSheetActivity.this,
                            hour, minute, true);
                } else {
                    btnDeleteTimesheet.setVisibility(View.GONE);
                    setTitle("Add Timesheet");
                    titleForm.setText("Add Timesheet");
                    act = "add";
                    txvUsername.setText("User : " + user.getName());


                    String mnth = String.valueOf(currentMonth + 1), sDay = String.valueOf(currentDay);
                    if (currentMonth+1 < 10) {
                        mnth = "0" + mnth;
                    }
                    if (currentDay < 10) {
                        sDay = "0" + sDay;
                    }
                    theDate = currentYear + "-" + mnth + "-" + sDay;

                    edtDate.setText(sDay+"/"+mnth+"/"+currentYear);
                    edtHours.setText("00:00");
                    btnAddItemToTimesheet.setText("Add");
                    datePickerDialog = new DatePickerDialog(
                            this, FormTimeSheetActivity.this, currentYear, currentMonth, currentDay);
                    timePickerDialog = new TimePickerDialog(this, FormTimeSheetActivity.this,
                            00, 00, true);
                }

                plusHours.setOnClickListener(this);
                minusHours.setOnClickListener(this);
                btnAddItemToTimesheet.setOnClickListener(this);
                imgBtnDate.setOnClickListener(this);
                closeAddRecord.setOnClickListener(this);
                btnDeleteTimesheet.setOnClickListener(this);
                btnDeleteYes.setOnClickListener(this);
                btnDeleteNo.setOnClickListener(this);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(edtDate.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

            } catch (NullPointerException err) {

            }
        }
    }

    public void hoursControl(int conotation) {
        String hoursString = edtHours.getText().toString();
        try {
        int min = Integer.parseInt(hoursString.substring(3,5));
        int hour = Integer.parseInt(hoursString.substring(0,2));
        if(conotation < 0 && min!= 0 && min >= 15) {
            min-= 15;
        } else if(conotation < 0 && min>= 0 && hour != 0
                && min < 15) {
            int temp_min = (hour*60) + min;
            temp_min-=15;
            hour = temp_min/60;
            min = temp_min%60;
        } else if(conotation < 0 && min>= 0 && hour == 0
                && min < 15) {
            min = 0;
        } else if(conotation > 0) {
            min+=15;
        }
        Log.d("min", min+"");
        Log.d("hour", hour+"");
        if(min > 59) {
            hour++;
            min = min % 60;
        }
        if(hour < 10) {
            if(min < 10) {
                theHours = "0" + hour + ":" +"0"+min;
            } else {
                theHours = "0" + hour + ":" +min;
            }
        } else {
            if(min < 10) {
                theHours = hour + ":" +"0"+min;
            } else {
                theHours = hour + ":" +min;
            }
        }
        edtHours.setText(theHours); }
        catch (NumberFormatException err) {
            odoo.MessageDialog(FormTimeSheetActivity.this, "Please enter a proper time.");
        }
        /*int jmlHour = (int) dateHour.getTime() / 3600;
        int jmlMinute = (int) (dateHour.getTime() % 3600) / 60;
        */
    }



    public void removeAccount() {
        AccountManager accountManager = (AccountManager) getApplicationContext().getSystemService(ACCOUNT_SERVICE);

        // loop through all accounts to remove them
        Account[] accounts = accountManager.getAccounts();
        for (int index = 0; index < accounts.length; index++) {
            if (accounts[index].type.intern() == "com.odoo.fpp.auth") {
                accountManager.removeAccount(accounts[index], null, null);
                Intent intent = new Intent(getApplicationContext(), OdooLogin.class);
                startActivity(intent);
                finish();
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        String mnth = String.valueOf(month + 1), day = String.valueOf(dayOfMonth);
        if(month < 10) {
            mnth = "0" + mnth;
        }
        if(dayOfMonth < 10) {
            day = "0" + day;
        }
        theDate = year+"-"+mnth+"-"+day;
        edtDate.setText(day+"/"+mnth+"/"+year);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnPlusHours:
                hoursControl(1);
                break;
            case R.id.btnMinusHours:
                hoursControl(-1);
                break;
            case R.id.btnAddItemtoTimesheet:
                Intent intent = new Intent();
                intent.putExtra("status", 1);
                if(act.equals("update")) {
                    final String date = theDate;
                    final String description = edtDesc.getText().toString();
                    String hours = edtHours.getText().toString();
                    double tempJam = 0;
                    String sJam = hours;
                    SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    final String createWrite = sdfCreateWrite.format(new Date());
                    if(hours.length() == 3) {
                        sJam = "0"+sJam;
                    } else if(sJam.length() == 2) {
                        sJam = "0"+sJam.substring(0,1) + ":" + "0" + sJam.substring(2);
                    }
                    double tempMin = (Double.parseDouble(sJam.substring(3)) / 60);

                    tempJam = Double.parseDouble(sJam.substring(0,2)) + tempMin;
                    final double jam = tempJam;
                    final int uids = Integer.parseInt(uid);
                    Log.d("idtimesheet", timesheet_id+"");
                    List dataToUpdate = Arrays.asList(
                            Arrays.asList(timesheet_id),
                            new HashMap() {{
                                put("unit_amount", jam);
                                put("date", date);
                                put("write_date", createWrite);
                                put("name", description);
                                put("project_id", project_id);
                                put("task_id", task_id);
                            }});
                    updateTaskId = odoo.update(listenerTimesheet, database, uid, password, "account.analytic.line", dataToUpdate);
                } else {
                    if(validTanggalDanHours()) {
                        try {
                            double tempJam = 0;
                            String sJam = edtHours.getText().toString();
                            if(sJam.toString().length() == 3) {
                                sJam = "0"+sJam;
                            } else if(sJam.length() == 2) {
                                sJam = "0"+sJam.substring(0,1) + ":" + "0" + sJam.substring(2);
                            }

                            tempJam = Double.parseDouble(sJam.substring(0,2) + "." + sJam.substring(3));
                            final double jam = tempJam;
                            intent.putExtra("act", "add");
                            String date = theDate;
                            String desc = edtDesc.getText().toString();
                            String hours = edtHours.getText().toString();
                            intent.putExtra("project_id", project_id);
                            intent.putExtra("task_id", task_id);
                            intent.putExtra("date", date);
                            intent.putExtra("name", desc);
                            intent.putExtra("hours", hours);
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (NumberFormatException err) {
                            odoo.MessageDialog(FormTimeSheetActivity.this, "Invalid hours format.");
                        }
                    } else {
                        odoo.MessageDialog(FormTimeSheetActivity.this,
                                "Tidak dapat mengisi timesheet tanggal\n H+2/H-2");
                    }
                }
                break;
            case R.id.btnExpandDate:
                datePickerDialog.show();
                break;
            case R.id.edtHours:
                timePickerDialog.show();
                break;
            case R.id.edtDate:
                datePickerDialog.show();
                break;
            case R.id.btnDeleteTimesheet:
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Yes button clicked
                                List data = Arrays.asList(
                                        Arrays.asList(timesheet_id));
                                deleteTaskId = odoo.delete(listenerTimesheet, user.getDatabase(),
                                        String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", data);
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(FormTimeSheetActivity.this);
                builder.setMessage("Are you sure to delete this item?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();
                break;
        }

    }

    public boolean validTanggalDanHours() {
        boolean valid = true;
        Date date = null;
        DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat writeFormat = new SimpleDateFormat("dd");
        try {
            date = readFormat.parse(theDate);
        } catch (ParseException e) {
            Log.d("error parse ", e.toString());
        }
        String tanggal = writeFormat.format(date);
        if(Integer.parseInt(tanggal) > currentDay+2 || Integer.parseInt(tanggal) < currentDay-2 ) {
            valid = false;
        }

        return valid;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FormTimeSheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                android.R.anim.fade_out);
    }

    XMLRPCCallback listenerTimesheet = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if(id == updateTaskId) {
                Intent intent = new Intent();
                intent.putExtra("status", 1);
                intent.putExtra("act", "update");
                setResult(RESULT_OK, intent);
                finish();
                FormTimeSheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
            } else if(id == deleteTaskId) {
                Intent intent = new Intent();
                intent.putExtra("status", 1);
                intent.putExtra("act", "delete");
                setResult(RESULT_OK, intent);
                finish();
                FormTimeSheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d("errornya", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d("errornyaserver", error.toString());
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == Activity.RESULT_OK) {
        }
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if(hourOfDay < 10 && minute < 10) {
            edtHours.setText("0"+hourOfDay+":0"+minute);
        } else if(hourOfDay < 10 && minute >= 10) {
            edtHours.setText("0"+hourOfDay+":"+minute);
        } else if(hourOfDay >= 10 && minute < 10) {
            edtHours.setText(hourOfDay+":0"+minute);
        } else {
            edtHours.setText(hourOfDay+":"+minute);
        }
    }
}
