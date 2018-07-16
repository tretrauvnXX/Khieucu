package vn.com.sonhasg.lichsha;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by nguyenphuoc on 04/23/2017.
 */

public class CustomerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private PrefManager prefManager;
    private SQLiteHandler sqLiteHandler;

    private NavigationView navigationView;
    private DrawerLayout drawer;

    HashMap<String, String> user;

    private String tag_string_req = "req_customers";
    private String city = "", domain = "";

    private GoogleMap map;

    private LatLng gps;

    private int num_city = 0;

    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        prefManager = new PrefManager(this);
        city = prefManager.getCity();
        domain = prefManager.getDomain();

        sqLiteHandler = new SQLiteHandler(this);
        user = sqLiteHandler.getUserDetails();

        setContentView(R.layout.activity_customer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Hệ thống đại lý");
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        View navHeader = navigationView.getHeaderView(0);
        TextView tvFullname = (TextView) navHeader.findViewById(R.id.tvFullname);
        TextView tvDepartment = (TextView) navHeader.findViewById(R.id.tvDepartment);
        ImageView imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        ImageView imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        Glide.with(this).load(AppConfig.URL_NAVHEADERBG).crossFade().diskCacheStrategy(DiskCacheStrategy.ALL).into(imgNavHeaderBg);
        Glide.with(this).load(AppConfig.URL_PROFILEIMG).crossFade().thumbnail(0.5f).bitmapTransform(new CircleTransform(this)).diskCacheStrategy(DiskCacheStrategy.ALL).into(imgProfile);

        tvFullname.setText(user.get("fullname"));
        tvDepartment.setText(user.get("partname"));

        switch (domain) {
            case "lichtt":
                gps = new LatLng(20.9725, 105.777);
                break;
            case "lichshq":
                gps = new LatLng(15.447, 108.609);
                break;
            default:
                gps = new LatLng(10.8230989, 106.6296638);
                break;
        }

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                drawer.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.nav_home:
                        startActivity(new Intent(CustomerActivity.this, MainActivity.class));
                        finish();

                        return false;

                    case R.id.nav_notifications:
                        startActivity(new Intent(CustomerActivity.this, EventActivity.class));
                        finish();

                        return true;

                    case R.id.nav_branchs:
                        startActivity(new Intent(CustomerActivity.this, BranchActivity.class));
                        finish();

                        return true;

                    case R.id.nav_customers:
                        break;

                    case R.id.nav_location:
                        startActivity(new Intent(CustomerActivity.this, LocationActivity.class));
                        finish();

                        return true;

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

        navigationView.getMenu().getItem(3).setChecked(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        navigationView.getMenu().getItem(5).setTitle("Tỉnh thành");

        if (!(connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected())) {
            showSnack();
        }
    }

    private void fetchCities() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CITIES, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {

                            navigationView.getMenu().getItem(5).getSubMenu().clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            num_city = data.length();

                            navigationView.getMenu().getItem(5).getSubMenu().add(3, 0, 0, "Tất cả").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                @Override
                                public boolean onMenuItemClick(MenuItem item) {
                                    city = item.getItemId() + "";
                                    prefManager.setCity(city);

                                    getSupportActionBar().setTitle("Hệ thống đại lý");

                                    fetchCustomers();

                                    for (int m = 0; m < num_city + 1; m++) {
                                        navigationView.getMenu().getItem(5).getSubMenu().getItem(m).setChecked(false);
                                    }
                                    return false;
                                }
                            });

                            for (int i = 1; i < num_city + 1; i++) {
                                navigationView.getMenu().getItem(5).getSubMenu().add(3, i, i, data.getString(i - 1)).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem item) {
                                        city = item.getItemId() + "";
                                        prefManager.setCity(city);

                                        fetchCustomers();

                                        for (int m = 0; m < num_city + 1; m++) {
                                            navigationView.getMenu().getItem(5).getSubMenu().getItem(m).setChecked(false);
                                        }
                                        return false;
                                    }
                                });
                            }

                            if (num_city > 0) {
                                if (city == null || city.equals("")) {
                                    city = navigationView.getMenu().getItem(5).getSubMenu().getItem(0).toString();
                                    prefManager.setCity(city);
                                }
                                navigationView.getMenu().findItem(Integer.parseInt(city)).setChecked(true);
                            }

                            fetchCustomers();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("domain", domain);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    private void fetchCustomers() {
        if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {

            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_CUSTOMERS, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) {
                            map.clear();

                            JSONArray data = jsonObject.getJSONArray("data");

                            int n = data.length();
                            for (int i = 0; i < n; i++) {
                                JSONObject item = data.getJSONObject(i);

                                LatLng position = new LatLng(Double.parseDouble(item.getString("latitude")), Double.parseDouble(item.getString("longitude")));
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.title(item.getString("name_vn"));
                                markerOptions.snippet(item.getString("diachi"));
                                String type = item.getString("type");
                                markerOptions.position(position);
                                if (type.equals("0")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type0));
                                }
                                if (type.equals("1")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type1));
                                }
                                if (type.equals("2")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type2));
                                }
                                if (type.equals("3")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type3));
                                }
                                if (type.equals("4")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type4));
                                }
                                if (type.equals("5")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.customer_type5));
                                }
                                map.addMarker(markerOptions);
                            }

                            StringRequest stringRequest = new StringRequest(Request.Method.POST, AppConfig.URL_BRANCHS, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {

                                    try {
                                        JSONObject jsonObject = new JSONObject(response);
                                        boolean success = jsonObject.getBoolean("success");
                                        if (success) {

                                            JSONArray data = jsonObject.getJSONArray("data");

                                            for (int i = 0; i < data.length(); i++) {
                                                JSONObject item = data.getJSONObject(i);

                                                LatLng position = new LatLng(Double.parseDouble(item.getString("latitude")), Double.parseDouble(item.getString("longitude")));
                                                MarkerOptions markerOptions = new MarkerOptions();
                                                markerOptions.title(item.getString("name_vn"));
                                                markerOptions.snippet(item.getString("diachi"));
                                                String type = item.getString("type");
                                                markerOptions.position(position);
                                                if (domain.equals("lichsha") || domain.equals("lichshq") || domain.equals("lichtt")) {
                                                    int maker_icon = getResources().getIdentifier(domain + "_branch_type" + type, "drawable", getPackageName());
                                                    markerOptions.icon(BitmapDescriptorFactory.fromResource(maker_icon));
                                                }
                                                map.addMarker(markerOptions);
                                            }
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        Toast.makeText(getApplicationContext(), "Không thể đọc dữ liệu máy chủ!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() {
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put("api_key", AppConfig.API_KEY);
                                    params.put("city", city.equals("") ? "" : navigationView.getMenu().findItem(Integer.parseInt(city)).toString());
                                    params.put("domain", domain);

                                    return params;
                                }
                            };

                            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);

                            if (city.equals("0")) {
                                getSupportActionBar().setTitle("Hệ thống đại lý");

                                switch (domain) {
                                    case "lichtt":
                                        new GeocoderTask().execute("Hà Nội, Việt Nam");
                                        break;
                                    case "lichshq":
                                        new GeocoderTask().execute("Quảng Nam, Việt Nam");
                                        break;
                                    default:
                                        new GeocoderTask().execute("Hồ Chí Minh, Việt Nam");
                                        break;
                                }
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 8));
                            } else {
                                getSupportActionBar().setTitle("Đại lý " + navigationView.getMenu().findItem(Integer.parseInt(city)).getTitle());

                                new GeocoderTask().execute(navigationView.getMenu().findItem(Integer.parseInt(city)).toString() + ", Việt Nam");
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 11));
                            }

                            if (n == 0) {
                                Toast.makeText(getApplicationContext(), "Danh sách đại lý trống!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Không thể tải dữ liệu từ máy chủ!", Toast.LENGTH_LONG).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Không thể đọc dữ liệu máy chủ!", Toast.LENGTH_LONG).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("api_key", AppConfig.API_KEY);
                    params.put("city", city.equals("") ? "" : navigationView.getMenu().findItem(Integer.parseInt(city)).toString());
                    params.put("domain", domain);

                    return params;
                }
            };

            AppController.getInstance().addToRequestQueue(stringRequest, tag_string_req);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logoutUser();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setMapToolbarEnabled(false);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(gps, 8));

        fetchCities();
    }

    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocationName(locationName[0], 3);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses == null || addresses.size() == 0){
                Toast.makeText(getBaseContext(), "Không tìm thấy vị trí với địa chỉ này!", Toast.LENGTH_SHORT).show();
            }

            for(int i=0; i < addresses.size(); i++){

                if(i == 0) {
                    Address address = (Address) addresses.get(i);

                    gps = new LatLng(address.getLatitude(), address.getLongitude());

                    map.animateCamera(CameraUpdateFactory.newLatLng(gps));
                }
            }
        }
    }

    private void logoutUser() {
        prefManager.setLogin(false);
        prefManager.setDomain("");
        sqLiteHandler.deleteUsers();

        Intent service = new Intent(getApplicationContext(), GPSTrackerService.class);
        stopService(service);

        Intent intent = new Intent(CustomerActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void showSnack() {
        Snackbar snackbar = Snackbar.make(findViewById(R.id.drawer_layout), "Vui lòng kết nối Internet!", Snackbar.LENGTH_LONG);

        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.RED);
        snackbar.show();
    }
}
