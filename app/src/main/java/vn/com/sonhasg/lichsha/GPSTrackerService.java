package vn.com.sonhasg.lichsha;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by nguyenphuoc on 04/19/2017.
 */

public class GPSTrackerService extends Service implements LocationListener {

    private static final int MIN_TIME_BW_UPDATES = 1000 * 180; // 3 minute

    private LocationManager mLocationManager;

    private ConnectivityManager connectivityManager;

    private PrefManager prefManager;

    private float NetworkLatitude, NetworkLongitude;
    private float GPSLatitude, GPSLongitude;

    private String sendData = "";

    public GPSTrackerService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefManager = new PrefManager(this);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable() {
        @Override
        public void run() {
            if (prefManager.isLoggedIn() && !prefManager.getUser().equals("0")) {

                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
                    try {
                        if (mLocationManager == null) {
                            return;
                        }

                        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }

                        if (!isGPSEnabled && !isNetworkEnabled) {
                            return;
                        }

                        if (isNetworkEnabled) {
                            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, GPSTrackerService.this);
                            Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                NetworkLatitude = (float) location.getLatitude();
                                NetworkLongitude = (float) location.getLongitude();
                            }
                        }

                        if (isGPSEnabled) {
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, GPSTrackerService.this);
                            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                GPSLatitude = (float) location.getLatitude();
                                GPSLongitude = (float) location.getLongitude();
                            }
                        }

                        final String newSendData = "&latitude=" + NetworkLatitude + "&longitude=" + NetworkLongitude + "&lat=" + GPSLatitude + "&lng=" + GPSLongitude;
                        Toast.makeText(getApplicationContext(), newSendData, Toast.LENGTH_SHORT).show();

                        if (!newSendData.equals(sendData)) {
                            sendData = newSendData;

                            RequestQueue queue = Volley.newRequestQueue(GPSTrackerService.this);

                            StringRequest stringRequest = new StringRequest(Request.Method.GET, AppConfig.URL_GPSDATA + "?api_key=" + AppConfig.API_KEY + "&uid=" + prefManager.getUser() + sendData, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });

                            queue.add(stringRequest);

                        }

                        mLocationManager.removeUpdates(GPSTrackerService.this);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            mHandler.postDelayed(mHandlerTask, MIN_TIME_BW_UPDATES);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandlerTask.run();
        return START_STICKY;
    }

    @Override
    public void onLocationChanged(Location location) {

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

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
