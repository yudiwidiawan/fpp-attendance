package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.adapters.TimesheetAdapter;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.models.TaskModel;
import com.odoo.models.TimesheetModel;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


public class TimesheetActivity extends AppCompatActivity {

    List<TimesheetModel> listTimesheet = new ArrayList<>();
    ListView listViewTimeSheet;
    Button btnAddItem;
    TimesheetAdapter timesheetAdapter;
    private long searchTaskId, createTaskId, updateTaskId,
            deleteTaskId, searchTaskIdTask, searchMessage;
    private List conditions, conditionsTask, conMessage;
    private Map fields, fieldsTask, fieldsMessage;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url,
            projectName;
    private SwipeRefreshLayout mSwipeTimesheet;
    private int task_id, project_id;
    private ImageButton imgButtonEdit;
    private LinearLayout btnExpandTask;
    private LinearLayout layoutDesc;
    private boolean hiddenTaskInfo = true;
    private TextView txvTaskTitle, txvProjectTitle, txvDeadlineDate,
            txvProgress, txvEffectiveHours;
    private ProgressBar pbProgress;
    private Button btnAddMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timesheet);
        mSwipeTimesheet = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshTimesheet);
        btnExpandTask = (LinearLayout) findViewById(R.id.layoutTaskDetails);
        layoutDesc = (LinearLayout) findViewById(R.id.taskInfo);
        txvTaskTitle = (TextView) findViewById(R.id.txvTaskTitle);
        txvProjectTitle = (TextView) findViewById(R.id.txvProjectTitle);
        txvDeadlineDate = (TextView) findViewById(R.id.txvDeadlineTime);
        pbProgress = (ProgressBar) findViewById(R.id.pbProgress);
        txvProgress = (TextView) findViewById(R.id.txvProgress);
        imgButtonEdit = (ImageButton) findViewById(R.id.btnEditTask);
        txvEffectiveHours = (TextView) findViewById(R.id.txvTotalHoursTimesheet);
        mSwipeTimesheet.setRefreshing(true);
        url = SharedData.getKey(TimesheetActivity.this, "url").toString();
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
                project_id = getIntent().getIntExtra("project_id", 0);
                task_id = getIntent().getIntExtra("task_id", 0);
                listViewTimeSheet = (ListView) findViewById(R.id.lvTimesheet);
                btnAddItem = (Button) findViewById(R.id.btnAddTimeSheet);
                btnAddMessage = (Button) findViewById(R.id.btnAddMessage);
                btnAddItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TimesheetActivity.this, FormTimeSheetActivity.class);
                        intent.putExtra("act", "add");
                        intent.putExtra("project_id", project_id);
                        intent.putExtra("task_id", task_id);
                        startActivityForResult(intent, 1); //ex: requestCode = 1
                        TimesheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }
                });
                btnAddMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TimesheetActivity.this, FormMessageActivity.class);
                        intent.putExtra("project_id", project_id);
                        intent.putExtra("project_name", projectName);
                        intent.putExtra("task_id", task_id);
                        startActivityForResult(intent, 1); //ex: requestCode = 1
                        TimesheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }
                });
                getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

                conditions = Arrays.asList(Arrays.asList(
                        Arrays.asList("task_id", "=", task_id)));
                //Arrays.asList("user_id", "=", user.getUserId()),
                //Arrays.asList("date", "like", "2017-08-%")));
                //Arrays.asList("create_date", "like", currentDate + "%")));
                // Arrays.asList("create_date", "like", "2017-07-28%")));

                fields = new HashMap() {{
                    put("fields", Arrays.asList("id", "name", "task_id", "project_id", "project", "task",
                            "date", "unit_amount", "sheet_id", "account_id", "user_id"));
                    put("order", "date desc");
                }};

                conditionsTask = Arrays.asList(Arrays.asList(
                        Arrays.asList("id", "=", task_id)));
                fieldsTask = new HashMap() {{
                    put("fields", Arrays.asList("id", "name", "project_id", "date_deadline", "progress",
                            "planned_hours", "user_id", "effective_hours", "message_ids"));
                }};

                searchTaskIdTask = odoo.search_read(listenerTask, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditionsTask,
                        fieldsTask);

                searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);


                listViewTimeSheet.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        TimesheetModel tm = listTimesheet.get(position);
                        Intent intent = new Intent(TimesheetActivity.this, FormTimeSheetActivity.class);
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
                        startActivity(intent);
                    }
                });
                mSwipeTimesheet.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                                String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);
                    }
                });
            } catch (NullPointerException err) {
                startActivity(new Intent(TimesheetActivity.this, OdooLogin.class));
            }
        }


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


    XMLRPCCallback listenerTimesheet = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskId) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listTimesheet.clear();
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObj = (Map<String, Object>) classObjs[i];
                                //List timesheet = OdooUtility.getOne2Many(classObj, "timesheet_ids");
                                TimesheetModel tm = new TimesheetModel();
                                M2OField project = OdooUtility.getMany2One(classObj, "project_id");
                                M2OField task = OdooUtility.getMany2One(classObj, "task_id");
                                M2OField timesheet = OdooUtility.getMany2One(classObj, "sheet_id");
                                M2OField account = OdooUtility.getMany2One(classObj, "account_id");
                                M2OField user = OdooUtility.getMany2One(classObj,"user_id");
                                tm.setId(Integer.parseInt(classObj.get("id").toString()));
                                tm.setProject(project.value);
                                tm.setProject_id(project.id);
                                tm.setTask(task.value);
                                tm.setTask_id(task.id);
                                tm.setSheet_id(timesheet.id);
                                String dateString = classObj.get("date").toString();
                                if (!dateString.equals("false")) {
                                    DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
                                    Date date = null;
                                    DateFormat writeFormat = new SimpleDateFormat("dd/MM/yyyy");
                                    try {
                                        date = readFormat.parse(dateString);
                                    } catch (ParseException e) {
                                        Log.d("error parse ", e.toString());
                                    }
                                    dateString = writeFormat.format(date);
                                } else {
                                    dateString = "Date parse error.";
                                }
                                tm.setDate(dateString);
                                tm.setDesc(classObj.get("name").toString());
                                tm.setHours(classObj.get("unit_amount").toString());
                                tm.setUid(user.id);
                                tm.setUname(user.value);
                                //Log.d("emp_id", SharedData.getKey(TimesheetActivity.this, "empID")+"");
                                //Log.d("account_id", account.id+"");
                                tm.toString();
                                Log.d("timesheet", tm.toString());
                                listTimesheet.add(tm);
                            }
                            fillListTimesheet();

                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mSwipeTimesheet.setRefreshing(false);
                            Toast.makeText(TimesheetActivity.this, "Empty timesheet", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else if (id == createTaskId) {
                searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);

                Intent intent = new Intent(TimesheetActivity.this, FormMessageActivity.class);
                intent.putExtra("project_id", project_id);
                intent.putExtra("project_name", projectName);
                intent.putExtra("task_id", task_id);
                startActivityForResult(intent, 1); //ex: requestCode = 1
                TimesheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
            } else if (id == updateTaskId) {
                searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d(" errornya", error.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeTimesheet.setRefreshing(false);
                }
            });
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d(" errornyaserver", error.toString());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSwipeTimesheet.setRefreshing(false);
                }
            });
        }
    };

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

    public void fillListTimesheet() {
        timesheetAdapter = new TimesheetAdapter(this, listTimesheet);
        listViewTimeSheet.setAdapter(timesheetAdapter);
        timesheetAdapter.notifyDataSetChanged();
        mSwipeTimesheet.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                /*TimesheetModel tm = new TimesheetModel(data.getStringExtra("date"),
                        data.getStringExtra("desc"),
                        data.getStringExtra("project"),
                        data.getStringExtra("hours"));*/
                Log.d("act", data.getStringExtra("act").toString());
                if (data.getStringExtra("act").toString().equals("update")) {
                    Log.d("edited timesheet", "success");
                    mSwipeTimesheet.setRefreshing(true);
                    searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                            String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);
                } else if (data.getStringExtra("act").toString().equals("delete")) {
                    Log.d("deleted timesheet", "success");
                    mSwipeTimesheet.setRefreshing(true);
                    searchTaskId = odoo.search_read(listenerTimesheet, user.getDatabase(),
                            String.valueOf(user.getUserId()), user.getPassword(), "account.analytic.line", conditions, fields);
                } else {
                    SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                    final String createWrite = sdfCreateWrite.format(new Date());
                    final int uids = Integer.parseInt(uid);
                    double tempJam = 0;
                    String sJam = data.getStringExtra("hours").toString();
                    if (data.getStringExtra("hours").toString().length() == 3) {
                        sJam = "0" + sJam;
                    } else if (sJam.length() == 2) {
                        sJam = "0" + sJam.substring(0, 1) + ":" + "0" + sJam.substring(2);
                    }
                    Log.d("SJAM", sJam);
                    double tempMin = (Double.parseDouble(sJam.substring(3)) / 60);
                    tempJam = Double.parseDouble(sJam.substring(0, 2)) + tempMin;
                    Log.d("SJAM DB", tempJam + "");
                    final double jam = tempJam;
                    final String date = data.getStringExtra("date");
                    final String description = data.getStringExtra("name");
                    final int projectID = data.getIntExtra("project_id", 0);
                    final int taskID = data.getIntExtra("task_id", 0);
                    List dataToInput = Arrays.asList(new HashMap() {{
                        put("create_uid", uids);
                        put("user_id", uids);
                        put("account_id", Integer.parseInt(SharedData.getKey(TimesheetActivity.this, "empID")));
                        put("write_uid", uids);
                        put("unit_amount", jam);
                        put("date", date);
                        put("create_date", createWrite);
                        put("write_date", createWrite);
                        put("name", description);
                        put("project_id", projectID);
                        put("task_id", taskID);
                        //put("sheet_id", 1817);
                    }});
                    Log.d("datatoinput", dataToInput.toString());
                    createTaskId = odoo.create(listenerTimesheet, database, uid, password,
                            "account.analytic.line", dataToInput);
                    mSwipeTimesheet.setRefreshing(true);
                }
                //listTimesheet.add(tm);
                //timesheetAdapter.notifyDataSetChanged();

            }
        } else {
            Log.d("timesheet act", "result not ok");
        }
    }

    XMLRPCCallback listenerTask = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskIdTask) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> classObj = (Map<String, Object>) classObjs[0];
                            List messages = OdooUtility.getOne2Many(classObj, "message_ids");
                            TaskModel taskModel = new TaskModel();
                            taskModel.setId(Integer.parseInt(classObj.get("id").toString()));
                            M2OField project = OdooUtility.getMany2One(classObj, "project_id");
                            taskModel.setName(classObj.get("name").toString());
                            String dateString = classObj.get("date_deadline").toString();
                            if (!dateString.equals("false")) {
                                DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd");
                                Date date = null;
                                DateFormat writeFormat = new SimpleDateFormat("dd/MM/yyyy");
                                try {
                                    date = readFormat.parse(dateString);
                                } catch (ParseException e) {
                                    Log.d("error parse ", e.toString());
                                }
                                dateString = writeFormat.format(date);
                            } else {
                                dateString = "No deadline is set.";
                            }
                            taskModel.setProjectName(project.value);
                            taskModel.setDeadline(dateString);
                            txvTaskTitle.setText(classObj.get("name").toString());
                            txvProjectTitle.setText(project.value);
                            projectName = project.value;
                            txvDeadlineDate.setText("Deadline " + dateString);
                            txvDeadlineDate.setTextColor(Color.RED);
                            int progress = Math.round(Float.parseFloat(classObj.get("progress").toString()));
                            txvProgress.setText("Progress : " + classObj.get("progress").toString() + "%");
                            pbProgress.setProgress(progress);
                            double hoursTotal = Double.parseDouble(classObj.get("effective_hours").toString());
                            Log.d("total", hoursTotal + "");
                            imgButtonEdit.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(TimesheetActivity.this, FormTaskActivity.class);
                                    intent.putExtra("act", "update");
                                    intent.putExtra("project_id", project_id);
                                    intent.putExtra("task_id", task_id);
                                    startActivityForResult(intent, 1);
                                    TimesheetActivity.this.overridePendingTransition(android.R.anim.fade_in,
                                            android.R.anim.fade_out);
                                }
                            });
                            txvEffectiveHours.setText(odoo.unitAmountToString(hoursTotal));

                            /*conMessage = Arrays.asList(Arrays.asList(
                                    Arrays.asList("create_uid", "=", user.getUserId()),
                                    Arrays.asList("id", "=", messages)));
                            fieldsMessage = new HashMap() {{
                                put("fields", Arrays.asList("attachment_ids",
                                        "author_avatar", "author_id",
                                        "body", "cannel_ids", "child_ids",
                                        "create_date", "create_uid",
                                        "date", "description",
                                        "display_name", "email_from",
                                        "id", "__last_update", "mail_server_id"
                                        , "message_id", "message_type", "model",
                                        "needaction", "needaction_partner_ids"
                                        , "no_auto_thread", "notification_ids", "parent_id",
                                        "parent_ids", "record_name", "reply_to", "res_id",
                                        "starred", "starred_partner_ids", "subject", "subtype_id"
                                        , "tracking_value_ids", "website_published", "write_date", "write_uid"));
                            }};
                            searchMessage = odoo.search_read(listenerMessage, user.getDatabase(),
                                    String.valueOf(user.getUserId()), user.getPassword(), "mail.message"
                                    , conMessage,
                                    fieldsMessage);*/


                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Log.d("task", "wrong");
                        }
                    });
                }
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
}
