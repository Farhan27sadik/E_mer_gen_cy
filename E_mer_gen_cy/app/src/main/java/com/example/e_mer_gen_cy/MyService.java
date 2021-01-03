package com.example.e_mer_gen_cy;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.text.DateFormat;


public class MyService extends Service
{


    private static final String TAG = "GPS";
    private LocationManager mLocationManager = null;
    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    Location mLastLocation;

    // decrease the LOCATION_INTERVAL and increase the LOCATION_DISTANCE
    // and then observe the values in Logcat under tag onLocationChanged you will see enough values

   private static final int LOCATION_INTERVAL = 1000; // 1 sec
   private static final float LOCATION_DISTANCE = 10f; // 10 meters


  //   private static final int LOCATION_INTERVAL = 5000; // 10,000 =  10 sec
  //   private static final float LOCATION_DISTANCE = 10f; // 200 = 200 meters


    ////////////////////////////////////////////////////////////
    /////////////////// INNER CLASS BEGINS HERE ////////////////
    ////////////////////////////////////////////////////////////

    private class LocationListener implements android.location.LocationListener
    {


        // The below method will trigger automatically whenever there will be change of location
        // and it will update mLastLocation with new GPS location value

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }


        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);

            // If we want to inform the main activity about change in longtitude and latitude
            // we can use the below code

            String time = DateFormat.getTimeInstance().format(location.getTime()).toString();

            SendBroadCastMsg(mLastLocation.getLatitude(), mLastLocation.getLongitude(), time);
        }


        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }


        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }


        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
    //##################################################
    //////////////// INNER CLASS ENDS HERE ///////////
    //##################################################



    // This method is used to send the broad cast messages to MainActivity whenever there is a change of location
    public void SendBroadCastMsg(double latitude, double longitude, String time)
    {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent("custom-event-name");

        // You can also include some extra data.

        String msg = "Time is " + time+ " Location is: http://maps.google.com/?q="+ latitude +","+longitude;
        intent.putExtra("message", msg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }



    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);

        Log.d("sender", "Broadcasting message");
        Intent myintent = new Intent("custom-event-name");
        // You can also include some extra data.
        intent.putExtra("message", "This is my message!");
        LocalBroadcastManager.getInstance(this).sendBroadcast(myintent);


        return START_STICKY;
    }


    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");

        getLocation();

    }


    public void getLocation()
    {
        Log.e(TAG, "getLocation");
        if (mLocationManager == null)
        {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }



        // perform checks on which provider is enabled. We will prefer GPS provider.


        // getting GPS status
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        // getting network status
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isGPSEnabled == true) {

            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        LOCATION_INTERVAL,
                        LOCATION_DISTANCE,
                        mLocationListeners[0]);

                if(mLocationManager != null)
                {
                    mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "gps provider does not exist " + ex.getMessage());
            }

        }

        else if(isNetworkEnabled == true)
        {
            try {
                mLocationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        LOCATION_INTERVAL,
                        LOCATION_DISTANCE,
                        mLocationListeners[1]);


                if (mLocationManager != null)
                {
                    mLastLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }

            } catch (java.lang.SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "network provider does not exist, " + ex.getMessage());
            }
        }

    }


    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null)
        {
            for (int i = 0; i < mLocationListeners.length; i++)
            {
                try
                {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                }
                catch (Exception ex)
                {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }


}
