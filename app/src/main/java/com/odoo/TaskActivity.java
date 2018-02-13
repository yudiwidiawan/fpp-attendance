package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.support.v7.widget.SearchView;

import com.google.android.gms.gcm.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.adapters.ProjectAdapter;
import com.odoo.adapters.TaskAdapter;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.models.ProjectModel;
import com.odoo.models.TaskModel;

import java.lang.reflect.Type;
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

public class TaskActivity extends AppCompatActivity {

    ArrayList<TaskModel> listTask = new ArrayList<>();
    ListView listViewTask;
    TaskAdapter taskAdapter;
    private long searchTaskId, createTaskId, updateTaskId, deleteTaskId;
    private List conditions;
    private Map fields;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    private SwipeRefreshLayout mSwipeTask;
    private Menu menu;
    private boolean isSearching = false;
    private int project_id;
    private Button btnAddTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);
        mSwipeTask = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshTask);
        mSwipeTask.setRefreshing(true);
        url = SharedData.getKey(TaskActivity.this, "url").toString();
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            try {
                btnAddTask = (Button) findViewById(R.id.btnAddTask);
                user = OUser.current(this);
                uid = user.getUserId().toString();
                password = user.getPassword();
                serverAddress = user.getHost();
                database = user.getDatabase();
                odoo = new OdooUtility(user.getHost(), "object");
                listViewTask = (ListView) findViewById(R.id.lvTask);

                project_id = getIntent().getIntExtra("id", 0);
                final String project_name = getIntent().getStringExtra("project_name");
                final M2OField company = new M2OField();
                company.id = getIntent().getIntExtra("company_id", 0);
                company.value = getIntent().getStringExtra("company");
                final M2OField customer = new M2OField();
                customer.id = getIntent().getIntExtra("partner_id", 0);
                customer.value = getIntent().getStringExtra("customer");
                company.toString();
                customer.toString();
                btnAddTask.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String taskTitle = btnAddTask.getText().toString().substring(13);
                        taskTitle = taskTitle.substring(0, taskTitle.length()-1);
                        Intent intent = new Intent(TaskActivity.this, FormTaskActivity.class);
                        intent.putExtra("act", "add");
                        intent.putExtra("project_id", project_id);
                        intent.putExtra("project_name", project_name);
                        intent.putExtra("task_title", taskTitle);
                        intent.putExtra("partner_id", customer.id);
                        intent.putExtra("customer", customer.value);
                        intent.putExtra("company_id", company.id);
                        intent.putExtra("company", company.value);
                        startActivity(intent);
                    }
                });
                getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);

                conditions = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", "true"),
                        Arrays.asList("project_id","=",project_id)));

                fields = new HashMap() {{
                    put("fields", Arrays.asList("id", "name", "date_deadline", "project_id", "stage_id"));
                }};
                //searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                  //      String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);
                loadPinned();
                mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadPinned();
                       // searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                         //       String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);
                    }
                });
            } catch(NullPointerException err) {
                startActivity(new Intent(TaskActivity.this, OdooLogin.class));
                Log.d("errortask", err.toString());
            }
        }
    }

    public void loadPinned() {
        Gson gson = new Gson();
        ArrayList<TaskModel> arrayList = new ArrayList<>();
        String prevJson = SharedData.getKey(TaskActivity.this, "pinned_task");
        listTask.clear();
        if(prevJson!="") {
            Type type = new TypeToken<ArrayList<TaskModel>>() {
            }.getType();
            arrayList = gson.fromJson(prevJson, type);
            if(arrayList.size()>0) {
                mSwipeTask.setRefreshing(false);
                for(int i = 0; i < arrayList.size(); i++) {
                    if(arrayList.get(i).getProject_id() == project_id) {
                        listTask.add(arrayList.get(i));
                    }
                }
                // Jika daftar task terbookmark pada project lebih besar dari 0 maka tampil
                if(listTask.size() > 0) {
                    fillListTask();
                    mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            loadPinned();
                        }
                    });
                } else { // Jika tidak ada daftar task terbookmark pada project
                    mSwipeTask.setRefreshing(false);
                    listViewTask.setVisibility(View.GONE);
                    findViewById(R.id.layoutEmptyPinnedTasks).setVisibility(View.VISIBLE);
                    mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            loadPinned();
                        }
                    });
                }

            } else {
                mSwipeTask.setRefreshing(false);
                listViewTask.setVisibility(View.GONE);
                findViewById(R.id.layoutEmptyPinnedTasks).setVisibility(View.VISIBLE);
                mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadPinned();
                    }
                });
            }
        } else {
            mSwipeTask.setRefreshing(false);
            listViewTask.setVisibility(View.GONE);
            findViewById(R.id.layoutEmptyPinnedTasks).setVisibility(View.VISIBLE);
            mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadPinned();
                }
            });
        }
    }
    XMLRPCCallback listenerTask = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskId && isSearching) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listTask.clear();
                            Gson gson = new Gson();
                            ArrayList<TaskModel> arrayList = new ArrayList<>();
                            String prevJson = SharedData.getKey(TaskActivity.this, "pinned_task");
                            if(prevJson!="") {
                                Type type = new TypeToken<ArrayList<TaskModel>>() {
                                }.getType();
                                arrayList = gson.fromJson(prevJson, type);
                                for(int i = 0; i < arrayList.size(); i++) {
                                    if(arrayList.get(i).getProject_id() == project_id) {
                                        listTask.add(arrayList.get(i));
                                    }
                                }
                            }

                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObj = (Map<String, Object>) classObjs[i];
                                TaskModel taskModel = new TaskModel();
                                taskModel.setId(Integer.parseInt(classObj.get("id").toString()));
                                M2OField project = OdooUtility.getMany2One(classObj, "project_id");
                                M2OField stage = OdooUtility.getMany2One(classObj, "stage_id");
                                taskModel.setName(classObj.get("name").toString());
                                String dateString = classObj.get("date_deadline").toString();
                                if(!dateString.equals("false")) {
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
                                taskModel.setProject_id(project.id);
                                taskModel.setProjectName(project.value);
                                taskModel.setStage_id(stage.id);
                                taskModel.setStage(stage.value);
                                taskModel.setDeadline(dateString);
                                Log.d("project", classObj.toString());
                                if (!checkIsBookmarked(taskModel.getId(), arrayList)) {
                                    taskModel.setIs_pinned(false);
                                    listTask.add(taskModel);
                                }
                            }
                            fillListTask();
                            mSwipeTask.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                @Override
                                public void onRefresh() {
                                    searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                                            String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);
                                }
                            });


                        }
                    });
                }else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_project, menu);
        this.menu = menu;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            SearchView search = (SearchView) menu.findItem(R.id.project_search).getActionView();
            search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    isSearching = true;
                    btnAddTask.setVisibility(View.VISIBLE);
                    btnAddTask.setText("Create Task '" + query +"'");
                    loadHistory(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    isSearching = true;
                    btnAddTask.setVisibility(View.VISIBLE);
                    btnAddTask.setText("Create Task '" + query +"'");
                    loadHistory(query);
                    return true;
                }
            });
            search.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSearching = true;
                    findViewById(R.id.layoutEmptyPinnedTasks).setVisibility(View.GONE);
                    mSwipeTask.setRefreshing(true);
                    searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                            String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);


                }
            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    isSearching = false;
                    btnAddTask.setText("Create Task");
                    btnAddTask.setVisibility(View.GONE);
                    loadPinned();
                    return false;
                }
            });
        }
        return true;
    }




    private List<String> items;
    private void loadHistory(String query) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(listTask.size() > 0) {
                conditions = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", "true"),
                        Arrays.asList("project_id","=",getIntent().getIntExtra("id",0)),
                        Arrays.asList("name", "ilike", "%"+query+"%")));
                searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);
            } else {
                searchTaskId = odoo.search_read(listenerTask, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.task", conditions, fields);
            }
        }
    }

    public void fillListTask() {
        findViewById(R.id.layoutEmptyPinnedTasks).setVisibility(View.GONE);
        listViewTask.setVisibility(View.VISIBLE);
        taskAdapter = new TaskAdapter(this, listTask);
        listViewTask.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();
        mSwipeTask.setRefreshing(false);
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
