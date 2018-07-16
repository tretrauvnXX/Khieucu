package vn.com.sonhasg.lichsha;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;

public class LocationActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private static final int REQUEST_PERMISSIONS = 99;

    private PrefManager prefManager;

    private DrawerLayout drawer;

    HashMap<String, String> user;

    private GoogleMap map;

    private LocationManager mLocationManager;

    private float NetworkLatitude, NetworkLongitude;
    private float GPSLatitude, GPSLongitude;

    private String sendData = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);

        SQLiteHandler sqLiteHandler = new SQLiteHandler(this);
        user = sqLiteHandler.getUserDetails();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        setContentView(R.layout.activity_location);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Kiểm tra vị trí");
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View navHeader = navigationView.getHeaderView(0);
        TextView tvFullname = (TextView) navHeader.findViewById(R.id.tvFullname);
        TextView tvDepartment = (TextView) navHeader.findViewById(R.id.tvDepartment);
        ImageView imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        Glide.with(this).load(AppConfig.URL_NAVHEADERBG).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(imgNavHeaderBg);
        Glide.with(this).load(AppConfig.URL_PROFILEIMG).crossFade().thumbnail(0.5f).bitmapTransform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgProfile);

        tvFullname.setText(user.get("fullname"));
        tvDepartment.setText(user.get("partname"));

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                drawer.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(LocationActivity.this, MainActivity.class));
                        finish();

                        return false;

                    case R.id.nav_notifications:
                        startActivity(new Intent(LocationActivity.this, EventActivity.class));
                        finish();

                        return true;

                    case R.id.nav_branchs:
                        startActivity(new Intent(LocationActivity.this, BranchActivity.class));
                        finish();

                        return true;

                    case R.id.nav_customers:
                        startActivity(new Intent(LocationActivity.this, CustomerActivity.class));
                        finish();

                        return true;

                    case R.id.nav_location:
                        break;

                    default:
                }

                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                return true;
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        drawer.setDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();

        navigationView.getMenu().getItem(4).setChecked(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        navigationView.getMenu().getItem(5).setVisible(false);

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_location, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_zoom_location) {
            sendData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);

        if ((ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS);
            }
        } else {
            sendData();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    sendData();
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng cho phép truy cập vị trí của bạn!", Toast.LENGTH_LONG).show();
                    finish();
                    System.exit(0);
                }
            }
        }
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

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS thiết bị đang tắt, bạn có muốn kích hoạt GPS?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void sendData() {
        try {
            boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

            boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                buildAlertMessageNoGps();
            } else {
                if (mLocationManager != null) {
                    if (isNetworkEnabled) {
                        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, LocationActivity.this);
                        Location location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            NetworkLatitude = (float) location.getLatitude();
                            NetworkLongitude = (float) location.getLongitude();
                        }
                    }
                    if (isGPSEnabled) {
                        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, LocationActivity.this);
                        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            GPSLatitude = (float) location.getLatitude();
                            GPSLongitude = (float) location.getLongitude();
                        }
                    }

                    map.clear();
                    LatLng pos = new LatLng(NetworkLatitude != 0 ? NetworkLatitude : GPSLatitude, NetworkLatitude != 0 ? NetworkLongitude : GPSLongitude);
                    map.addMarker(new MarkerOptions().position(pos).title("My location").icon(BitmapDescriptorFactory.fromResource(R.drawable.gmap_marker)));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));

                    String newSendData = "&latitude=" + NetworkLatitude + "&longitude=" + NetworkLongitude + "&lat=" + GPSLatitude + "&lng=" + GPSLongitude;
                    if (!newSendData.equals(sendData)) {

                        sendData = newSendData;

                        RequestQueue queue = Volley.newRequestQueue(LocationActivity.this);

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, AppConfig.URL_GPSDATA + "?api_key=" + AppConfig.API_KEY + "&uid=" + prefManager.getUser() + sendData, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Toast.makeText(getApplicationContext(), "Gửi vị trí thành công!", Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });

                        queue.add(stringRequest);

                    }

                    mLocationManager.removeUpdates(LocationActivity.this);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }
}
