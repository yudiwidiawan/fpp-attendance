package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.odoo.adapters.CheckInAdapter;
import com.odoo.base.addons.res.ResCompany;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.orm.OModel;
import com.odoo.core.orm.ServerDataHelper;
import com.odoo.core.rpc.helper.OArguments;
import com.odoo.core.service.GeocoderIntentService;
import com.odoo.core.service.receivers.DistanceCalculatorService;
import com.odoo.core.support.OUser;
import com.odoo.core.utils.InternetPeripheralUtils;
import com.odoo.models.AttendanceModel;

import org.w3c.dom.Text;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

/**
 * Created by makan on 26/07/2017.
 */

public class AttendanceActivity extends AppCompatActivity implements View.OnClickListener,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private OdooUtility odoo;
    private String uid;
    private String password;
    private String serverAddress;
    private String database;
    private String thisIPAddress, thisAddress;
    private long searchTaskId, createAttendanceTaskId, updateAttendanceTaskId;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private String checkInId;
    private int employeeID;
    private List conditions,conditions1;
    private Map fields,fields1;
    TextView txvGreetings, txvStatusCheckInOut;
    private int checkedIn = 0;
    private Button btnCheck;
    OUser user;
    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, mFirstLocation;
    Marker mCurrLocationMarker;
    SharedPreferences sharedPref;
    String url;
    PendingIntent pendingIntent;
    double workedHours = 0;

    private int PLACE_PICKER_REQUEST = 1;
    private ProgressDialog dialog;
    private GeocoderResultReceiver geocoderReceiver;
    private TextView txvAddress;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private Animation animationSlideDown, animationMoveRight
            ,animationSlideUp;

    private String[] mMenuTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout layoutStatusCheckInOut, layoutStatusAllCheckIn,
    layoutInfoAllCheckIn;
    private ImageButton imgBtnCloseLayoutStatusCIO;
    private boolean slideState = false;
    private String fpp_user_pref;
    private Chronometer chronoMeter;
    private TextView getTxvStatusCheckTime;
    private Button btnAddHoursToTimesheet;
    private long searchEMP;
    private String currentDate;
    private TextView statusCheckInToday;

    private ListView lvCheckInOut;
    private CheckInAdapter checkInAdapter;
    private ArrayList<AttendanceModel> listAttendanceModel = new ArrayList<>();
    private boolean tableOpened = false;
    private ImageButton imgButtonExpandAttendance;
    private OModel mModel;
    LocationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        animationSlideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
        animationMoveRight = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.move_right);
        animationSlideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        fpp_user_pref = SharedData.getKey(AttendanceActivity.this, "fpp_user_pref").toString();


        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        animationSlideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutStatusCheckInOut.setVisibility(View.GONE);
                imgBtnCloseLayoutStatusCIO.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        chronoMeter = (Chronometer) findViewById(R.id.chronometer2);
        lvCheckInOut = (ListView) findViewById(R.id.lvCheckedInToday);

        btnAddHoursToTimesheet = (Button) findViewById(R.id.btnAddHourtoTimesheet);
        btnAddHoursToTimesheet.setOnClickListener(this);
        layoutStatusCheckInOut = (LinearLayout) findViewById(R.id.layoutStatusCheckInOut);
        txvStatusCheckInOut = (TextView) findViewById(R.id.checkInTime);
        imgBtnCloseLayoutStatusCIO = (ImageButton) findViewById(R.id.closeNotifCheckIn);
        imgButtonExpandAttendance = (ImageButton) findViewById(R.id.btnExpandAttendance);
        imgButtonExpandAttendance.setOnClickListener(this);
        imgBtnCloseLayoutStatusCIO.setOnClickListener(this);
        layoutStatusAllCheckIn = (LinearLayout) findViewById(R.id.infoAllCheckin);
        layoutInfoAllCheckIn = (LinearLayout) findViewById(R.id.layoutInfoAllCheckIn);
        layoutStatusAllCheckIn.setOnClickListener(this);
        getTxvStatusCheckTime = (TextView) findViewById(R.id.txvStatusCheckTime);
        statusCheckInToday = (TextView) findViewById(R.id.infoCheckIn);


        /*Toolbar toolbar = (Toolbar) findViewById(R.id.alltoolbar);
        toolbar.setLogo(R.drawable.action_bar_menu);
        toolbar.setBackgroundColor(getResources().getColor(R.color.odoo_theme));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if(slideState){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else{
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });
        setSupportActionBar(toolbar);*/
        getSupportActionBar().setBackgroundDrawable(getApplicationContext().getResources().getDrawable(R.color.odoo_theme));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        mMenuTitles = getResources().getStringArray(R.array.menus_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout_attendance);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerToggle = new ActionBarDrawerToggle(AttendanceActivity.this,
                mDrawerLayout, R.string.opened, R.string.closed) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                slideState = false;
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                slideState = true;
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);


        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, mMenuTitles));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshAttendance);
        //txvAddress = (TextView) findViewById(R.id.txvAddress);
        url = SharedData.getKey(this, "url").toString();
        // letakkan method pemanggilan ini di dalam onCreate
        //geocoderReceiver = new GeocoderResultReceiver(new Handler());
        database = SharedData.getKey(this, "db").toString();
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            user = OUser.current(this);
            btnCheck = (Button) findViewById(R.id.btnCheck);
            txvGreetings = (TextView) findViewById(R.id.greetings);
            txvGreetings.setText("Welcome, " + user.getName());
            btnCheck.setOnClickListener(this);
            uid = user.getUserId().toString();
            password = user.getPassword();
            serverAddress = user.getHost();
            database = user.getDatabase();
            odoo = new OdooUtility(user.getHost(), "object");

            setupDialog();
            //txvCheckedInTime = (TextView) findViewById(R.id.txvCheckInTime);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            currentDate = sdf.format(new Date());
            loadInfoAttendance();

            /*if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                buildAlertMessageNoGPS();
            } else {


                //Arrays.asList("create_date", "like", "2017" + "%")));
                //Arrays.asList("create_date", "like", currentDate + "%")));
                // Arrays.asList("create_date", "like", "2017-07-28%")));

               loadInfoAttendance();

                //sendMessage();



            /*ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBarWorkHours);
            ObjectAnimator animation = ObjectAnimator.ofInt (progressBar, "progress", 0, 5000); // see this max value coming back here, we animale towards that value
            animation.setDuration(1000);
            animation.setRepeatMode(ValueAnimator.RESTART);
            animation.setRepeatCount(ValueAnimator.INFINITE);
            animation.setInterpolator (new DecelerateInterpolator());
            animation.start();
            }*/
        }

    }

    public void loadInfoAttendance() {
        fields = new HashMap() {{
            put("fields", Arrays.asList("id", "employee_id", "create_uid",
                    "check_in", "check_out", "worked_hours",
                    "lat_in", "long_in", "create_date"));
            put("limit", 75);
        }};
        conditions1 = Arrays.asList(Arrays.asList(
                //Arrays.asList("create_uid", "=", user.getUserId())));
                Arrays.asList("user_id", "=", user.getUserId())));
        // Arrays.asList("create_date", "like", "2017-07-28%")));

        fields1 = new HashMap() {{
            put("fields", Arrays.asList("id", "user_id", "name",
                    "display_name", "company_id"));
        }};
        searchEMP = odoo.search_read(listenerEmp, user.getDatabase(),
                String.valueOf(user.getUserId()), user.getPassword(), "hr.employee", conditions1, fields1);
        thisIPAddress = InternetPeripheralUtils.getIPAddress(true);
    }

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public void getAttendanceStatus() {
        conditions = Arrays.asList(Arrays.asList(
                Arrays.asList("employee_id", "=", employeeID)));
        searchTaskId = odoo.search_read(listener, user.getDatabase(),
                String.valueOf(user.getUserId()), user.getPassword(), "hr.attendance", conditions, fields);
    }

    private void buildAlertMessageNoGPS() {
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle("GPS seems to be disabled")
                .setMessage("This app needs the GPS, please enable GPS or High Accuracy mode GPS.")
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                            @Override
                            public void onRefresh() {
                                loadInfoAttendance();
                            }
                        });
                    }
                })
                .create()
                .show();

    }

    public void setAlarm() {
        Intent myIntent = new Intent(this, NotificationReceiver.class);
        myIntent.putExtra("checkInId", checkInId+1);
        myIntent.putExtra("ip_address_out", thisIPAddress);
        pendingIntent = PendingIntent.getBroadcast(this,
                0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 1000,
                1000, pendingIntent);

    }

    public void setAlarm(long diff) {
        Intent myIntent = new Intent(this, NotificationReceiver.class);
        myIntent.putExtra("checkInId", checkInId);
        myIntent.putExtra("ip_address_out", thisIPAddress);
        pendingIntent = PendingIntent.getBroadcast(this,
                0, myIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long toBeSet = 7200000 - (diff % 7200000);
        Log.d("getTime",toBeSet+"");

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+toBeSet,
                7200000, pendingIntent);
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

    XMLRPCCallback listenerEmp = new XMLRPCCallback() {
        @Override
        public void onResponse(long id, Object result) {
            if(id == searchEMP) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (int i = 0; i < length; i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                employeeID = Integer.parseInt(classObjPer.get("id").toString());
                                SharedData.setKey(AttendanceActivity.this, "empID", String.valueOf(employeeID));
                                getAttendanceStatus();
                                mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                    @Override
                                    public void onRefresh() {
                                        getAttendanceStatus();
                                    }
                                });
                                //Initialize Google Play Services
                                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(AttendanceActivity.this,
                                            Manifest.permission.ACCESS_FINE_LOCATION)
                                            == PackageManager.PERMISSION_GRANTED) {
                                        //Location Permission already granted
                                        buildGoogleApiClient();
                                    } else {
                                        //Request Location Permission
                                        checkLocationPermission();
                                    }
                                } else {
                                    buildGoogleApiClient();
                                }
                            }
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

    long StartTime;
    XMLRPCCallback listener = new XMLRPCCallback() {
        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == searchTaskId) {
                final Object[] classObjs = (Object[]) result;
                final int length = classObjs.length;
                if (length > 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            double total_hours = 0.0;int hour=0,minute=0;
                            int total_check_in = 0;
                            listAttendanceModel.clear();
                            for(int i =0; i<length;i++) {
                                Map<String, Object> classObjPer
                                        = (Map<String, Object>) classObjs[i];
                                String tanggalStr = classObjPer.get("create_date")
                                        .toString().substring(0,10);
                                if(tanggalStr.equals(currentDate)) {
                                    total_hours = Double
                                            .parseDouble(classObjPer.get("worked_hours").toString());
                                    total_hours = total_hours * 3600;
                                    AttendanceModel attendanceModel = new AttendanceModel();
                                    attendanceModel.setId(Integer.parseInt(classObjPer.get("id").toString()));
                                    attendanceModel.setCheck_in(classObjPer.get("check_in").toString());
                                    attendanceModel.setCheck_out(classObjPer.get("check_out").toString());
                                    attendanceModel.setHours(classObjPer.get("worked_hours").toString());
                                    listAttendanceModel.add(attendanceModel);
                                    hour += (int) (total_hours / 3600);
                                    minute += (int) (total_hours % 3600)/60;
                                    total_check_in++;
                                }
                            }
                            Log.d("total worked_hours", hour+":"+minute);
                            if(minute >= 60) {
                                hour += (minute / 60);
                                minute = (minute % 60);
                            }
                            String sHour = (hour > 9) ? hour+"" : ("0"+hour);
                            String sMinute = (minute > 9) ? minute+"" : ("0"+minute);
                            SharedData.setKey(AttendanceActivity.this, "total_hours", sHour+":"+sMinute);
                            statusCheckInToday.setText("Today, you've checked in "+ total_check_in
                                    +" time(s) with total " + sHour + ":" + sMinute + " of work time.");
                            btnAddHoursToTimesheet.setVisibility(View.VISIBLE);
                            Map<String, Object> classObj = (Map<String, Object>) classObjs[0];
                            checkInId = classObj.get("id").toString();
                            checkInAdapter = new CheckInAdapter(AttendanceActivity.this, listAttendanceModel);
                            lvCheckInOut.setAdapter(checkInAdapter);
                            setListViewHeightBasedOnChildren(lvCheckInOut);

                            if (classObj.get("check_out").toString().equals("false")) {
                                /*stopService(new Intent(AttendanceActivity.this, DistanceCalculatorService.class));
                                Intent intent = new Intent(AttendanceActivity.this,
                                        DistanceCalculatorService.class);
                                intent.putExtra("workLocation", mLastLocation);
                                intent.putExtra("checkInId", checkInId);
                                intent.putExtra("ip_address_out", thisIPAddress);
                                startService(intent);
                                /*PendingIntent pendingIntentGPS = PendingIntent.getBroadcast(AttendanceActivity.this,
                                        0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                                        SystemClock.elapsedRealtime() + 72000,
                                        72000, pendingIntentGPS);*/
                                //setAlarm(classObj.get("check_in").toString());
                                checkedIn = 1;
                                btnCheck.setText("Check Out");
                                getTxvStatusCheckTime.setText("Your time of work elapsed since last check in :");
                                SimpleDateFormat sdfGet = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                try {
                                    Date date = sdfGet.parse(classObj.get("check_in").toString());
                                    date.setTime(date.getTime() + 3600 * 7000);
                                    long diff = System.currentTimeMillis() - date.getTime();
                                    //StartTime = diff;
                                    unsetAlarm();
                                    setAlarm(diff);
                                    chronoMeter.setFormat("%s");
                                    chronoMeter.setBase(SystemClock.elapsedRealtime() - diff);
                                    chronoMeter.start();
                                } catch (ParseException e) {
                                    Log.e("error parse ", e.toString());
                                }
                                try {
                                    if(Float.parseFloat(classObj.get("lat_in").toString()) == 0.0000000 ||
                                            Float.parseFloat(classObj.get("long_in").toString()) == 0.0000000) {
                                        SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                                        preferences.edit().remove("firstLat").commit();
                                        preferences.edit().remove("firstLong").commit();
                                    }
                                } catch (NullPointerException err) {
                                    SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                                    preferences.edit().remove("firstLat").commit();
                                    preferences.edit().remove("firstLong").commit();
                                }
                            } else {
                                //unsetAlarm();
                                //stopService(new Intent(AttendanceActivity.this, DistanceCalculatorService.class));
                                getTxvStatusCheckTime.setText("Total hours you've been worked today :");
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
                                checkedIn = 2;
                                btnCheck.setText("Check In");

                                chronoMeter.stop();
                                chronoMeter.setText(hour+" hour(s) "+minute + " minute(s)");
                                    btnAddHoursToTimesheet.setVisibility(View.VISIBLE);
                            }
                            mSwipeRefreshLayout.setRefreshing(false);
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            stopService(new Intent(AttendanceActivity.this, DistanceCalculatorService.class));
                            btnAddHoursToTimesheet.setVisibility(View.GONE);
                            checkedIn = 2;
                            btnCheck.setText("Check In");
                            //Map<String, Object> classObj = (Map<String, Object>) classObjs[0];
                            //checkInId = classObj.get("id").toString();
                            //conditions = Arrays.asList(Arrays.asList(
                            //        Arrays.asList("create_uid", "=", user.getUserId())));
                                    //Arrays.asList("create_date", "like", "2017" + "%")))
                            /*fields = new HashMap() {{
                                put("fields", Arrays.asList("id", "employee_id", "create_uid",
                                        "check_in", "check_out", "worked_hours",
                                        "lat_in", "long_in", "create_date"));
                                put("limit", 100);
                            }};*/

                            //getAttendanceStatus();
                        }
                    });
                }
            }
            Looper.loop();
        }

        @Override
        public void onError(long id,final XMLRPCException error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!AttendanceActivity.this.isFinishing()) {
                        odoo.MessageDialog(AttendanceActivity.this, "Error happened : " + error.toString());
                    }

                }
            });
        }

        @Override
        public void onServerError(long id, final XMLRPCServerException error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!AttendanceActivity.this.isFinishing()) {
                        odoo.MessageDialog(AttendanceActivity.this, "Server error : " + error.toString());

                    }
                }
            });
        }

    };
    XMLRPCCallback listenerAttendance = new XMLRPCCallback() {

        public void onResponse(long id, Object result) {
            Looper.prepare();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            final String currentDate = sdf.format(new Date(System.currentTimeMillis()));
            final Intent intent = new Intent(AttendanceActivity.this, MessageBoxCheckInOut.class);
            Log.d("result createnya", result.toString());
            if (id == createAttendanceTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getAttendanceStatus();
                        //setAlarm();
                        layoutStatusCheckInOut.setVisibility(View.GONE);
                        txvStatusCheckInOut.setText("You checked in at " + currentDate);
                        imgBtnCloseLayoutStatusCIO.setVisibility(View.VISIBLE);
                        layoutStatusCheckInOut.setVisibility(View.VISIBLE);
                        layoutStatusCheckInOut.startAnimation(animationSlideDown);
                    }
                });
            } else if (id == updateAttendanceTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //unsetAlarm();
                        //unsetService();
                        getLocation();
                        getAttendanceStatus();
                        layoutStatusCheckInOut.setVisibility(View.GONE);
                        txvStatusCheckInOut.setText("You checked out at " + currentDate);
                        imgBtnCloseLayoutStatusCIO.setVisibility(View.VISIBLE);
                        layoutStatusCheckInOut.setVisibility(View.VISIBLE);
                        layoutStatusCheckInOut.startAnimation(animationSlideDown);
                    }
                });
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, final XMLRPCException error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!AttendanceActivity.this.isFinishing()) {
                    odoo.MessageDialog(AttendanceActivity.this, "Error happened : " + error.toString());

                    }
                }
            });
        }

        @Override
        public void onServerError(long id, final XMLRPCServerException error) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!AttendanceActivity.this.isFinishing()) {
                    odoo.MessageDialog(AttendanceActivity.this, "Server error : " + error.toString());

                    }
                }
            });
        }

    };

    public void unsetAlarm() {
        Intent myIntent = new Intent(this, NotificationReceiver.class);
        pendingIntent = PendingIntent
                .getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void unsetService() {
        Intent myIntent = new Intent(DistanceCalculatorService.service);
        PendingIntent pendingIntentGPS = PendingIntent
                .getBroadcast(this, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntentGPS);
    }


    private void updateAttendance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
        List data = Arrays.asList(
                Arrays.asList(Integer.parseInt(checkInId)),
                new HashMap() {{
                    put("check_out", currentDate);
                    put("lat_out", mLastLocation.getLatitude());
                    put("long_out", mLastLocation.getLongitude());
                    put("ipaddress_out", thisIPAddress);
                    put("address_out", thisAddress);
                }});
        //Log.d("datanyahehe", data.toString());
        updateAttendanceTaskId = odoo.update(listenerAttendance, database, uid, password, "hr.attendance", data);
    }



    private void createAttendance() {
        Log.d("","Entering create attendance");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SimpleDateFormat sdfCreateWrite = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
        final String createWrite = sdfCreateWrite.format(new Date());
        SharedData.setKey(AttendanceActivity.this, "firstLat", String.valueOf(mLastLocation.getLatitude()));
        SharedData.setKey(AttendanceActivity.this, "firstLong", String.valueOf(mLastLocation.getLongitude()));
        List data = Arrays.asList(new HashMap() {{
            put("check_in", currentDate);
            put("employee_id", employeeID);
            put("create_date", createWrite);
            put("write_date", createWrite);
            put("lat_in", mLastLocation.getLatitude());
            put("long_in", mLastLocation.getLongitude());
            put("ipaddress_in", thisIPAddress);
            put("address_in", thisAddress);
        }});
        createAttendanceTaskId = odoo.create(listenerAttendance, database, uid, password, "hr.attendance", data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCheck:
                btnCheck.setText("Wait...");
                if (checkedIn == 2) {
                    try {
                        if(!thisAddress.equals("null") || thisAddress != "") {
                            createAttendance();
                            //setAlarm();
                        } else {
                            Toast.makeText(this, "Check your connection and try again.", Toast.LENGTH_LONG).show();
                            getLocation();
                        }
                    } catch(NullPointerException err) {

                    }

                } else if (checkedIn == 1) {
                    SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                    preferences.edit().remove("firstLat").commit();
                    preferences.edit().remove("firstLong").commit();
                    updateAttendance();
                    unsetAlarm();
                }
                break;
            case R.id.closeNotifCheckIn:
                layoutStatusCheckInOut.startAnimation(animationSlideUp);
                break;
            case R.id.btnAddHourtoTimesheet:
                startActivity(new Intent(AttendanceActivity.this, ProjectActivity.class));
                break;
            case R.id.infoAllCheckin:
                expandUnexpandAttendance();
                break;
            case R.id.btnExpandAttendance:
                expandUnexpandAttendance();
                break;
        }
    }

    public void expandUnexpandAttendance() {
        if(!tableOpened) {
            tableOpened = true;
            imgButtonExpandAttendance.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));
            layoutInfoAllCheckIn.setVisibility(View.VISIBLE);
        } else {
            tableOpened = false;
            imgButtonExpandAttendance.setImageDrawable(getResources().getDrawable(R.drawable.ic_expand_more_black_24dp  ));
            layoutInfoAllCheckIn.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(slideState){
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else{
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void getLocation() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            Location latLng = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastLocation = latLng;
            //loginPaAndhi();
            startIntentService();
        }
    }
    @Override
    public void onConnected(Bundle bundle) {
        getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

   /* @Override
    public void onLocationChanged(Location location) {
        if(!SharedData.getKey(this, "firstLat").isEmpty() && checkedIn == 1) {
            if (mCurrLocationMarker != null) {
                mCurrLocationMarker.remove();
            }
            float[] results = new float[1];
            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Location.distanceBetween(Double.parseDouble(SharedData.getKey(this, "firstLat").toString()),
                    Double.parseDouble(SharedData.getKey(this, "firstLong").toString()),
                    latLng.latitude, latLng.longitude, results);
            if(results[0] >  200) {
                Intent scheduledIntent = new Intent(this, MessageBoxAlert.class);
                scheduledIntent.putExtra("checkInId", checkInId);
                scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(scheduledIntent);
                AttendanceActivity.this.overridePendingTransition(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.ic_action_company)
                                .setContentTitle("FPP Notification : Warning!")
                                .setContentText("You are 200m away from your workplace!")
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(Notification.PRIORITY_HIGH);
                Intent resultIntent = new Intent(this, AttendanceActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the back stack for the Intent (but not the Intent itself)
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

                // mNotificationId is a unique integer your app uses to identify the
                // notification. For example, to cancel the notification, you can pass its ID
                // number to NotificationManager.cancel().
                mNotificationManager.notify(0, mBuilder.build());
            }
        }
    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }


    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new android.support.v7.app.AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(AttendanceActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    private void setupDialog(){
        dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setMessage("Loading");
    }

    protected String address;

    class GeocoderResultReceiver extends ResultReceiver {
        public GeocoderResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Menerima balikan data dari GeocoderIntentService and menampilkan Toast di MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Menampilkan alamat, atau error yang didapat dari proses reverse geocoding
            address = resultData.getString(Constants.RESULT_DATA_KEY);

            // Memunculkan toast message jika ada alamat ditemukan
            if (resultCode == Constants.SUCCESS_RESULT) {
                txvAddress.setText(address);
                thisAddress = address;
                getAttendanceStatus();
            }

            dialog.dismiss();
        }
    }

    protected void startIntentService() {
        // Membuat intent yang mengarah ke IntentService untuk proses reverse geocoding
        Intent intent = new Intent(this, GeocoderIntentService.class);

        // Mengirim ResultReceiver sebagai extra ke intent service.
        intent.putExtra(Constants.RECEIVER, geocoderReceiver);

        // Mengirim location data sebagai extra juga ke intent service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        // tl;dr = menyalakan intent service <img draggable="false" class="emoji" alt="🙂" src="https://s.w.org/images/core/emoji/2.3/svg/1f642.svg">
        startService(intent);
    }
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied! Allow GPS access for this app in your setting, or" +
                            " contact your administrator.", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    recreate();
                    break;
                case 1:
                    startActivity(new Intent(AttendanceActivity.this, ProjectActivity.class));
                    break;
                case 2:
                    break;
                case 3:
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    unsetAlarm();
                                    unsetService();
                                    removeAccount();
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(AttendanceActivity.this);
                    builder.setMessage("Are you sure want to log out?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                    break;
            }
        }
    }
}

