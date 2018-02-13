package com.odoo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Build;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.odoo.adapters.ProjectAdapter;
import com.odoo.adapters.SearchProjectAdapter;
import com.odoo.adapters.TimesheetAdapter;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.support.OUser;
import com.odoo.models.ProjectModel;
import com.odoo.models.TimesheetModel;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;


public class ProjectActivity extends AppCompatActivity {

    List<ProjectModel> listProject = new ArrayList<>();
    ListView listViewProject;
    ProjectAdapter projectAdapter;
    private long searchTaskId, createTaskId, updateTaskId, deleteTaskId;
    private List conditions;
    private Map fields;
    private OdooUtility odoo;
    private OUser user;
    private String uid, password, serverAddress, database, url;
    private SwipeRefreshLayout mSwipeProject;
    private Menu menu;
    private boolean isSearching = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        mSwipeProject = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshProject);
        mSwipeProject.setRefreshing(true);
        url = SharedData.getKey(ProjectActivity.this, "url").toString();
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

                listViewProject = (ListView) findViewById(R.id.lvProjects);

                getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);



                conditions = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", "true")));

                fields = new HashMap() {{
                    put("fields", Arrays.asList("id", "name", "doc_count", "task_count", "write_date",
                            "__last_update", "is_favorite", "company_id", "partner_id"));
                }};
                /*searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);*/
                loadPinned();
                mSwipeProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadPinned();
                        //searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                         //       String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);
                    }
                });
            } catch(NullPointerException err) {
                startActivity(new Intent(ProjectActivity.this, OdooLogin.class));
            }
        }
    }

    public void loadPinned() {
        Gson gson = new Gson();
        ArrayList<ProjectModel> arrayList = new ArrayList<>();
        String prevJson = SharedData.getKey(ProjectActivity.this, "pinned_project");
        listProject.clear();
        if(prevJson!="") {
            Type type = new TypeToken<ArrayList<ProjectModel>>() {}.getType();
            arrayList = gson.fromJson(prevJson, type);
            if(arrayList.size() > 0) {
                listProject.addAll(arrayList);
                findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.GONE);
                fillListProject();
                mSwipeProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadPinned();
                    }
                });
            } else {
                mSwipeProject.setRefreshing(false);
                listViewProject.setVisibility(View.GONE);
                findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.VISIBLE);
                mSwipeProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        loadPinned();
                    }
                });
            }
        } else {
            mSwipeProject.setRefreshing(false);
            listViewProject.setVisibility(View.GONE);
            findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.VISIBLE);
            mSwipeProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    loadPinned();
                }
            });
        }
    }

    XMLRPCCallback listenerProject = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskId && isSearching) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeProject.setRefreshing(false);
                    }
                });
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.GONE);
                            listProject.clear();
                            Gson gson = new Gson();
                            ArrayList<ProjectModel> arrayList = new ArrayList<>();
                            String prevJson = SharedData.getKey(ProjectActivity.this, "pinned_project");
                            Log.d("json project", prevJson);
                            if(prevJson!="") {
                                Type type = new TypeToken<ArrayList<ProjectModel>>() {
                                }.getType();
                                arrayList = gson.fromJson(prevJson, type);
                                if (!arrayList.isEmpty()) {
                                    listProject.addAll(arrayList);
                                }
                            }
                                for (int i = 0; i < length; i++) {
                                    Map<String, Object> classObj = (Map<String, Object>) classObjs[i];
                                    M2OField company = OdooUtility.getMany2One(classObj, "company_id");
                                    M2OField customer = OdooUtility.getMany2One(classObj, "partner_id");
                                    ProjectModel projectModel = new ProjectModel();
                                    projectModel.setId(Integer.parseInt(classObj.get("id").toString()));
                                    projectModel.setName(classObj.get("name").toString());
                                    projectModel.setTask_count(Integer.parseInt(classObj.get("task_count").toString()));
                                    projectModel.setDoc_count(Integer.parseInt(classObj.get("doc_count").toString()));
                                    //projectModel.setRecentLogDate(classObj.get("write_date").toString());
                                    String dateString = classObj.get("__last_update").toString();
                                    DateFormat readFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                    Date date = null;
                                    DateFormat writeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                                    try {
                                        date = readFormat.parse(dateString);
                                    } catch (ParseException e) {
                                        Log.d("error parse ", e.toString());
                                    }
                                    dateString = writeFormat.format(date);
                                    projectModel.setLastUpdatedOn(dateString);
                                    projectModel.setCompany_id(company.id);
                                    projectModel.setCompany(company.value);
                                    projectModel.setPartner_id(customer.id);
                                    projectModel.setCustomer(customer.value);
                                    projectModel.setIs_favorite(classObj.get("is_favorite").toString());
                                    Log.d("project", classObj.toString());
                                    if (!checkIsBookmarked(projectModel.getId(), arrayList)) {
                                        projectModel.setIs_pinned(false);
                                        listProject.add(projectModel);
                                    }
                                }
                                fillListProject();
                                mSwipeProject.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                    @Override
                                    public void onRefresh() {

                                        searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                                                String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);
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
            } else {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        loadPinned();
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
                    loadHistory(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String query) {
                    isSearching = true;
                    loadHistory(query);
                    return true;
                }
            });

            search.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    isSearching = true;
                    findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.GONE);
                    mSwipeProject.setRefreshing(true);
                    searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                            String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);


                }
            });
            search.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    isSearching = false;
                    loadPinned();
                    return false;
                }
            });
        }
        return true;
    }

    private void loadHistory(String query) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if(listProject.size() > 0) {
                conditions = Arrays.asList(Arrays.asList(
                        Arrays.asList("active", "=", "true"),
                        Arrays.asList("name","ilike","%" + query + "%")));
                searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);
            } else {
                searchTaskId = odoo.search_read(listenerProject, user.getDatabase(),
                        String.valueOf(user.getUserId()), user.getPassword(), "project.project", conditions, fields);
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

    public void fillListProject() {
        //findViewById(R.id.layoutEmptyPinnedProjects).setVisibility(View.GONE);
        listViewProject.setVisibility(View.VISIBLE);
        projectAdapter = new ProjectAdapter(this, listProject);
        listViewProject.setAdapter(projectAdapter);
        projectAdapter.notifyDataSetChanged();
        mSwipeProject.setRefreshing(false);

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
