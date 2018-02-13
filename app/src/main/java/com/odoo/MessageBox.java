package com.odoo;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
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

public class MessageBox extends Activity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{
    /** Called when the activity is first created. */

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private long updateAttendanceTaskId;
    private OUser user;
    private String url, uid, username, password, database, serverAddress, checkInId;
    int checkInIdInt;
    private LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, mFirstLocation;
    OdooUtility odoo;
    private TextView textViewCountDown;
    private GeocoderResultReceiver geocoderReceiver;
    private String thisAddress="", thisIpAddress="";
    private Button btnYes, btnNo;
    private Vibrator vibrator;

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
                checkInId = getIntent().getStringExtra("checkInId");
                thisIpAddress = getIntent().getStringExtra("ip_address_out");
                Log.d("ip_address:", thisIpAddress);
                Log.d("checkInId:", checkInId);
                checkInIdInt = Integer.parseInt(checkInId);
                uid = user.getUserId().toString();
                password = user.getPassword();
                serverAddress = user.getHost();
                database = user.getDatabase();



                Runnable rnble = new Runnable() {
                    @Override
                    public void run() {
                        updateAttendance();
                    }
                };
                geocoderReceiver = new GeocoderResultReceiver(new Handler());
                odoo = new OdooUtility(user.getHost(), "object");
                btnYes = (Button) findViewById(R.id.Ok);
                btnYes.setText("Waiting for location");
                btnNo = (Button) findViewById(R.id.No);
                textViewCountDown = (TextView) findViewById(R.id.countDownNotification);
                TextView tvContent = (TextView) findViewById(R.id.contentText);
                PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                        "MyWakelockTag");
                wakeLock.acquire();
                vibrator = (Vibrator) MessageBox.this.getSystemService(Context.VIBRATOR_SERVICE);
                long[] pattern = {0, 500, 100000};
                vibrator.vibrate(pattern,0);
                tvContent.setText("You have been worked for 2 hours, will you take a break?");

                //chronoMeter.setBase(SystemClock.elapsedRealtime() - 300000);
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


                final Handler hndler = new Handler();
                hndler.postDelayed(rnble, 300000);


                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vibrator.cancel();
                        hndler.removeCallbacksAndMessages(null);
                        startActivity(new Intent(MessageBox.this, AttendanceActivity.class));
                        finish();
                        MessageBox.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);

                    }
                });
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(MessageBox.this,
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
            } catch (NullPointerException err) {
                //startActivity(new Intent(MessageBox.this, OdooLogin.class));
                err.printStackTrace();
                Log.e("error", Log.getStackTraceString(err));
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
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
                stopService(new Intent(MessageBox.this, DistanceCalculatorService.class));
                thisAddress = address;
                btnYes.setText("Yes");
                btnYes.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        vibrator.cancel();
                        updateAttendance();
                    }
                });

                Log.d("address :", mLastLocation.toString());
            }
        }
    }

    XMLRPCCallback listenerAttendance = new XMLRPCCallback() {

        public void onResponse(long id, Object result) {
            Looper.prepare();
            if (id == updateAttendanceTaskId) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                        preferences.edit().remove("firstLat").commit();
                        preferences.edit().remove("firstLong").commit();
                        finish();
                        MessageBox.this.overridePendingTransition(android.R.anim.fade_in,
                                android.R.anim.fade_out);
                    }
                });
            }
            Looper.loop();
        }

        @Override
        public void onError(long id, XMLRPCException error) {
            Log.e("errornya", error.toString());
        }

        @Override
        public void onServerError(long id, XMLRPCServerException error) {
            Log.e("errornya", error.toString());
        }


    };

    private void updateAttendance() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String currentDate = sdf.format(new Date(System.currentTimeMillis()-(3600*7000)));
        List data = Arrays.asList(
                Arrays.asList(Integer.parseInt(checkInId)),
                new HashMap() {{
                    put("check_out", currentDate);
                    put("lat_out", mLastLocation.getLatitude());
                    put("long_out", mLastLocation.getLongitude());
                    put("ipaddress_out", thisIpAddress);
                    put("address_out", thisAddress);
                }});
        Log.d("datanyahehe", data.toString());
        updateAttendanceTaskId = odoo.update(listenerAttendance, database, uid, password, "hr.attendance", data);

        /*List data = Arrays.asList(
                Arrays.asList(checkInIdInt),
                new HashMap() {{
                    put("check_out", currentDate);
                    put("lat_out", mLastLocation.getLatitude());
                    put("long_out", mLastLocation.getLongitude());
                    put("ipaddress_out", thisIpAddress);
                    put("address_out", thisAddress);
                }});
        updateAttendanceTaskId = odoo.update(listenerAttendance, database, uid, password, "hr.attendance", data);*/
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
                                ActivityCompat.requestPermissions(MessageBox.this,
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
}
