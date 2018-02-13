package com.odoo.core.service.receivers;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.odoo.AttendanceActivity;
import com.odoo.MessageBoxAlert;
import com.odoo.OdooActivity;
import com.odoo.R;
import com.odoo.SharedData;
import com.odoo.core.account.AppIntro;

/**
 * Created by makan on 28/08/2017.
 */

public class DistanceCalculatorService extends Service {

    Location workLocation;
    String checkInId, thisIpAddress;
    public static final String service = ".core.service.receivers.DistanceCalculatorService";


    public DistanceCalculatorService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            float[] results = new float[1];
            //Place current location marker
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            Location.distanceBetween(workLocation.getLatitude(),
                    workLocation.getLongitude(),
                    latLng.latitude, latLng.longitude, results);
            Log.d("now", latLng.toString());
            Log.d("work", workLocation.toString());
            Log.d("distance", results[0]+"");
            String mobile = SharedData.getKey(DistanceCalculatorService.this, "is_mobile").toString();
            String firstLat = SharedData.getKey(DistanceCalculatorService.this, "firstLat").toString();
            if(results[0] >  500 && mobile == "" || mobile == "SAFE_PARCELABLE_NULL_STRING") {
                SharedData.setKey(DistanceCalculatorService.this, "is_mobile", "true");
                Intent scheduledIntent = new Intent(DistanceCalculatorService.this, MessageBoxAlert.class);
                scheduledIntent.putExtra("ip_address_out", thisIpAddress);
                scheduledIntent.putExtra("checkInId", checkInId);
                scheduledIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(scheduledIntent);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //LatLng latlong = new LatLng(Double.parseDouble(SharedData.getKey(DistanceCalculatorService.this,
                //"firstLat")), Double.parseDouble(SharedData.getKey(DistanceCalculatorService"firstLong")));
        workLocation = intent.getParcelableExtra("workLocation");
        checkInId = intent.getStringExtra("checkInId");
        thisIpAddress = intent.getStringExtra("ip_address_out");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                return START_NOT_STICKY;
            }
        }
        locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
                locationListener);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences preferences = getSharedPreferences("MyPrefs", 0);
                preferences.edit().remove("is_mobile").commit();
            }
        }, 600000);

        return START_NOT_STICKY;

    }

}
