package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.odoo.core.account.OdooLogin;
import com.odoo.core.service.GeocoderIntentService;
import com.odoo.core.service.receivers.DistanceCalculatorService;
import com.odoo.core.support.OUser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.timroes.axmlrpc.XMLRPCCallback;
import de.timroes.axmlrpc.XMLRPCException;
import de.timroes.axmlrpc.XMLRPCServerException;

/**
 * Created by makan on 20/07/2017.
 */

public class MessageBoxAlert extends Activity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    /** Called when the activity is first created. */

    private long updateAttendanceTaskId;
    private String checkInId;
    private OdooUtility odoo;
    private OUser user;
    private String url, password, uid, database, serverAddress;
    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, mFirstLocation;
    private TextView textViewCountDown;
    private MessageBoxAlert.GeocoderResultReceiver geocoderReceiver;
    private String thisAddress="", thisIpAddress="";
    private Button btnYes, btnNo;
    private Vibrator vibrator;
    public static NotificationCompat.Builder mBuilder;
    public static Runnable rnble;
    public static Handler hndler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.messagebox);
        url = SharedData.getKey(this, "url").toString();
        if (url == "SAFE_PARCELABLE_NULL_STRING" || url == "") {
            removeAccount();
            finish();
        } else {
            try {
                user = OUser.current(this);
                odoo = new OdooUtility(user.getHost(), "object");
                uid = user.getUserId().toString();
                password = user.getPassword();
                serverAddress = user.getHost();
                database = user.getDatabase();
                checkInId = getIntent().getStringExtra("checkInId");
                thisIpAddress = getIntent().getStringExtra("ip_address_out");
                geocoderReceiver = new MessageBoxAlert.GeocoderResultReceiver(new Handler());
                textViewCountDown = (TextView) findViewById(R.id.countDownNotification);
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
                wakeLock.acquire();
                vibrator = (Vibrator) MessageBoxAlert.this.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 500, 100000};
                vibrator.vibrate(pattern,0);
                new CountDownTimer(300000, 1000) {

                    public void onTick(long millisUntilFinished) {
                        int seconds = (int)millisUntilFinished / 1000;
                        int minutes = (int)seconds / 60;
                        seconds = seconds % 60;
                        String sMinutes = (minutes <= 9) ? "0" + minutes : ""+minutes;
                        String sSeconds = (seconds <= 9) ? "0" + seconds : ""+seconds;

                        textViewCountDown.setText(sMinutes + " : " + sSeconds);
                    }

                    public void onFinish() {
                        textViewCountDown.setText("You're going to check out!");
                        updateAttendance();
                        vibrator.cancel();
                    }
                }.start();
                mBuilder =
                        new NotificationCompat.Builder(MessageBoxAlert.this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle("FPP Notification : Warning!")
                                .setContentText("You are 200m away from your workplace!")
                                .setAutoCancel(true)
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setPriority(Notification.PRIORITY_HIGH);
                Intent resultIntent = new Intent(MessageBoxAlert.this, AttendanceActivity.class);
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(MessageBoxAlert.this);
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
                        (NotificationManager) MessageBoxAlert.this
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                // mNotificationId is a unique integer your app uses to identify the
                // notification. For example, to cancel the notification, you can pass its ID
                // number to NotificationManager.cancel().
                mNotificationManager.notify(0, mBuilder.build());
                setTitle("WARNING!");
                rnble = new Runnable() {
                    @Override
                    public void run() {
                        updateAttendance();
                    }
                };
                btnYes = (Button) findViewById(R.id.Ok);
                btnYes.setText("Getting loc...");
                btnNo = (Button) findViewById(R.id.No);
                btnNo.setText("Check out");
                TextView tvContent = (TextView) findViewById(R.id.contentText);
                tvContent.setTextColor(Color.RED);
                tvContent.setText("You are 200 meters away from your work location!");

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MessageBoxAlert.this,
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
                hndler = new Handler();
                hndler.postDelayed(rnble, 300000);
                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vibrator.cancel();
                        updateAttendance();
                    }
                });
            } catch(NullPointerException err) {
                //startActivity(new Intent(MessageBoxAlert.this, OdooLogin.class));
            }
        }
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
                thisAddress = address;
                btnYes.setText("Continue Working");
                btnYes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        vibrator.cancel();
                        mBuilder.setAutoCancel(true);
                        mBuilder.getNotification().flags |= Notification.FLAG_AUTO_CANCEL;
                        stopService(new Intent(MessageBoxAlert.this, DistanceCalculatorService.class));
                        hndler.removeCallbacksAndMessages(null);
                        finish();
                        MessageBoxAlert.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }
                });

                Log.d("address :", mLastLocation.toString());
            }
        }
    }

    XMLRPCCallback listenerAttendance = new XMLRPCCallback() {

        public void onResponse(long id, Object result) {
            Looper.prepare();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            final String currentDate = sdf.format(new Date(System.currentTimeMillis()));
            //final Intent intent = new Intent(MessageBoxAlert.this, MessageBoxCheckInOut.class);
            if (id == updateAttendanceTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        vibrator.cancel();
                        SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                        preferences.edit().remove("firstLat").commit();
                        preferences.edit().remove("firstLong").commit();
                        Intent intentTimesheet = new Intent(MessageBoxAlert.this, AttendanceActivity.class);
                        intentTimesheet.putExtra("check_time", currentDate);
                        intentTimesheet.putExtra("status", 2);
                        startActivity(intentTimesheet);
                        MessageBoxAlert.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }
                });
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            odoo.MessageDialog(MessageBoxAlert.this, "Error happened : " + error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            odoo.MessageDialog(MessageBoxAlert.this, "Server error : " + error.toString());
        }

    };

    private void updateAttendance() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDate = sdf.format(new Date(System.currentTimeMillis() - 3600 * 7000));
        List data = Arrays.asList(
                Arrays.asList(Integer.parseInt(checkInId)),
                new HashMap() {{
                    put("check_out", currentDate);
                    put("lat_out", mLastLocation.getLatitude());
                    put("long_out", mLastLocation.getLongitude());
                    put("ipaddress_out", thisIpAddress);
                    put("address_out", thisAddress);
                }});
        updateAttendanceTaskId = odoo.update(listenerAttendance, database, uid, password, "hr.attendance", data);
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
    public void onBackPressed() {
        super.onBackPressed();
        vibrator.cancel();
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
            Log.d("location :", mLastLocation.toString());
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
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
                                ActivityCompat.requestPermissions(MessageBoxAlert.this,
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
}
