package ca.dmdev.test.wom;

import android.app.Activity;
import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import ca.dmdev.test.wom.acccount.User;

/**
 * Created by Doug on 2016-02-22.
 */
public class WordOfMouth extends Application implements
        Application.ActivityLifecycleCallbacks,
        android.location.LocationListener {

    //this is the base class for the entire app
    //all user data will be stored here
    //requests for updated reviews too?
    private LocationManager locationManager;
    private Location lastLocation;

    private static final String TAG = MainActivity.class.getName();
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initializeLocation();

        User.initInstance();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Toast.makeText(this, "----------onCreate()---------", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.i(TAG,"----------onActivityStarted()---------");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.i(TAG,"----------onActivityResumed()---------");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 2000, 0, this);  // time in miliseconds, distance in meters (Distance drains battry life) originally set to 20000 and 0

        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Toast.makeText(this, "----------onActivityPaused()---------", Toast.LENGTH_LONG).show();
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.i(TAG,"----------onActivityStopped()---------");
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.i(TAG,"----------onActivitySaveInstanceState()---------");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
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

    private void initializeLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String bestProvider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(bestProvider);
            if (location != null) {
                onLocationChanged(location);
            }
            locationManager.requestLocationUpdates(bestProvider, 5, 5, this);  // time in miliseconds, distance in meters (Distance drains battry life) originally set to 20000 and 0

        }
    }

    public Location getLastLocation() {
        return lastLocation;
    }


}
