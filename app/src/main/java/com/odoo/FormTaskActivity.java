package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.odoo.adapters.ProjectAutoCompleteAdapter;
import com.odoo.adapters.TaskAutoCompleteAdapter;
import com.odoo.adapters.UserAutoCompleteAdapter;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.dialog.MyTimePickerDialog;
import com.odoo.models.ProjectModel;
import com.odoo.models.TaskModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


/**
 * Created by makan on 06/09/2017.
 */

public class FormTaskActivity extends AppCompatActivity implements View.OnClickListener {

    int task_id;
    private TextView titleForm;
    private ImageButton imgBtnDate, closeFormTask, plusHours, minusHours;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    private String act = "add";
    private int timesheet_id = 0, uids = 0;
    private long createTaskId, deleteTaskId, searchTaskId,
            searchTaskIdProject, searchTaskIdTask, searchTaskUserId,
            searchTaskPartnerId, searchTaskIdCompany, updateTaskId;
    private Calendar calendar;
    private int currentYear, currentMonth, currentDay;
    private Button btnSaveTask;
    private EditText edtTaskName, edtInitial;

    String createWriteWithoutSec;
    private Map fields, fieldsProject, fieldsTask, fieldsUser, fieldsPartner, fieldsCompany;
    private List conditions, conditionsProject, conditionsTask, conditionsUser, conditionsPartner, conditionsCompany;
    private ProjectAutoCompleteAdapter projectAutoCompleteAdapter;
    private TaskAutoCompleteAdapter taskAutoCompleteAdapter;
    private UserAutoCompleteAdapter userAutoCompleteAdapter;
    private UserAutoCompleteAdapter partnerAutoCompleteAdapter;
    private UserAutoCompleteAdapter companyAutoCompleteAdapter;
    private ArrayList<ProjectModel> projectModelArrayList = new ArrayList<>();
    private ArrayList<TaskModel> taskModelArrayList = new ArrayList<>();
    private ArrayList<OUser> userModelArrayList = new ArrayList<>();
    private ArrayList<OUser> partnerModelArrayList = new ArrayList<>();
    private ArrayList<OUser> companyModelArrayList = new ArrayList<>();
    private AutoCompleteTextView autoCompleteTextViewProject, autoCompleteTextViewTask,
            autoCompleteTextViewUsers, autoCompleteTextViewPartners, autoCompleteTextViewCompanys;
    private int project_id = 0, parent_id = 0, user_id = 0, partner_id = 0, company_id = 0;
    private double initially = 0.0;
    private Button btnStartDate, btnEndDate, btnDeadline;
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_task);
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
                task_id = getIntent().getIntExtra("task_id", 0);

                btnSaveTask = (Button) findViewById(R.id.btnSaveTask);

                autoCompleteTextViewProject = (AutoCompleteTextView) findViewById(R.id.edtProject);
                autoCompleteTextViewTask = (AutoCompleteTextView) findViewById(R.id.edtParentTask);
                autoCompleteTextViewUsers = (AutoCompleteTextView) findViewById(R.id.edtAssignedTo);
                edtInitial = (EditText) findViewById(R.id.edtManDays);

                btnStartDate = (Button) findViewById(R.id.edtStartingDate);
                btnEndDate = (Button) findViewById(R.id.edtEndingDate);
                btnDeadline = (Button) findViewById(R.id.edtDeadline);

                btnStartDate.setOnClickListener(this);
                btnEndDate.setOnClickListener(this);
                btnDeadline.setOnClickListener(this);

                edtTaskName = (EditText) findViewById(R.id.edtTaskName);

                btnSaveTask.setOnClickListener(this);

                uids = Integer.parseInt(uid);
                SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
                SimpleDateFormat sdfCreateWriteWSec = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //createWrite = sdfCreateWrite.format(new Date());
                createWriteWithoutSec = sdfCreateWriteWSec.format(new Date());
                int task_id = getIntent().getIntExtra("task_id", 0);
                project_id = getIntent().getIntExtra("project_id", 0);

                getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                setupDialog();
                fieldsProject = new HashMap() {{
                    put("fields", Arrays.asList("id", "name"));
                }};
                conditionsProject = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", true)));

                fieldsTask = new HashMap() {{
                    put("fields", Arrays.asList("id", "name"));
                }};

                conditionsTask = Arrays.asList(Arrays.asList(
                        Arrays.asList("project_id", "=", project_id)));

                fieldsUser = new HashMap() {{
                    put("fields", Arrays.asList("id", "name"));
                }};

                conditionsUser = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", true)));

                fieldsPartner = new HashMap() {{
                    put("fields", Arrays.asList("id", "name"));
                }};

                conditionsPartner = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", true)));

                conditionsCompany = Arrays.asList(Arrays.asList(
                        Arrays.asList("id", ">=", 0)));

                searchTaskIdProject = odoo.search_read(listenerProject, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.project",
                        conditionsProject, fieldsProject);
                searchTaskIdTask = odoo.search_read(listenerTask, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.task",
                        conditionsTask, fieldsTask);
                searchTaskUserId = odoo.search_read(listenerUser, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "res.users",
                        conditionsUser, fieldsUser);

                if (getIntent().getStringExtra("act").toString().equals("update")) {
                    dialog.show();
                    act = "update";
                    setTitle("Edit Task");
                    fields = new HashMap() {{
                        put("fields", Arrays.asList("id", "name", "parent_id",
                                "project_id", "user_id", "planned_hours", "date_deadline",
                                "partner_id", "company_id", "date_start"
                                , "date_end"));
                    }};
                    conditions = Arrays.asList(Arrays.asList(
                            //Arrays.asList("create_uid", "=", user.getUserId())));
                            Arrays.asList("id", "=", task_id)));
                    // Arrays.asList("create_date", "like", "2017-07-28%")));

                    searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                            String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);

                } else {
                    setTitle("Create Task");
                    act = "add";
                    user_id = user.getUserId();
                    company_id = getIntent().getIntExtra("company_id", 0);
                    partner_id = getIntent().getIntExtra("partner_id", 0);
                    String company = getIntent().getStringExtra("company");
                    String customer = getIntent().getStringExtra("customer");
                    String taskTitle = getIntent().getStringExtra("task_title");
                    String projectName = getIntent().getStringExtra("project_name");
                    edtTaskName.setText(taskTitle);
                    autoCompleteTextViewProject.setText(projectName);
                    autoCompleteTextViewUsers.setText(user.getName());
                }

            } catch (NullPointerException err) {
                err.printStackTrace();
            }

        }

    }

    Calendar date, dateStart = null, dateEnd = null;


    public void showDateTimePicker(final String mode) {
        final Calendar currentDate = Calendar.getInstance();
        date = Calendar.getInstance();
        dateStart = Calendar.getInstance();
        dateEnd = Calendar.getInstance();
        new DatePickerDialog(FormTaskActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                date.set(year, monthOfYear, dayOfMonth);
                String sMonth = (monthOfYear < 10) ? "0" + monthOfYear : monthOfYear + "";
                String sDay = (dayOfMonth < 10) ? "0" + dayOfMonth : dayOfMonth + "";
                if (mode.equals("start")) {
                    btnStartDate.setText(year + "-" + sMonth + "-" + sDay);
                    dateStart.set(year, monthOfYear, dayOfMonth);
                } else if (mode.equals("end")) {
                    btnEndDate.setText(year + "-" + sMonth + "-" + sDay);
                    dateEnd.set(year, monthOfYear, dayOfMonth);
                } else if (mode.equals("dead")) {
                    btnDeadline.setText(year + "-" + sMonth + "-" + sDay);
                }
                if (mode.equals("start") || mode.equals("end")) {
                    MyTimePickerDialog mTimePicker = new MyTimePickerDialog(FormTaskActivity.this, new MyTimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(com.odoo.core.utils.dialog.TimePicker view, int hourOfDay, int minute, int seconds) {
                            date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            date.set(Calendar.MINUTE, minute);
                            String sHours = (hourOfDay < 10) ? "0" + hourOfDay : hourOfDay + "";
                            String sMinute = (minute < 10) ? "0" + minute : minute + "";
                            String sSecond = (seconds < 10) ? "0" + seconds : seconds + "";
                            Log.v("s", "The choosen one " + date.getTime());
                            if (mode.equals("start")) {
                                btnStartDate.setText(btnStartDate.getText() + " " +
                                        sHours + ":" + sMinute + ":" + sSecond);
                                dateStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                dateStart.set(Calendar.MINUTE, minute);
                                dateStart.set(Calendar.SECOND, seconds);
                            } else if (mode.equals("end")) {
                                btnEndDate.setText(btnEndDate.getText() + " " +
                                        sHours + ":" + sMinute + ":" + sSecond);
                                dateEnd.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                dateEnd.set(Calendar.MINUTE, minute);
                                dateEnd.set(Calendar.SECOND, seconds);
                            }
                        }

                    }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), currentDate.get(Calendar.SECOND), true);
                    mTimePicker.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (mode.equals("start")) {
                                btnStartDate.setText("");
                            } else if (mode.equals("end")) {
                                btnEndDate.setText("");
                            } else if (mode.equals("dead")) {
                                btnDeadline.setText("");
                            }
                        }
                    });
                    mTimePicker.show();
                }

            }
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    XMLRPCCallback listenerUser = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if (id == searchTaskUserId) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                OUser userModel = new OUser();
                                userModel.setUserId(Integer.parseInt(classObjPer.get("id").toString()));
                                userModel.setName(classObjPer.get("name").toString());
                                userModelArrayList.add(userModel);
                            }
                            userAutoCompleteAdapter = new UserAutoCompleteAdapter(FormTaskActivity.this,
                                    userModelArrayList);
                            autoCompleteTextViewUsers.setAdapter(userAutoCompleteAdapter);
                            autoCompleteTextViewUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.d("selected id user", userAutoCompleteAdapter.getItemId(position) + "");
                                    user_id = (int) userAutoCompleteAdapter.getItemId(position);
                                }
                            });


                        }
                    });
                } else {
                    Log.d("user data", "null");
                }
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {

        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {

        }
    };

    XMLRPCCallback listenerTask = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if (id == searchTaskId) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Map<String, Object> classObjPer
                                    = (Map<String, Object>) classObjs[0];
                            Log.d("task title", classObjPer.get("name").toString());
                            edtTaskName.setText(classObjPer.get("name").toString());
                            M2OField project = OdooUtility.getMany2One(classObjPer, "project_id");
                            M2OField parent_task = OdooUtility.getMany2One(classObjPer, "parent_id");
                            M2OField user = OdooUtility.getMany2One(classObjPer, "user_id");
                            Log.d("task project", project.value);
                            Log.d("task parent", parent_task.value);
                            Log.d("task assigned to", user.value);
                            //Log.d("task initially planned", classObjPer.get("planned_hours").toString());
                            //Log.d("task deadline", classObjPer.get("date_deadline").toString());
                            Log.d("task date start", classObjPer.get("date_start").toString());
                            Log.d("task date end", classObjPer.get("date_end").toString());
                            autoCompleteTextViewProject.setText(project.value);
                            autoCompleteTextViewTask.setText(parent_task.value);
                            autoCompleteTextViewUsers.setText(user.value);
                            edtInitial.setText(classObjPer.get("planned_hours").toString());
                            SimpleDateFormat sdfGet = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            try {
                                Date dateStartD = sdfGet.parse(classObjPer.get("date_start").toString());
                                Date dateEndD = sdfGet.parse(classObjPer.get("date_end").toString());
                                dateStartD.setTime(dateStartD.getTime() + 3600 * 7000);
                                String dateStart = sdfGet.format(dateStartD);
                                String dateEnd = sdfGet.format(dateEndD);
                                btnStartDate.setText(dateStart);
                                btnEndDate.setText(dateEnd);
                            } catch (ParseException e) {
                                Log.e("error parse ", e.toString());
                            }
                            btnDeadline.setText(classObjPer.get("date_deadline").toString());
                            parent_id = parent_task.id;
                            user_id = user.id;
                            project_id = project.id;
                            initially = Double.parseDouble(classObjPer.get("planned_hours").toString());
                            dialog.hide();
                        }
                    });
                }
            } else if (id == searchTaskIdTask) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                TaskModel taskModel = new TaskModel();
                                taskModel.setId(Integer.parseInt(classObjPer.get("id").toString()));
                                taskModel.setName(classObjPer.get("name").toString());
                                taskModelArrayList.add(taskModel);
                            }
                            taskAutoCompleteAdapter = new TaskAutoCompleteAdapter(FormTaskActivity.this,
                                    taskModelArrayList);
                            autoCompleteTextViewTask.setAdapter(taskAutoCompleteAdapter);
                            autoCompleteTextViewTask.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.d("selected id task", taskAutoCompleteAdapter.getItemId(position) + "");
                                    parent_id = (int) taskAutoCompleteAdapter.getItemId(position);
                                }
                            });


                        }
                    });
                }
            } else if (id == createTaskId) {
                final int new_task_id = Integer.parseInt(result.toString());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        Intent intent = new Intent(FormTaskActivity.this, TimesheetActivity.class);
                        intent.putExtra("project_id", project_id);
                        intent.putExtra("task_id", new_task_id);
                        startActivity(intent);
                        finish();
                    }
                });
            } else if (id == updateTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        Intent intent = new Intent(FormTaskActivity.this, TimesheetActivity.class);
                        intent.putExtra("project_id", project_id);
                        intent.putExtra("task_id", task_id);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.d("errornya ", error.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.hide();
                }
            });

        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.d("errornya server", error.getMessage());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.hide();
                }
            });
        }
    };

    XMLRPCCallback listenerProject = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if (id == searchTaskIdProject) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                ProjectModel projectModel = new ProjectModel();
                                projectModel.setId(Integer.parseInt(classObjPer.get("id").toString()));
                                projectModel.setName(classObjPer.get("name").toString());
                                projectModelArrayList.add(projectModel);
                            }
                            projectAutoCompleteAdapter = new ProjectAutoCompleteAdapter(FormTaskActivity.this,
                                    projectModelArrayList);
                            autoCompleteTextViewProject.setAdapter(projectAutoCompleteAdapter);
                            autoCompleteTextViewProject.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Log.d("selected id project", projectAutoCompleteAdapter.getItemId(position) + "");
                                    project_id = (int) projectAutoCompleteAdapter.getItemId(position);
                                    taskModelArrayList.clear();
                                    taskAutoCompleteAdapter.clearItems();
                                    taskAutoCompleteAdapter.notifyDataSetChanged();
                                    conditionsTask = Arrays.asList(Arrays.asList(
                                            Arrays.asList("project_id", "=", project_id)));
                                    searchTaskIdTask = odoo.search_read(listenerTask, user.getDatabase(),
                                            String.valueOf(user.getUserId()), user.getPassword(), "project.task",
                                            conditionsTask, fieldsTask);
                                }
                            });


                        }
                    });
                }
            }
        }

        @Override
        public void onError(long id, XMLRPCException error) {

        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {

        }
    };

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

    private void setupDialog() {
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading");
        dialog.setCancelable(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveTask:
                dialog.show();
                if (getIntent().getStringExtra("act").toString().equals("add")) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
                    try {
                        if (btnDeadline.getText().toString().isEmpty() ||
                                btnStartDate.getText().toString().isEmpty() ||
                                btnEndDate.getText().toString().isEmpty()) {
                            dialog.hide();
                            odoo.MessageDialog(FormTaskActivity.this, "Please enter a valid date");
                        }
                        List dataToInput = Arrays.asList(new HashMap() {{
                            put("create_uid", uids);
                            put("user_id", user_id);
                            put("name", edtTaskName.getText().toString());
                            put("project_id", project_id);
                            put("parent_id", parent_id);
                            put("partner_id", partner_id);
                            put("company_id", company_id);
                            put("planned_hours", Double.parseDouble(edtInitial.getText().toString()));
                            put("create_date", currentDate);
                            put("__last_update", currentDate);
                            put("date_deadline", btnDeadline.getText().toString());
                            put("date_start", btnStartDate.getText().toString());
                            put("date_end", btnEndDate.getText().toString());
                        }});
                        //Log.d("data", dataToInput.toString());
                        createTaskId = odoo.create(listenerTask, user.getDatabase(), String.valueOf(user.getUserId()),
                                user.getPassword(), "project.task", dataToInput);
                    } catch (NumberFormatException excption) {
                        odoo.MessageDialog(FormTaskActivity.this, "Enter valid number of initially planned hours!");
                        dialog.hide();

                    }

                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
                    List dataToUpdate = Arrays.asList(
                            Arrays.asList(task_id),
                            new HashMap() {{
                                put("user_id", user_id);
                                put("name", edtTaskName.getText().toString());
                                put("project_id", project_id);
                                put("parent_id", parent_id);
                                put("partner_id", partner_id);
                                put("company_id", company_id);
                                put("planned_hours", Double.parseDouble(edtInitial.getText().toString()));
                                put("__last_update", currentDate);
                                put("date_deadline", btnDeadline.getText().toString());
                                put("date_start", btnStartDate.getText().toString());
                                put("date_end", btnEndDate.getText().toString());
                            }});
                    updateTaskId = odoo.update(listenerTask, database, uid, password, "project.task", dataToUpdate);

                }
                break;
            case R.id.edtStartingDate:
                showDateTimePicker("start");
                break;
            case R.id.edtEndingDate:
                showDateTimePicker("end");
                break;
            case R.id.edtDeadline:
                showDateTimePicker("dead");
                break;
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
}
